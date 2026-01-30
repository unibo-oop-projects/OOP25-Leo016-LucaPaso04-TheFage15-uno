package uno.model.cards.behaviors;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

public class DrawBehavior implements CardSideBehavior {
    private final CardColor color;
    private final CardValue value;
    private final int amountToDraw;

    public DrawBehavior(CardColor color, CardValue value, int amountToDraw) {
        this.color = color;
        this.value = value;
        this.amountToDraw = amountToDraw;
    }

    @Override
    public void executeEffect(Game game) {
        game.makeNextPlayerDraw(amountToDraw);
        game.skipPlayers(1);
    }

    @Override public CardColor getColor() { return color; }
    @Override public CardValue getValue() { return value; }
    @Override public String toString() { return color + " " + value; }
}