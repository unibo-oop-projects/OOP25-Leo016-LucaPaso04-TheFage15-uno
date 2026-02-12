package uno.model.game.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameRules;
import uno.model.game.api.ScoreManager;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.impl.TestLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the ScoreManagerImpl class, which calculates the points for a 
 * round based on the cards left in the players' hands.
 */
class ScoreManagerTest {

    private static final int SCORE_1 = 14;
    private static final int SCORE_2 = 20;
    private static final int SCORE_3 = 50;
    private static final int SCORE_4 = 95;

    private GameImpl game;
    private ScoreManager scoreManager;
    private List<AbstractPlayer> players;
    private AbstractPlayer winner;
    private AbstractPlayer loser1;
    private AbstractPlayer loser2;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        winner = new AIClassic("Winner");
        loser1 = new AIClassic("Loser1");
        loser2 = new AIClassic("Loser2");
        players.add(winner);
        players.add(loser1);
        players.add(loser2);

        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final TestLogger logger = new TestLogger();
        final StandardDeck deck = new StandardDeck(logger);

        game = new GameImpl(deck, players, turnManager, discardPile, "TEST", logger, rules);
        scoreManager = new ScoreManagerImpl();
    }

    @Test
    void testCalculatePointsNumeric() {
        setPlayerHand(loser1,
                createCard(CardColor.RED, CardValue.FIVE),
                createCard(CardColor.BLUE, CardValue.NINE));

        setPlayerHand(loser2);

        final int points = scoreManager.calculateRoundPoints(winner, players, game);
        assertEquals(SCORE_1, points);
    }

    @Test
    void testCalculatePointsAction() {
        setPlayerHand(loser1, createCard(CardColor.GREEN, CardValue.SKIP));

        final int points = scoreManager.calculateRoundPoints(winner, players, game);
        assertEquals(SCORE_2, points);
    }

    @Test
    void testCalculatePointsWild() {
        setPlayerHand(loser1, createWildCard());

        final int points = scoreManager.calculateRoundPoints(winner, players, game);
        assertEquals(SCORE_3, points);
    }

    @Test
    void testCalculatePointsMixed() {
        setPlayerHand(loser1, createWildCard(), createCard(CardColor.RED, CardValue.FIVE));

        setPlayerHand(loser2,
                createCard(CardColor.BLUE, CardValue.SKIP),
                createCard(CardColor.YELLOW, CardValue.REVERSE));

        final int points = scoreManager.calculateRoundPoints(winner, players, game);
        assertEquals(SCORE_4, points);
    }

    /**
     * Helper method to set a player's hand with the given cards.
     * 
     * @param player the player whose hand is to be set
     * @param cards the cards to be added to the player's hand
     */
    private void setPlayerHand(final AbstractPlayer player, final Card... cards) {
        final List<Optional<Card>> hand = new LinkedList<>();
        for (final Card c : cards) {
            hand.add(Optional.of(c));
        }
        player.setHand(hand);
    }

    /**
     * Creates a numeric card for testing purposes.
     * 
     * @param c color of the card
     * @param v value of the card
     * @return a DoubleSidedCard with NumericBehavior on the front and BackSideBehavior on the back
     */
    private Card createCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }

    /**
     * Creates a Wild card for testing purposes.
     * 
     * @return a DoubleSidedCard with WildBehavior on the front and BackSideBehavior on the back
     */
    private Card createWildCard() {
        return new DoubleSidedCard(new WildBehavior(CardValue.WILD, 0), BackSideBehavior.getInstance());
    }
}
