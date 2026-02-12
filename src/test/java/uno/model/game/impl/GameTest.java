package uno.model.game.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.utils.impl.TestLogger;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.cards.types.api.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.types.impl.DoubleSidedCard;

import uno.model.game.api.DiscardPile;
import uno.model.game.api.TurnManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.LinkedList;

/**
 * Unit tests for the GameImpl class, covering game initialization, card playing logic, 
 * drawing and passing turns, and flipping the world.
 */
class GameTest {

    private static final int PLAYER_HAND_SIZE = 7;
    private static final String GAME_MODE = "TEST";
    private List<AbstractPlayer> players;
    private TestLogger logger;
    private StandardDeck deck;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new AIClassic("P1"));
        players.add(new AIClassic("P2"));
        players.add(new AIClassic("P3"));
        logger = new TestLogger();
        deck = new StandardDeck(logger);
    }

    @Test
    void testGameInitialization() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        assertEquals(GameState.RUNNING, game.getGameState());
        assertNotNull(game.getDrawDeck());
        assertNotNull(game.getDiscardPile());
        assertEquals(3, game.getPlayers().size());
    }

    @Test
    void testStartNewRound() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        game.startNewRound();

        for (final AbstractPlayer p : players) {
            assertEquals(PLAYER_HAND_SIZE, p.getHand().size());
        }

        assertEquals(1, game.getDiscardPile().size());
        assertNotNull(game.getCurrentPlayedCard());

        assertEquals(GameState.RUNNING, game.getGameState());
    }

    @Test
    void testPlayValidCard() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        final Card topCard = createCard(CardColor.RED, CardValue.FIVE);
        game.setCurrentPlayedCard(topCard);
        game.setCurrentColor(CardColor.RED);
        game.getDiscardPile().addCard(topCard);

        final Card validCard = createCard(CardColor.RED, CardValue.EIGHT);
        setPlayerHandWithCard(currentPlayer, validCard);

        assertTrue(game.isValidMove(validCard));

        game.playCard(Optional.of(validCard));

        assertEquals(validCard, game.getCurrentPlayedCard());
        assertEquals(validCard, game.getDiscardPile().getTopCard().get());
        assertEquals(0, currentPlayer.getHandSize());
    }

    @Test
    void testPlayInvalidCard() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        final Card topCard = createCard(CardColor.BLUE, CardValue.FIVE);
        game.setCurrentPlayedCard(topCard);
        game.setCurrentColor(CardColor.BLUE);
        game.getDiscardPile().addCard(topCard);

        final Card invalidCard = createCard(CardColor.RED, CardValue.NINE);
        setPlayerHandWithCard(currentPlayer, invalidCard);

        assertFalse(game.isValidMove(invalidCard));

        assertThrows(IllegalStateException.class, () -> {
            game.playCard(Optional.of(invalidCard));
        });
    }

    @Test
    void testDrawCardAndPass() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        deck.addCard(createCard(CardColor.GREEN, CardValue.ONE));

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();
        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        game.playerInitiatesDraw();

        assertTrue(game.hasCurrentPlayerDrawn(currentPlayer));
        assertEquals(1, currentPlayer.getHandSize());

        game.playerPassTurn();

        assertEquals(nextPlayer, game.getCurrentPlayer());
        assertFalse(game.hasCurrentPlayerDrawn(nextPlayer));
    }

    @Test
    void testCannotPassWithoutDrawing() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, new DiscardPileImpl(), GAME_MODE, logger, rules);

        assertThrows(IllegalStateException.class, () -> {
            game.playerPassTurn();
        });
    }

    @Test
    void testFlipTheWorld() {
        final GameRules rules = GameRulesImpl.defaultRules();
        final GameImpl game = new GameImpl(deck, players, new TurnManagerImpl(players, rules),
                new DiscardPileImpl(), GAME_MODE, logger, rules);

        final Card startCard = new DoubleSidedCard(
                new NumericBehavior(CardColor.RED, CardValue.FIVE),
                new NumericBehavior(CardColor.PURPLE, CardValue.FIVE)
        );

        game.setCurrentPlayedCard(startCard);
        game.setCurrentColor(CardColor.RED);

        assertFalse(game.isDarkSide());

        game.flipTheWorld();

        assertTrue(game.isDarkSide());
        game.flipTheWorld();
        assertFalse(game.isDarkSide());
    }

    /**
     * Helper to set a player's hand to contain only the specified card. This allows us to control the test scenario precisely.
     * 
     * @param player the player whose hand we want to set
     * @param card the card to place in the player's hand
     */
    private void setPlayerHandWithCard(final AbstractPlayer player, final Card card) {
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(card));
        player.setHand(hand);
    }

    /**
     * Helper to create a simple numeric card with given color and value. Uses DoubleSidedCard with a dummy back side.
     * 
     * @param c card color
     * @param v card value
     * @return a new Card instance with the specified color and value
     */
    private Card createCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }
}
