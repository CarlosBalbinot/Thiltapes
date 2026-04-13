import { BaseEntity, EntitySchema } from 'typeorm';

export class PlayerInventory extends BaseEntity {}

export const PlayerInventoryEntity = new EntitySchema({
  name: 'PlayerInventory',
  target: PlayerInventory,
  tableName: 'player_inventories',
  columns: {
    id: {
      type: 'uuid',
      primary: true,
      generated: 'uuid',
      default: () => 'gen_random_uuid()',
    },
    found_location: {
      type: 'geometry',
      spatialFeatureType: 'Point',
      srid: 4326,
    },
    found_at: {
      type: 'timestamptz',
      default: () => 'NOW()',
    },
  },
  relations: {
    player: {
      type: 'many-to-one',
      target: 'User',
      joinColumn: {
        name: 'player_id',
      },
      nullable: false,
      onDelete: 'CASCADE',
      inverseSide: 'inventories',
    },
    game_card: {
      type: 'many-to-one',
      target: 'GameCard',
      joinColumn: {
        name: 'game_card_id',
      },
      nullable: false,
      onDelete: 'CASCADE',
      inverseSide: 'inventories',
    },
  },
  uniques: [
    {
      name: 'uq_player_inventory_player_card',
      columns: ['player', 'game_card'],
    },
  ],
});
