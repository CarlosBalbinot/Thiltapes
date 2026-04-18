import './config/env.js';
import express from 'express';
import cors from 'cors';
import bodyParser from 'body-parser';
import routes from './routes/index.js';

import errorHandler from './middlewares/errorHandler.js';
import requestLogger from './middlewares/requestLogger.js';

const app = express();

const corsOrigins = process.env.CORS_ORIGIN?.split(',')
  .map((origin) => origin.trim())
  .filter(Boolean);

// Middlewares
app.use(requestLogger);

app.use(
  cors({
    origin:
      corsOrigins?.length > 0
        ? corsOrigins
        : process.env.NODE_ENV === 'development'
          ? ['http://localhost:3000', 'http://localhost:8080']
          : false,
    credentials: true,
  })
);

app.use(bodyParser.json({ limit: '10mb' }));
app.use(bodyParser.urlencoded({ limit: '10mb', extended: true }));

// Health Check Route
app.get('/health', (req, res) => {
  res.status(200).json({
    status: 'OK',
    message: 'Servidor está funcionando',
    timestamp: new Date().toISOString(),
  });
});

// API Routes
app.use('/api', routes);

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    status: 'error',
    message: 'Rota não encontrada',
    path: req.path,
  });
});

// Error Handler (deve ser o último middleware)
app.use(errorHandler);

export default app;
