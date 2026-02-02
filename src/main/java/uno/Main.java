package uno;

import uno.controller.impl.MenuControllerImpl;
import uno.view.impl.GameFrameImpl;
import uno.view.scenes.impl.MenuSceneImpl;

import javax.swing.SwingUtilities;

/**
 * Entry point of the UNO application.
 */
public final class Main {

    private Main() {
        // Prevent instantiation
    }

    /**
     * Main method to launch the UNO application.
     * @param args command line arguments
     */
    public static void main(final String[] args) {

        SwingUtilities.invokeLater(() -> {

            final GameFrameImpl frame = new GameFrameImpl("UNO");
            final MenuControllerImpl menuController = new MenuControllerImpl(frame);
            final MenuSceneImpl menuScene = new MenuSceneImpl();

            menuScene.setObserver(menuController);
            frame.showScene(menuScene);

            frame.setVisible(true);
        });
    }
}
