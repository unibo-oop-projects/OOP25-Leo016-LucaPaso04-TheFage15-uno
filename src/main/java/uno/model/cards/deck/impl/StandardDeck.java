package uno.model.cards.deck.impl;

import java.util.Arrays;
import java.util.List;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.behaviors.impl.ActionBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.DrawBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;

/**
 * Concrete implementation of a Standard UNO Deck (108 cards).
 * It extends {@link DeckImpl} and initializes the cards in the constructor
 * by creating {@link DoubleSidedCard} instances where the back side is always
 * the standard {@link BackSideBehavior}.
 */
public class StandardDeck extends DeckImpl<Card> {

    /**
     * Constructs a new StandardDeck by populating it with the standard UNO cards.
     */
    public StandardDeck() {
        super(); // 1. Inizializza la lista vuota nel genitore
        initializeDeck(); // 2. Popola il mazzo
        shuffle(); // 3. Mischia alla fine
    }

    /**
     * Fills the deck according to standard UNO rules.
     * - 19 cards per color (0-9, with two of 1-9)
     * - 2 Skip, 2 Reverse, 2 Draw Two per color
     * - 4 Wild, 4 Wild Draw Four
     */
    private void initializeDeck() {
        final List<CardColor> colors = Arrays.asList(
            CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW
        );

        final List<CardValue> numberValues = Arrays.asList(
            CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR,
            CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE
        );

        for (final CardColor color : colors) {
            // --- 1. ZERO (Only 1 per color) ---
            createAndAddCard(new NumericBehavior(color, CardValue.ZERO));

            // --- 2. NUMBERS 1-9 (2 per color) ---
            for (final CardValue value : numberValues) {
                createAndAddCard(new NumericBehavior(color, value));
                createAndAddCard(new NumericBehavior(color, value));
            }

            // --- 3. ACTIONS (2 per color) ---
            // Skip
            createAndAddCard(new ActionBehavior(color, CardValue.SKIP, g -> g.skipPlayers(1)));
            createAndAddCard(new ActionBehavior(color, CardValue.SKIP, g -> g.skipPlayers(1)));

            // Reverse
            createAndAddCard(new ActionBehavior(color, CardValue.REVERSE, g -> g.reversePlayOrder()));
            createAndAddCard(new ActionBehavior(color, CardValue.REVERSE, g -> g.reversePlayOrder()));

            // Draw Two
            createAndAddCard(new DrawBehavior(color, CardValue.DRAW_TWO, 2));
            createAndAddCard(new DrawBehavior(color, CardValue.DRAW_TWO, 2));
        }

        // --- 4. WILD CARDS (4 each) ---
        for (int i = 0; i < 4; i++) {
            // Standard Wild: No draw, Needs Color Choice
            createAndAddCard(new WildBehavior(CardValue.WILD, 0));

            // Wild Draw Four: Draw 4, Needs Color Choice (Skip logic is handled inside WildBehavior)
            createAndAddCard(new WildBehavior(CardValue.WILD_DRAW_FOUR, 4));
        }
    }

    /**
     * Helper method to wrap a behavior into a standard DoubleSidedCard.
     * Uses the parent's addCard() method to insert it into the protected list.
     * @param frontBehavior The behavior for the front side of the card.
     */
    private void createAndAddCard(final CardSideBehavior frontBehavior) {
        // Create the card: Front is the behavior passed, Back is the standard Card Back
        final Card card = new DoubleSidedCard(frontBehavior, BackSideBehavior.getInstance());

        // Use the public method from DeckImpl, NOT direct list access
        this.addCard(card);
    }
}
