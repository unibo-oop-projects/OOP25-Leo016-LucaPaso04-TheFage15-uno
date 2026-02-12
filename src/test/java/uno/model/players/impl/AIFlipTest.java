package uno.model.players.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.FlipBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.FlipDeck;
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
 * Test class for AIFlip behavior in Uno game.
 */
class AIFlipTest {

    private Game game;
    private AIFlip aiFlip;

    @BeforeEach
    void setUp() {
        aiFlip = new AIFlip("AI-Flip");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiFlip);

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck = new FlipDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "FLIP", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    private Card createCard(final CardColor color, final CardValue value, final boolean isDarkSide) {

        if (value == CardValue.FLIP) {
            return new DoubleSidedCard(
                    new FlipBehavior(color, value),
                    new FlipBehavior(CardColor.ORANGE, value)
            );
        } else if (value == CardValue.WILD_DRAW_COLOR) {
            return new DoubleSidedCard(
                    new NumericBehavior(CardColor.RED, CardValue.ONE),
                    new WildBehavior(value, 0)
            );
        } else if (value == CardValue.DRAW_FIVE) {
            final int drawAmount = 5;
            return new DoubleSidedCard(
                    new NumericBehavior(CardColor.RED, CardValue.ONE),
                    new WildBehavior(value, drawAmount)
            );
        } else {
            return new DoubleSidedCard(
                    new NumericBehavior(color, value),
                    new NumericBehavior(isDarkSide ? color : CardColor.TEAL, value));
        }
    }

    @Test
    void testAIPrioritizesFlipCard() {

        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.ONE, false));
        game.setCurrentColor(CardColor.RED);

        final Card flipCard = createCard(CardColor.RED, CardValue.FLIP, false);
        final Card redNine = createCard(CardColor.RED, CardValue.NINE, false);

        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        listcard.add(Optional.of(flipCard));
        aiFlip.setHand(listcard);

        if (!game.getCurrentPlayer().equals(aiFlip)) {
            game.aiAdvanceTurn();
        }

        aiFlip.takeTurn(game);

        assertEquals(CardValue.FLIP, game.getTopDiscardCard().get().getValue(game),
                "L'IA dovrebbe dare priorità alla carta FLIP.");
    }

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    void testAIDarkSidePowerCards() {
        final Card dummyCard = createCard(CardColor.RED, CardValue.ONE, false);
        try {
            final java.lang.reflect.Field field = GameImpl.class.getDeclaredField("currentPlayedCard");
            field.setAccessible(true);
            field.set(game, dummyCard);
        } catch (final ReflectiveOperationException e) {
            org.junit.jupiter.api.Assertions.fail("Reflection failed: " + e.getMessage());
        }

        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.NINE, false));
        game.setCurrentColor(CardColor.RED);

        game.flipTheWorld();
        assertTrue(game.isDarkSide());

        game.getDiscardPile().addCard(createCard(CardColor.TEAL, CardValue.ONE, true));
        game.setCurrentColor(CardColor.TEAL);

        final Card darkWild = createCard(CardColor.WILD, CardValue.WILD_DRAW_COLOR, true);

        final Card tealNine = createCard(CardColor.TEAL, CardValue.NINE, true);

        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(tealNine));
        listcard.add(Optional.of(darkWild));
        aiFlip.setHand(listcard);

        while (!game.getCurrentPlayer().equals(aiFlip)) {
            game.aiAdvanceTurn();
        }

        aiFlip.takeTurn(game);

        assertEquals(CardValue.WILD_DRAW_COLOR, game.getTopDiscardCard().get().getValue(game));
    }
}
