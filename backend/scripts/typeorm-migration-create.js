import { spawnSync } from 'child_process';

function getMigrationName() {
  const rawName = process.argv[2];

  if (!rawName || !rawName.trim()) {
    console.error(
      'Informe o nome da migration. Exemplo: npm run typeorm:migration:create -- add-status-to-user'
    );
    process.exit(1);
  }

  return rawName.trim();
}

function run() {
  const migrationName = getMigrationName();

  const result = spawnSync(
    'npx',
    ['typeorm', 'migration:create', `src/migrations/${migrationName}`, '--outputJs', '--esm'],
    {
      stdio: 'inherit',
      env: process.env,
      shell: true,
    }
  );

  process.exit(result.status ?? 1);
}

run();
