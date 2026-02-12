package uno.model.game.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.DeckHandler;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameRules;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.impl.TestLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for the {@link DeckHandlerImpl} class.
 */
class DeckHandlerTest {

    private GameImpl game;
    private DeckHandler deckHandler;
    private StandardDeck deck;
    private DiscardPile discardPile;
    private AbstractPlayer player;

    @BeforeEach
    void setUp() {
        final List<AbstractPlayer> players = new ArrayList<>();
        player = new AIClassic("Tester");
        players.add(player);
        players.add(new AIClassic("Opponent"));

        final GameRules rules = GameRulesImpl.defaultRules();
        discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final TestLogger logger = new TestLogger();
        deck = new StandardDeck(logger);

        game = new GameImpl(deck, players, turnManager, discardPile, "TEST", logger, rules);

        deckHandler = new DeckHandlerImpl(deck, discardPile, rules, logger, "SYSTEM");
    }

    @Test
    void testDrawCardSuccess() {

        final int initialHandSize = player.getHandSize();
        final int initialDeckSize = deck.size();

        final boolean result = deckHandler.drawCardForPlayer(player, game);

        assertTrue(result, "Draw should succeed");
        assertEquals(initialHandSize + 1, player.getHandSize());
        assertEquals(initialDeckSize - 1, deck.size());
    }

    @Test
    void testRefillFromDiscard() {
        while (!deck.isEmpty()) {
            deck.draw();
        }

        discardPile.addCard(createCard(CardColor.RED, CardValue.ONE));
        discardPile.addCard(createCard(CardColor.BLUE, CardValue.TWO));
        discardPile.addCard(createCard(CardColor.GREEN, CardValue.THREE));
        assertEquals(3, discardPile.size());
        assertEquals(0, deck.size());

        final boolean result = deckHandler.drawCardForPlayer(player, game);

        assertTrue(result, "Draw should succeed after refill");
        assertEquals(1, deck.size());
        assertEquals(1, discardPile.size());
    }

    @Test
    void testEmptyDeckAndDiscard() {

        while (!deck.isEmpty()) {
            deck.draw();
        }

        final boolean result = deckHandler.drawCardForPlayer(player, game);

        assertTrue(result, "Should return true (standard rule)");

    }

    @Test
    void testMandatoryPassNoRefill() {

        final GameRules rules = new GameRulesImpl(false, false, true, false);
        final DeckHandler localHandler = new DeckHandlerImpl(deck, discardPile, rules, new TestLogger(), "SYSTEM");

        while (!deck.isEmpty()) {
            deck.draw();
        }

        final boolean result = localHandler.drawCardForPlayer(player, game);

        assertFalse(result, "Should return false when deck is empty and Mandatory Pass is ON");
    }

    /**
     * Helper method to create a simple card for testing purposes.
     * 
     * @param c color
     * @param v value
     * @return a new DoubleSidedCard with the specified color and value on the front side
     */
    private Card createCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }
}
