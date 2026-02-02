package uno.view.scenes.impl;

import uno.controller.api.MenuObserver;
import uno.view.scenes.api.MenuScene;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

/**
 * The panel (JPanel) representing the main menu screen.
 * This implementation uses a modern and accessible graphical style.
 */
public class MenuSceneImpl extends JPanel implements MenuScene {

    // Modern Color Palette
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color TITLE_COLOR = Color.WHITE;
    private static final Color BUTTON_COLOR = new Color(211, 47, 47); // UNO Red
    private static final Color BUTTON_HOVER_COLOR = new Color(255, 82, 82);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final int TITLE_FONT_SIZE = 82;
    private static final EmptyBorder TITLE_BORDER = new EmptyBorder(0, 0, 40, 0);
    private static final Dimension RIGID_AREA_DIMENSION = new Dimension(0, 20);
    private static final int ARC = 20;
    private static final int BUTTON_FONT_SIZE = 18;
    private static final Dimension BUTTON_DIMENSION = new Dimension(350, 60);
    private static final EmptyBorder BUTTON_BORDER = new EmptyBorder(10, 20, 10, 20);

    private MenuObserver observer;

    /**
     * Constructs the MenuSceneImpl, setting up the layout and components.
     */
    public MenuSceneImpl() {
        super(new GridBagLayout()); // Use GridBagLayout to center everything
        setBackground(BACKGROUND_COLOR);

        // Inner panel to hold components vertically
        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // Make transparent to show background

        // 1. Title
        final JLabel title = new JLabel("UNO");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(TITLE_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(TITLE_BORDER); // Space below title

        // 2. Create Styled Buttons
        final JButton classicButton = createStyledButton("Classic Mode");
        classicButton.setMnemonic(KeyEvent.VK_C); 

        final JButton flipButton = createStyledButton("Flip Mode");
        flipButton.setMnemonic(KeyEvent.VK_F); 

        final JButton allWildButton = createStyledButton("All Wild Mode");
        allWildButton.setMnemonic(KeyEvent.VK_W);

        // --- Rules Button ---
        final JButton rulesButton = createStyledButton("Game Rules");
        rulesButton.setMnemonic(KeyEvent.VK_R); // Accessibility (Alt+R)

        final JButton quitButton = createStyledButton("Exit Game");
        quitButton.setMnemonic(KeyEvent.VK_E); 

        // 3. Add components to the inner panel
        contentPanel.add(title);
        contentPanel.add(classicButton);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION)); 
        contentPanel.add(flipButton);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));
        contentPanel.add(allWildButton);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION));
        // Add rules button with spacing
        contentPanel.add(rulesButton); 
        contentPanel.add(Box.createRigidArea(RIGID_AREA_DIMENSION)); 

        contentPanel.add(quitButton);

        // 4. Add the inner panel to the main scene (centered by GridBag)
        add(contentPanel, new GridBagConstraints());

        // 5. Link ActionListeners to the Observer
        classicButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartClassicGame();
            }
        });

        flipButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartFlipGame();
            }
        });

        allWildButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartAllWildGame();
            }
        });

        rulesButton.addActionListener(e -> {
            if (observer != null) {
                observer.onOpenRules();
            }
        });

        quitButton.addActionListener(e -> {
            if (observer != null) {
                observer.onQuit();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObserver(final MenuObserver observer) {
        this.observer = observer;
    }

    /**
     * Helper method to create a unified styled button with hover effects.
     * @param text the button text
     * @return the styled JButton
     */
    private JButton createStyledButton(final String text) {
        final JButton button = new JButton(text) {
            @Override
            protected void paintComponent(final Graphics g) {
                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isRollover()) {
                    g2.setColor(BUTTON_HOVER_COLOR);
                } else {
                    g2.setColor(getBackground());
                }

                // Rounded corners (radius 20)
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC); 
                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(new Font("Helvetica Neue", Font.BOLD, BUTTON_FONT_SIZE));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(BUTTON_DIMENSION);
        button.setPreferredSize(BUTTON_DIMENSION);
        button.setMinimumSize(BUTTON_DIMENSION);

        button.setBorder(BUTTON_BORDER);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); 
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}
