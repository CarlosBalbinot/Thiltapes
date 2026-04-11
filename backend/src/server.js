import './config/env.js';
import app from './app.js';
import AppDataSource from './config/dataSource.js';

const PORT = process.env.PORT || 3000;

let server;

async function shutdown(signal) {
  console.log(`${signal} recebido. Encerrando servidor...`);

  if (server) {
    await new Promise((resolve) => {
      server.close(resolve);
    });
  }

  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }

  console.log('Servidor encerrado');
  process.exit(0);
}

async function startServer() {
  await AppDataSource.initialize();

  server = app.listen(PORT, () => {
    console.log(`
╔════════════════════════════════════════╗
║   🚀 Servidor Thiltapes iniciado!     ║
║   Porta: ${PORT}                          
║   Ambiente: ${process.env.NODE_ENV}    
╚════════════════════════════════════════╝
  `);
  });
}

startServer().catch(async (error) => {
  console.error('Falha ao iniciar servidor:', error);
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }
  process.exit(1);
});

// Graceful Shutdown
process.on('SIGTERM', () => {
  shutdown('SIGTERM');
});

process.on('SIGINT', () => {
  shutdown('SIGINT');
});
