import 'reflect-metadata';
import './env.js';

import { DataSource } from 'typeorm';

import { UserEntity } from '../entities/UserEntity.js';
import { GameEntity } from '../entities/GameEntity.js';
import { GameCardEntity } from '../entities/GameCardEntity.js';
import { PlayerInventoryEntity } from '../entities/PlayerInventoryEntity.js';
import { InitialSchema1712958000000 } from '../migrations/1712958000000-InitialSchema.js';

const enableLogging = process.env.NODE_ENV !== 'production';

export const AppDataSource = new DataSource({
  type: 'postgres',
  url: process.env.DATABASE_URL,
  logging: enableLogging ? ['error', 'warn'] : ['error'],
  entities: [UserEntity, GameEntity, GameCardEntity, PlayerInventoryEntity],
  migrations: [InitialSchema1712958000000],
  synchronize: false,
});

export default AppDataSource;
