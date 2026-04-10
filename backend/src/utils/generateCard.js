import TCGdex from "@tcgdex/sdk";

export default async function generateCard() {
  const tcgdex = new TCGdex();
  const randomCard = await tcgdex.random.card();

  return {
    thiltapes_name: randomCard.name,
    rarity: randomCard.rarity,
    image: randomCard.getImageURL("high", "png"),
  };
}
