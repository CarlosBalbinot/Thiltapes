import { BaseEntity, EntitySchema } from 'typeorm';

export class GameCard extends BaseEntity {}

export const GameCardEntity = new EntitySchema({
  name: 'GameCard',
  target: GameCard,
  tableName: 'game_cards',
  columns: {
    id: {
      type: 'uuid',
      primary: true,
      generated: 'uuid',
      default: () => 'gen_random_uuid()',
    },
    thiltapes_name: {
      type: String,
    },
    image_url: {
      type: String,
    },
    rarity: {
      type: String,
    },
    location: {
      type: 'geometry',
      spatialFeatureType: 'Point',
      srid: 4326,
    },
    radius_meters: {
      type: Number,
      default: 10,
    },
    is_collected: {
      type: Boolean,
      default: false,
    },
    created_at: {
      type: 'timestamptz',
      default: () => 'NOW()',
    },
  },
  relations: {
    game: {
      type: 'many-to-one',
      target: 'Game',
      joinColumn: {
        name: 'game_id',
      },
      nullable: false,
      onDelete: 'CASCADE',
      inverseSide: 'game_cards',
    },
    inventories: {
      type: 'one-to-many',
      target: 'PlayerInventory',
      inverseSide: 'game_card',
    },
  },
  indices: [
    {
      name: 'idx_game_cards_location_gist',
      spatial: true,
      columns: ['location'],
    },
  ],
});
