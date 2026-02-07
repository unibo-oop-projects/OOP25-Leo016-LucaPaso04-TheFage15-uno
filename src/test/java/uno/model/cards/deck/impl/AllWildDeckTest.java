package uno.model.cards.deck.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.AIClassic;
import uno.model.utils.api.GameLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

class AllWildDeckTest {

    private static final int DECK_SIZE = 112;
    private static final int COPIES_PER_TYPE = 14;
    private static final int SAMPLE_SIZE = 15;
    private static final int DISCARD_SIZE = 5;

    private Deck<Card> deck;
    private Game game;

    @BeforeEach
    void setUp() {
        // Setup logger e giocatori
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final AIClassic aiClassic = new AIClassic("AI-Bot");
        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);

        // Inizializziamo il mazzo AllWild
        deck = new AllWildDeck(logger);

        // Inizializziamo una partita fittizia
        final GameRules rules = new GameRulesImpl(false, false, false); // Dummy rules
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "ALLWILD", logger, rules);
    }

    @Test
    void testDeckInitializationSize() {
        // Il mazzo All Wild deve contenere 112 carte (14 copie per 8 tipi)
        assertEquals(DECK_SIZE, deck.size(), "Il mazzo All Wild deve contenere inizialmente 112 carte.");
        assertFalse(deck.isEmpty(), "Il mazzo appena creato non deve essere vuoto.");
    }

    @Test
    void testDrawDecrementsSize() {
        final int initialSize = deck.size();
        final Optional<Card> card = deck.draw();

        assertTrue(card.isPresent(), "Dovrebbe essere possibile pescare una carta.");
        assertEquals(initialSize - 1, deck.size(), "La dimensione del mazzo deve diminuire di 1 dopo una pesca.");
    }

    @Test
    void testDeckComposition() {
        // Estraiamo tutte le carte
        final List<Card> allCards = new ArrayList<>();
        while (!deck.isEmpty()) {
            deck.draw().ifPresent(allCards::add);
        }

        assertEquals(DECK_SIZE, allCards.size());

        // Definiamo i tipi di carte attesi in All Wild
        final CardValue[] expectedValues = {
                CardValue.WILD_ALLWILD, // Classic Wild
                CardValue.WILD_DRAW_FOUR_ALLWILD, // Wild Draw 4
                CardValue.WILD_DRAW_TWO_ALLWILD, // Wild Draw 2
                CardValue.WILD_REVERSE, // Wild Reverse
                CardValue.WILD_SKIP, // Wild Skip (Salto 1)
                CardValue.WILD_SKIP_TWO, // Wild Skip Two (Salto 2)
                CardValue.WILD_FORCED_SWAP, // Forced Swap
                CardValue.WILD_TARGETED_DRAW_TWO, // Targeted Draw 2
        };

        for (final CardValue value : expectedValues) {
            final long count = allCards.stream()
                    .filter(c -> c.getValue(game) == value)
                    .count();

            assertEquals(COPIES_PER_TYPE, count, "Ci devono essere 14 carte di tipo " + value);
        }

        // Verifica che NON ci siano carte colorate normali o altri tipi non previsti
        final long unexpectedCards = allCards.stream()
                .filter(c -> !List.of(expectedValues).contains(c.getValue(game)))
                .count();
        assertEquals(0, unexpectedCards, "Non ci devono essere carte diverse dai tipi Wild specificati.");
    }

    @Test
    void testShuffleChangesOrder() {
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck1 = new AllWildDeck(logger);
        final Deck<Card> deck2 = new AllWildDeck(logger);

        final List<Card> cards1 = new ArrayList<>();
        final List<Card> cards2 = new ArrayList<>();

        // Peschiamo un campione di carte
        for (int i = 0; i < SAMPLE_SIZE; i++) {
            deck1.draw().ifPresent(cards1::add);
            deck2.draw().ifPresent(cards2::add);
        }

        // Verifica che l'ordine sia diverso
        assertNotEquals(cards1, cards2,
                "Due mazzi AllWild mescolati non dovrebbero avere la stessa identica sequenza.");
    }

    @Test
    void testRefill() {
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertEquals(0, deck.size());

        // Simuliamo pila degli scarti
        final List<Card> discardPile = new ArrayList<>();
        final AllWildDeck tempDeck = new AllWildDeck(new uno.model.utils.impl.TestLogger());
        for (int i = 0; i < DISCARD_SIZE; i++) {
            tempDeck.draw().ifPresent(discardPile::add);
        }

        deck.refill(discardPile);

        assertEquals(DISCARD_SIZE, deck.size(), "Il mazzo deve essere ricaricato correttamente.");
        assertFalse(deck.isEmpty());
    }
}
