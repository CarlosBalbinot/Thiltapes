import { Router } from 'express';
import AppDataSource from '../../config/dataSource.js';
import generateCard from '../../utils/generateCard.js';
import getRandomLocation from '../../utils/generateRandomCoords.js';
import { getDateTimeNow } from '../../utils/datetimenow.js';

export const router = Router();

/**
 * Creates a new game instance, generates the requested number of cards,
 * and distributes them geographically within a specified radius.
 */
router.post('/create', async (req, res) => {
  try {
    const { name, admin_id, number_of_cards, current_location, radius } = req.body;
    const gameRepository = AppDataSource.getRepository('Game');

    const game = await gameRepository.save({
      admin: { id: admin_id },
      name,
      status: 'ACTIVE',
      created_at: getDateTimeNow(),
    });

    const generatedFeatures = [];

    // Rodando sequencialmente para não tomar block (429) da API externa
    for (let i = 0; i < number_of_cards; i++) {
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

      generatedFeatures.push({
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [lng, lat] },
        properties: {
          id: queryResult[0].id,
          name: card.thiltapes_name,
          rarity: card.rarity,
        },
      });
    }

    res.status(201).json({
      status: 'success',
      message: 'Jogo e cartas criados com sucesso',
      game_id: game.id,
      map_data: {
        type: 'FeatureCollection',
        features: generatedFeatures,
      },
    });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao criar jogo' });
  }
});

/**
 * Retrieves a specific game configuration and data by its UUID.
 */
router.get('/find-by-id/:game_id', async (req, res) => {
  try {
    const { game_id } = req.params;
    const gameRepository = AppDataSource.getRepository('Game');

    const game = await gameRepository.findOneBy({ id: game_id });
    if (!game) return res.status(404).json({ status: 'error', message: 'Jogo não encontrado' });

    res.status(200).json(game);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao buscar jogo' });
  }
});

/**
 * Fetches all currently active games from the database.
 */
router.get('/fetch-all', async (req, res) => {
  try {
    const gameRepository = AppDataSource.getRepository('Game');
    const games = await gameRepository.find({ where: { status: 'ACTIVE' } });

    res.status(200).json(games);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao buscar jogos' });
  }
});

/**
 * Updates the name or status of an existing game.
 */
router.put('/:game_id', async (req, res) => {
  try {
    const { game_id } = req.params;
    const { name, status } = req.body;
    const gameRepository = AppDataSource.getRepository('Game');

    const updateData = {};
    if (name) updateData.name = name;
    if (status) updateData.status = status.toUpperCase();

    const updateResult = await gameRepository.update({ id: game_id }, updateData);

    if (updateResult.affected === 0) {
      return res.status(404).json({ status: 'error', message: 'Jogo não encontrado' });
    }

    const updatedGame = await gameRepository.findOneBy({ id: game_id });
    res.status(200).json(updatedGame);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao atualizar jogo' });
  }
});

/**
 * Generates and inserts a single new card into an existing game's map.
 */
router.put('/insert-card/:game_id', async (req, res) => {
  try {
    const { game_id } = req.params;
    const { current_location, radius } = req.body;

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
      [game_id, card.image, card.rarity, card.thiltapes_name, radius, lng, lat]
    );

    res.status(201).json({
      status: 'success',
      message: 'Carta gerada com sucesso',
      card: {
        type: 'Feature',
        geometry: { type: 'Point', coordinates: [lng, lat] },
        properties: {
          id: queryResult[0].id,
          name: card.thiltapes_name,
          rarity: card.rarity,
          image: card.image,
        },
      },
    });
  } catch (error) {
    console.error('❌ Erro ao gerar carta:', error);
    res.status(500).json({ status: 'error', message: 'Falha na geração da carta' });
  }
});
