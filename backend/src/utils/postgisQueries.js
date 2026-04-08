import prisma from '../config/prisma.js';

export async function insertGameCardOnMap({ gameId, cardId, lat, lng, radius = 10 }) {
  return prisma.$executeRaw`
    INSERT INTO game_cards (game_id, card_id, location, radius_meters)
    VALUES (
      ${gameId}::uuid,
      ${cardId}::uuid,
      ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326),
      ${radius}
    )
  `;
}

export async function findCardsWithinPlayerRadius({ gameId, lat, lng }) {
  return prisma.$queryRaw`
    SELECT
      gc.id,
      gc.game_id,
      gc.card_id,
      gc.radius_meters,
      gc.created_at,
      c.name AS card_name,
      c.image_url,
      c.description,
      ST_AsText(gc.location) AS location_wkt
    FROM game_cards gc
    INNER JOIN cards c ON c.id = gc.card_id
    WHERE gc.game_id = ${gameId}::uuid
      AND ST_DWithin(
        gc.location::geography,
        ST_SetSRID(ST_MakePoint(${lng}, ${lat}), 4326)::geography,
        gc.radius_meters
      )
    ORDER BY gc.created_at ASC;
  `;
}
