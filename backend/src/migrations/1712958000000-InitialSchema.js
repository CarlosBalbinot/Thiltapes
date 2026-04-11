export class InitialSchema1712958000000 {
  name = 'InitialSchema1712958000000';

  async up(queryRunner) {
    await queryRunner.query('CREATE EXTENSION IF NOT EXISTS pgcrypto');
    await queryRunner.query('CREATE EXTENSION IF NOT EXISTS postgis');

    await queryRunner.query(`
      DO $$
      BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'role_enum') THEN
          CREATE TYPE role_enum AS ENUM ('ADMIN', 'PLAYER');
        END IF;
      END $$;
    `);

    await queryRunner.query(`
      DO $$
      BEGIN
        IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'game_status_enum') THEN
          CREATE TYPE game_status_enum AS ENUM ('ACTIVE', 'ENDED');
        END IF;
      END $$;
    `);

    await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS users (
        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
        username varchar NOT NULL UNIQUE,
        password_hash varchar NOT NULL,
        role role_enum NOT NULL DEFAULT 'PLAYER',
        created_at timestamptz NOT NULL DEFAULT NOW()
      )
    `);

    await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS games (
        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
        admin_id uuid NOT NULL,
        name varchar NOT NULL,
        status game_status_enum NOT NULL DEFAULT 'ACTIVE',
        created_at timestamptz NOT NULL DEFAULT NOW(),
        CONSTRAINT fk_games_admin FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE RESTRICT
      )
    `);

    await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS game_cards (
        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
        game_id uuid NOT NULL,
        thiltapes_name varchar NOT NULL,
        image_url varchar NOT NULL,
        rarity varchar NOT NULL,
        location geometry(Point, 4326) NOT NULL,
        radius_meters integer NOT NULL DEFAULT 10,
        is_collected boolean NOT NULL DEFAULT false,
        created_at timestamptz NOT NULL DEFAULT NOW(),
        CONSTRAINT fk_game_cards_game FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE
      )
    `);

    await queryRunner.query(`
      CREATE TABLE IF NOT EXISTS player_inventories (
        id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
        player_id uuid NOT NULL,
        game_card_id uuid NOT NULL,
        found_at timestamptz NOT NULL DEFAULT NOW(),
        CONSTRAINT fk_player_inventories_player FOREIGN KEY (player_id) REFERENCES users(id) ON DELETE CASCADE,
        CONSTRAINT fk_player_inventories_card FOREIGN KEY (game_card_id) REFERENCES game_cards(id) ON DELETE CASCADE,
        CONSTRAINT uq_player_inventory_player_card UNIQUE (player_id, game_card_id)
      )
    `);

    await queryRunner.query(
      'CREATE INDEX IF NOT EXISTS idx_game_cards_location_gist ON game_cards USING GIST (location)'
    );
    await queryRunner.query(
      'CREATE INDEX IF NOT EXISTS idx_game_cards_game_id ON game_cards (game_id)'
    );
    await queryRunner.query(
      'CREATE INDEX IF NOT EXISTS idx_player_inventories_player_id ON player_inventories (player_id)'
    );
  }

  async down(queryRunner) {
    await queryRunner.query('DROP INDEX IF EXISTS idx_player_inventories_player_id');
    await queryRunner.query('DROP INDEX IF EXISTS idx_game_cards_game_id');
    await queryRunner.query('DROP INDEX IF EXISTS idx_game_cards_location_gist');

    await queryRunner.query('DROP TABLE IF EXISTS player_inventories');
    await queryRunner.query('DROP TABLE IF EXISTS game_cards');
    await queryRunner.query('DROP TABLE IF EXISTS games');
    await queryRunner.query('DROP TABLE IF EXISTS users');

    await queryRunner.query('DROP TYPE IF EXISTS game_status_enum');
    await queryRunner.query('DROP TYPE IF EXISTS role_enum');
  }
}
