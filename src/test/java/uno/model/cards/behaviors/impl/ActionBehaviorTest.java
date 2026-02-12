package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

/**
 * Test class for verification of Card Behaviors using the Strategy Pattern.
 * Uses a MockGame to intercept and verify calls made by the behaviors.
 */
class ActionBehaviorTest {

    private Game game;
    private AIClassic aiClassic1;

    @BeforeEach
    void setUp() {
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        aiClassic1 = new AIClassic("AI-Bot-1");
        final AIClassic aiClassic2 = new AIClassic("AI-Bot-2");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic1);
        players.add(aiClassic2);
        final Deck<Card> deck = new StandardDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "CLASSIC", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    @Test
    void testSkipBehavior() {

        final Card blueSkip = createCard(CardColor.BLUE, CardValue.SKIP);
        final Card blueFive = createCard(CardColor.BLUE, CardValue.FIVE);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(blueSkip));
        hand.add(Optional.of(blueFive));
        aiClassic1.setHand(hand);

        final Card discardCard = createCard(CardColor.BLUE, CardValue.ONE);
        game.getDiscardPile().addCard(discardCard);
        game.setCurrentColor(CardColor.BLUE);

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }
        assertEquals(aiClassic1, game.getCurrentPlayer());

        aiClassic1.takeTurn(game);

        assertTrue(game.getTopDiscardCard().isPresent());
        assertEquals(blueSkip, game.getTopDiscardCard().get());

        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "In una partita a 2, lo Skip deve far tornare il turno al giocatore corrente.");
    }

    @Test
    void testReverseBehavior() {

        final Card yellowReverse = createCard(CardColor.YELLOW, CardValue.REVERSE);
        final Card yellowFive = createCard(CardColor.YELLOW, CardValue.FIVE);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(yellowReverse));
        hand.add(Optional.of(yellowFive));
        aiClassic1.setHand(hand);

        final Card discardCard = createCard(CardColor.YELLOW, CardValue.ONE);
        game.getDiscardPile().addCard(discardCard);
        game.setCurrentColor(CardColor.YELLOW);

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }
        final boolean initialClockwise = game.isClockwise();

        aiClassic1.takeTurn(game);

        assertNotEquals(initialClockwise, game.isClockwise(),
                "La carta Reverse deve invertire la direzione del gioco.");

        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "In partita a 2, Reverse agisce come Skip e fa rigiocare il turno.");

        assertEquals(yellowReverse, game.getTopDiscardCard().get());
    }

    /**
     * Helper to create a card for testing.
     *
     * @param color card color
     * @param value card value
     * @return created card
     */
    private Card createCard(final CardColor color, final CardValue value) {
        if (value == CardValue.WILD) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 0),
                    BackSideBehavior.getInstance());
        } else if (value == CardValue.WILD_DRAW_FOUR) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 4),
                    BackSideBehavior.getInstance());
        } else if (isAction(value)) {
            return new DoubleSidedCard(
                    new ActionBehavior(color, value, correctAction(value)),
                    BackSideBehavior.getInstance());
        } else if (value == CardValue.DRAW_TWO) {
            return new DoubleSidedCard(
                    new DrawBehavior(color, value, 2),
                    BackSideBehavior.getInstance());
        } else {
            return new DoubleSidedCard(
                    new NumericBehavior(color, value),
                    BackSideBehavior.getInstance());
        }
    }

    /**
     * Helper to determine if a card value corresponds to an action card.
     * 
     * @param value card value to check
     * @return true if the value is an action card, false otherwise
     */
    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    /**
     * Helper to return the correct action for a given action card value.
     * 
     * @param value the card value for which to get the action
     * @return a Consumer<Game> representing the action to be performed when the card is played
     */
    private Consumer<Game> correctAction(final CardValue value) {
        if (value == CardValue.SKIP) {
            return g -> g.skipPlayers(1);
        } else if (value == CardValue.REVERSE) {
            return Game::reversePlayOrder;
        }

        return g -> {
        };
    }
}
