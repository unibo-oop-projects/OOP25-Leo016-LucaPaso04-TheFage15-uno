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
class DrawBehaviorTest {

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
    void testDrawBehavior() {

        final Card blueDrawTwo = createCard(CardColor.BLUE, CardValue.DRAW_TWO);
        final Card redFive = createCard(CardColor.RED, CardValue.FIVE);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(blueDrawTwo));
        hand.add(Optional.of(redFive));
        aiClassic1.setHand(hand);

        final Card discardCard = createCard(CardColor.BLUE, CardValue.ONE);
        game.setCurrentColor(CardColor.BLUE);

        game.getDiscardPile().addCard(discardCard);

        final int initialHandSizeP2 = aiClassic2.getHand().size();

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }
        assertEquals(aiClassic1, game.getCurrentPlayer(), "Deve essere il turno di AI 1");

        aiClassic1.takeTurn(game);

        final int finalHandSizeP2 = aiClassic2.getHand().size();

        assertEquals(initialHandSizeP2 + 2, finalHandSizeP2,
                "Il giocatore successivo dovrebbe avere 2 carte in più (effetto Draw Two).");

        assertTrue(game.getTopDiscardCard().isPresent());
        assertEquals(blueDrawTwo, game.getTopDiscardCard().get(), "Il Draw Two deve essere in cima agli scarti.");

        assertTrue(game.isClockwise(), "La direzione del gioco non deve cambiare.");
        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "In partita a 2, dopo un +2 l'avversario salta e tocca di nuovo a chi ha giocato.");
    }

    /**
     * Helper method to create a card with the specified color and value, using the appropriate behavior.
     *
     * @param color card color
     * @param value card value
     * @return created card with the correct behavior
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
     * Helper method to determine if a card value corresponds to an action card (Skip or Reverse).
     * 
     * @param value card value to check
     * @return true if the value is Skip or Reverse, false otherwise
     */
    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    /**
     * Helper method to return the correct action for Skip and Reverse cards.
     * 
     * @param value card value (should be Skip or Reverse)
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
