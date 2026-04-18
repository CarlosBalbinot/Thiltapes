import jwt from 'jsonwebtoken';

/**
 * Middleware para validar o token JWT nas rotas protegidas.
 */
export const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({
      status: 'error',
      message: 'Acesso negado: Token não fornecido',
      timestamp: new Date().toISOString(),
    });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({
        status: 'error',
        message: 'Acesso negado: Token inválido ou expirado',
        timestamp: new Date().toISOString(),
      });
    }

    req.user = user; // Injeta os dados do usuário do token na request
    next();
  });
};
