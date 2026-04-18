import dotenv from 'dotenv';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const nodeEnv = process.env.NODE_ENV === 'production' ? 'production' : 'development';
const envFile = `.env.${nodeEnv}`;
const envPath = path.resolve(__dirname, '../../config', envFile);

process.env.NODE_ENV = nodeEnv;

// Nao sobrescreve variaveis ja definidas no ambiente (ex.: Docker Compose).
dotenv.config({ path: envPath, override: false });

// Construir DATABASE_URL a partir de componentes individuais
function buildDatabaseUrl(host, port, user, password, database) {
  return `postgresql://${encodeURIComponent(user)}:${encodeURIComponent(password)}@${host}:${port}/${database}`;
}

const dbHost = process.env.DB_HOST || 'localhost';
const dbPort = process.env.DB_PORT || '5432';
const dbUser = process.env.DB_USER || 'postgres';
const dbPassword = process.env.DB_PASSWORD || 'postgres';
const dbName = process.env.DB_NAME || 'thiltapes_db';

const dbAdminHost = process.env.DB_ADMIN_HOST || dbHost;
const dbAdminPort = process.env.DB_ADMIN_PORT || dbPort;
const dbAdminUser = process.env.DB_ADMIN_USER || dbUser;
const dbAdminPassword = process.env.DB_ADMIN_PASSWORD || dbPassword;

process.env.DATABASE_URL = buildDatabaseUrl(dbHost, dbPort, dbUser, dbPassword, dbName);
process.env.DATABASE_ADMIN_URL = buildDatabaseUrl(
  dbAdminHost,
  dbAdminPort,
  dbAdminUser,
  dbAdminPassword,
  'postgres'
);
