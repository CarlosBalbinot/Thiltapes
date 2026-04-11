import AppDataSource from '../config/dataSource.js';

export async function insertGameCardOnMap({
  gameId,
  thiltapesName,
  imageUrl,
  rarity,
  lat,
  lng,
  radius = 10,
}) {
  const result = await AppDataSource.query(
    `
      INSERT INTO game_cards (game_id, thiltapes_name, image_url, rarity, location, radius_meters)
      VALUES (
        $1::uuid,
        $2,
        $3,
        $4,
        ST_SetSRID(ST_MakePoint($5, $6), 4326),
        $7
      )
      RETURNING id, game_id, thiltapes_name, image_url, rarity, radius_meters, is_collected, created_at
    `,
    [gameId, thiltapesName, imageUrl, rarity, lng, lat, radius]
  );

  return result[0];
}

export async function findCardsWithinPlayerRadius({ gameId, lat, lng }) {
  return AppDataSource.query(
    `
    SELECT
      gc.id,
      gc.game_id,
      gc.thiltapes_name,
      gc.image_url,
      gc.rarity,
      gc.radius_meters,
      gc.is_collected,
      gc.created_at,
      ST_X(gc.location) AS lng,
      ST_Y(gc.location) AS lat,
      ST_AsText(gc.location) AS location_wkt
    FROM game_cards gc
    WHERE gc.game_id = $1::uuid
      AND ST_DWithin(
        gc.location::geography,
        ST_SetSRID(ST_MakePoint($2, $3), 4326)::geography,
        gc.radius_meters
      )
    ORDER BY gc.created_at ASC
    `,
    [gameId, lng, lat]
  );
}
