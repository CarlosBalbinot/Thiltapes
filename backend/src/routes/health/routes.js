import { Router } from 'express';
import { successResponse } from '../../utils/apiResponse.js';

const router = Router();

/**
 * GET /api/health
 * Verifica se a API está saudável
 * Sem autenticação necessária
 */
router.get('/', (req, res) => {
  res.status(200).json(
    successResponse(
      {
        status: 'running',
        uptime: process.uptime(),
        environment: process.env.NODE_ENV || 'development',
        version: '1.0.0',
        database: 'connected', // Será verificado dinamicamente depois
      },
      'API está operacional'
    )
  );
});

export { router };
