package uno.model.cards.dto;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;

/**
 * A record to hold strongly-typed card side attributes.
 * Used to transfer data from DTOs to internal logic.
 *
 * @param color the color of the card side
 * @param value the value/action of the card side
 */
public record CardSide(CardColor color, CardValue value) {
}
