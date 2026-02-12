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
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.impl.TestLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.TurnManager;

/**
 * Unit tests for GameRules and related game mechanics.
 */
class GameRulesTest {

    private static final int SCORE_PLAYER_HAND = 490;
    private static final int FINAL_SCORE = 500;
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

    @Test
    void testUnoPenaltyDisabledNoPenaltyApplied() {
        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        setPlayerHandSize(nextPlayer, 1);
        nextPlayer.setHasCalledUno(false);

        game.getTurnManager().advanceTurn(game);

        assertEquals(nextPlayer, game.getCurrentPlayer());

        assertEquals(1, nextPlayer.getHandSize(), "Should NOT receive cards if penalty is disabled");
    }

    @Test
    void testUnoPenaltyEnabledPenaltyApplied() {
        final GameRules rules = new GameRulesImpl(true, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer nextPlayer = game.getTurnManager().peekNextPlayer();

        setPlayerHandSize(nextPlayer, 1);
        nextPlayer.setHasCalledUno(false);

        try {
            game.getTurnManager().advanceTurn(game);
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("UNO! Penalty"), "Should throw penalty exception");
        }

        assertEquals(3, nextPlayer.getHandSize(), "Should receive 2 penalty cards (1 -> 3)");
    }

    @Test
    void testDeckReshuffleDefaultBehavior() {
        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);
        deck.addCard(createCard(CardColor.RED, CardValue.ONE)); // Ensure deck has cards initially

        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertTrue(deck.isEmpty());

        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.FIVE));
        game.getDiscardPile().addCard(createCard(CardColor.GREEN, CardValue.TWO));
        game.getDiscardPile().addCard(createCard(CardColor.YELLOW, CardValue.NINE));

        game.playerInitiatesDraw();

        assertFalse(deck.isEmpty(), "Deck should be refilled from discard pile");
        assertEquals(1, deck.size(), "Deck should have cards remaining");
    }

    @Test
    void testDeckNoReshuffleMandatoryPass() {
        final GameRules rules = new GameRulesImpl(false, false, true, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);
        deck.addCard(createCard(CardColor.RED, CardValue.ONE));

        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertTrue(deck.isEmpty());

        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.FIVE));

        game.drawCardForPlayer(game.getCurrentPlayer());

        assertTrue(deck.isEmpty(), "Deck should NOT be refilled");

        assertEquals(GameState.GAME_OVER, game.getGameState(),
                "Game should end if deck is empty and rules forbid reshuffle");
    }

    @Test
    void testSkipAfterDrawEnabled() {
        final GameRules rules = new GameRulesImpl(false, true, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        game.playerInitiatesDraw();

        assertTrue(game.hasCurrentPlayerDrawn(currentPlayer), "Player should have drawn status");

        final Card playableCard = createCard(CardColor.RED, CardValue.FIVE);
        final Card topCard = createCard(CardColor.RED, CardValue.TWO);

        game.setCurrentColor(CardColor.RED);
        game.setCurrentPlayedCard(topCard);
        game.getDiscardPile().addCard(topCard);

        setPlayerHandWithCard(currentPlayer, playableCard);

        try {
            game.playCard(Optional.of(playableCard));
            throw new AssertionError("Should have thrown IllegalStateException due to SkipAfterDraw rule");
        } catch (final IllegalStateException e) {
            assertTrue(e.getMessage().contains("Skip After Draw"), "Exception should mention Skip After Draw");
        }
    }

    @Test
    void testSkipAfterDrawDisabled() {
        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        game.playerInitiatesDraw();

        final Card playableCard = createCard(CardColor.RED, CardValue.FIVE);
        final Card topCard = createCard(CardColor.RED, CardValue.TWO);

        game.setCurrentColor(CardColor.RED);
        game.setCurrentPlayedCard(topCard);
        game.getDiscardPile().addCard(topCard);

        setPlayerHandWithCard(currentPlayer, playableCard);

        game.playCard(Optional.of(playableCard));

        assertEquals(playableCard, game.getDiscardPile().getTopCard().get(), "Card should be on discard pile");
    }

    @Test
    void testScoringDisabledDryRound() {

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        final Card lastCard = createCard(CardColor.BLUE, CardValue.NINE);
        final Card topCard = createCard(CardColor.BLUE, CardValue.ONE);

        game.setCurrentColor(CardColor.BLUE);
        game.setCurrentPlayedCard(topCard);
        game.getDiscardPile().addCard(topCard);

        setPlayerHandWithCard(currentPlayer, lastCard);

        game.playCard(Optional.of(lastCard));

        assertEquals(GameState.GAME_OVER, game.getGameState(),
                "Game should be over immediately if scoring is disabled");
        assertEquals(currentPlayer, game.getWinner(), "Current player should be the winner");
    }

    @Test
    void testScoringEnabledRoundOver() {
        final GameRules rules = new GameRulesImpl(false, false, false, true);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        final Card lastCard = createCard(CardColor.GREEN, CardValue.SKIP);
        final Card topCard = createCard(CardColor.GREEN, CardValue.ONE);

        game.setCurrentColor(CardColor.GREEN);
        game.setCurrentPlayedCard(topCard);
        game.getDiscardPile().addCard(topCard);

        setPlayerHandWithCard(currentPlayer, lastCard);

        game.playCard(Optional.of(lastCard));

        assertEquals(GameState.ROUND_OVER, game.getGameState(), "Should be ROUND_OVER if score target not reached");
        assertEquals(currentPlayer, game.getWinner(), "Current player should be round winner");
        assertTrue(currentPlayer.getScore() < FINAL_SCORE, "Score should be less than 500");
    }

    @Test
    void testScoringEnabledMatchWin() {
        final GameRules rules = new GameRulesImpl(false, false, false, true);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final GameImpl game = new GameImpl(deck, players, turnManager, discardPile, GAME_MODE, logger, rules);

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();

        currentPlayer.addScore(SCORE_PLAYER_HAND);
        final AbstractPlayer otherPlayer = game.getTurnManager().peekNextPlayer();
        setPlayerHandWithCard(otherPlayer, createCard(CardColor.RED, CardValue.DRAW_TWO));

        final Card lastCard = createCard(CardColor.YELLOW, CardValue.ZERO);
        final Card topCard = createCard(CardColor.YELLOW, CardValue.ONE);

        game.setCurrentColor(CardColor.YELLOW);
        game.setCurrentPlayedCard(topCard);
        game.getDiscardPile().addCard(topCard);

        setPlayerHandWithCard(currentPlayer, lastCard);

        game.playCard(Optional.of(lastCard));

        assertEquals(GameState.GAME_OVER, game.getGameState(), "Game should end when score >= 500");
        assertTrue(currentPlayer.getScore() >= FINAL_SCORE, "Score should be >= 500");
    }

    /**
     * Helper to set player's hand with a single card for testing purposes.
     * 
     * @param player the player whose hand to set
     * @param card the card to put in the player's hand (as the only card)
     */
    private void setPlayerHandWithCard(final AbstractPlayer player, final Card card) {
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(card));
        player.setHand(hand);
    }

    /**
     * Helper to set player's hand size with dummy cards for testing purposes.
     * 
     * @param player the player whose hand size to set
     * @param size the desired hand size (number of cards) to set for the player
     */
    private void setPlayerHandSize(final AbstractPlayer player, final int size) {
        final List<Optional<Card>> hand = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            hand.add(Optional.of(createCard(CardColor.RED, CardValue.values()[i])));
        }
        player.setHand(hand);
    }

    /**
     * Helper to create a simple card with given color and value for testing purposes.
     * 
     * @param c the color of the card
     * @param v the value of the card
     * @return a new Card instance with the given color and value
     */
    private Card createCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }
}
