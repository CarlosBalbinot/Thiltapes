import '../src/config/env.js';

import { Client } from 'pg';

function getDatabaseName(databaseUrl) {
  const url = new URL(databaseUrl);
  return url.pathname.replace(/^\//, '');
}

function getMaintenanceUrl(databaseUrl) {
  const url = new URL(databaseUrl);
  url.pathname = '/postgres';
  url.search = '';
  url.hash = '';
  return url.toString();
}

async function ensureDatabaseExists() {
  const databaseUrl = process.env.DATABASE_URL;
  const adminDatabaseUrl = process.env.DATABASE_ADMIN_URL || databaseUrl;

  if (!databaseUrl) {
    throw new Error('DATABASE_URL não encontrado no ambiente.');
  }

  const targetDatabase = getDatabaseName(databaseUrl);

  if (!targetDatabase) {
    throw new Error('DATABASE_URL inválida: nome do banco ausente.');
  }

  const client = new Client({ connectionString: getMaintenanceUrl(adminDatabaseUrl) });

  try {
    await client.connect();
  } catch (error) {
    throw new Error(
      `Não foi possível conectar com as credenciais administrativas para criar o banco. ` +
        `Defina DATABASE_ADMIN_URL com um usuário que tenha permissão CREATE DATABASE. ` +
        `Detalhe: ${error.message}`
    );
  }

  try {
    const result = await client.query('SELECT 1 FROM pg_database WHERE datname = $1', [
      targetDatabase,
    ]);

    if (result.rowCount === 0) {
      console.log(`Banco "${targetDatabase}" não existe. Criando...`);
      await client.query(`CREATE DATABASE "${targetDatabase.replace(/"/g, '""')}"`);
      console.log(`Banco "${targetDatabase}" criado com sucesso.`);
    } else {
      console.log(`Banco "${targetDatabase}" já existe.`);
    }
  } finally {
    await client.end();
  }
}

ensureDatabaseExists().catch((error) => {
  console.error('Falha ao garantir existência do banco:', error.message);
  process.exit(1);
});
