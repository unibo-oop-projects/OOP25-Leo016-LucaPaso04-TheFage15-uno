package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
class NumericBehaviorTest {

    private Game game;
    private AIClassic aiClassic1;
    private AIClassic aiClassic2;

    @BeforeEach
    void setUp() {
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        aiClassic1 = new AIClassic("AI-Bot-1");
        aiClassic2 = new AIClassic("AI-Bot-2");

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
    void testNumericBehavior() {
        final Card redNine = createCard(CardColor.RED, CardValue.NINE);
        final Card blueSkip1 = createCard(CardColor.BLUE, CardValue.SKIP);
        final Card blueSkip2 = createCard(CardColor.BLUE, CardValue.SKIP);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        listcard.add(Optional.of(blueSkip1));
        listcard.add(Optional.of(blueSkip2));
        aiClassic1.setHand(listcard);

        final Card discardCard = new DoubleSidedCard(
                new NumericBehavior(CardColor.RED, CardValue.ONE),
                BackSideBehavior.getInstance());

        game.getDiscardPile().addCard(discardCard);
        game.setCurrentColor(CardColor.RED);

        final int initialHandSize = aiClassic2.getHand().size();

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }

        assertEquals(aiClassic1, game.getCurrentPlayer(), "Deve essere il turno di AI 1");
        aiClassic1.takeTurn(game);

        final int finalHandSize = aiClassic2.getHand().size();

        assertEquals(initialHandSize, finalHandSize,
                "Il giocatore successivo non dovrebbe ricevere carte per un NumericBehavior.");
        assertEquals(redNine, game.getTopDiscardCard().get());
        assertTrue(game.isClockwise(), "La direzione del gioco non deve cambiare.");
    }

    /**
     * Helper method to create a card with the appropriate behavior based on its value.
     *
     * @param color card color
     * @param value card value
     * @return card with the correct behavior
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
     * Helper method to determine if a card value corresponds to an action card.
     * 
     * @param value card value
     * @return true if the value is an action card, false otherwise
     */
    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    /**
     * Helper method to return the correct action for a given action card value.
     * 
     * @param value card value
     * @return a Consumer<Game> that performs the correct action for the given card value
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
