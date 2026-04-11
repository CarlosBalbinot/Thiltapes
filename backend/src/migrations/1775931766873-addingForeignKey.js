/**
 * Migration to add foreign keys and update enums.
 */
export class AddingForeignKey1775931766873 {
    name = 'AddingForeignKey1775931766873'

    async up(queryRunner) {
        await queryRunner.query(`ALTER TABLE "games" DROP CONSTRAINT "fk_games_admin"`);
        await queryRunner.query(`ALTER TABLE "game_cards" DROP CONSTRAINT "fk_game_cards_game"`);
        await queryRunner.query(`ALTER TABLE "player_inventories" DROP CONSTRAINT "fk_player_inventories_player"`);
        await queryRunner.query(`ALTER TABLE "player_inventories" DROP CONSTRAINT "fk_player_inventories_card"`);
        await queryRunner.query(`DROP INDEX "public"."idx_game_cards_game_id"`);
        await queryRunner.query(`DROP INDEX "public"."idx_player_inventories_player_id"`);
        await queryRunner.query(`ALTER TABLE "game_cards" RENAME COLUMN "is_collected" TO "collected_by_id"`);
        await queryRunner.query(`ALTER TYPE "public"."role_enum" RENAME TO "role_enum_old"`);
        await queryRunner.query(`CREATE TYPE "public"."users_role_enum" AS ENUM('ADMIN', 'PLAYER')`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" DROP DEFAULT`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" TYPE "public"."users_role_enum" USING "role"::"text"::"public"."users_role_enum"`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" SET DEFAULT 'PLAYER'`);
        await queryRunner.query(`DROP TYPE "public"."role_enum_old"`);
        await queryRunner.query(`ALTER TYPE "public"."game_status_enum" RENAME TO "game_status_enum_old"`);
        await queryRunner.query(`CREATE TYPE "public"."games_status_enum" AS ENUM('ACTIVE', 'ENDED')`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" DROP DEFAULT`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" TYPE "public"."games_status_enum" USING "status"::"text"::"public"."games_status_enum"`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" SET DEFAULT 'ACTIVE'`);
        await queryRunner.query(`DROP TYPE "public"."game_status_enum_old"`);
        await queryRunner.query(`ALTER TABLE "game_cards" DROP COLUMN "collected_by_id"`);
        await queryRunner.query(`ALTER TABLE "game_cards" ADD "collected_by_id" uuid`);
        await queryRunner.query(`ALTER TABLE "games" ADD CONSTRAINT "FK_f7e664f484f44390129bde3850d" FOREIGN KEY ("admin_id") REFERENCES "users"("id") ON DELETE RESTRICT ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "game_cards" ADD CONSTRAINT "FK_20449eeced3418c8d94bcf6234f" FOREIGN KEY ("game_id") REFERENCES "games"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "game_cards" ADD CONSTRAINT "FK_9b921d71c52c640405819a9052e" FOREIGN KEY ("collected_by_id") REFERENCES "users"("id") ON DELETE SET NULL ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "player_inventories" ADD CONSTRAINT "FK_7beef50f29217616e0d17f7ad6a" FOREIGN KEY ("player_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "player_inventories" ADD CONSTRAINT "FK_842515720e27d5e9483790b007e" FOREIGN KEY ("game_card_id") REFERENCES "game_cards"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
    }

    async down(queryRunner) {
        await queryRunner.query(`ALTER TABLE "player_inventories" DROP CONSTRAINT "FK_842515720e27d5e9483790b007e"`);
        await queryRunner.query(`ALTER TABLE "player_inventories" DROP CONSTRAINT "FK_7beef50f29217616e0d17f7ad6a"`);
        await queryRunner.query(`ALTER TABLE "game_cards" DROP CONSTRAINT "FK_9b921d71c52c640405819a9052e"`);
        await queryRunner.query(`ALTER TABLE "game_cards" DROP CONSTRAINT "FK_20449eeced3418c8d94bcf6234f"`);
        await queryRunner.query(`ALTER TABLE "games" DROP CONSTRAINT "FK_f7e664f484f44390129bde3850d"`);
        await queryRunner.query(`ALTER TABLE "game_cards" DROP COLUMN "collected_by_id"`);
        await queryRunner.query(`ALTER TABLE "game_cards" ADD "collected_by_id" boolean NOT NULL DEFAULT false`);
        await queryRunner.query(`CREATE TYPE "public"."game_status_enum_old" AS ENUM('ACTIVE', 'ENDED')`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" DROP DEFAULT`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" TYPE "public"."game_status_enum_old" USING "status"::"text"::"public"."game_status_enum_old"`);
        await queryRunner.query(`ALTER TABLE "games" ALTER COLUMN "status" SET DEFAULT 'ACTIVE'`);
        await queryRunner.query(`DROP TYPE "public"."games_status_enum"`);
        await queryRunner.query(`ALTER TYPE "public"."game_status_enum_old" RENAME TO "game_status_enum"`);
        await queryRunner.query(`CREATE TYPE "public"."role_enum_old" AS ENUM('ADMIN', 'PLAYER')`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" DROP DEFAULT`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" TYPE "public"."role_enum_old" USING "role"::"text"::"public"."role_enum_old"`);
        await queryRunner.query(`ALTER TABLE "users" ALTER COLUMN "role" SET DEFAULT 'PLAYER'`);
        await queryRunner.query(`DROP TYPE "public"."users_role_enum"`);
        await queryRunner.query(`ALTER TYPE "public"."role_enum_old" RENAME TO "role_enum"`);
        await queryRunner.query(`ALTER TABLE "game_cards" RENAME COLUMN "collected_by_id" TO "is_collected"`);
        await queryRunner.query(`CREATE INDEX "idx_player_inventories_player_id" ON "player_inventories" ("player_id") `);
        await queryRunner.query(`CREATE INDEX "idx_game_cards_game_id" ON "game_cards" ("game_id") `);
        await queryRunner.query(`ALTER TABLE "player_inventories" ADD CONSTRAINT "fk_player_inventories_card" FOREIGN KEY ("game_card_id") REFERENCES "game_cards"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "player_inventories" ADD CONSTRAINT "fk_player_inventories_player" FOREIGN KEY ("player_id") REFERENCES "users"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "game_cards" ADD CONSTRAINT "fk_game_cards_game" FOREIGN KEY ("game_id") REFERENCES "games"("id") ON DELETE CASCADE ON UPDATE NO ACTION`);
        await queryRunner.query(`ALTER TABLE "games" ADD CONSTRAINT "fk_games_admin" FOREIGN KEY ("admin_id") REFERENCES "users"("id") ON DELETE RESTRICT ON UPDATE NO ACTION`);
    }
}