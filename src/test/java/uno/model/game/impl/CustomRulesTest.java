package uno.model.game.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.AIClassic;
import uno.model.utils.impl.TestLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.TurnManager;

class CustomRulesTest {

    private static final String GAME_MODE = "TEST";
    private List<AbstractPlayer> players;
    private TestLogger logger;
    private StandardDeck deck;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new AIClassic("P1"));
        players.add(new AIClassic("P2"));
        logger = new TestLogger();
        deck = new StandardDeck(logger);
    }

    /**
     * Test verifying that when UNO Penalty is DISABLED, no penalty is applied
     * even if a player has 1 card and hasn't called UNO.
     */
    @Test
    void testUnoPenaltyDisabledNoPenaltyApplied() {
        // 1. Disable Uno Penalty
        final GameRules rules = new GameRulesImpl(false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        // 2. Identify NEXT player (who will start their turn next)
        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        // 3. Setup Next Player with 1 card who hasn't called UNO
        setPlayerHandSize(nextPlayer, 1);
        nextPlayer.setHasCalledUno(false);

        // 4. Advance turn (Current -> Next)
        game.getTurnManager().advanceTurn(game);

        // 5. Verify Next is now Current
        assertEquals(nextPlayer, game.getCurrentPlayer());

        // 6. Verify NO Penalty (Hand size remains 1)
        assertEquals(1, nextPlayer.getHandSize(), "Should NOT receive cards if penalty is disabled");
    }

    /**
     * Test verifying that when UNO Penalty is ENABLED (Default),
     * a penalty IS applied if a player has 1 card and hasn't called UNO.
     */
    @Test
    void testUnoPenaltyEnabledPenaltyApplied() {
        // 1. Enable Uno Penalty
        final GameRules rules = new GameRulesImpl(true, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        // 2. Identify NEXT player
        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        // 3. Setup Next Player with 1 card and NO Uno Call
        setPlayerHandSize(nextPlayer, 1);
        nextPlayer.setHasCalledUno(false);

        // 4. Advance turn (Current -> Next) which triggers the check
        try {
            game.getTurnManager().advanceTurn(game);
        } catch (final IllegalStateException e) {
            // "UNO! Penalty applied..." exception is thrown by TurnManagerImpl to notify UI
            assertTrue(e.getMessage().contains("UNO! Penalty"), "Should throw penalty exception");
        }

        // 5. Verify Hand Size increased (1 + 2 = 3)
        // Note: The IllegalStateException interrupts the flow in TurnManager BEFORE
        // allowing further play,
        // but the penalty logic (drawing cards) happens BEFORE the exception in
        // TurnManagerImpl:
        // player.unoPenalty(game); -> Draws 2 cards
        // throw new IllegalStateException...

        assertEquals(3, nextPlayer.getHandSize(), "Should receive 2 penalty cards (1 -> 3)");
    }

    /**
     * Test standard behavior: Deck reshuffles when empty.
     */
    @Test
    void testDeckReshuffleDefaultBehavior() {
        // 1. MandatoryPass = FALSE (Default)
        final GameRules rules = new GameRulesImpl(false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);
        deck.addCard(createCard(CardColor.RED, CardValue.ONE)); // Ensure deck has cards initially

        // 2. Empty the deck manually
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertTrue(deck.isEmpty());

        // 3. Add cards to discard pile (so there is something to reshuffle)
        // Add at least 3 cards so takeAllExceptTop leaves 1 and provides 2 for refill.
        // Drawing 1 then leaves 1 in deck.
        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.FIVE));
        game.getDiscardPile().addCard(createCard(CardColor.GREEN, CardValue.TWO));
        game.getDiscardPile().addCard(createCard(CardColor.YELLOW, CardValue.NINE));

        // 4. Trigger Draw (which should trigger refill)
        game.playerInitiatesDraw();

        // 5. Verify Deck is NO LONGER empty (Refilled)
        // We put 3 in discard. discard.takeAllExceptTop() takes 2.
        // Deck gets 2.
        // playerInitiatesDraw calls draw() -> takes 1.
        // Remaining in deck = 1.
        assertFalse(deck.isEmpty(), "Deck should be refilled from discard pile");
        assertEquals(1, deck.size(), "Deck should have cards remaining");
    }

    /**
     * Test custom rule: No reshuffle (Mandatory Pass).
     */
    @Test
    void testDeckNoReshuffleMandatoryPass() {
        // 1. MandatoryPass = TRUE
        final GameRules rules = new GameRulesImpl(false, false, true);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);
        deck.addCard(createCard(CardColor.RED, CardValue.ONE));

        // 2. Empty the deck
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertTrue(deck.isEmpty());

        // 3. Add cards to discard pile (to prove we DON'T use them)
        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.FIVE));

        // 4. Trigger Draw
        game.drawCardForPlayer(game.getCurrentPlayer()); // Direct draw call

        // 5. Verify Deck is STILL empty
        assertTrue(deck.isEmpty(), "Deck should NOT be refilled");

        // 6. Verify Game Over (based on typical implementation of this rule in this
        // codebase)
        assertEquals(GameState.GAME_OVER, game.getGameState(),
                "Game should end if deck is empty and rules forbid reshuffle");
    }

    // Helper to force hand size
    private void setPlayerHandSize(final AbstractPlayer player, final int size) {
        // Clean hand
        final List<Optional<Card>> hand = new LinkedList<>();
        // Add dummy cards
        for (int i = 0; i < size; i++) {
            hand.add(Optional.of(createCard(CardColor.RED, CardValue.values()[i])));
        }
        player.setHand(hand);
    }

    private Card createCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }
}
