package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BackSideBehaviorTest {

    @Test
    void testSingleton() {
        BackSideBehavior i1 = BackSideBehavior.getInstance();
        BackSideBehavior i2 = BackSideBehavior.getInstance();
        assertNotNull(i1);
        assertEquals(i1, i2);
    }

    @Test
    void testGetColor_ThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            BackSideBehavior.getInstance().getColor();
        });
    }

    @Test
    void testGetValue_ThrowsException() {
        assertThrows(UnsupportedOperationException.class, () -> {
            BackSideBehavior.getInstance().getValue();
        });
    }

    @Test
    void testPerformEffect_ThrowsException() {
        assertThrows(IllegalStateException.class, () -> {
            BackSideBehavior.getInstance().executeEffect(null);
        });
    }
}
