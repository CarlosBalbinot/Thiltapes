# Thiltapes - Backend API

Backend da aplicação Thiltapes construída com Node.js, Express, Prisma e PostgreSQL com suporte a geolocalização via PostGIS.

## 📋 Pré-requisitos

- **Node.js** v18 ou superior
- **PostgreSQL** 14+ com extensão **PostGIS** instalada
- **npm** ou **yarn** para gerenciar dependências

## 🚀 Configuração Inicial

### 1. Instalar Dependências

```bash
npm install
```

### 2. Configurar Variáveis de Ambiente

O projeto usa um sistema centralizado de variáveis de ambiente. Não edite URLs diretamente—use componentes separados para maior clareza.

Crie em `backend/config/.env.development`:

```env
NODE_ENV=development
PORT=3000

# Database Connection - Componentes separados
DB_HOST=localhost
DB_PORT=5432
DB_USER=postgres
DB_PASSWORD=postgres
DB_NAME=thiltapes_db

# Database Admin (para criar o banco se necessário)
DB_ADMIN_HOST=localhost
DB_ADMIN_PORT=5432
DB_ADMIN_USER=postgres
DB_ADMIN_PASSWORD=postgres

CORS_ORIGIN=http://localhost:3000,http://localhost:8080
JWT_SECRET=change_this_development_secret
JWT_EXPIRATION=7d
```

**⚠️ Importante**: Não compartilhe `.env.development` e `.env.production` em repositórios. Eles estão no `.gitignore`.

### 3. Gerar Prisma Client

```bash
npm run prisma:generate
```

### 4. Gerar o Migrate

```
npm run prisma:migrate-create
```

### 5 Adicionar essa linha em `/prisma/migrations/<migration_criada>/migration.sql`

```
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 6. Executar Migrations

```bash
npm run prisma:migrate
```

Este comando:

- ✅ Cria automaticamente o banco de dados (usando `DB_ADMIN_*`)
- ✅ Aplica todas as migrations pendentes
- ✅ Cria extensões PostgreSQL (`pgcrypto`, `postgis`)
- ✅ Cria todas as tabelas do schema

### 5. Iniciar o Servidor

**Desenvolvimento** (com hot-reload):

```bash
npm run dev
```

**Produção**:

```bash
npm start
```

---

## 📦 Scripts do package.json

| Comando                         | O que faz                                                |
| ------------------------------- | -------------------------------------------------------- |
| `npm start`                     | Inicia em **produção** (`NODE_ENV=production`)           |
| `npm run dev`                   | Inicia em **desenvolvimento** com hot-reload (`nodemon`) |
| `npm run format`                | Formata **todo** o código com Prettier                   |
| `npm run format:check`          | Verifica formatação sem alterar nada                     |
| `npm run prisma:generate`       | Gera o Prisma Client                                     |
| `npm run prisma:migrate`        | Cria banco automaticamente e executa migrations          |
| `npm run prisma:migrate-create` | Cria nova migration **sem aplicar** (para revisar SQL)   |
| `npm run prisma:studio`         | Abre GUI visual para gerenciar dados do banco            |

---

## 🎨 Formatação de Código com Prettier

O projeto usa **Prettier** para manter o código padronizado e consistente.

### Como Funciona

- Prettier formata automaticamente ao salvar (ativado em `.vscode/settings.json`)
- Estilo definido em `.prettierrc`:
  - 100 caracteres por linha
  - Semicolon no final
  - Aspas simples `'` em strings
  - Trailing comma em objetos/arrays

### Usar Prettier

**Formatar tudo**:

```bash
npm run format
```

**Verificar sem alterar**:

```bash
npm run format:check
```

**Arquivos ignorados**:

- `node_modules/`, `prisma/migrations/`, `.prisma/`

---

## 🗄️ Variáveis de Ambiente

### Sistema Estruturado (Sem URLs Confusas)

Ao invés de `postgresql://user:pass@host:port/db`, use componentes:

```env
DB_HOST=localhost           # Host do PostgreSQL
DB_PORT=5432                # Porta
DB_USER=app_user            # Usuário da aplicação
DB_PASSWORD=senha_da_app    # Senha da aplicação
DB_NAME=thiltapes_db        # Nome do banco
```

O arquivo `src/config/env.js` **monta automaticamente** as URLs:

- `DATABASE_URL = postgresql://app_user:senha@localhost:5432/thiltapes_db`
- `DATABASE_ADMIN_URL = postgresql://postgres:senha@localhost:5432/postgres`

### Fluxo de Carregamento

1. `src/config/env.js` é carregado
2. Lê `NODE_ENV` (development ou production)
3. Carrega `config/.env.development` ou `config/.env.production`
4. Monta `DATABASE_URL` e `DATABASE_ADMIN_URL` automaticamente
5. Todos os módulos acessam via `process.env.DATABASE_URL`

---

## 🌍 PostGIS - Sistema Geoespacial

O projeto usa **PostGIS** para armazenar e consultar coordenadas geográficas.

### Por Que PostGIS?

A tabela `game_cards` armazena `location` como `geometry(Point, 4326)`:

- **4326** = sistema de coordenadas WGS84 (GPS padrão)
- PostGIS permite consultas como: "encontre todos os pontos dentro de 100 metros"

### Instalação no Windows

1. Abra **StackBuilder** (vem com PostgreSQL):
   - Path: `C:\Program Files\PostgreSQL\16\bin\stackbuilder.exe`

2. Selecione sua instalação PostgreSQL

3. Vá para **Spatial Extensions → PostGIS**

4. Instale a versão compatível

5. Teste a migration:

```bash
npm run prisma:migrate
```

### Se der Erro

Execute manualmente no banco:

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;
```

### Consultas Geoespaciais

Use helpers em `src/utils/postgisQueries.js`:

```javascript
import { insertGameCardOnMap } from './src/utils/postgisQueries.js';

// Inserir carta no mapa
await insertGameCardOnMap({
  gameId: '123e4567-e89b-12d3-a456-426614174000',
  cardId: '987fcdeb-51a2-11ec-81d3-0242ac120002',
  lat: -29.1676,
  lng: -51.1799,
  radius: 20, // metros
});

// Buscar cartas próximas
import { findCardsWithinPlayerRadius } from './src/utils/postgisQueries.js';

const nearby = await findCardsWithinPlayerRadius({
  gameId: '123e4567-e89b-12d3-a456-426614174000',
  lat: -29.1676,
  lng: -51.1799,
});
```

---

## 🏗️ Estrutura do Projeto

```
backend/
├── config/
│   ├── .env.development          # ⚠️ Não versionar (credenciais)
│   ├── .env.development.example  # ✅ Template versionado
│   ├── .env.production           # ⚠️ Não versionar
│   └── .env.production.example   # ✅ Template versionado
├── scripts/
│   ├── prisma-runner.js          # Injeta env antes de rodar Prisma
│   └── ensure-database.js        # Cria banco automaticamente
├── prisma/
│   ├── schema.prisma             # Definição de modelos
│   ├── migrations/               # Histórico de mudanças
│   └── migration_lock.toml       # Lock Prisma (não edite)
├── src/
│   ├── config/
│   │   ├── env.js                # Loader de variáveis
│   │   └── prisma.js             # Instância do PrismaClient
│   ├── middlewares/
│   │   ├── errorHandler.js       # Tratamento de erros
│   │   └── requestLogger.js      # Logger de requisições
│   ├── routes/
│   │   └── index.js              # Rotas da API
│   ├── utils/
│   │   └── postgisQueries.js     # Helpers geoespaciais
│   ├── app.js                    # Configuração Express
│   └── server.js                 # Entrada
├── .prettierrc                   # Config Prettier
├── .gitignore                    # Não versionar
├── package.json                  # Dependências
└── README.md                     # Este arquivo
```

---

## 🔗 Endpoints

- **GET** `/health` - Saúde do servidor
- **GET** `/api/test` - API de teste

---

## 🐛 Troubleshooting

### "autenticação do tipo senha falhou"

Verifique em `config/.env.development`:

- `DB_USER`, `DB_PASSWORD`, `DB_HOST`, `DB_PORT` estão corretos?
- O PostgreSQL está rodando?

### "extensão postgis não está disponível"

Instale PostGIS via StackBuilder (ver seção 🌍 PostGIS acima)

### Código não formatado

Rode: `npm run format`

### Erro na migration "p3006"

Verifique:

1. PostGIS está instalado?
2. PostgreSQL está rodando?
3. Credenciais em `.env.development` estão corretas?

---

## 📚 Documentação

- [Express.js](https://expressjs.com) - Framework Web
- [Prisma ORM](https://www.prisma.io/docs/) - ORM do Banco
- [PostgreSQL](https://www.postgresql.org/docs/) - Banco de Dados
- [PostGIS](https://postgis.net/documentation/) - Geoespacial

---

## 🔐 Segurança

- ✅ CORS configurável por ambiente
- ✅ Body parser limitado
- ✅ Tratamento de erros
- ✅ Variáveis sensíveis não versionadas (`.gitignore`)
- 🔲 Validação de entrada (a fazer)
- 🔲 Autenticação JWT (a fazer)
- 🔲 Rate limiting (a fazer)

---

## 📄 Licença

ISC
