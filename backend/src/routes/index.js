/**
 * Router principal da API
 * Monta todos os routers de recursos específicos
 * Padrão: /api/<recurso>/<ação>
 */

import { Router } from 'express';
import healthRouter from './health.js';

const router = Router();

// Montar routers
router.use('/', healthRouter);

export default router;
