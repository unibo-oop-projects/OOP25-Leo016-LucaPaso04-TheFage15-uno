package uno.model.players.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HumanPlayerTest {

    @Test
    void testConstructor() {
        HumanPlayer p = new HumanPlayer("Test");
        assertEquals("Test", p.getName());
    }

    @Test
    void testTakeTurn() {
        HumanPlayer p = new HumanPlayer("Test");
        // Verify it doesn't throw and does nothing interactive (passive)
        p.takeTurn(null);
    }
}
