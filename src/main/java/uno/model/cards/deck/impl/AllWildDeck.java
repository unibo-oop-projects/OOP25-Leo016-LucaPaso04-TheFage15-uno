package uno.model.cards.deck.impl;

import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;

/**
 * Represents the deck for the "UNO All Wild" variant.
 * In this mode, every single card is a Wild card. Colors do not matter for matching,
 * but specific cards have powerful action effects (Skip Two, Targeted Draw, Swap).
 */
public class AllWildDeck extends DeckImpl<Card> {

    private static final int CARDS_OF_EACH_TYPE = 14;

    /**
     * Constructs an All Wild Deck with 112 Wild cards.
     */
    public AllWildDeck() {
        super();
        initializeDeck();
        shuffle();
    }

    /**
     * Populates the deck with 112 Wild cards.
     * Note: In "All Wild", cards typically do NOT require a color choice 
     * because they match everything. We set `requiresColorChoice` to false
     * for most cards, unless your specific house rules require it.
     */
    private void initializeDeck() {
        // Distribution based on typical All Wild rules (approx. 14 of each type)
        // Adjust the loop count if you want exact official distribution numbers.
        for (int i = 0; i < CARDS_OF_EACH_TYPE; i++) {

            // 1. Classic Wild (Basic play)
            // Value, Draw, ColorChoice, TargetChoice, Skip, Reverse
            createAndAddCard(new WildBehavior(
                CardValue.WILD_ALLWILD, 0, false, false, 0, false
            ));

            // 2. Wild Draw 4 (Penalty)
            // Draws 4, usually skips the next player (Skip=1)
            createAndAddCard(new WildBehavior(
                CardValue.WILD_DRAW_FOUR_ALLWILD, 4, true, false, 1, false
            ));

            // 3. Wild Draw 2 (Penalty)
            // Draws 2, usually skips the next player (Skip=1)
            createAndAddCard(new WildBehavior(
                CardValue.WILD_DRAW_TWO_ALLWILD, 2, false, false, 1, false
            ));

            // 4. Wild Reverse
            createAndAddCard(new WildBehavior(
                CardValue.WILD_REVERSE, 0, false, false, 0, true
            ));

            // 5. Wild Skip (Skips 1)
            createAndAddCard(new WildBehavior(
                CardValue.WILD_SKIP, 0, false, false, 1, false
            ));

            // 6. Wild Skip Two (Skips 2 players)
            createAndAddCard(new WildBehavior(
                CardValue.WILD_SKIP_TWO, 0, false, false, 2, false
            ));

            // --- SPECIAL TARGETING CARDS ---

            // 7. Forced Swap
            // No draw, No skip, YES Target Player (to swap with)
            createAndAddCard(new WildBehavior(
                CardValue.WILD_FORCED_SWAP, 0, false, true, 0, false
            ));

            // 8. Targeted Draw 2
            // No automatic next-player draw (0), YES Target Player.
            // The Game logic handles the drawing for the chosen target.
            createAndAddCard(new WildBehavior(
                CardValue.WILD_TARGETED_DRAW_TWO, 0, false, true, 0, false
            ));
        }
    }

    /**
     * Helper to create the DoubleSidedCard.
     * Front is the Wild Action, Back is Standard.
     * @param frontBehavior The behavior for the front side of the card.
     */
    private void createAndAddCard(final CardSideBehavior frontBehavior) {
        final Card card = new DoubleSidedCard(frontBehavior, BackSideBehavior.getInstance());
        // Uses the public method from DeckImpl
        this.addCard(card);
    }
}
