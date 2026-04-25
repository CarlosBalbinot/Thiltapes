import TCGdex from '@tcgdex/sdk';

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms));

export default async function generateCard() {
  const tcgdex = new TCGdex(); 
  let attempts = 3;

  while (attempts > 0) {
    try {
      const randomCard = await tcgdex.random.card();
      
      if (randomCard && randomCard.image && randomCard.rarity) {
        return {
          thiltapes_name: randomCard.name,
          rarity: randomCard.rarity,
          image: randomCard.getImageURL("high", "png"),
        };
      }
    } catch (error) {
      console.warn(`[TCGdex Aviso] Falha na tentativa ${4 - attempts}/3:`, error.message);
    }
    
    await delay(500); 
    attempts--;
  }

  console.error("❌ TCGdex inacessível após 3 tentativas. Injetando MissingNo.");
  return {
    thiltapes_name: "MissingNo (Fallback)",
    rarity: "Common",
    image: "https://assets.tcgdex.net/en/swsh/swsh3/1/high.png", 
  };
}