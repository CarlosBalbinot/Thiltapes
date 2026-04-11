# Thiltapes - Backend API

Backend da aplicação Thiltapes construído com **Node.js 18+**, **Express 5.x**, **TypeORM 0.3.27** e **PostgreSQL 14+** com suporte a **PostGIS**.

---

## 🔄 Migração de Prisma para TypeORM

### O que foi alterado?

**Antes (Prisma):**

- ORM: Prisma Client
- Scheama: `prisma/schema.prisma`
- Migrações: Geradas automaticamente em `prisma/migrations/`
- Queries: `$executeRaw()`, `$queryRaw()`

**Depois (TypeORM):**

- ORM: **TypeORM 0.3.27** com padrão Active Record
- Entidades: Definidas em `src/entities/` usando `EntitySchema` (sem decoradores)
- Migrações: Manuais em `src/migrations/` (mais controle, seguro idempotente)
- Queries: `AppDataSource.query()` para raw SQL, repositórios tipados para operações ORM

### Arquivos removidos:

- ❌ `prisma/schema.prisma`
- ❌ `prisma/migrations/`
- ❌ `src/config/prisma.js`
- ❌ `scripts/prisma-runner.js`

### Arquivos criados/modificados:

- ✅ `src/config/dataSource.js` (novo)
- ✅ `src/entities/UserEntity.js` (novo)
- ✅ `src/entities/GameEntity.js` (novo)
- ✅ `src/entities/GameCardEntity.js` (novo)
- ✅ `src/entities/PlayerInventoryEntity.js` (novo)
- ✅ `src/migrations/1712958000000-InitialSchema.js` (novo)
- ✅ `scripts/typeorm-migrate.js` (novo)
- ✅ `scripts/typeorm-migrate-revert.js` (novo)
- ✅ `scripts/typeorm-reset.js` (novo)
- ✅ `src/server.js` (modificado - lifecycle TypeORM)
- ✅ `src/utils/postgisQueries.js` (adaptado para TypeORM)
- ✅ `package.json` (dependências atualizadas)

---

## 📋 Pré-requisitos

- **Node.js 18+** (recomendado 18.17.0 ou superior)
- **PostgreSQL 14+** (com extensões `pgcrypto` e `postgis`)
- **npm** (ou yarn/pnpm)
- Editor de código (VS Code, WebStorm, etc) - opcional

### Verificar instalações

```bash
node --version      # v18.17.0+
npm --version       # 9.6.0+
psql --version      # PostgreSQL 14+
```

---

## 🚀 Configuração Local - Passo a Passo

### 1️⃣ Clonar repositório e instalar dependências

```bash
cd c:\univates\Thiltapes\backend
npm install
```

**O que acontece:**

- Baixa todas as dependências incluindo `typeorm`, `express`, `reflect-metadata`
- Cria `node_modules/`
- Verifica 0 vulnerabilidades de segurança

Expected output:

```
added 203 packages, audited 204 packages in 39s
61 packages are looking for funding
found 0 vulnerabilities
```

---

### 2️⃣ Configurar variáveis de ambiente

Crie o arquivo `config/.env.development`:

```bash
# Usar WSL ou Git Bash no Windows
touch config/.env.development
# Ou criar manualmente em: backend\config\.env.development
```

**Conteúdo do arquivo:**

```env
# Aplicação
NODE_ENV=development
PORT=3000

# Banco de dados (credenciais PostgreSQL locais)
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=postgres
DB_NAME=thiltapes_db

# Credenciais admin para criar banco se não existir
DB_ADMIN_HOST=localhost
DB_ADMIN_PORT=5432
DB_ADMIN_USER=postgres
DB_ADMIN_PASSWORD=postgres

# CORS (frontend)
CORS_ORIGIN=http://localhost:3000,http://localhost:8080

# JWT (segurança)
JWT_SECRET=change_this_development_secret_use_strong_key
JWT_EXPIRATION=7d

# TypeORM logging (opcional)
TYPEORM_LOGGING=false   # true para ver todas as queries SQL
```

**🔐 Observação de segurança:**

- Nunca comitar `.env.development` com dados sensíveis
- Em produção, use variáveis de ambiente do servidor
- Para JWT_SECRET em produção, use uma chave forte (uuid, hash)

---

### 3️⃣ Preparar o Banco de Dados PostgreSQL

#### Opção A: Usar pgAdmin (UI - mais simples)

1. Abra pgAdmin no navegador: `http://localhost:5050`
2. Conecte com credenciais PostgreSQL
3. Não precisa criar nada manual - o script faz isso

#### Opção B: Usar psql (CLI)

```bash
psql -U postgres -h localhost -p 5432

# Dentro do psql:
CREATE DATABASE thiltapes_db;
\c thiltapes_db
CREATE EXTENSION pgcrypto;
CREATE EXTENSION postgis;
\q
```

#### Opção C: Deixar o script fazer (recomendado ✅)

O script `scripts/ensure-database.js` cria tudo automaticamente quando executar as migrations. Apenas certifique-se que:

- PostgreSQL está rodando
- Credenciais em `.env.development` estão corretas
- Usuário `postgres` tem permissão de criar databases

---

### 4️⃣ Executar migrations (criar tabelas)

Este é o comando **MAIS IMPORTANTE**:

```bash
npm run typeorm:migrate
```

**O que este comando faz (Step-by-step):**

1. ✅ Carrega variáveis de `config/.env.development`
2. ✅ Conecta com PostgreSQL usando credenciais admin
3. ✅ Cria banco de dados `thiltapes_db` se não existir
4. ✅ Conecta ao banco criado
5. ✅ Cria extensão `pgcrypto` (para UUIDs com `gen_random_uuid()`)
6. ✅ Cria extensão `postgis` (para geometria de pontos)
7. ✅ Cria tipos ENUM (role_enum: ADMIN/PLAYER, game_status_enum: ACTIVE/ENDED)
8. ✅ Cria todas as tabelas:
   - `users` - usuários do sistema
   - `games` - instâncias de jogos
   - `game_cards` - cards posicionados no mapa
   - `player_inventories` - cards coletados pelos jogadores
9. ✅ Cria índices (incluindo índice GIST para PostGIS)
10. ✅ Registra migration como "aplicada" no `_prisma_migrations`

**Expected output:**

```
◇ injected env (9) from config\.env.development
Banco "thiltapes_db" criado com sucesso.
Migrations TypeORM aplicadas com sucesso.
```

Ou se banco já existe:

```
◇ injected env (9) from config\.env.development
Banco "thiltapes_db" já existe.
Migrations TypeORM aplicadas com sucesso.
```

---

### 5️⃣ Iniciar o servidor

```bash
npm run dev
```

**O que acontece:**

1. Carrega variáveis de ambiente
2. Inicializa TypeORM DataSource (conecta ao banco)
3. Inicia Express server na porta 3000
4. Ativa nodemon para reload automático em mudanças de código

**Expected output:**

```
> backend@1.0.0 dev
> nodemon --exec node --experimental-modules src/server.js

[nodemon] 3.0.1
[nodemon] to restart at any time, type `rs`
[nodemon] watching path(s): src/**
[nodemon] watching extensions: js,json
Server running on port 3000
```

---

## 📊 Estrutura do Banco de Dados

### Diagrama de Tabelas

```
┌──────────────────┐
│      users       │
├──────────────────┤
│ id (UUID) [PK]   │◄────────────────────────┐
│ email (unique)   │                         │
│ name             │                         │
│ role (ENUM)      │ ──┐                     │
│ created_at       │   │                     │
│ updated_at       │   │                     │
└──────────────────┘   │                     │
         └─────────────┼───────────┐         │
                       │           │         │
                       ▼           ▼         │
         ┌──────────────────┐  ┌──────────────────────┐
         │      games       │  │ player_inventories   │
         ├──────────────────┤  ├──────────────────────┤
         │ id (UUID) [PK]   │  │ id (UUID) [PK]       │
         │ admin_id (FK)    │──│ player_id (FK)       │
         │ name             │  │ game_card_id (FK)    │
         │ status (ENUM)    │  │ collected_at         │
         │ created_at       │  │ updated_at           │
         │ updated_at       │  └──────────────────────┘
         └──────────────────┘
                   │
                   ▼
         ┌──────────────────────┐
         │     game_cards       │
         ├──────────────────────┤
         │ id (UUID) [PK]       │
         │ game_id (FK)         │
         │ collected_by (FK)    │────────────┘
         │ thiltapes_name       │
         │ image_url            │
         │ rarity               │
         │ location (Point)     │ ◄─── PostGIS geometry
         │ latitude             │
         │ longitude            │
         │ created_at           │
         │ updated_at           │
         └──────────────────────┘
```

### Descrição das Tabelas

#### **users** - Usuários do sistema

```sql
CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  name VARCHAR(255) NOT NULL,
  role role_enum NOT NULL DEFAULT 'PLAYER',  -- ADMIN, PLAYER
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Campos:**

- `id`: Identificador único (UUID)
- `email`: Email do usuário (único)
- `name`: Nome do usuário
- `role`: ADMIN (gerencia jogos) ou PLAYER (coleta cards)
- `created_at`: Timestamp de criação
- `updated_at`: Timestamp de última alteração

---

#### **games** - Instâncias de jogos

```sql
CREATE TABLE games (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  admin_id UUID NOT NULL REFERENCES users(id),
  name VARCHAR(255) NOT NULL,
  status game_status_enum NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, ENDED
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Campos:**

- `id`: Identificador único do jogo
- `admin_id`: FK para usuário que criou o jogo (ADMIN)
- `name`: Nome do jogo
- `status`: ACTIVE (jogando) ou ENDED (finalizado)
- `created_at`, `updated_at`: Timestamps

---

#### **game_cards** - Cards posicionados no mapa (com PostGIS)

```sql
CREATE TABLE game_cards (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  game_id UUID NOT NULL REFERENCES games(id) ON DELETE CASCADE,
  thiltapes_name VARCHAR(255) NOT NULL,
  image_url TEXT,
  rarity VARCHAR(50),
  location geometry(Point, 4326) NOT NULL,  -- ◄─── PostGIS!
  latitude NUMERIC(10, 8),
  longitude NUMERIC(11, 8),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Índice GIST para queries de proximidade PostGIS
CREATE INDEX idx_game_cards_location ON game_cards USING GIST (location);
```

**Campos:**

- `id`: Identificador único do card
- `game_id`: FK para o jogo (CASCADE delete se jogo deletado)
- `thiltapes_name`: Nome do card
- `image_url`: URL da imagem do card
- `rarity`: Raridade (comum, rara, épica, lendária, etc)
- `location`: **Geometria PostGIS ponto (WGS84/4326)**
- `latitude`, `longitude`: Campos duplicados para facilitar queries
- `created_at`, `updated_at`: Timestamps

**Exemplo Insert com PostGIS:**

```javascript
const result = await AppDataSource.query(
  `
  INSERT INTO game_cards 
  (id, game_id, thiltapes_name, image_url, rarity, location, latitude, longitude)
  VALUES 
  ($1, $2, $3, $4, $5, ST_SetSRID(ST_MakePoint($6, $7), 4326), $8, $9)
  RETURNING *;
`,
  [id, gameId, name, imageUrl, rarity, lng, lat, lat, lng]
);
```

**Query de proximidade:**

```javascript
// Encontrar cards dentro de 1km do jogador
const cards = await AppDataSource.query(
  `
  SELECT id, thiltapes_name, image_url, rarity,
         ST_X(location) as lng, 
         ST_Y(location) as lat
  FROM game_cards
  WHERE game_id = $1
    AND ST_DWithin(location, ST_SetSRID(ST_MakePoint($2, $3), 4326), 1000)
  ORDER BY ST_Distance(location, ST_SetSRID(ST_MakePoint($2, $3), 4326));
`,
  [gameId, playerLng, playerLat]
);
```

---

#### **player_inventories** - Cards coletados (Join table)

```sql
CREATE TABLE player_inventories (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  player_id UUID NOT NULL REFERENCES users(id),
  game_card_id UUID NOT NULL REFERENCES game_cards(id),
  collected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(player_id, game_card_id)  -- Uma vez por jogador/card
);
```

**Campos:**

- `id`: Identificador único
- `player_id`: FK para usuário que coletou
- `game_card_id`: FK para card coletado
- `collected_at`: Quando foi coletado
- `UNIQUE(player_id, game_card_id)`: Garante que cada jogador coleta cada card só 1x

---

## 📁 Estrutura de Arquivos

```
backend/
├── config/
│   └── .env.development           # Variáveis de ambiente (NÃO comitar!)
│
├── scripts/
│   ├── ensure-database.js         # Cria banco se não existir
│   ├── typeorm-migrate.js         # Carrega migrations (MAIN)
│   ├── typeorm-migrate-revert.js  # Reverte última migration
│   ├── prisma-runner.js           # [DELETADO]
│   └── typeorm-reset.js           # Deleta tudo e reaplica
│
├── src/
│   ├── config/
│   │   ├── env.js                 # Composição de variáveis (DATABASE_URL)
│   │   ├── dataSource.js          # Configuração TypeORM ⭐
│   │   └── prisma.js              # [DELETADO]
│   │
│   ├── entities/                  # Definição de modelos (Schemas)
│   │   ├── UserEntity.js          # Usuários (ADMIN/PLAYER)
│   │   ├── GameEntity.js          # Jogos (ACTIVE/ENDED)
│   │   ├── GameCardEntity.js      # Cards com geometry ponto
│   │   └── PlayerInventoryEntity.js # Join table (Many-to-Many)
│   │
│   ├── migrations/
│   │   └── 1712958000000-InitialSchema.js # Criação inicial + idempotência
│   │
│   ├── routes/
│   │   └── index.js               # [A IMPLEMENTAR] Endpoints API
│   │
│   ├── middlewares/
│   │   ├── errorHandler.js
│   │   └── requestLogger.js
│   │
│   ├── utils/
│   │   ├── postgisQueries.js      # Helpers PostGIS ✅ (atualizado)
│   │   └── generateCard.js        # Card generation
│   │
│   ├── app.js                     # Configuração Express
│   ├── server.js                  # Entry point + TypeORM init ✅
│   └── prisma/                    # [DELETADO]
│
├── package.json                   # ✅ Atualizado (Prisma → TypeORM)
├── package-lock.json
├── .gitignore                     # ✅ Atualizado (removidas referências Prisma)
├── .prettierignore                # ✅ Atualizado
└── README.md                      # Este arquivo
```

---

## 🧩 Entidades TypeORM (Padrão Active Record)

### UserEntity.js

```javascript
export const UserEntity = new EntitySchema({
  name: 'User',
  tableName: 'users',
  columns: {
    id: { type: 'uuid', primary: true, generated: 'uuid', default: 'gen_random_uuid()' },
    email: { type: 'varchar', unique: true },
    name: { type: 'varchar' },
    role: { type: 'enum', enum: ['ADMIN', 'PLAYER'], default: 'PLAYER' },
    createdAt: { name: 'created_at', type: 'timestamp', createDate: true },
    updatedAt: { name: 'updated_at', type: 'timestamp', updateDate: true },
  },
  relations: {
    games: { type: 'one-to-many', target: 'Game', inverseSide: 'admin' },
    inventories: { type: 'one-to-many', target: 'PlayerInventory', inverseSide: 'player' },
  },
});
```

### GameEntity.js

```javascript
export const GameEntity = new EntitySchema({
  name: 'Game',
  tableName: 'games',
  columns: {
    id: { type: 'uuid', primary: true, generated: 'uuid', default: 'gen_random_uuid()' },
    adminId: { name: 'admin_id', type: 'uuid' },
    name: { type: 'varchar' },
    status: { type: 'enum', enum: ['ACTIVE', 'ENDED'], default: 'ACTIVE' },
    createdAt: { name: 'created_at', type: 'timestamp', createDate: true },
    updatedAt: { name: 'updated_at', type: 'timestamp', updateDate: true },
  },
  relations: {
    admin: { type: 'many-to-one', target: 'User', joinColumn: { name: 'admin_id' } },
    gameCards: { type: 'one-to-many', target: 'GameCard', inverseSide: 'game' },
  },
});
```

### GameCardEntity.js (com PostGIS)

```javascript
export const GameCardEntity = new EntitySchema({
  name: 'GameCard',
  tableName: 'game_cards',
  columns: {
    id: { type: 'uuid', primary: true, generated: 'uuid', default: 'gen_random_uuid()' },
    gameId: { name: 'game_id', type: 'uuid' },
    thiltapesName: { name: 'thiltapes_name', type: 'varchar' },
    imageUrl: { name: 'image_url', type: 'text' },
    rarity: { type: 'varchar' },
    location: { type: 'geometry', spatialFeatureType: 'Point', srid: 4326 },
    latitude: { type: 'numeric', precision: 10, scale: 8 },
    longitude: { type: 'numeric', precision: 11, scale: 8 },
    createdAt: { name: 'created_at', type: 'timestamp', createDate: true },
    updatedAt: { name: 'updated_at', type: 'timestamp', updateDate: true },
  },
  indices: [{ spatial: true, columns: ['location'] }],
  relations: {
    game: { type: 'many-to-one', target: 'Game', joinColumn: { name: 'game_id' } },
    inventories: { type: 'one-to-many', target: 'PlayerInventory', inverseSide: 'gameCard' },
  },
});
```

### PlayerInventoryEntity.js

```javascript
export const PlayerInventoryEntity = new EntitySchema({
  name: 'PlayerInventory',
  tableName: 'player_inventories',
  columns: {
    id: { type: 'uuid', primary: true, generated: 'uuid', default: 'gen_random_uuid()' },
    playerId: { name: 'player_id', type: 'uuid' },
    gameCardId: { name: 'game_card_id', type: 'uuid' },
    collectedAt: { name: 'collected_at', type: 'timestamp', createDate: true },
    updatedAt: { name: 'updated_at', type: 'timestamp', updateDate: true },
  },
  uniques: [{ columns: ['playerId', 'gameCardId'] }],
  relations: {
    player: { type: 'many-to-one', target: 'User', joinColumn: { name: 'player_id' } },
    gameCard: { type: 'many-to-one', target: 'GameCard', joinColumn: { name: 'game_card_id' } },
  },
});
```

---

## 🔧 Scripts Disponíveis

| Comando                          | O que faz                       | Quando usar                             |
| -------------------------------- | ------------------------------- | --------------------------------------- |
| `npm install`                    | Instala dependências            | Inicial ou após mudança em package.json |
| **`npm run typeorm:migrate`**    | **Cria banco e tabelas (MAIN)** | **Primeiro setup e ambiente novo**      |
| `npm run typeorm:migrate-revert` | Desfaz última migration         | Caso de erro na migration anterior      |
| `npm run typeorm:reset`          | Deleta DB e reaplica tudo       | Reset completo (dados perdidos!)        |
| `npm run db:ensure`              | Apenas garante banco existe     | Raro (chamado por typeorm-migrate)      |
| `npm run dev`                    | Inicia servidor com nodemon     | Desenvolvimento                         |
| `npm start`                      | Inicia servidor (produção)      | Produção                                |
| `npm run format`                 | Formata código com Prettier     | Antes de commitar                       |
| `npm run format:check`           | Valida formatação               | CI/CD                                   |

---

## 🌍 PostGIS - Consultas Geoespaciais

### Insert com geometria de ponto

```javascript
const result = await AppDataSource.query(
  `INSERT INTO game_cards 
   (id, game_id, thiltapes_name, image_url, rarity, location, latitude, longitude)
   VALUES 
   ($1, $2, $3, $4, $5, ST_SetSRID(ST_MakePoint($6, $7), 4326), $8, $9)
   RETURNING *;`,
  [uuidv4(), gameId, name, imageUrl, rarity, lng, lat, lat, lng]
);
```

### Query por proximidade (ex: cards dentro de 1km)

```javascript
const nearbyCards = await AppDataSource.query(
  `SELECT id, thiltapes_name, image_url, rarity,
          ST_X(location) as lng, 
          ST_Y(location) as lat,
          ST_Distance(location, ST_SetSRID(ST_MakePoint($2, $3), 4326)) as distance_m
   FROM game_cards
   WHERE game_id = $1
     AND ST_DWithin(location, ST_SetSRID(ST_MakePoint($2, $3), 4326), 1000)
   ORDER BY distance_m;`,
  [gameId, playerLng, playerLat]
);
```

### Funções PostGIS principais

- `ST_MakePoint(lng, lat)` - Cria ponto 2D
- `ST_SetSRID(point, 4326)` - Define projeção WGS84 (coordenadas do GPS)
- `ST_DWithin(point1, point2, meters)` - Distância euclidiana (rápido)
- `ST_Distance(point1, point2)` - Distância real entre pontos
- `ST_X(point)`, `ST_Y(point)` - Extrai lng/lat
- `ST_AsText(geometry)` - Converte para WKT format

---

## ✅ Checklist de Setup Completo

- [ ] Node.js 18+ instalado (`node --version`)
- [ ] PostgreSQL 14+ rodando (`psql --version`)
- [ ] Dependências instaladas (`npm install`)
- [ ] `.env.development` criado com credenciais corretas
- [ ] Database criado (`npm run typeorm:migrate`)
- [ ] Zero erros no comando anterior
- [ ] Servidor rodando (`npm run dev`)
- [ ] Porta 3000 acessível

---

## 🧪 Teste Rápido

```bash
# Terminal 1: Iniciar servidor
npm run dev

# Terminal 2: Testar via curl
curl http://localhost:3000/health

# Esperado:
# {"status": "ok"}
```

---

## � Gateway de Comunicação Front-Back

Este projeto utiliza um **gateway robusto e escalável** que padroniza a comunicação entre o Backend (Node.js) e o Frontend (Android).

### Arquitetura

```
┌─────────────────────────────────────────────────────────┐
│                  Android App (Java)                     │
│  ├─ Retrofit HTTP Client                               │
│  ├─ ApiClient (singleton instance)                     │
│  ├─ ApiService (interface com endpoints)               │
│  └─ AppConfig (URLs dev/prod automáticas)              │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ REST API (JSON over HTTP/HTTPS)
                 │
┌────────────────▼────────────────────────────────────────┐
│                Backend API (Node.js)                    │
│  ├─ Routes (src/routes/) - endpoints organizados        │
│  ├─ Utils (apiResponse.js) - padrão de resposta         │
│  ├─ Middlewares - logger, error handler                │
│  └─ Entities (TypeORM) - modelos de dados              │
└────────────────┬────────────────────────────────────────┘
                 │
                 │ TypeORM + SQL
                 │
┌────────────────▼────────────────────────────────────────┐
│         PostgreSQL + PostGIS (Database)                 │
└─────────────────────────────────────────────────────────┘
```

### Padrão de Resposta

**Sucesso (HTTP 200-201):**

```json
{
  "success": true,
  "data": {
    "id": "uuid-123",
    "name": "Exemplo",
    ...
  },
  "message": "Operação realizada com sucesso",
  "timestamp": "2026-04-11T16:15:30.123Z"
}
```

**Erro (HTTP 400, 401, 404, 500):**

```json
{
  "success": false,
  "error": "VALIDATION_ERROR|AUTH_ERROR|NOT_FOUND|SERVER_ERROR",
  "message": "Descrição legível do erro",
  "details": { ... },
  "timestamp": "2026-04-11T16:15:30.123Z"
}
```

### Como criar uma nova rota

#### Backend: Criar arquivo de rota

Crie `src/routes/games.js`:

```javascript
import { Router } from 'express';
import { successResponse } from '../utils/apiResponse.js';

const router = Router();

/**
 * GET /api/games
 * Retorna lista de todos os jogos
 */
router.get('/', async (req, res) => {
  try {
    // Sua lógica com TypeORM
    const games = []; // await AppDataSource.getRepository(GameEntity).find();

    res.status(200).json(successResponse(games, 'Games listados com sucesso'));
  } catch (error) {
    res.status(500).json(errorResponse('SERVER_ERROR', error.message));
  }
});

/**
 * GET /api/games/:gameId
 * Retorna um jogo específico
 */
router.get('/:gameId', async (req, res) => {
  const { gameId } = req.params;

  // Sua implementação aqui
  res.status(200).json(successResponse({ id: gameId }, 'Game encontrado'));
});

/**
 * POST /api/games
 * Cria novo jogo
 */
router.post('/', async (req, res) => {
  const { name, adminId } = req.body;

  // Sua implementação aqui
  res.status(201).json(successResponse({ id: 'new-uuid', name, adminId }, 'Game criado'));
});

export default router;
```

#### Backend: Registrar rota no router principal

Edite `src/routes/index.js`:

```javascript
import { Router } from 'express';
import healthRouter from './health.js';
import gamesRouter from './games.js'; // ← Adicionar import

const router = Router();

router.use('/', healthRouter);
router.use('/games', gamesRouter); // ← Adicionar mount

export default router;
```

**Resultado:** Sua rota estará disponível em `/api/games`

---

#### Frontend Android: Adicionar método em ApiService

Edite `app/src/main/java/com/example/frontend/api/ApiService.java`:

```java
public interface ApiService {
    @GET("health")
    Call<ApiResponse> healthCheck();

    // ===== GAMES =====

    @GET("games")
    Call<ApiResponse> getGames();

    @GET("games/{gameId}")
    Call<ApiResponse> getGame(@Path("gameId") String gameId);

    @POST("games")
    Call<ApiResponse> createGame(@Body GameRequest request);

    // Adicione mais conforme necessário...
}
```

#### Frontend Android: Usar em Activity/Fragment

```java
import com.example.frontend.api.ApiClient;

public class GamesActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obter games
        ApiClient.getApiService().getGames().enqueue(
            new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();

                        if (apiResponse.isSuccess()) {
                            // Processar dados
                            Object games = apiResponse.getData();
                            // Usar 'games' na UI
                        } else {
                            // Mostrar erro
                            String error = apiResponse.getError();
                            showErrorDialog(error, apiResponse.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    showErrorDialog("Conexão", "Falha ao conectar: " + t.getMessage());
                }
            }
        );
    }
}
```

### Padrão de uso de erro

No **Backend**, você pode lançar erros customizados:

```javascript
import { errorResponse } from '../utils/apiResponse.js';

// Validação
if (!name) {
  return res.status(400).json(errorResponse('VALIDATION_ERROR', 'Nome é obrigatório'));
}

// Auth
if (!user) {
  return res.status(401).json(errorResponse('UNAUTHORIZED', 'Token inválido'));
}

// Não encontrado
if (!game) {
  return res.status(404).json(errorResponse('NOT_FOUND', 'Game não encontrado'));
}
```

### Adicionar autenticação JWT (futuro)

**Backend:**

```javascript
// src/middlewares/authMiddleware.js
export const requireAuth = (req, res, next) => {
  const token = req.headers.authorization?.split(' ')[1];

  if (!token) {
    return res.status(401).json(errorResponse('UNAUTHORIZED', 'Token não fornecido'));
  }

  // Validar token aqui
  // Se válido: req.user = decoded; next();
};

// Usar em rotas:
router.get('/me', requireAuth, (req, res) => {
  // req.user está disponível aqui
});
```

**Android:**

```java
// Em ApiInterceptor.java (já tem placeholder)
String token = TokenManager.getInstance().getToken();
if (token != null && !token.isEmpty()) {
    requestBuilder.header("Authorization", "Bearer " + token);
}
```

### Configuração de ambientes (Dev vs Produção)

**Backend:**

- Dev: `npm run dev` - listening on port 3000
- Prod: `npm start` - NODE_ENV=production

**Android:**

```java
// AppConfig.java
public static final String API_BASE_URL = BuildConfig.DEBUG
    ? "http://10.0.2.2:3000/api"        // Desenvolvimento (emulador)
    : "https://api.thiltapes.com/api";  // Produção
```

Deploy em produção:

- Altere URL em `AppConfig.java`
- Backend em servidor remoto com HTTPS/SSL
- Nenhuma outra mudança necessária!

### Status da Aplicação (Home Screen)

A página inicial do app (`MainActivity.java`) mostra:

- ✅ **Status visual** - Indicador colorido (verde/vermelho/laranja)
- ✅ **URL da API** - Mostra qual endpoint está configurado
- ✅ **Ambiente** - Development ou Release
- ✅ **Resposta completa** - Mostra JSON detalhado da API

Este é o **canário da aplicação** - se a home conecta, o resto provavelmente funciona.

---

## 🚀 Expandindo o Gateway

### Checklist: Adicionar novo endpoint

1. **Backend:**
   - [ ] Criar `src/routes/recurso.js`
   - [ ] Implementar rotas (GET, POST, PUT, DELETE)
   - [ ] Importar e registrar em `src/routes/index.js`
   - [ ] Testar com curl: `curl http://localhost:3000/api/recurso`

2. **Android:**
   - [ ] Adicionar método em `ApiService.java`
   - [ ] Usar em Activity com `ApiClient.getApiService().metodo()`
   - [ ] Implementar `Callback<ApiResponse>` para processar resposta

3. **Tipo de dados:**
   - [ ] Se precisa modelo Java, criar classe em `models/` (futuro)
   - [ ] Backend retorna sempre padrão `ApiResponse`

---

## 📝 Próximos Passos

1. ✅ **Gateway base criado** - Ambos os lados comunicando
2. ⏳ **Seu time expande rotas** - Games, Cards, Inventory
3. ⏳ **Adicionar JWT** - Autenticação (sem refação)
4. ⏳ **Geolocalização** - LocationListener no Android
5. ⏳ **Deploy** - Trocar URLs + vendor em servidor

---

## 🐛 Troubleshooting

### Erro: "Banco de dados já existe com diferenças de schema"

```bash
# Solução 1: Resetar banco (⚠️ CAUSA PERDA DE DADOS)
npm run typeorm:reset

# Solução 2: Inspecionar migrations aplicadas
npm run typeorm:migrate --show-migrations
```

### Erro de conexão PostgreSQL

- Verificar se PostgreSQL está rodando
- Verificar credenciais em `.env.development`
- Verificar se firewall permite porta 5432

### Erro: "module not found 'reflect-metadata'"

```bash
npm install reflect-metadata
```

### Indices PostGIS não criados

```bash
# Reconectar ao banco e validar
psql -U postgres -d thiltapes_db -c "\d+ game_cards"
# Procurar por: idx_game_cards_location
```

---

## 📚 Dependências principais

```json
{
  "express": "^5.0.0",
  "typeorm": "^0.3.27",
  "reflect-metadata": "^0.2.2",
  "pg": "^8.12.0",
  "nodemon": "^3.0.1",
  "dotenv": "^16.4.5"
}
```

---

## 📖 Links úteis

- [TypeORM Docs](https://typeorm.io/)
- [PostGIS Manual](https://postgis.net/documentation/)
- [Express Docs](https://expressjs.com/)
- [PostgreSQL Docs](https://www.postgresql.org/docs/)
