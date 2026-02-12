package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Test class for {@link BackSideBehavior}.
 */
class BackSideBehaviorTest {

    @Test
    void testSingleton() {
        final BackSideBehavior i1 = BackSideBehavior.getInstance();
        final BackSideBehavior i2 = BackSideBehavior.getInstance();
        assertNotNull(i1);
        assertEquals(i1, i2);
    }

    @Test
    void testGetColorThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            BackSideBehavior.getInstance().getColor();
        });
    }

    @Test
    void testGetValueThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            BackSideBehavior.getInstance().getValue();
        });
    }

    @Test
    void testPerformEffectThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            BackSideBehavior.getInstance().executeEffect(null);
        });
    }
}
