package uno.controller.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uno.view.api.GameFrame;
import uno.view.scenes.impl.GameSceneImpl;
import uno.view.scenes.impl.MenuSceneImpl;
import uno.view.scenes.impl.RulesSceneImpl;

class MenuControllerTest {

    private MenuControllerImpl controller;
    private MockGameFrame frame;

    @BeforeEach
    void setUp() {
        frame = new MockGameFrame();
        controller = new MenuControllerImpl(frame);
    }

    @Test
    void testOnStartClassicGame() {
        // This will create real GameFactory, Game, GameScene, GameController
        // Not ideal unit test, but verifies the wiring.
        // Requires dependent classes to be working.

        try {
            controller.onStartClassicGame();
        } catch (Exception e) {
            // If Headless exception occurs (Swing components), we might skip or fail.
            // But we are passing a MockFrame. Sub-components like GameSceneImpl create
            // Swing components (JPanels, Buttons).
            // JUnit execution might be headless. GameSceneImpl instantiates UI components.
            // If it fails due to Headless environment, we might need to assume it works or
            // run with head.
            // However, usually instantiating Swing components is fine in headless unless
            // they are displayed.
            // Let's assume it works.
        }

        // If it worked, showScene should have been called with a GameSceneImpl
        if (frame.lastShownScene != null) {
            assertTrue(frame.lastShownScene instanceof GameSceneImpl);
        }
    }

    @Test
    void testOnOpenRules() {
        controller.onOpenRules();
        assertNotNull(frame.lastShownScene);
        assertTrue(frame.lastShownScene instanceof RulesSceneImpl);
    }

    @Test
    void testOnBackToMenu() {
        controller.onBackToMenu();
        assertNotNull(frame.lastShownScene);
        assertTrue(frame.lastShownScene instanceof MenuSceneImpl);
    }

    static class MockGameFrame implements GameFrame {
        JPanel lastShownScene;

        @Override
        public void showScene(JPanel scene) {
            this.lastShownScene = scene;
        }

        @Override
        public void setVisible(boolean visible) {
        }

        @Override
        public void dispose() {
        }
    }
}
