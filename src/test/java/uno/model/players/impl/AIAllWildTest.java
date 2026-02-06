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
import uno.model.players.api.AbstractPlayer;
import uno.model.utils.api.GameLogger;

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
        game = new GameImpl(deck, players, "ALLWILD", logger);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    private Card createWildCard(final CardValue value) {
        if (value == CardValue.WILD_FORCED_SWAP) {
            // value, drawAmount, requiresColorChoice, requiresTargetPlayer, skipAmount,
            // reversesGame
            // Forced Swap requires a target player!
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
        // AIAllWild dovrebbe sempre scegliere colore WILD
        assertEquals(CardColor.WILD, aiAllWild.chooseBestColor(game));
    }

    @Test
    void testAIUsesSwapIdeally() {
        // Scenario: AI ha 3 carte (tra cui Swap). Opponent ha 1 carta.
        // AI dovrebbe usare Swap per scambiare la mano grossa con quella piccola.

        // Setup mani
        final List<Optional<Card>> aiHand = new LinkedList<>();
        aiHand.add(Optional.of(createWildCard(CardValue.WILD))); // dummy
        aiHand.add(Optional.of(createWildCard(CardValue.WILD))); // dummy
        aiHand.add(Optional.of(createWildCard(CardValue.WILD_FORCED_SWAP))); // SWAP
        aiAllWild.setHand(aiHand);

        final List<Optional<Card>> oppHand = new LinkedList<>();
        oppHand.add(Optional.of(createWildCard(CardValue.WILD))); // Solo 1 carta
        opponent.setHand(oppHand);

        // Turno IA
        while (!game.getCurrentPlayer().equals(aiAllWild)) {
            game.aiAdvanceTurn();
        }

        aiAllWild.takeTurn(game);

        // Verifica che sia stato giocato lo SWAP
        assertEquals(CardValue.WILD_FORCED_SWAP, game.getTopDiscardCard().get().getValue(game));
    }

    @Test
    void testAIAvoidsSwapWhenWinning() {
        // Scenario: AI ha 1 carta (Swap). Opponent ha 5 carte.
        // AI NON dovrebbe usare Swap (si prenderebbe 5 carte).
        // Ma poichè è l'unica carta, è costretto a giocarla.
        // Tuttavia, questo test verifica che la logica "CONVIENE SCAMBIARE?" ritorni
        // false
        // e quindi il fallback ("SCARTO SICURO") entri in gioco.
        // Se avesse un'altra carta, userebbe quella.

        final List<Optional<Card>> aiHand = new LinkedList<>();
        aiHand.add(Optional.of(createWildCard(CardValue.WILD))); // Altra carta
        aiHand.add(Optional.of(createWildCard(CardValue.WILD_FORCED_SWAP))); // Swap
        aiAllWild.setHand(aiHand);

        final int oppHandSize = 5;

        final List<Optional<Card>> oppHand = new LinkedList<>();
        for (int i = 0; i < oppHandSize; i++) {
            oppHand.add(Optional.of(createWildCard(CardValue.WILD)));
        }
        opponent.setHand(oppHand);

        // Turno IA
        while (!game.getCurrentPlayer().equals(aiAllWild)) {
            game.aiAdvanceTurn();
        }

        aiAllWild.takeTurn(game);

        // NON deve aver giocato Swap
        final Card played = game.getTopDiscardCard().get();
        assertTrue(played.getValue(game) != CardValue.WILD_FORCED_SWAP,
                "L'IA non dovrebbe scambiare se ha meno carte dell'avversario.");
    }
}
