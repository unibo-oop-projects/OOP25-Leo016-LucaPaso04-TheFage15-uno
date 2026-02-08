
package uno.model.game;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import uno.model.utils.impl.TestLogger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.impl.AbstractDeckImpl;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameRulesImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GameModeTest {

    private GameImpl game;
    private AbstractPlayer p1;
    private AbstractPlayer p2;
    private DiscardPileImpl discardPile;
    private TurnManagerImpl turnManager;

    // Helper to create simple cards
    private Card createCard(CardColor color, CardValue value) {
        NumericBehavior behavior = new NumericBehavior(color, value);
        return new DoubleSidedCard(behavior, behavior);
    }

    @BeforeEach
    void setUp() {
        p1 = new HumanPlayer("P1");
        p2 = new HumanPlayer("P2");
        List<AbstractPlayer> players = Arrays.asList(p1, p2);

        discardPile = new DiscardPileImpl();
        discardPile.addCard(createCard(CardColor.RED, CardValue.ONE));

        turnManager = new TurnManagerImpl(players);
    }

    @Test
    void testOneRoundMode() {
        // Rule: Scoring Mode DISABLED (One Round Match)
        GameRules rules = new GameRulesImpl(false, false, false, false);

        game = new GameImpl(new AbstractDeckImpl<>(new TestLogger()) {
        }, Arrays.asList(p1, p2), turnManager, discardPile, "test", new TestLogger(), rules);

        // P1 wins the round
        Card winningCard = createCard(CardColor.RED, CardValue.NINE);
        p1.addCardToHand(winningCard);
        // Ensure P2 has points so score would be > 0
        p2.addCardToHand(createCard(CardColor.BLUE, CardValue.FIVE));

        game.setCurrentColorOptional(Optional.of(CardColor.RED));
        // Force p1 turn
        while (!game.getCurrentPlayer().equals(p1)) {
            turnManager.advanceTurn(game);
        }

        // Action: Play winning card
        game.playCard(Optional.of(winningCard));

        // Assert: Match Ends immediately (GAME_OVER), not ROUND_OVER
        assertEquals(GameState.GAME_OVER, game.getGameState(), "Game should end immediately in One Round mode");
        assertEquals(5, p1.getScore(), "Score should still be calculated");
    }

    @Test
    void testScoringMode() {
        // Rule: Scoring Mode ENABLED (Play to 500)
        GameRules rules = new GameRulesImpl(false, false, false, true);

        game = new GameImpl(new AbstractDeckImpl<>(new TestLogger()) {
        }, Arrays.asList(p1, p2), turnManager, discardPile, "test", new TestLogger(), rules);

        // P1 wins the round with low score
        Card winningCard = createCard(CardColor.RED, CardValue.NINE);
        p1.addCardToHand(winningCard);
        p2.addCardToHand(createCard(CardColor.BLUE, CardValue.FIVE)); // 5 points

        game.setCurrentColorOptional(Optional.of(CardColor.RED));
        // Force p1 turn
        while (!game.getCurrentPlayer().equals(p1)) {
            turnManager.advanceTurn(game);
        }

        game.playCard(Optional.of(winningCard));

        // Assert: Match continues (ROUND_OVER) because score < 500
        assertEquals(GameState.ROUND_OVER, game.getGameState(), "Game should continue in Scoring mode if score < 500");
    }
}
