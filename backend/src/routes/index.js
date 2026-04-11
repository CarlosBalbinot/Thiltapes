import express, { Router } from 'express';
import AppDataSource from '../config/dataSource.js';
import { hashPassword, verifyPassword } from '../utils/bcrypt.js';
import generateCard from '../utils/generateCard.js';
import getRandomLocation from '../utils/generateRandomCoords.js';
import healthRouter from './health.js';

const dateTimeNow = new Date().toISOString('pt-BR', { timeZone: 'America/Sao_Paulo' });

const router = Router();

router.use('/', healthRouter);

router.post('/user/create', async (req, res) => {
  const { username, password_hash, role } = req.body;
  try {
    const hashedPassword = await hashPassword(password_hash);
    const userRepository = AppDataSource.getRepository('User');

    await userRepository.save({
      username: username,
      password_hash: hashedPassword,
      role: role.toUpperCase(),
      created_at: dateTimeNow,
    });

    res.status(201).json({
      status: 'success',
      message: 'Usuário criado com sucesso',
      timestamp: dateTimeNow,
    });
  } catch (error) {
    console.error(error);
    res
      .status(500)
      .json({ status: 'error', message: 'Erro ao criar usuário', timestamp: dateTimeNow });
  }
});

router.post('/user/login', async (req, res) => {
  try {
    const { username, password_hash } = req.body;
    const userRepository = AppDataSource.getRepository('User');

    const user = await userRepository.findOneBy({ username: username });

    if (!user) {
      return res
        .status(401)
        .json({ status: 'error', message: 'Credenciais inválidas', timestamp: dateTimeNow });
    }

    const isValid = await verifyPassword(password_hash, user.password_hash);

    if (isValid) {
      res.status(200).json({
        status: 'success',
        message: 'Login realizado com sucesso',
        timestamp: dateTimeNow,
      });
    } else {
      res
        .status(401)
        .json({ status: 'error', message: 'Credenciais inválidas', timestamp: dateTimeNow });
    }
  } catch (error) {
    console.error(error);
    res
      .status(500)
      .json({ status: 'error', message: 'Erro ao fazer login', timestamp: dateTimeNow });
  }
});

router.post('/game/create', async (req, res) => {
  try {
    const { name, admin_id, number_of_cards, current_location, radius } = req.body;
    const gameRepository = AppDataSource.getRepository('Game');

    const game = await gameRepository.save({
      admin: { id: admin_id },
      name,
      status: 'ACTIVE',
      created_at: dateTimeNow,
    });

    const generatedFeatures = [];

    const cardCreationPromises = Array.from({ length: number_of_cards }).map(async () => {
      const card = await generateCard();
      const { lat, lng } = getRandomLocation(current_location.lat, current_location.lng, radius);

      const queryResult = await AppDataSource.query(
        `
        INSERT INTO game_cards (
          game_id, image_url, rarity, thiltapes_name, radius_meters, location
        )
        VALUES (
          $1::uuid, $2, $3, $4, $5, ST_SetSRID(ST_MakePoint($6, $7), 4326)
        )
        RETURNING id
      `,
        [game.id, card.image, card.rarity, card.thiltapes_name, radius, lng, lat]
      );

      const insertedCard = queryResult[0];

      generatedFeatures.push({
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [lng, lat] },
        properties: {
          id: insertedCard.id,
          name: card.thiltapes_name,
          rarity: card.rarity,
        },
      });
    });

    await Promise.all(cardCreationPromises);

    const geoJsonPayload = {
      type: 'FeatureCollection',
      features: generatedFeatures,
    };

    res.status(201).json({
      status: 'success',
      message: 'Jogo e cartas criados com sucesso',
      game_id: game.id,
      map_data: geoJsonPayload,
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao criar jogo' });
  }
});

router.get('/game/find-by-id/:gameId', async (req, res) => {
  try {
    const { gameId } = req.params;
    const gameRepository = AppDataSource.getRepository('Game');

    const game = await gameRepository.findOneBy({ id: gameId });
    if (!game) return res.status(404).json({ status: 'error', message: 'Jogo não encontrado' });

    res.status(200).json(game);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao buscar jogo' });
  }
});

router.get('/game/fetch-all', async (req, res) => {
  try {
    const gameRepository = AppDataSource.getRepository('Game');

    const games = await gameRepository.find();
    res.status(200).json(games);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao buscar jogos' });
  }
});

router.put('/game/:gameId', async (req, res) => {
  try {
    const { gameId } = req.params;
    const { name, status } = req.body;
    const gameRepository = AppDataSource.getRepository('Game');

    const updateData = {};
    if (name) updateData.name = name;
    if (status) updateData.status = status.toUpperCase();

    const updateResult = await gameRepository.update({ id: gameId }, updateData);

    // Equivalente ao erro P2025 do Prisma
    if (updateResult.affected === 0) {
      return res.status(404).json({ status: 'error', message: 'Jogo não encontrado' });
    }

    const updatedGame = await gameRepository.findOneBy({ id: gameId });
    res.status(200).json(updatedGame);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao atualizar jogo' });
  }
});

router.put('/game/insert-card/:gameId', async (req, res) => {
  try {
    const { gameId } = req.params;
    const { current_location, radius } = req.body;

    const card = await generateCard();
    const { lat, lng } = getRandomLocation(current_location.lat, current_location.lng, radius);

    const queryResult = await AppDataSource.query(
      `
      INSERT INTO game_cards (
        game_id, image_url, rarity, thiltapes_name, radius_meters, is_collected, location
      )
      VALUES (
        $1::uuid, $2, $3, $4, $5, false, ST_SetSRID(ST_MakePoint($6, $7), 4326)
      )
      RETURNING id
    `,
      [gameId, card.image, card.rarity, card.thiltapes_name, radius, lng, lat]
    );

    const insertedCard = queryResult[0];

    const feature = {
      type: 'Feature',
      geometry: {
        type: 'Point',
        coordinates: [lng, lat],
      },
      properties: {
        id: insertedCard.id,
        name: card.thiltapes_name,
        rarity: card.rarity,
        image: card.image,
      },
    };

    res.status(201).json({
      status: 'success',
      message: 'Carta gerada com sucesso',
      card: feature,
    });
  } catch (error) {
    console.error('❌ Erro ao gerar carta:', error);
    res.status(500).json({ status: 'error', message: 'Falha na geração da carta' });
  }
});

export default router;

/*
 /game
 edit game
 add card to game
 get games (with card count)

  /get players inventory

*/
