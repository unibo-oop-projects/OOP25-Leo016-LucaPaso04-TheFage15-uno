package uno.Model.Cards.Attributes;

public enum CardValue {
    // Carte Numerate
    ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE,
    
    // Carte Azione
    SKIP,     // Salta
    REVERSE,  // Inverti
    DRAW_TWO, // Pesca Due
    
    // Carte Jolly
    WILD,           // Jolly
    WILD_DRAW_FOUR,  // Jolly Pesca Quattro

    FLIP, // Flip
    WILD_DARK, // Jolly Lato Scuro (per Flip)
    WILD_DRAW_TWO, // Jolly Pesca Due (per Flip)
    DRAW_FIVE, // Pesca Cinque (per Flip)
    DRAW_ONE, // Pesca Uno (per Flip)
    SKIP_EVERYONE, // Salta Tutti (per Flip)
    WILD_DRAW_COLOR, // Jolly Pesca Colore (per Flip)

    // Carte per All Wild Mode
    WILD_ALLWILD,
    WILD_DRAW_FOUR_ALLWILD,
    WILD_DRAW_TWO_ALLWILD,
    WILD_TARGETED_DRAW_TWO, // Jolly Pesca Due Mirato
    WILD_FORCED_SWAP, // Jolly Scambio Forzato
    WILD_REVERSE, // Jolly Inverti
    WILD_SKIP, // Jolly Salta
    WILD_SKIP_TWO, // Jolly Salta Due
}
