import { BaseEntity, EntitySchema } from 'typeorm';

export const GameStatus = {
  ACTIVE: 'ACTIVE',
  ENDED: 'ENDED',
};

export class Game extends BaseEntity {}

export const GameEntity = new EntitySchema({
  name: 'Game',
  target: Game,
  tableName: 'games',
  columns: {
    id: {
      type: 'uuid',
      primary: true,
      generated: 'uuid',
      default: () => 'gen_random_uuid()',
    },
    name: {
      type: String,
    },
    status: {
      type: 'enum',
      enum: Object.values(GameStatus),
      default: GameStatus.ACTIVE,
    },
    created_at: {
      type: 'timestamptz',
      default: () => 'NOW()',
    },
  },
  relations: {
    admin: {
      type: 'many-to-one',
      target: 'User',
      joinColumn: {
        name: 'admin_id',
      },
      nullable: false,
      onDelete: 'RESTRICT',
      inverseSide: 'games_admin',
    },
    game_cards: {
      type: 'one-to-many',
      target: 'GameCard',
      inverseSide: 'game',
    },
  },
});
