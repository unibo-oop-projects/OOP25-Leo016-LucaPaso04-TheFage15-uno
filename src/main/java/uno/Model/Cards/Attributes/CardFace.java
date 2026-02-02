package uno.model.cards.attributes;

/**
 * Represents the face of a card in the Uno game, defined by its color and value.
 * @param color the color of the card
 * @param value the value of the card
 */
public record CardFace(CardColor color, CardValue value) {
}
