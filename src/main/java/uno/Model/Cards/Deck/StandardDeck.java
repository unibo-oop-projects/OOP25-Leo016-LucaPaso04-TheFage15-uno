package uno.model.cards.deck;

import java.util.Arrays;
import java.util.List;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.*;
import uno.model.cards.types.DoubleSidedCard;

public class StandardDeck extends Deck<Card> {

    // Istanza unica del dorso per risparmiare memoria
    private final CardSideBehavior STANDARD_BACK = new BackSideBehavior();

    public StandardDeck() {
        super();
    }

    @Override
    protected void createDeck() {
        List<CardColor> colors = Arrays.asList(
                CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW
        );

        List<CardValue> numberValues = Arrays.asList(
                CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR,
                CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE
        );

        for (CardColor color : colors) {
            // 1. ZERO (1 per colore)
            addCard(new NumericBehavior(color, CardValue.ZERO));

            // 2. Numeri 1-9 (2 per colore)
            for (CardValue value : numberValues) {
                addCard(new NumericBehavior(color, value));
                addCard(new NumericBehavior(color, value));
            }

            // 3. Azioni (2 per colore)
            // Skip
            addCard(new ActionBehavior(color, CardValue.SKIP, (g) -> g.skipPlayers(1)));
            addCard(new ActionBehavior(color, CardValue.SKIP, (g) -> g.skipPlayers(1)));

            // Reverse
            addCard(new ActionBehavior(color, CardValue.REVERSE, (g) -> g.reversePlayOrder()));
            addCard(new ActionBehavior(color, CardValue.REVERSE, (g) -> g.reversePlayOrder()));

            // Draw Two
            addCard(new DrawBehavior(color, CardValue.DRAW_TWO, 2));
            addCard(new DrawBehavior(color, CardValue.DRAW_TWO, 2));
        }

        // 4. Jolly (4 Wild + 4 Wild Draw Four)
        for (int i = 0; i < 4; i++) {
            addCard(new WildBehavior(CardValue.WILD, 0));
            addCard(new WildBehavior(CardValue.WILD_DRAW_FOUR, 4));
        }
    }

    /**
     * Helper per creare la DoubleSidedCard con il dorso standard
     */
    private void addCard(CardSideBehavior frontBehavior) {
        // Creiamo una carta che ha il comportamento voluto DAVANTI
        // e il dorso standard DIETRO.
        Card card = new DoubleSidedCard(frontBehavior, STANDARD_BACK);
        this.cards.add(card);
    }
}