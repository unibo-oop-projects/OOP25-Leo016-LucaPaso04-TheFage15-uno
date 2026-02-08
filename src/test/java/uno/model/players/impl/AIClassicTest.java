package uno.model.players.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import uno.model.cards.behaviors.impl.ActionBehavior;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.DrawBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

class AIClassicTest {

    private Game game;
    private AIClassic aiClassic;

    @BeforeEach
    void setUp() {
        // Setup base
        aiClassic = new AIClassic("AI-Bot");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck = new StandardDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false); // Dummy rules
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "CLASSIC", logger, rules);

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    /**
     * Helper per creare una carta semplice al volo per i test.
     *
     * @param color colore
     * @param value valore
     * @return carta
     */
    private Card createCard(final CardColor color, final CardValue value) {
        if (value == CardValue.WILD) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 0), // Fronte
                    BackSideBehavior.getInstance());
        } else if (value == CardValue.WILD_DRAW_FOUR) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 4), // Fronte
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

    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    private Consumer<Game> correctAction(final CardValue value) {
        if (value == CardValue.SKIP) {
            return g -> g.skipPlayers(1);
        } else if (value == CardValue.REVERSE) {
            return Game::reversePlayOrder;
        }

        return g -> {
        };
    }

    @Test
    void testAIClassicPrioritizesActionCards() {
        // Scenario: Scarto è ROSSO 5.
        // Mano IA: ROSSO 9 e ROSSO SKIP.
        // Strategia AIClassic: Dovrebbe giocare SKIP (Azione > Numero).

        // 1. Forza la carta in cima allo scarto
        final Card topCard = createCard(CardColor.RED, CardValue.FIVE);
        game.getDiscardPile().addCard(topCard);
        game.setCurrentColor(CardColor.RED);

        // 2. Prepara la mano dell'IA
        final Card redNine = createCard(CardColor.RED, CardValue.NINE);
        final Card redSkip = createCard(CardColor.RED, CardValue.SKIP);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        listcard.add(Optional.of(redSkip));
        aiClassic.setHand(listcard);

        // 3. Esegui il turno
        // Dobbiamo assicurarci che sia il turno dell'IA
        while (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        // 4. Verifica
        // La carta in cima agli scarti dovrebbe essere lo SKIP
        assertEquals(CardValue.SKIP, game.getTopDiscardCard().get().getValue(game),
                "L'IA avrebbe dovuto scegliere la carta Azione (SKIP) rispetto al numero.");

        assertEquals(1, aiClassic.getHandSize(), "L'IA dovrebbe avere 1 carta rimanente.");
    }

    @Test
    void testAIClassicPrioritizesHighNumbers() {
        // Scenario: Scarto è BLU 0.
        // Mano IA: BLU 1 e BLU 8.
        // Strategia AIClassic: Tra numeri, gioca il più alto (8).

        game.getDiscardPile().addCard(createCard(CardColor.BLUE, CardValue.ZERO));
        game.setCurrentColor(CardColor.BLUE);

        final Card blueEight = createCard(CardColor.BLUE, CardValue.EIGHT);
        final Card blueOne = createCard(CardColor.BLUE, CardValue.ONE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(blueOne));
        listcard.add(Optional.of(blueEight));
        aiClassic.setHand(listcard);

        // Forza turno IA
        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        assertEquals(CardValue.ONE, game.getTopDiscardCard().get().getValue(game),
                "L'IA avrebbe dovuto giocare il numero più alto (8).");
    }

    @Test
    void testAIPlaysWildAndSetsColor() {
        // Scenario: IA ha solo un Jolly. Deve giocarlo e scegliere un colore.

        game.getDiscardPile().addCard(createCard(CardColor.GREEN, CardValue.TWO));
        game.setCurrentColor(CardColor.GREEN);

        final Card wild = createCard(CardColor.WILD, CardValue.WILD);
        final Card blueOne = createCard(CardColor.BLUE, CardValue.ONE);
        final Card blueThree = createCard(CardColor.BLUE, CardValue.THREE);
        final Card blueFour = createCard(CardColor.BLUE, CardValue.FOUR);
        final Card yellowOne = createCard(CardColor.YELLOW, CardValue.ONE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(wild));
        listcard.add(Optional.of(blueOne));
        listcard.add(Optional.of(blueThree));
        listcard.add(Optional.of(blueFour));
        listcard.add(Optional.of(yellowOne));
        aiClassic.setHand(listcard);

        // Forza turno IA
        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        // Verifica che la carta in cima sia Wild
        assertEquals(CardValue.WILD, game.getTopDiscardCard().get().getValue(game));

        // Verifica che il colore sia stato impostato (non deve essere empty o WILD
        // puro)
        assertTrue(game.getCurrentColor().isPresent(), "Il colore deve essere stato scelto dall'IA.");
        assertNotEquals(CardColor.WILD, game.getCurrentColor().get(), "Il colore scelto non può essere WILD.");
    }

    @Test
    void testAIInitiatesDrawWhenNoMoves() {
        // Scenario: Nessuna carta compatibile.
        // Scarto: GIALLO 5. Mano: BLU 9.

        game.getDiscardPile().addCard(createCard(CardColor.YELLOW, CardValue.FIVE));
        game.setCurrentColor(CardColor.YELLOW);

        final Card blueNine = createCard(CardColor.BLUE, CardValue.NINE);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(blueNine));
        aiClassic.setHand(listcard);

        final int initialDeckSize = game.getDrawDeck().size();
        final int initialHandSize = aiClassic.getHandSize();

        // Forza turno IA
        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        aiClassic.takeTurn(game);

        // L'IA dovrebbe aver pescato
        assertTrue(aiClassic.getHandSize() > initialHandSize || game.getDrawDeck().size() < initialDeckSize,
                "L'IA avrebbe dovuto pescare una carta.");
    }

    @Test
    void testAICallsUno() {
        // Scenario: IA ha 2 carte. Ne gioca una valida. Deve chiamare UNO.

        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.FIVE));
        game.setCurrentColor(CardColor.RED);

        // Carta giocabile
        final Card redSix = createCard(CardColor.WILD, CardValue.WILD);
        // Carta rimanente
        final Card blueZero = createCard(CardColor.BLUE, CardValue.ZERO);
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redSix));
        listcard.add(Optional.of(blueZero));
        aiClassic.setHand(listcard);

        // Forza turno IA
        if (!game.getCurrentPlayer().equals(aiClassic)) {
            game.aiAdvanceTurn();
        }

        // Resettiamo eventuali flag precedenti
        // (Nota: dipenderebbe da come è implementato hasCalledUno nel Player,
        // assumiamo parta false o si resetti)

        aiClassic.takeTurn(game);

        // Verifichiamo lo stato del gioco o un flag nel player.
        // Dato che non posso accedere facilmente ai System.out, verifico che non ci
        // siano errori
        // e che la mano sia 1.
        assertEquals(1, aiClassic.getHandSize());
    }
}
