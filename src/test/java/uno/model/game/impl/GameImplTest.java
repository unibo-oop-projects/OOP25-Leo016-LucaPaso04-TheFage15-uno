package uno.model.game.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.utils.impl.TestLogger;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.players.impl.AIClassic;
import uno.model.cards.types.api.Card;
import uno.model.players.api.AbstractPlayer;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.types.impl.DoubleSidedCard;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.LinkedList;

class GameImplTest {

    private static final String GAME_MODE = "TEST";

    private List<AbstractPlayer> players;
    private TestLogger logger;
    private StandardDeck deck;

    @BeforeEach
    void setUp() {
        players = new ArrayList<>();
        players.add(new AIClassic("P1"));
        players.add(new AIClassic("P2"));
        logger = new TestLogger();
        deck = new StandardDeck(logger);
    }

    @Test
    void testSkipAfterDrawEnabled() {
        // Rule: Skip After Draw = TRUE
        final GameRules rules = new GameRulesImpl(true, true, false);
        final GameImpl game = new GameImpl(deck, players, GAME_MODE, logger, rules);
        final GameSetupImpl setup = new GameSetupImpl(game, deck, game.getDiscardPile(), players);
        setup.initializeGame(false);

        final AbstractPlayer current = game.getCurrentPlayer();

        game.getTurnManager().setHasDrawnThisTurn(true);

        // Cheat: Peep top discard
        final Card top = game.getTopDiscardCard().get();
        // Create a card that matches top
        final Card matching = new DoubleSidedCard(
                new NumericBehavior(top.getColor(game), top.getValue(game)), // Exact match
                BackSideBehavior.getInstance());
        current.addCardToHand(matching);

        // Attempt play
        assertThrows(IllegalStateException.class, () -> {
            game.playCard(Optional.of(matching));
        }, "Should check Skip After Draw rule");
    }

    @Test
    void testUnoPenaltyDisabled() {
        // Rule: Uno Penalty = FALSE
        final GameRules rules = new GameRulesImpl(false, false, false);
        final GameImpl game = new GameImpl(deck, players, GAME_MODE, logger, rules);
        final GameSetupImpl setup = new GameSetupImpl(game, deck, game.getDiscardPile(), players);
        setup.initializeGame(false);

        final AbstractPlayer current = game.getCurrentPlayer();

        // Set hand to 1 card
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(deck.draw().get()));
        current.setHand(hand);

        // Set P2 hand to 1 card.
        final AbstractPlayer next = game.getTurnManager().peekNextPlayer();
        final List<Optional<Card>> handNext = new LinkedList<>();
        handNext.add(Optional.of(deck.draw().get()));
        next.setHand(handNext);

        // Advance turn so P2 becomes current
        game.getTurnManager().advanceTurn(game);

        // Assert hand size is still 1 (No penalty applied)
        assertEquals(1, next.getHandSize(), "No penalty should be applied when rule is disabled");
    }

    @Test
    void testNoReshuffleRule() {
        // Rule: Mandatory Pass / No Reshuffle = TRUE
        final GameRules rules = new GameRulesImpl(true, false, true);
        final GameImpl game = new GameImpl(deck, players, GAME_MODE, logger, rules);
        final GameSetupImpl setup = new GameSetupImpl(game, deck, game.getDiscardPile(), players);
        setup.initializeGame(false);

        // Empty deck
        while (!deck.isEmpty()) {
            deck.draw();
        }

        // Try to draw
        final AbstractPlayer current = game.getCurrentPlayer();
        game.drawCardForPlayer(current);

        // Assert Game Over
        assertEquals(GameState.GAME_OVER, game.getGameState(),
                "Game should end when deck is empty and NoReshuffle is on");
    }
}
