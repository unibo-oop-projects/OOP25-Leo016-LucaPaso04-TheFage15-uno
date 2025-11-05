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
    WILD_DRAW_FOUR  // Jolly Pesca Quattro
}
