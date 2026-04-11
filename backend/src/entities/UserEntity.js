import { BaseEntity, EntitySchema } from 'typeorm';

export const UserRole = {
  ADMIN: 'ADMIN',
  PLAYER: 'PLAYER',
};

export class User extends BaseEntity {}

export const UserEntity = new EntitySchema({
  name: 'User',
  target: User,
  tableName: 'users',
  columns: {
    id: {
      type: 'uuid',
      primary: true,
      generated: 'uuid',
      default: () => 'gen_random_uuid()',
    },
    username: {
      type: String,
      unique: true,
    },
    password_hash: {
      type: String,
    },
    role: {
      type: 'enum',
      enum: Object.values(UserRole),
      default: UserRole.PLAYER,
    },
    created_at: {
      type: 'timestamptz',
      default: () => 'NOW()',
    },
  },
  relations: {
    games_admin: {
      type: 'one-to-many',
      target: 'Game',
      inverseSide: 'admin',
    },
    inventories: {
      type: 'one-to-many',
      target: 'PlayerInventory',
      inverseSide: 'player',
    },
  },
});
