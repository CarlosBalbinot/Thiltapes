export default function getRandomLocation(centerLat, centerLng, radiusInMeters) {
  // ~111320 meters in one degree
  const radiusInDegrees = radiusInMeters / 111320; 
  
  const u = Math.random();
  const v = Math.random();
  const w = radiusInDegrees * Math.sqrt(u);
  const t = 2 * Math.PI * v;
  
  const dLat = w * Math.cos(t);
  // Adjust longitude based on the latitude
  const dLng = (w * Math.sin(t)) / Math.cos((centerLat * Math.PI) / 180);

  return {
    lat: centerLat + dLat,
    lng: centerLng + dLng,
  };
}