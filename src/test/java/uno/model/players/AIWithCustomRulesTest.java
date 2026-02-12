package uno.model.players;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;

import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;

import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameRulesImpl;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.utils.impl.TestLogger;

import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;

/**
 * Test class for {@link AIClassic} with custom rules.
 */
class AIWithCustomRulesTest {

    private Game game;
    private AbstractPlayer aiPlayer;

    @BeforeEach
    void setUp() {
        aiPlayer = new AIClassic("AI-Test");
        final AbstractPlayer aiPlayer2 = new AIClassic("AI-2");
        final List<AbstractPlayer> players = List.of(aiPlayer, aiPlayer2);
        final GameRules rules = new GameRulesImpl(false, true, false, false);

        final Deck<Card> deck = new Deck<>() {
            @Override
            public void shuffle() {
            }

            @Override
            public Optional<Card> draw() {
                return Optional.of(new DoubleSidedCard(new NumericBehavior(CardColor.RED, CardValue.ONE),
                        BackSideBehavior.getInstance()));
            }

            @Override
            public Optional<Card> peek() {
                return Optional.empty();
            }

            @Override
            public void addCard(final Card card) {
            }

            @Override
            public void refill(final List<Card> newCards) {
            }

            @Override
            public boolean isEmpty() {
                return false;
            }

            @Override
            public int size() {
                return 100;
            }

            @Override
            public GameLogger getLogger() {
                return new TestLogger();
            }
        };

        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "CLASSIC", new TestLogger(), rules);
    }

    @Test
    void testAISkipsTurnAfterDrawIfRuleEnabled() {
        final Card topCard = new DoubleSidedCard(new NumericBehavior(CardColor.RED, CardValue.ZERO),
                BackSideBehavior.getInstance());
        game.getDiscardPile().addCard(topCard);
        game.setCurrentColor(CardColor.RED);

        final Card aiCard = new DoubleSidedCard(new NumericBehavior(CardColor.BLUE, CardValue.NINE),
                BackSideBehavior.getInstance());
        aiPlayer.setHand(Collections.singletonList(Optional.of(aiCard)));

        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            game.aiAdvanceTurn();
        }

        aiPlayer.takeTurn(game);

        assertEquals(2, aiPlayer.getHandSize(), "AI should have drawn a card (Hand 1 -> 2).");

        assertEquals("AI-2", game.getCurrentPlayer().getName(), "Turn should have passed to AI-2.");
    }
}
