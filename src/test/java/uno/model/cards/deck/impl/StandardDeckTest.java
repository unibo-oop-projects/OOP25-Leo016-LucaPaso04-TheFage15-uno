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
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

/**
 * Test for the StandardDeck class, ensuring correct initialization, drawing, 
 * peeking, composition, shuffling, and refilling behavior.
 */
class StandardDeckTest {

    private static final int DECK_SIZE = 108;
    private static final int CARDS_PER_COLOR = 25;
    private static final int WILD_COUNT = 4;
    private static final int WILD_DRAW_FOUR_COUNT = 4;
    private static final int SAMPLE_SIZE = 10;
    private static final int REFILL_SIZE = 5;

    private Deck<Card> deck;
    private Game game;
    private GameLogger logger;

    @BeforeEach
    void setUp() {
        logger = new uno.model.utils.impl.TestLogger();

        final AIClassic aiClassic = new AIClassic("AI-Bot");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);
        deck = new StandardDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false); // Dummy rules
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);

        game = new GameImpl(deck, players, turnManager, discardPile, "CLASSIC", logger, rules);
    }

    @Test
    void testDeckInitializationSize() {
        assertEquals(DECK_SIZE, deck.size(), "Il mazzo standard deve contenere inizialmente 108 carte.");
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
    void testPeekDoesNotRemoveCard() {
        final int initialSize = deck.size();
        final Optional<Card> peekedCard = deck.peek();
        final Optional<Card> drawnCard = deck.draw();

        assertEquals(initialSize - 1, deck.size(), "Peek non dovrebbe rimuovere carte, ma draw sì.");
        assertTrue(peekedCard.isPresent());
        assertTrue(drawnCard.isPresent());
        assertEquals(peekedCard.get(), drawnCard.get(),
                "Peek deve restituire la stessa carta che verrebbe pescata con draw.");
    }

    @Test
    void testDeckComposition() {
        final List<Card> allCards = new ArrayList<>();
        while (!deck.isEmpty()) {
            deck.draw().ifPresent(allCards::add);
        }

        final CardColor[] standardColors = {CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW };

        for (final CardColor color : standardColors) {
            final long count = allCards.stream()
                    .filter(c -> c.getColor(game) == color)
                    .count();
            assertEquals(CARDS_PER_COLOR, count, "Ci devono essere 25 carte per il colore " + color);
        }

        final long wildCount = allCards.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD)
                .count();
        assertEquals(WILD_COUNT, wildCount, "Ci devono essere 4 carte Wild (Cambio Colore).");

        final long wildDrawFourCount = allCards.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD_DRAW_FOUR)
                .count();
        assertEquals(WILD_DRAW_FOUR_COUNT, wildDrawFourCount, "Ci devono essere 4 carte Wild Draw Four (+4).");

        assertEquals(DECK_SIZE, allCards.size(), "Il totale delle carte deve essere 108");
    }

    @Test
    void testShuffleChangesOrder() {
        final StandardDeck deck1 = new StandardDeck(logger);
        final StandardDeck deck2 = new StandardDeck(logger);

        final List<Card> cards1 = new ArrayList<>();
        final List<Card> cards2 = new ArrayList<>();

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            deck1.draw().ifPresent(cards1::add);
            deck2.draw().ifPresent(cards2::add);
        }

        assertNotEquals(cards1, cards2,
                "Due mazzi mescolati non dovrebbero avere la stessa identica sequenza di carte.");
    }

    @Test
    void testRefill() {
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertEquals(0, deck.size());

        final List<Card> discardPile = new ArrayList<>();
        final StandardDeck tempDeck = new StandardDeck(logger);
        for (int i = 0; i < REFILL_SIZE; i++) {
            tempDeck.draw().ifPresent(discardPile::add);
        }

        deck.refill(discardPile);

        assertEquals(REFILL_SIZE, deck.size(), "Il mazzo dovrebbe contenere le carte ricaricate.");
        assertFalse(deck.isEmpty());
    }
}
