package uno.view.components.impl;

import uno.controller.api.GameViewObserver;
import uno.model.players.api.Player;
import uno.view.components.api.PlayerChooserPanel;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;

/**
 * A panel that prompts the current player to choose a target opponent.
 * Used for specific cards (e.g., "Swap Hands" or targeted draws).
 */
public class PlayerChooserPanelImpl extends JPanel implements ActionListener, PlayerChooserPanel {

    // UI Styling constants
    private static final Color PANEL_BACKGROUND = new Color(50, 50, 50);
    private static final Color TITLE_TEXT_COLOR = Color.WHITE;
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 14);

    private static final Dimension PANEL_SIZE = new Dimension(220, 150);
    private static final Dimension BUTTON_SIZE = new Dimension(200, 40);
    private static final Dimension SPACER_SIZE = new Dimension(0, 5);

    private final Optional<GameViewObserver> observer;
    private final List<Player> availableOpponents;

    /**
     * Constructs the player chooser panel.
     *
     * @param observer  The controller to notify when a player is picked.
     * @param opponents The list of valid target players (usually excluding the current player).
     */
    public PlayerChooserPanelImpl(final Optional<GameViewObserver> observer, final List<Player> opponents) {
        this.observer = observer;
        this.availableOpponents = opponents;

        // Vertical layout for the list of buttons
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(PANEL_BACKGROUND);

        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Choose Opponent",
            TitledBorder.LEFT, TitledBorder.TOP, UI_FONT, TITLE_TEXT_COLOR
        ));

        // Create a button for each valid opponent
        for (final Player opponent : opponents) {

            // Logic from original code: specific filtering based on class type.
            // Assuming this filters out base 'Player' objects (e.g., Humans) if needed.
            if (opponent.getClass().equals(Player.class)) {
                continue; 
            }

            // Create and add the button
            final JButton button = createStyledButton(opponent.getName());
            button.setActionCommand(opponent.getName()); // Use name as ID
            button.addActionListener(this);

            add(button);
            add(Box.createRigidArea(SPACER_SIZE)); // Spacer between buttons
        }

        // Set a preferred size suitable for a list
        setPreferredSize(PANEL_SIZE);
    }

    /**
     * Helper method to create a consistent styled button.
     * @param text The button label.
     * @return The styled JButton.
     */
    private JButton createStyledButton(final String text) {
        final JButton button = new JButton(text);
        button.setFont(UI_FONT);
        button.setBackground(Color.DARK_GRAY);
        button.setForeground(Color.WHITE);

        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        // Fixed button size for uniformity
        final Dimension btnSize = BUTTON_SIZE;
        button.setMaximumSize(btnSize);
        button.setPreferredSize(btnSize);

        return button;
    }

    /** 
     * Handles button clicks to notify the observer of the chosen player.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final String chosenName = e.getActionCommand();

        final Optional<Player> chosenPlayer = availableOpponents.stream()
                .filter(p -> p.getName().equals(chosenName))
                .findFirst();

        chosenPlayer.ifPresent(player -> {
            observer.ifPresent(obs -> obs.onPlayerChosen(player));
            closeChooser();
        });
    }

    /** 
     * {@inheritDoc}
     */
    @Override
    public void closeChooser() {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(this)).ifPresent(Window::dispose);
    }
}
