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
 * Test for the AllWildDeck class, ensuring it initializes correctly, maintains the expected composition of cards,
 * and behaves as expected when drawing and refilling.
 */
class AllWildDeckTest {

    private static final int DECK_SIZE = 112;
    private static final int COPIES_PER_TYPE = 14;
    private static final int SAMPLE_SIZE = 15;
    private static final int DISCARD_SIZE = 5;

    private Deck<Card> deck;
    private Game game;

    @BeforeEach
    void setUp() {
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final AIClassic aiClassic = new AIClassic("AI-Bot");
        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic);

        deck = new AllWildDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false); // Dummy rules
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "ALLWILD", logger, rules);
    }

    @Test
    void testDeckInitializationSize() {
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
        final List<Card> allCards = new ArrayList<>();
        while (!deck.isEmpty()) {
            deck.draw().ifPresent(allCards::add);
        }

        assertEquals(DECK_SIZE, allCards.size());

        final CardValue[] expectedValues = {
                CardValue.WILD_ALLWILD,
                CardValue.WILD_DRAW_FOUR_ALLWILD,
                CardValue.WILD_DRAW_TWO_ALLWILD,
                CardValue.WILD_REVERSE,
                CardValue.WILD_SKIP,
                CardValue.WILD_SKIP_TWO,
                CardValue.WILD_FORCED_SWAP,
                CardValue.WILD_TARGETED_DRAW_TWO,
        };

        for (final CardValue value : expectedValues) {
            final long count = allCards.stream()
                    .filter(c -> c.getValue(game) == value)
                    .count();

            assertEquals(COPIES_PER_TYPE, count, "Ci devono essere 14 carte di tipo " + value);
        }

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

        for (int i = 0; i < SAMPLE_SIZE; i++) {
            deck1.draw().ifPresent(cards1::add);
            deck2.draw().ifPresent(cards2::add);
        }

        assertNotEquals(cards1, cards2,
                "Due mazzi AllWild mescolati non dovrebbero avere la stessa identica sequenza.");
    }

    @Test
    void testRefill() {
        while (!deck.isEmpty()) {
            deck.draw();
        }
        assertEquals(0, deck.size());

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
