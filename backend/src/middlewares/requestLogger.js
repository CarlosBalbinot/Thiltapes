/**
 * Middleware de logging de requisições
 */

const requestLogger = (req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.path}`);

  // Hook para logar resposta
  res.on('finish', () => {
    console.log(`[${timestamp}] ${req.method} ${req.path} - Status: ${res.statusCode}`);
  });

  next();
};

export default requestLogger;
