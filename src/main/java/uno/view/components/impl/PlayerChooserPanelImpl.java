package uno.view.components.impl;

import uno.model.players.impl.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.view.api.GameViewObserver;
import uno.view.components.api.PlayerChooserPanel;
import uno.view.style.UnoTheme;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A panel that prompts the current player to choose a target opponent.
 * Used for specific cards (e.g., "Swap Hands" or targeted draws).
 */
@SuppressFBWarnings({ "SE_BAD_FIELD", "EI_EXPOSE_REP2" })
public final class PlayerChooserPanelImpl extends JPanel implements ActionListener, PlayerChooserPanel {

    private static final long serialVersionUID = 1L;

    private static final Dimension PANEL_SIZE = new Dimension(400, 300);
    private static final Dimension BUTTON_SIZE = new Dimension(350, 50);
    private static final Dimension SPACER_SIZE = new Dimension(0, 5);

    private final Optional<GameViewObserver> observer;
    private final List<AbstractPlayer> availableOpponents;

    /**
     * Constructs the player chooser panel.
     *
     * @param observer  The controller to notify when a player is picked.
     * @param opponents The list of valid target players (usually excluding the
     *                  current player).
     */
    public PlayerChooserPanelImpl(final Optional<GameViewObserver> observer, final List<AbstractPlayer> opponents) {
        this.observer = observer;
        this.availableOpponents = opponents;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(UnoTheme.PANEL_COLOR);

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Choose Opponent",
                TitledBorder.LEFT, TitledBorder.TOP, UnoTheme.TEXT_FONT, UnoTheme.TEXT_COLOR));

        add(Box.createVerticalGlue());

        for (final AbstractPlayer opponent : opponents) {

            if (opponent instanceof HumanPlayer) {
                continue;
            }

            final JButton button = createStyledButton(opponent.getName());
            button.setActionCommand(opponent.getName());
            button.addActionListener(this);

            add(button);
            add(Box.createRigidArea(SPACER_SIZE));
        }

        add(Box.createVerticalGlue());

        setPreferredSize(PANEL_SIZE);
    }

    /**
     * Helper method to create a consistent styled button.
     * 
     * @param text The button label.
     * @return The styled JButton.
     */
    private JButton createStyledButton(final String text) {
        final StyledButtonImpl button = new StyledButtonImpl(text);
        button.setSize(BUTTON_SIZE.width, BUTTON_SIZE.height);
        return button;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final String chosenName = e.getActionCommand();

        final Optional<AbstractPlayer> chosenPlayer = availableOpponents.stream()
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
