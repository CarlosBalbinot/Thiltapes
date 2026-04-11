import AppDataSource from '../src/config/dataSource.js';

async function run() {
  await AppDataSource.initialize();
  await AppDataSource.undoLastMigration();
  await AppDataSource.destroy();
  console.log('Última migration TypeORM revertida com sucesso.');
}

run().catch(async (error) => {
  console.error('Falha ao reverter migration TypeORM:', error);
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }
  process.exit(1);
});
