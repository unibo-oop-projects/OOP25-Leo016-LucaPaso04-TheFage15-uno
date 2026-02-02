package uno.view.impl;

import uno.view.api.GameFrame;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * Concrete implementation of the main application window using Java Swing.
 */
public class GameFrameImpl extends JFrame implements GameFrame {

    private static final Dimension MIN_SIZE = new Dimension(1200, 800);

    /**
     * Constructs the main window with standard settings.
     *
     * @param title The title to be displayed in the window's title bar.
     */
    public GameFrameImpl(final String title) {
        super(title);

        // Ensure the application closes when the window is closed
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set the minimum size to ensure UI elements fit correctly
        setMinimumSize(MIN_SIZE);

        // Center the window on the screen
        setLocationRelativeTo(null); 
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void showScene(final JPanel scene) {
        // Set the new panel as the content pane
        setContentPane(scene);

        // Revalidate and repaint to ensure the UI updates immediately
        // (SwingUtilities.invokeLater is good practice for UI updates)
        SwingUtilities.invokeLater(() -> {
            validate();
            repaint();
        });
    }
}
