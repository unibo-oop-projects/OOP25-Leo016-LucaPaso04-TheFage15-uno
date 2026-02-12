package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
class WildBehaviorTest {

    private Deck<Card> deck;
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
        deck = new StandardDeck(logger);

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
    void testWildBehaviorStandard() {
        final Card wildCard = createCard(CardColor.WILD, CardValue.WILD);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(wildCard));
        hand.add(Optional.of(createCard(CardColor.RED, CardValue.ONE)));
        aiClassic1.setHand(hand);

        final Card discardCard = createCard(CardColor.BLUE, CardValue.NINE);
        game.getDiscardPile().addCard(discardCard);
        game.setCurrentColor(CardColor.BLUE);

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }

        final int initialHandSizeP2 = aiClassic2.getHand().size();

        aiClassic1.takeTurn(game);

        assertEquals(wildCard, game.getTopDiscardCard().get());

        assertTrue(game.getCurrentColor().isPresent());
        assertNotNull(game.getCurrentColor().get());
        assertNotEquals(CardColor.WILD, game.getCurrentColor().get(),
                "Il colore corrente non può rimanere WILD dopo la giocata.");

        assertEquals(initialHandSizeP2, aiClassic2.getHand().size(),
                "Una Wild standard non deve far pescare carte.");

        assertEquals(aiClassic2, game.getCurrentPlayer(),
                "Dopo una Wild standard, il turno deve passare al prossimo giocatore.");
    }

    @Test
    void testWildDrawFourBehavior() {
        final Card wildDrawFour = createCard(CardColor.WILD, CardValue.WILD_DRAW_FOUR);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(wildDrawFour));
        hand.add(Optional.of(createCard(CardColor.GREEN, CardValue.TWO)));
        aiClassic1.setHand(hand);

        game.getDiscardPile().addCard(createCard(CardColor.YELLOW, CardValue.FIVE));
        game.setCurrentColor(CardColor.YELLOW);

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }

        final int initialHandSizeP2 = aiClassic2.getHand().size();

        aiClassic1.takeTurn(game);

        assertEquals(wildDrawFour, game.getTopDiscardCard().get());

        assertTrue(game.getCurrentColor().isPresent());
        assertNotEquals(CardColor.WILD, game.getCurrentColor().get());

        final int expectedHandSize = initialHandSizeP2 + 4;
        assertEquals(expectedHandSize, aiClassic2.getHand().size(),
                "Il giocatore successivo deve avere 4 carte in più.");

        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "Il Wild Draw 4 deve far saltare il turno all'avversario (in 2p tocca di nuovo a P1).");
    }

    @Test
    void testWildDrawColorBehavior() {

        final Card wildDrawColor = createCard(CardColor.WILD, CardValue.WILD_DRAW_COLOR);

        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(wildDrawColor));
        hand.add(Optional.of(createCard(CardColor.RED, CardValue.ONE)));
        aiClassic1.setHand(hand);

        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.NINE));
        game.setCurrentColor(CardColor.BLUE);

        while (!deck.isEmpty()) {
            deck.draw();
        }

        deck.addCard(createCard(CardColor.RED, CardValue.FIVE));
        deck.addCard(createCard(CardColor.YELLOW, CardValue.TWO));
        deck.addCard(createCard(CardColor.BLUE, CardValue.THREE));

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }

        final int initialHandSizeP2 = aiClassic2.getHand().size();

        aiClassic1.takeTurn(game);

        assertEquals(wildDrawColor, game.getTopDiscardCard().get());

        assertEquals(CardColor.RED, game.getCurrentColor().get());

        final int expectedHandSize = initialHandSizeP2 + 3;
        assertEquals(expectedHandSize, aiClassic2.getHand().size(),
                "Il giocatore successivo deve aver pescato 3 carte (2 sbagliate + 1 del colore target).");

        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "Dopo aver pescato fino al colore, il turno dovrebbe essere saltato.");
    }

    /**
     * Helper mothod to create cards with specific behaviors for testing purposes.
     *
     * @param color card color
     * @param value card value
     * @return card with the appropriate behavior based on its value
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
        } else if (value == CardValue.WILD_DRAW_COLOR) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 0),
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
