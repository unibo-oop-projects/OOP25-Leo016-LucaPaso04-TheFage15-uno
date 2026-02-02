package uno.view.components.impl;

import uno.controller.api.GameViewObserver;
import uno.model.cards.attributes.CardColor;
import uno.view.components.api.ColorChooserPanel;
import java.util.Optional;

import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * A panel that allows the player to choose a new color during the game.
 * It adapts its color palette based on whether the game is currently on the "Light" or "Dark" side.
 */
public class ColorChooserPanelImpl extends JPanel implements ActionListener, ColorChooserPanel {

    private static final Color PANEL_BACKGROUND = new Color(50, 50, 50);
    private static final Color TITLE_TEXT_COLOR = Color.WHITE;
    private static final Font UI_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Dimension PANEL_SIZE = new Dimension(220, 150);

    private static final Color PINK_COLOR = new Color(255, 105, 180);
    private static final Color TEAL_COLOR = new Color(0, 128, 128);
    private static final Color ORANGE_COLOR = new Color(255, 140, 0);
    private static final Color PURPLE_COLOR = new Color(153, 50, 204);

    private static final Color RED_COLOR = new Color(211, 47, 47);
    private static final Color GREEN_COLOR = new Color(76, 175, 80);
    private static final Color BLUE_COLOR = new Color(33, 150, 243);
    private static final Color YELLOW_COLOR = new Color(255, 235, 59);

    private final Optional<GameViewObserver> observer;

    /**
     * Constructs the panel with the appropriate colors.
     *
     * @param observer   The controller to notify when a color is picked.
     * @param isDarkSide True if using Uno Flip dark side colors, false for standard colors.
     */
    public ColorChooserPanelImpl(final Optional<GameViewObserver> observer, final boolean isDarkSide) {
        this.observer = observer;

        // 2 rows, 2 columns with 10px gap
        setLayout(new GridLayout(2, 2, 10, 10));
        setBackground(PANEL_BACKGROUND);

        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Pick a Color",
            TitledBorder.LEFT, TitledBorder.TOP, UI_FONT, TITLE_TEXT_COLOR
        ));

        // 1. Determine which colors to display
        final List<CardColor> colorsToOffer = isDarkSide 
            ? List.of(CardColor.PINK, CardColor.TEAL, CardColor.ORANGE, CardColor.PURPLE) 
            :   List.of(CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW);

        // 2. Add buttons dynamically based on the side
        for (final CardColor colorEnum : colorsToOffer) {
            setupButtonForColor(colorEnum, isDarkSide);
        }

        setPreferredSize(PANEL_SIZE);
    }

    /**
     * Helper to map CardColor to AWT colors and add the button to the panel.
     * @param colorEnum The CardColor to create a button for.
     * @param isDarkSide Whether the current side is dark (Uno Flip).
     */
    private void setupButtonForColor(final CardColor colorEnum, final boolean isDarkSide) {
        final Color bgColor;
        Color fgColor = Color.WHITE; // Default text
        final String label;

        if (isDarkSide) {
            switch (colorEnum) {
                case PINK:   bgColor = PINK_COLOR; fgColor = Color.BLACK; label = "PINK"; break;
                case TEAL:   bgColor = TEAL_COLOR; label = "TEAL"; break;
                case ORANGE: bgColor = ORANGE_COLOR; fgColor = Color.BLACK; label = "ORANGE"; break;
                case PURPLE: bgColor = PURPLE_COLOR; label = "PURPLE"; break;
                default: return;
            }
        } else {
            switch (colorEnum) {
                case RED:    bgColor = RED_COLOR; label = "RED"; break;
                case GREEN:  bgColor = GREEN_COLOR; label = "GREEN"; break;
                case BLUE:   bgColor = BLUE_COLOR; label = "BLUE"; break;
                case YELLOW: bgColor = YELLOW_COLOR; fgColor = Color.BLACK; label = "YELLOW"; break;
                default: return;
            }
        }

        add(createButton(label, bgColor, fgColor, colorEnum));
    }

    private JButton createButton(final String text, final Color bg, final Color fg, final CardColor colorEnum) {
        final JButton btn = new JButton(text);
        btn.setFont(UI_FONT);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setOpaque(true);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

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
