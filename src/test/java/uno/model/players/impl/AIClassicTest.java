package uno.model.players.impl;

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
import uno.model.cards.behaviors.impl.ActionBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.DrawBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.utils.api.GameLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

/**
 * Test class for AIClassic player behavior in a UNO game.
 */
class AIClassicTest {

    private Game game;
    private AIClassic aiClassic;

    @BeforeEach
    void setUp() {
        aiClassic = new AIClassic("AI-Bot");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
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

    /**
     * Helper to create a card based on color and value, handling special cases for wild and action cards.
     *
     * @param color color
     * @param value value
     * @return card instance with appropriate behavior based on color and value
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
     * Helper to check if a CardValue is an action card (SKIP or REVERSE).
     * 
     * @param value the CardValue to check
     * @return true if the value is SKIP or REVERSE, false otherwise
     */
    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    /**
     * Creates a Consumer<Game> that performs the correct action for the given action card value.
     * 
     * @param value the CardValue of the action card (e.g., SKIP, REVERSE)
     * @return a Consumer<Game> that executes the appropriate game action when called
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

    @Test
    void testAIClassicPrioritizesActionCards() {
        final Card topCard = createCard(CardColor.RED, CardValue.FIVE);
        game.getDiscardPile().addCard(topCard);
        game.setCurrentColor(CardColor.RED);

        final Card redNine = createCard(CardColor.RED, CardValue.NINE);
        final Card redSkip = createCard(CardColor.RED, CardValue.SKIP);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        listcard.add(Optional.of(redSkip));
        aiClassic.setHand(listcard);

        while (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        assertEquals(CardValue.SKIP, game.getTopDiscardCard().get().getValue(game),
                "L'IA avrebbe dovuto scegliere la carta Azione (SKIP) rispetto al numero.");

        assertEquals(1, aiClassic.getHandSize(), "L'IA dovrebbe avere 1 carta rimanente.");
    }

    @Test
    void testAIClassicPrioritizesHighNumbers() {
        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.ZERO));
        game.setCurrentColor(CardColor.BLUE);

        final Card blueEight = createCard(CardColor.BLUE, CardValue.EIGHT);
        final Card blueOne = createCard(CardColor.BLUE, CardValue.ONE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(blueOne));
        listcard.add(Optional.of(blueEight));
        aiClassic.setHand(listcard);

        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        assertEquals(CardValue.EIGHT, game.getTopDiscardCard().get().getValue(game),
                "L'IA avrebbe dovuto giocare il numero più alto (8).");
    }

    @Test
    void testAIPlaysWildAndSetsColor() {

        game.getDiscardPile().addCard(createCard(CardColor.GREEN, CardValue.TWO));
        game.setCurrentColor(CardColor.GREEN);

        final Card wild = createCard(CardColor.WILD, CardValue.WILD);
        final Card blueOne = createCard(CardColor.BLUE, CardValue.ONE);
        final Card blueThree = createCard(CardColor.BLUE, CardValue.THREE);
        final Card blueFour = createCard(CardColor.BLUE, CardValue.FOUR);
        final Card yellowOne = createCard(CardColor.YELLOW, CardValue.ONE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(wild));
        listcard.add(Optional.of(blueOne));
        listcard.add(Optional.of(blueThree));
        listcard.add(Optional.of(blueFour));
        listcard.add(Optional.of(yellowOne));
        aiClassic.setHand(listcard);

        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        assertEquals(CardValue.WILD, game.getTopDiscardCard().get().getValue(game));

        assertTrue(game.getCurrentColor().isPresent(), "Il colore deve essere stato scelto dall'IA.");
        assertNotEquals(CardColor.WILD, game.getCurrentColor().get(), "Il colore scelto non può essere WILD.");
    }

    @Test
    void testAIInitiatesDrawWhenNoMoves() {
        game.getDiscardPile().addCard(createCard(CardColor.YELLOW, CardValue.FIVE));
        game.setCurrentColor(CardColor.YELLOW);

        final Card blueNine = createCard(CardColor.BLUE, CardValue.NINE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(blueNine));
        aiClassic.setHand(listcard);

        final int initialDeckSize = game.getDrawDeck().size();
        final int initialHandSize = aiClassic.getHandSize();

        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        assertTrue(aiClassic.getHandSize() > initialHandSize || game.getDrawDeck().size() < initialDeckSize,
                "L'IA avrebbe dovuto pescare una carta.");
    }

    @Test
    void testAICallsUno() {

        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.FIVE));
        game.setCurrentColor(CardColor.RED);

        final Card redSix = createCard(CardColor.WILD, CardValue.WILD);
        final Card blueZero = createCard(CardColor.BLUE, CardValue.ZERO);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redSix));
        listcard.add(Optional.of(blueZero));
        aiClassic.setHand(listcard);

        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }
        aiClassic.takeTurn(game);
        assertEquals(1, aiClassic.getHandSize());
    }
}
