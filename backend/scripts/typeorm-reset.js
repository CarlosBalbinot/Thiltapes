import { spawnSync } from 'child_process';

import AppDataSource from '../src/config/dataSource.js';

function runMigrationsAgain() {
  const result = spawnSync('node', ['scripts/typeorm-migrate.js'], {
    stdio: 'inherit',
    env: process.env,
    shell: true,
  });

  if (result.status !== 0) {
    process.exit(result.status ?? 1);
  }
}

async function run() {
  await AppDataSource.initialize();
  await AppDataSource.dropDatabase();
  await AppDataSource.destroy();
  runMigrationsAgain();
  console.log('Banco resetado e migrations reaplicadas com sucesso.');
}

run().catch(async (error) => {
  console.error('Falha ao resetar banco com TypeORM:', error);
  if (AppDataSource.isInitialized) {
    await AppDataSource.destroy();
  }
  process.exit(1);
});
