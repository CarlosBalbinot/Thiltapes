export const getDateTimeNow = () => {
  return new Date().toISOString('pt-BR', { timeZone: 'America/Sao_Paulo' });
};