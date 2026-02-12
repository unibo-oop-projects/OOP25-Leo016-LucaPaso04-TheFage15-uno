package uno;

import uno.controller.api.MenuController;
import uno.controller.impl.MenuControllerImpl;
import uno.view.api.GameFrame;
import uno.view.impl.GameFrameImpl;
import uno.view.scenes.api.MenuScene;
import uno.view.scenes.impl.MenuSceneImpl;

import javax.swing.SwingUtilities;
import java.awt.Container;

/**
 * Entry point of the UNO application.
 */
public final class Main {

    private Main() {
        // Prevent instantiation
    }

    /**
     * Main method to launch the UNO application.
     * 
     * @param args command line arguments
     */
    public static void main(final String[] args) {

        SwingUtilities.invokeLater(() -> {

            final GameFrame frame = new GameFrameImpl("UNO");
            final MenuController menuController = new MenuControllerImpl(frame);
            final MenuScene menuScene = new MenuSceneImpl();

            menuScene.setObserver(menuController);
            frame.showScene((Container) menuScene);

            frame.setVisible(true);
        });
    }
}
