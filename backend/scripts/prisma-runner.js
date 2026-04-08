import '../src/config/env.js';

import { spawnSync } from 'child_process';
import { fileURLToPath } from 'url';
import path from 'path';

const args = process.argv.slice(2);

if (args.length === 0) {
  console.error('Usage: node scripts/prisma-runner.js <prisma-args...>');
  process.exit(1);
}

const shouldEnsureDatabase = args[0] === 'migrate' && args[1] === 'dev';

if (shouldEnsureDatabase) {
  const ensureDatabaseScript = path.resolve(
    path.dirname(fileURLToPath(import.meta.url)),
    'ensure-database.js'
  );

  const ensureResult = spawnSync('node', [ensureDatabaseScript], {
    stdio: 'inherit',
    env: process.env,
  });

  if (ensureResult.status !== 0) {
    process.exit(ensureResult.status ?? 1);
  }
}

const result = spawnSync('npx', ['prisma', ...args], {
  stdio: 'inherit',
  env: process.env,
  shell: true,
});

process.exit(result.status ?? 1);
