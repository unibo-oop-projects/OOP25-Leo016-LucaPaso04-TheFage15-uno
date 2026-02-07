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

// Correct Imports
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;

import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameRulesImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.AIClassic;
import uno.model.utils.api.GameLogger; // Needed for mock
import uno.model.utils.impl.TestLogger;

import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;

class AIWithCustomRulesTest {

    private Game game;
    private AbstractPlayer aiPlayer;

    @BeforeEach
    void setUp() {
        aiPlayer = new AIClassic("AI-Test");
        final AbstractPlayer aiPlayer2 = new AIClassic("AI-2");
        final List<AbstractPlayer> players = List.of(aiPlayer, aiPlayer2);

        // Custom Rules: Skip After Draw ENABLED
        // Constructor: (unoPenalty, skipAfterDraw, mandatoryPass)
        final GameRules rules = new GameRulesImpl(false, true, false);

        // Mock Deck that returns a specific card when drawn
        final Deck<Card> deck = new Deck<>() {
            @Override
            public void shuffle() {
            }

            @Override
            public Optional<Card> draw() {
                // Returns a playable RED ONE
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
        // Setup:
        // Top Card is RED ZERO
        final Card topCard = new DoubleSidedCard(new NumericBehavior(CardColor.RED, CardValue.ZERO),
                BackSideBehavior.getInstance());
        game.getDiscardPile().addCard(topCard);
        game.setCurrentColor(CardColor.RED);

        // AI Hand has BLUE NINE (Not playable on RED ZERO)
        final Card aiCard = new DoubleSidedCard(new NumericBehavior(CardColor.BLUE, CardValue.NINE),
                BackSideBehavior.getInstance());
        aiPlayer.setHand(Collections.singletonList(Optional.of(aiCard)));

        if (!game.getCurrentPlayer().equals(aiPlayer)) {
            game.aiAdvanceTurn();
        }

        // Assert AI has no playable cards
        // AI should draw RED ONE (from mock deck) which IS playable on RED ZERO.

        // But Rule 'SkipAfterDraw' is ON.
        // AI should DRAW, see it can't play (due to rule), and PASS.

        // Current Bug: AI sees RED ONE is playable (by color) and tries to play it ->
        // Exception.

        // Bug Fixed: AI should check rule and PASS properly.
        aiPlayer.takeTurn(game);

        // Verification
        assertEquals(2, aiPlayer.getHandSize(), "AI should have drawn a card (Hand 1 -> 2).");

        // Since AI passed turn, the current player should now be "Human"
        assertEquals("AI-2", game.getCurrentPlayer().getName(), "Turn should have passed to AI-2.");
    }
}
