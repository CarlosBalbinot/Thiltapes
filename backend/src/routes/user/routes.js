import AppDataSource from '../../config/dataSource.js';
import { hashPassword, verifyPassword } from '../../utils/bcrypt.js';
import { getDateTimeNow } from '../../utils/datetimenow.js';
import { Router } from 'express';
import jwt from 'jsonwebtoken';

const router = Router();

/**
 * Creates a new user with a hashed password and specified role.
 *
 * @route POST /user/create
 * @param {Object} req.body - Request payload.
 * @param {string} req.body.username - The user's username.
 * @param {string} req.body.password - The user's plain text password.
 * @param {string} req.body.role - The user's role.
 * @returns {Object} 201 - Success response with timestamp.
 * @returns {Object} 500 - Internal server error.
 */
router.post('/create', async (req, res) => {
  const { username, password, role } = req.body;
  try {
    const hashedPassword = await hashPassword(password);
    const userRepository = AppDataSource.getRepository('User');

    await userRepository.save({
      username: username,
      password_hash: hashedPassword,
      role: role.toUpperCase(),
      created_at: getDateTimeNow(),
    });

    res.status(201).json({
      status: 'success',
      message: 'Usuário criado com sucesso',
      timestamp: getDateTimeNow(),
    });
  } catch (error) {
    console.error(error);
    res
      .status(500)
      .json({ status: 'error', message: 'Erro ao criar usuário', timestamp: getDateTimeNow() });
  }
});

/**
 * Authenticates a user and returns their ID upon successful login.
 *
 * @route POST /user/login
 * @param {Object} req.body - Request payload.
 * @param {string} req.body.username - The user's username.
 * @param {string} req.body.password - The user's plain text password.
 * @returns {Object} 200 - Success response with user ID.
 * @returns {Object} 401 - Invalid credentials.
 * @returns {Object} 500 - Internal server error.
 */
router.post('/login', async (req, res) => {
  try {
    const { username, password } = req.body;
    const userRepository = AppDataSource.getRepository('User');

    const user = await userRepository.findOneBy({ username: username });

    if (!user) {
      return res
        .status(401)
        .json({ status: 'error', message: 'Credenciais inválidas', timestamp: getDateTimeNow() });
    }

    const isValid = await verifyPassword(password, user.password_hash);

    if (isValid) {
      // Gera o token JWT com o ID e Role do usuário
      const token = jwt.sign(
        { id: user.id, role: user.role, username: user.username },
        process.env.JWT_SECRET || 'secret_de_fallback',
        { expiresIn: process.env.JWT_EXPIRATION || '7d' }
      );

      res.status(200).json({
        status: 'success',
        message: 'Login realizado com sucesso',
        timestamp: getDateTimeNow(),
        user_id: user.id,
        token: token, // Envia o token na resposta
      });
    } else {
      res
        .status(401)
        .json({ status: 'error', message: 'Credenciais inválidas', timestamp: getDateTimeNow() });
    }
  } catch (error) {
    console.error(error);
    res
      .status(500)
      .json({ status: 'error', message: 'Erro ao fazer login', timestamp: getDateTimeNow() });
  }
});

/**
 * Collects a specific game card for a player and adds it to their inventory.
 *
 * @route POST /user/collect-thiltapes
 * @param {Object} req.body - Request payload.
 * @param {string} req.body.player_id - The ID of the player collecting the card.
 * @param {string} req.body.card_id - The ID of the card being collected.
 * @returns {Object} 200 - Success message confirming collection.
 * @returns {Object} 404 - Player or Card not found.
 * @returns {Object} 500 - Internal server error.
 */
router.post('/collect-thiltapes', async (req, res) => {
  try {
    const { player_id, card_id } = req.body;

    const gameRepository = AppDataSource.getRepository('GameCard');
    const inventoryRepository = AppDataSource.getRepository('PlayerInventory');

    if (!player_id)
      return res.status(404).json({ status: 'error', message: 'Jogador nao encontrado' });

    const card = await gameRepository.findOneBy({ id: card_id });

    if (!card) return res.status(404).json({ status: 'error', message: 'Carta nao encontrada' });
    await gameRepository.update({ id: card_id }, { collected_by: player_id });

    await inventoryRepository.save({
      player: player_id,
      game_card: card_id,
      found_at: getDateTimeNow(),
      found_location: card.location,
    });

    res.status(200).json({ status: 'success', message: 'Carta coletada com sucesso' });
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao coletar carta' });
  }
});

/**
 * Retrieves the inventory of game cards collected by a specific player.
 *
 * @route GET /user/get-inventory/:player_id
 * @param {string} req.params.player_id - The ID of the player.
 * @returns {Array} 200 - A list of game cards collected by the player.
 * @returns {Object} 500 - Internal server error.
 */
router.get('/get-inventory/:player_id', async (req, res) => {
  try {
    const { player_id } = req.params;
    const gameCardRepository = AppDataSource.getRepository('GameCard');

    const cards = await gameCardRepository.findBy({
      collected_by: { id: player_id },
    });

    res.status(200).json(cards);
  } catch (error) {
    console.error(error);
    res.status(500).json({ status: 'error', message: 'Erro ao buscar jogador' });
  }
});

export { router };
