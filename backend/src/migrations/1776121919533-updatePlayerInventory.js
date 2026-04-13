/**
 * @typedef {import('typeorm').MigrationInterface} MigrationInterface
 * @typedef {import('typeorm').QueryRunner} QueryRunner
 */

/**
 * @class
 * @implements {MigrationInterface}
 */
export class UpdatePlayerInventory1776121919533 {
  /**
   * @param {QueryRunner} queryRunner
   */
  async up(queryRunner) {
    // Adicionar coluna found_location do tipo geometry do PostGIS
    await queryRunner.query(`
            ALTER TABLE player_inventories
            ADD COLUMN found_location geometry(Point, 4326)
        `);

    // Criar índice GIST para otimizar queries espaciais
    await queryRunner.query(`
            CREATE INDEX IF NOT EXISTS idx_player_inventories_location_gist 
            ON player_inventories USING GIST (found_location)
        `);
  }

  /**
   * @param {QueryRunner} queryRunner
   */
  async down(queryRunner) {
    // Remover índice
    await queryRunner.query(`
            DROP INDEX IF EXISTS idx_player_inventories_location_gist
        `);

    // Remover coluna
    await queryRunner.query(`
            ALTER TABLE player_inventories
            DROP COLUMN IF EXISTS found_location
        `);
  }
}
