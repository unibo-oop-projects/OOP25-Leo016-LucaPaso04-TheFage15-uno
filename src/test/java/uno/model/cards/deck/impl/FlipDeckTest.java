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
import uno.model.cards.attributes.CardColor;
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

class FlipDeckTest {

    private static final int DECK_SIZE = 112;
    private static final int CARDS_PER_COLOR = 26;
    private static final int FLIP_CARDS_PER_COLOR = 2;
    private static final int DRAW_ONE_PER_COLOR = 2;
    private static final int WILD_COUNT = 4;
    private static final int WILD_DRAW_TWO_COUNT = 4;
    private static final int SAMPLE_SIZE = 10;
    private static final int REFILL_SIZE = 10;

    private Deck<Card> deck;
    private Game game;
    private GameLogger logger;

    @BeforeEach
    void setUp() {
        // Mock logger
        logger = new uno.model.utils.impl.TestLogger();

        // Setup base
        final AIClassic aiClassic = new AIClassic("AI-Bot");
        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);

        // Inizializziamo il FlipDeck invece dello StandardDeck
        deck = new FlipDeck(logger);

        // Creiamo una partita fittizia per poter risolvere colori e valori dinamici
        final GameRules rules = new GameRulesImpl(false, false, false, false); // Dummy rules
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "FLIP", logger, rules);
    }

    @Test
    void testDeckInitializationSize() {
        // Un mazzo UNO Flip deve avere 112 carte
        assertEquals(DECK_SIZE, deck.size(), "Il mazzo Flip deve contenere inizialmente 112 carte.");
        assertFalse(deck.isEmpty(), "Il mazzo appena creato non deve essere vuoto.");
    }

    @Test
    void testDrawDecrementsSize() {
        final int initialSize = deck.size();
        final Optional<Card> card = deck.draw();

        assertTrue(card.isPresent(), "Dovrebbe essere possibile pescare una carta da un mazzo pieno.");
        assertEquals(initialSize - 1, deck.size(), "La dimensione del mazzo deve diminuire di 1 dopo una pesca.");
    }

    @Test
    void testLightSideComposition() {
        // Peschiamo tutte le carte per analizzarne la composizione (Lato Chiaro di
        // default)
        final List<Card> allCards = new ArrayList<>();
        while (!deck.isEmpty()) {
            deck.draw().ifPresent(allCards::add);
        }

        assertEquals(DECK_SIZE, allCards.size());

        // 1. Verifica Carte Colorate Standard (Light Side: Rosso, Blu, Verde, Giallo)
        final CardColor[] lightColors = { CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW };

        for (final CardColor color : lightColors) {
            final long count = allCards.stream()
                    .filter(c -> c.getColor(game) == color)
                    .count();
            assertEquals(CARDS_PER_COLOR, count,
                    "Ci devono essere 26 carte per il colore " + color + " nel lato chiaro.");

            // Verifica specifica Carta Flip per colore
            final long flipCount = allCards.stream()
                    .filter(c -> c.getColor(game) == color && c.getValue(game) == CardValue.FLIP)
                    .count();
            assertEquals(FLIP_CARDS_PER_COLOR, flipCount, "Ci devono essere 2 carte Flip per il colore " + color);

            // Verifica specifica Draw One (pesca 1) per colore
            final long drawOneCount = allCards.stream()
                    .filter(c -> c.getColor(game) == color && c.getValue(game) == CardValue.DRAW_ONE)
                    .count();
            assertEquals(DRAW_ONE_PER_COLOR, drawOneCount,
                    "Ci devono essere 2 carte Draw One (+1) per il colore " + color);
        }

        // 2. Verifica Carte Wild (Light Side)
        // 4 Wild + 4 Wild Draw Two = 8 carte speciali nere
        final long wildCount = allCards.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD)
                .count();
        assertEquals(WILD_COUNT, wildCount, "Ci devono essere 4 carte Wild (Cambio Colore).");

        final long wildDrawTwoCount = allCards.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD_DRAW_TWO)
                .count();
        assertEquals(WILD_DRAW_TWO_COUNT, wildDrawTwoCount, "Ci devono essere 4 carte Wild Draw Two (+2).");

        // 3. Totale complessivo
        // 104 (colorate) + 8 (wild) = 112
        assertEquals(DECK_SIZE, allCards.size(), "Il totale delle carte Flip deve essere 112.");
    }

    @Test
    void testShuffleChangesOrder() {
        final Deck<Card> deck1 = new FlipDeck(logger);
        final Deck<Card> deck2 = new FlipDeck(logger);

        final List<Card> cards1 = new ArrayList<>();
        final List<Card> cards2 = new ArrayList<>();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            deck1.draw().ifPresent(cards1::add);
            deck2.draw().ifPresent(cards2::add);
        }

        assertNotEquals(cards1, cards2, "Due mazzi Flip mescolati non dovrebbero essere identici.");
    }

    @Test
    void testRefill() {
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertEquals(0, deck.size());

        final List<Card> discardPile = new ArrayList<>();
        // Creiamo un mazzo temporaneo solo per estrarre carte valide da inserire negli
        // scarti
        final FlipDeck tempDeck = new FlipDeck(logger);
        for (int i = 0; i < REFILL_SIZE; i++) {
            tempDeck.draw().ifPresent(discardPile::add);
        }

        deck.refill(discardPile);

        assertEquals(REFILL_SIZE, deck.size(), "Il mazzo deve essere ricaricato con le carte fornite.");
        assertFalse(deck.isEmpty());
    }
}
