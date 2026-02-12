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
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.AllWildDeck;
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
 * Test class for AIAllWild behavior.
 */
class AIAllWildTest {

    private Game game;
    private AIAllWild aiAllWild;
    private AbstractPlayer opponent;

    @BeforeEach
    void setUp() {
        aiAllWild = new AIAllWild("AI-Wild");
        opponent = new HumanPlayer("Opponent");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiAllWild);
        players.add(opponent);

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck = new AllWildDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "ALLWILD", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    private Card createWildCard(final CardValue value) {
        if (value == CardValue.WILD_FORCED_SWAP) {

            return new DoubleSidedCard(
                    new WildBehavior(value, 0, true, true, 0, false),
                    BackSideBehavior.getInstance());
        }
        return new DoubleSidedCard(
                new WildBehavior(value, 0),
                BackSideBehavior.getInstance());
    }

    @Test
    void testAIChoosesWildColor() {
        assertEquals(CardColor.WILD, aiAllWild.chooseBestColor(game));
    }

    @Test
    void testAIUsesSwapIdeally() {

        final List<Optional<Card>> aiHand = new LinkedList<>();
        aiHand.add(Optional.of(createWildCard(CardValue.WILD)));
        aiHand.add(Optional.of(createWildCard(CardValue.WILD)));
        aiHand.add(Optional.of(createWildCard(CardValue.WILD_FORCED_SWAP)));
        aiAllWild.setHand(aiHand);

        final List<Optional<Card>> oppHand = new LinkedList<>();
        oppHand.add(Optional.of(createWildCard(CardValue.WILD)));
        opponent.setHand(oppHand);

        while (!game.getCurrentPlayer().equals(aiAllWild)) {
            game.aiAdvanceTurn();
        }

        aiAllWild.takeTurn(game);

        assertEquals(CardValue.WILD_FORCED_SWAP, game.getTopDiscardCard().get().getValue(game));
    }

    @Test
    void testAIAvoidsSwapWhenWinning() {
        final List<Optional<Card>> aiHand = new LinkedList<>();
        aiHand.add(Optional.of(createWildCard(CardValue.WILD)));
        aiHand.add(Optional.of(createWildCard(CardValue.WILD_FORCED_SWAP)));
        aiAllWild.setHand(aiHand);

        final int oppHandSize = 5;

        final List<Optional<Card>> oppHand = new LinkedList<>();
        for (int i = 0; i < oppHandSize; i++) {
            oppHand.add(Optional.of(createWildCard(CardValue.WILD)));
        }
        opponent.setHand(oppHand);

        while (!game.getCurrentPlayer().equals(aiAllWild)) {
            game.aiAdvanceTurn();
        }

        aiAllWild.takeTurn(game);

        final Card played = game.getTopDiscardCard().get();
        assertTrue(played.getValue(game) != CardValue.WILD_FORCED_SWAP,
                "L'IA non dovrebbe scambiare se ha meno carte dell'avversario.");
    }
}
