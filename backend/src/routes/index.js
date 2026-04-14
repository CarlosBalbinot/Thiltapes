import { Router } from 'express';
import { router as gameRouter } from './game/routes.js';
import { router as userRouter } from './user/routes.js';
import { router as healthRouter } from './health/routes.js';

const router = Router();

router.use('/health', healthRouter);
router.use('/game', gameRouter);
router.use('/user', userRouter);

export default router;
