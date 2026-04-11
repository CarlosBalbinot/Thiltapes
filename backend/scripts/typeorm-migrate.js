import { spawnSync } from 'child_process';

import AppDataSource from '../src/config/dataSource.js';

function ensureDatabase() {
  const result = spawnSync('node', ['scripts/ensure-database.js'], {
    stdio: 'inherit',
    env: process.env,
    shell: true,
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

async function run() {
  ensureDatabase();
  await AppDataSource.initialize();
  await AppDataSource.runMigrations();
  await AppDataSource.destroy();
  console.log('Migrations TypeORM aplicadas com sucesso.');
}

run().catch(async (error) => {
  console.error('Falha ao executar migrations TypeORM:', error);
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }
  process.exit(1);
});
