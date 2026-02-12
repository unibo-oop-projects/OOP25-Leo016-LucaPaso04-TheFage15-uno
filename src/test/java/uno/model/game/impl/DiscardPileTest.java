package uno.model.game.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.DiscardPile;

/**
 * Test class for DiscardPileImpl. It verifies the correct behavior of the discard pile, including adding cards, 
 * retrieving the top card, and taking all cards except the top one.
 */
class DiscardPileTest {

    private DiscardPile discardPile;

    @BeforeEach
    void setUp() {
        discardPile = new DiscardPileImpl();
    }

    @Test
    void testInitialization() {
        assertTrue(discardPile.isEmpty(), "La pila degli scarti deve essere inizialmente vuota.");
        assertEquals(0, discardPile.size());
        assertTrue(discardPile.getTopCard().isEmpty());
    }

    @Test
    void testAddCardAndTopCard() {
        final Card card1 = createDummyCard(CardColor.RED, CardValue.ONE);
        final Card card2 = createDummyCard(CardColor.BLUE, CardValue.TWO);

        discardPile.addCard(card1);
        assertFalse(discardPile.isEmpty());
        assertEquals(1, discardPile.size());
        assertTrue(discardPile.getTopCard().isPresent());
        assertEquals(card1, discardPile.getTopCard().get(), "La carta in cima deve essere quella appena aggiunta.");

        discardPile.addCard(card2);
        assertEquals(2, discardPile.size());
        assertEquals(card2, discardPile.getTopCard().get(), "La nuova carta deve diventare la Top Card.");
    }

    @Test
    void testAddNullCardThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> discardPile.addCard(null),
                "Aggiungere null deve lanciare un'eccezione.");
    }

    @Test
    void testTakeAllExceptTop() {
        final Card c1 = createDummyCard(CardColor.RED, CardValue.ONE);
        final Card c2 = createDummyCard(CardColor.BLUE, CardValue.TWO);
        final Card c3 = createDummyCard(CardColor.GREEN, CardValue.THREE);

        discardPile.addCard(c1);
        discardPile.addCard(c2);
        discardPile.addCard(c3);

        assertEquals(3, discardPile.size());

        final List<Card> recycledCards = discardPile.takeAllExceptTop();

        assertEquals(2, recycledCards.size(), "Deve restituire 2 carte (3 totali - 1 top).");
        assertTrue(recycledCards.contains(c1));
        assertTrue(recycledCards.contains(c2));
        assertFalse(recycledCards.contains(c3), "La Top Card non deve essere riciclata.");

        assertEquals(1, discardPile.size(), "La pila deve rimanere con 1 sola carta.");
        assertEquals(c3, discardPile.getTopCard().get(), "La Top Card originale deve rimanere in cima.");
    }

    @Test
    void testTakeAllExceptTopWithSingleCard() {
        final Card c1 = createDummyCard(CardColor.RED, CardValue.ONE);
        discardPile.addCard(c1);

        final List<Card> recycledCards = discardPile.takeAllExceptTop();

        assertTrue(recycledCards.isEmpty(), "Se c'è solo una carta, non c'è nulla da riciclare.");
        assertEquals(1, discardPile.size());
        assertEquals(c1, discardPile.getTopCard().get());
    }

    @Test
    void testSnapshotIsDefensive() {
        discardPile.addCard(createDummyCard(CardColor.RED, CardValue.ONE));

        final List<Card> snapshot = discardPile.getSnapshot();
        assertEquals(1, snapshot.size());

        snapshot.clear();
        assertEquals(1, discardPile.size(), "Il metodo getSnapshot() deve restituire una copia difensiva.");
    }

    /**
     * Creates a dummy card with the specified color and value. This is a helper method to simplify test setup.
     * 
     * @param color the color of the card
     * @param value the value of the card
     * @return a new Card instance with the given color and value
     */
    private Card createDummyCard(final CardColor color, final CardValue value) {
        return new DoubleSidedCard(
                new NumericBehavior(color, value),
                BackSideBehavior.getInstance());
    }
}
