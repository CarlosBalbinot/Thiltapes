import TCGdex from "@tcgdex/sdk";

/**
 * Fetches a random card from the TCGdex API.
 * Continuously polls for a new card if the current one is missing an image or rarity, 
 * preventing 'undefined' values from being inserted into the database.
 * * @returns {Promise<{thiltapes_name: string, rarity: string, image: string}>} A validated card object.
 */
export default async function generateCard() {
  const tcgdex = new TCGdex();
  let randomCard = await tcgdex.random.card();

  // Garante que a carta possui imagem e raridade antes de prosseguir
  while (!randomCard || !randomCard.image || !randomCard.rarity) {
    randomCard = await tcgdex.random.card();
  }

  return {
    thiltapes_name: randomCard.name,
    rarity: randomCard.rarity,
    image: randomCard.getImageURL("high", "png"),
  };
}