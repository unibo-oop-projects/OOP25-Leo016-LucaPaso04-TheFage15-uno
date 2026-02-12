package uno.model.game.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import uno.model.game.api.MoveValidator;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.impl.TestLogger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the MoveValidatorImpl class, ensuring that move validation logic works correctly.
 */
class MoveValidatorTest {

    private GameImpl game;
    private MoveValidator moveValidator;
    private AbstractPlayer player;

    @BeforeEach
    void setUp() {
        final List<AbstractPlayer> players = new ArrayList<>();
        player = new AIClassic("Tester");
        players.add(player);
        players.add(new AIClassic("Opponent"));

        final GameRules rules = GameRulesImpl.defaultRules();
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final TestLogger logger = new TestLogger();
        final StandardDeck deck = new StandardDeck(logger);

        game = new GameImpl(deck, players, turnManager, discardPile, "TEST", logger, rules);
        moveValidator = new MoveValidatorImpl(game);
    }

    @Test
    void testIsValidMoveSameColor() {
        final Card topCard = createNumericCard(CardColor.RED, CardValue.FIVE);
        setupDismissPile(topCard);

        final Card cardToPlay = createNumericCard(CardColor.RED, CardValue.EIGHT);
        assertTrue(moveValidator.isValidMove(cardToPlay), "Red on Red should be valid");
    }

    @Test
    void testIsValidMoveSameValue() {
        final Card topCard = createNumericCard(CardColor.BLUE, CardValue.FIVE);
        setupDismissPile(topCard);

        final Card cardToPlay = createNumericCard(CardColor.GREEN, CardValue.FIVE);
        assertTrue(moveValidator.isValidMove(cardToPlay), "Five on Five should be valid");
    }

    @Test
    void testIsValidMoveWild() {
        final Card topCard = createNumericCard(CardColor.BLUE, CardValue.NINE);
        setupDismissPile(topCard);

        final Card wildCard = createWildCard();
        assertTrue(moveValidator.isValidMove(wildCard), "Wild card should be playable on anything");
    }

    @Test
    void testIsValidMoveOnWildWithColorSet() {
        final Card topCard = createWildCard();
        setupDismissPile(topCard);
        game.setCurrentColor(CardColor.RED);

        final Card redCard = createNumericCard(CardColor.RED, CardValue.TWO);
        assertTrue(moveValidator.isValidMove(redCard), "Red should be playable on Wild (Red)");

        final Card blueCard = createNumericCard(CardColor.BLUE, CardValue.TWO);
        assertFalse(moveValidator.isValidMove(blueCard), "Blue should NOT be playable on Wild (Red)");
    }

    @Test
    void testInvalidMove() {
        final Card topCard = createNumericCard(CardColor.YELLOW, CardValue.ONE);
        setupDismissPile(topCard);

        final Card badCard = createNumericCard(CardColor.PURPLE, CardValue.NINE);
        assertFalse(moveValidator.isValidMove(badCard), "Purple on Yellow (diff value) should be invalid");
    }

    @Test
    void testPlayerHasPlayableCard() {
        final Card topCard = createNumericCard(CardColor.RED, CardValue.FIVE);
        setupDismissPile(topCard);

        final Card matching = createNumericCard(CardColor.RED, CardValue.SEVEN);
        setPlayerHand(matching);
        assertTrue(moveValidator.playerHasPlayableCard(player), "Player has valid card");

        final Card nonMatching = createNumericCard(CardColor.BLUE, CardValue.TWO);
        setPlayerHand(nonMatching);
        assertFalse(moveValidator.playerHasPlayableCard(player), "Player has no valid cards");
    }

    /**
     * Helper to set up the discard pile with a specific top card and set the current color accordingly.
     * 
     * @param top the card to place on top of the discard pile
     */
    private void setupDismissPile(final Card top) {
        game.getDiscardPile().addCard(top);
        game.setCurrentColorOptional(Optional.of(top.getColor(game)));
    }

    /**
     * Helper to set the player's hand with the provided cards.
     * 
     * @param cards cards to set in the player's hand
     */
    private void setPlayerHand(final Card... cards) {
        final List<Optional<Card>> hand = new LinkedList<>();
        for (final Card c : cards) {
            hand.add(Optional.of(c));
        }
        player.setHand(hand);
    }

    /**
     * Helper to create a numeric card for testing.
     * 
     * @param c color of the card
     * @param v value of the card
     * @return a DoubleSidedCard with NumericBehavior on the front and BackSideBehavior on the back
     */
    private Card createNumericCard(final CardColor c, final CardValue v) {
        return new DoubleSidedCard(new NumericBehavior(c, v), BackSideBehavior.getInstance());
    }

    /**
     * Helper to create a Wild card for testing.
     * 
     * @return a DoubleSidedCard with WildBehavior on the front and BackSideBehavior on the back
     */
    private Card createWildCard() {
        return new DoubleSidedCard(new WildBehavior(CardValue.WILD, 0), BackSideBehavior.getInstance());
    }
}
