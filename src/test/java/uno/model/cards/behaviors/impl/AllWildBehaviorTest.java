package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.impl.AllWildDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.impl.AIAllWild;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

/**
 * Test class for verification of All Wild Card Behaviors using the Strategy Pattern.
 * Uses a MockGame to intercept and verify calls made by the behaviors.
 */
class AllWildBehaviorTest {

    private Game game;
    private AIAllWild p1;
    private AIAllWild p2;
    private AIAllWild p3;

    @BeforeEach
    void setUp() {
        final GameLogger logger = new uno.model.utils.impl.TestLogger();

        p1 = new AIAllWild("AI-1");
        p2 = new AIAllWild("AI-2");
        p3 = new AIAllWild("AI-3");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(p1);
        players.add(p2);
        players.add(p3);

        final AllWildDeck deck = new AllWildDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "ALLWILD", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    @Test
    void testSkipTwoBehavior() {

        final Card skipTwoCard = createComplexWild(CardValue.WILD_SKIP_TWO, 0, 2, false);

        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(skipTwoCard));
        hand.add(Optional.of(createDummyCard()));
        p1.setHand(hand);

        while (!game.getCurrentPlayer().equals(p1)) {
            game.aiAdvanceTurn();
        }

        game.getDiscardPile().addCard(createDummyCard());

        p1.takeTurn(game);

        assertEquals(p1, game.getCurrentPlayer(),
                "Il Skip Two in una partita a 3 dovrebbe saltare P2 e P3 e tornare a P1.");

        assertEquals(skipTwoCard, game.getTopDiscardCard().get());
    }

    @Test
    void testTargetedDrawTwoBehavior() {

        final Card targetedDrawCard = new DoubleSidedCard(
                new WildBehavior(CardValue.WILD_TARGETED_DRAW_TWO, 0, false, true, 0, false),
                BackSideBehavior.getInstance());

        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(targetedDrawCard));
        hand.add(Optional.of(createDummyCard()));
        p1.setHand(hand);
        p1.hasCalledUno();

        final int p2HandSize = p2.getHand().size();
        final int p3HandSize = p3.getHand().size();

        while (!game.getCurrentPlayer().equals(p1)) {
            game.aiAdvanceTurn();
        }

        p1.takeTurn(game);

        final boolean p2Drew = p2.getHand().size() == p2HandSize + 2;
        final boolean p3Drew = p3.getHand().size() == p3HandSize + 2;

        assertTrue(p2Drew || p3Drew, "Uno degli avversari avrebbe dovuto subire l'effetto 'Targeted Draw 2'.");

        assertEquals(p2, game.getCurrentPlayer(),
                "La 'Pesca Mirata' solitamente non salta il turno del giocatore successivo"
                        + " (a meno che non sia lui il target e la regola lo imponga).");
    }

    @Test
    void testForcedSwapBehavior() {

        final Card swapCard = createComplexWild(CardValue.WILD_FORCED_SWAP, 0, 0, true);

        final List<Optional<Card>> p1Hand = new LinkedList<>();
        p1Hand.add(Optional.of(swapCard));
        p1Hand.add(Optional.of(createDummyCard()));
        p1Hand.add(Optional.of(createDummyCard()));
        p1Hand.add(Optional.of(createDummyCard()));
        p1.setHand(p1Hand);

        final Card p2UniqueCard = createDummyCard();
        final List<Optional<Card>> p2Hand = new LinkedList<>();
        p2Hand.add(Optional.of(p2UniqueCard));
        p2.setHand(p2Hand);

        final List<Optional<Card>> p3Hand = new LinkedList<>();
        p3Hand.add(Optional.of(createDummyCard()));
        p3Hand.add(Optional.of(createDummyCard()));
        p3.setHand(p3Hand);

        while (!game.getCurrentPlayer().equals(p1)) {
            game.aiAdvanceTurn();
        }

        p1.takeTurn(game);

        assertEquals(swapCard, game.getTopDiscardCard().get());

        final boolean swappedWithP2 = p1.getHand().size() == 1 && p2.getHand().size() == 3;
        final boolean swappedWithP3 = p1.getHand().size() == 2 && p3.getHand().size() == 3;

        assertTrue(swappedWithP2 || swappedWithP3,
                "P1 avrebbe dovuto scambiare la mano con uno degli avversari. "
                        + "Stato mani: P1=" + p1.getHand().size() + ", P2=" + p2.getHand().size() + ", P3="
                        + p3.getHand().size());

        if (swappedWithP2) {
            assertEquals(p2UniqueCard, p1.getHand().get(0).get(),
                    "P1 avrebbe dovuto ricevere la carta specifica di P2.");
        }
    }

    @Test
    void testWildReverseBehavior() {

        final Card reverseCard = new DoubleSidedCard(
                new WildBehavior(CardValue.WILD_REVERSE, 0, false, false, 0, true),
                BackSideBehavior.getInstance());

        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(reverseCard));
        hand.add(Optional.of(createDummyCard()));
        p1.setHand(hand);

        while (!game.getCurrentPlayer().equals(p1)) {
            game.aiAdvanceTurn();
        }

        final boolean initialDirection = game.isClockwise();
        assertTrue(initialDirection, "Il gioco dovrebbe iniziare in senso orario.");

        p1.takeTurn(game);

        assertNotEquals(initialDirection, game.isClockwise(),
                "La Wild Reverse deve invertire la direzione di gioco.");

        assertEquals(p3, game.getCurrentPlayer(),
                "Dopo un Reverse in una partita a 3, il turno dovrebbe passare al giocatore precedente (P3).");

        assertEquals(reverseCard, game.getTopDiscardCard().get());
    }

    /**
     * Helper to create a complex wild card with specific behaviors for testing.
     * 
     * @param val value of the wild card
     * @param draw number of cards to draw (if applicable)
     * @param skip number of players to skip (if applicable)
     * @param target whether the card has a targeted effect (like Targeted Draw or Forced Swap)
     * @return the created complex wild card
     */
    private Card createComplexWild(final CardValue val, final int draw, final int skip, final boolean target) {
        return new DoubleSidedCard(
                new WildBehavior(val, draw, false, target, skip, false),
                BackSideBehavior.getInstance());
    }

    /**
     * Helper to create a dummy card for testing purposes.
     * 
     * @return a dummy card
     */
    private Card createDummyCard() {
        return new DoubleSidedCard(
                new WildBehavior(CardValue.WILD_FORCED_SWAP, 0, false),
                BackSideBehavior.getInstance());
    }
}
