/**
 * Wrapper padrão de respostas da API
 * Padroniza todas as respostas (sucesso e erro) em um formato consistente
 * Facilita consumo no frontend (Android/Web)
 */

/**
 * Resposta de sucesso
 * @param {*} data - Dados a serem retornados
 * @param {string} message - Mensagem descritiva (default: 'Operação realizada com sucesso')
 * @returns {Object} Resposta formatada
 */
export const successResponse = (data, message = 'Operação realizada com sucesso') => ({
  success: true,
  data,
  message,
  timestamp: new Date().toISOString(),
});

/**
 * Resposta de erro
 * @param {string} error - Tipo de erro (VALIDATION_ERROR|AUTH_ERROR|NOT_FOUND|CONFLICT|SERVER_ERROR)
 * @param {string} message - Mensagem descritiva para o usuário
 * @param {*} details - Detalhes adicionais (opcional)
 * @returns {Object} Resposta de erro formatada
 */
export const errorResponse = (error, message, details = null) => ({
  success: false,
  error,
  message,
  ...(details && { details }),
  timestamp: new Date().toISOString(),
});

/**
 * Mapeia tipos de erro para mensagens padrão
 * @param {string} errorType
 * @returns {string} Mensagem legível
 */
export const getErrorMessage = (errorType) => {
  const messages = {
    VALIDATION_ERROR: 'Dados inválidos fornecidos',
    AUTH_ERROR: 'Não autorizado ou token inválido',
    NOT_FOUND: 'Recurso não encontrado',
    CONFLICT: 'Recurso já existe',
    SERVER_ERROR: 'Erro interno do servidor',
    UNAUTHORIZED: 'Autenticação necessária',
  };
  return messages[errorType] || 'Erro desconhecido';
};

export default {
  successResponse,
  errorResponse,
  getErrorMessage,
};
