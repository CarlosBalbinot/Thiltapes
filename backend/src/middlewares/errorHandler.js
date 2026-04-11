/**
 * Middleware de erro global
 * Centraliza o tratamento de erros da aplicação
 * Utiliza padrão de resposta apiResponse para consistência
 */

import { errorResponse, getErrorMessage } from '../utils/apiResponse.js';

const errorHandler = (err, req, res, next) => {
  console.error('[ERROR]', {
    timestamp: new Date().toISOString(),
    path: req.path,
    method: req.method,
    error: err.message,
    stack: err.stack,
  });

  const statusCode = err.statusCode || 500;
  const errorType = err.errorType || 'SERVER_ERROR';
  const message = err.message || getErrorMessage(errorType);
  const details = process.env.NODE_ENV === 'development' ? { stack: err.stack } : null;

  res.status(statusCode).json(errorResponse(errorType, message, details));
};

export default errorHandler;
