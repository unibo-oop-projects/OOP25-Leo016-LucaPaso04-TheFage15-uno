package uno.model.players.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link HumanPlayer}.
 */
class HumanPlayerTest {

    private static final String PLAYER_NAME = "TestPlayer";

    @Test
    void testConstructor() {
        final HumanPlayer p = new HumanPlayer(PLAYER_NAME);
        assertEquals(PLAYER_NAME, p.getName());
    }

    @Test
    void testTakeTurn() {
        final HumanPlayer p = new HumanPlayer(PLAYER_NAME);
        p.takeTurn(null);
    }
}
