package uno.model.cards.behaviors;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

public class NumericBehavior implements CardSideBehavior {
    private final CardColor color;
    private final CardValue value;

    public NumericBehavior(CardColor color, CardValue value) {
        this.color = color;
        this.value = value;
    }

    @Override
    public void executeEffect(Game game) {
        // Le carte numeriche non fanno nulla
    }

    @Override public CardColor getColor() { return color; }
    @Override public CardValue getValue() { return value; }
    @Override public String toString() { return color + " " + value; }
}