package uno.view.components.impl;

import uno.model.cards.attributes.CardColor;
import uno.view.api.GameViewObserver;
import uno.view.components.api.ColorChooserPanel;
import uno.view.style.UnoTheme;

import java.util.Optional;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A panel that allows the player to choose a new color during the game.
 * It adapts its color palette based on whether the game is currently on the
 * "Light" or "Dark" side.
 */
@edu.umd.cs.findbugs.annotations.SuppressFBWarnings("SE_BAD_FIELD")
public final class ColorChooserPanelImpl extends JPanel implements ActionListener, ColorChooserPanel {

    private static final long serialVersionUID = 1L;

    private static final Dimension PANEL_SIZE = new Dimension(400, 300);

    private final Optional<GameViewObserver> observer;

    /**
     * Constructs the panel with the appropriate colors.
     *
     * @param observer   The controller to notify when a color is picked.
     * @param isDarkSide True if using Uno Flip dark side colors, false for standard
     *                   colors.
     */
    public ColorChooserPanelImpl(final Optional<GameViewObserver> observer, final boolean isDarkSide) {
        this.observer = observer;

        // 2 rows, 2 columns with 10px gap
        setLayout(new GridLayout(2, 2, 10, 10));
        setBackground(UnoTheme.PANEL_COLOR);

        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Pick a Color",
                TitledBorder.LEFT, TitledBorder.TOP, UnoTheme.TEXT_FONT, UnoTheme.TEXT_COLOR));

        // 1. Determine which colors to display
        final List<CardColor> colorsToOffer = isDarkSide
                ? List.of(CardColor.PINK, CardColor.TEAL, CardColor.ORANGE, CardColor.PURPLE)
                : List.of(CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW);

        // 2. Add buttons dynamically based on the side
        for (final CardColor colorEnum : colorsToOffer) {
            setupButtonForColor(colorEnum, isDarkSide);
        }

        setPreferredSize(PANEL_SIZE);
    }

    /**
     * Helper to map CardColor to AWT colors and add the button to the panel.
     * 
     * @param colorEnum  The CardColor to create a button for.
     * @param isDarkSide Whether the current side is dark (Uno Flip).
     */
    private void setupButtonForColor(final CardColor colorEnum, final boolean isDarkSide) {
        final Color bgColor;
        final String label;

        if (isDarkSide) {
            switch (colorEnum) {
                case PINK:
                    bgColor = UnoTheme.PINK_COLOR;
                    label = "PINK";
                    break;
                case TEAL:
                    bgColor = UnoTheme.TEAL_COLOR;
                    label = "TEAL";
                    break;
                case ORANGE:
                    bgColor = UnoTheme.ORANGE_COLOR;
                    label = "ORANGE";
                    break;
                case PURPLE:
                    bgColor = UnoTheme.PURPLE_COLOR;
                    label = "PURPLE";
                    break;
                default:
                    return;
            }
        } else {
            switch (colorEnum) {
                case RED:
                    bgColor = UnoTheme.BUTTON_COLOR; // Using standard red
                    label = "RED";
                    break;
                case GREEN:
                    bgColor = UnoTheme.GREEN_COLOR;
                    label = "GREEN";
                    break;
                case BLUE:
                    bgColor = UnoTheme.BLUE_COLOR;
                    label = "BLUE";
                    break;
                case YELLOW:
                    bgColor = UnoTheme.YELLOW_COLOR;
                    label = "YELLOW";
                    break;
                default:
                    return;
            }
        }

        // Create Styled Button with specific color and hover effect
        // For hover, we just make it slightly brighter or same for now to keep simple
        add(createButton(label, bgColor, colorEnum));
    }

    private JButton createButton(final String text, final Color bg, final CardColor colorEnum) {
        // Create StyledButton with custom background and a derived hover color
        // (brighter)
        final StyledButtonImpl btn = new StyledButtonImpl(text, bg, bg.brighter());

        // Ensure text is readable (Black for Yellow/Pink/Orange, White for others)
        if (bg.equals(UnoTheme.YELLOW_COLOR) || bg.equals(UnoTheme.PINK_COLOR) || bg.equals(UnoTheme.ORANGE_COLOR)) {
            btn.setForeground(Color.BLACK);
        } else {
            btn.setForeground(Color.WHITE);
        }

        btn.setActionCommand(colorEnum.name());
        btn.addActionListener(this);
        return btn;
    }

    /**
     * Handles button clicks to notify the observer of the chosen color.
     */
    @Override
    public void actionPerformed(final ActionEvent e) {
        final CardColor chosen = CardColor.valueOf(e.getActionCommand());

        observer.ifPresent(obs -> obs.onColorChosen(chosen));

        closeChooser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void closeChooser() {
        Optional.ofNullable(SwingUtilities.getWindowAncestor(this)).ifPresent(Window::dispose);
    }
}
