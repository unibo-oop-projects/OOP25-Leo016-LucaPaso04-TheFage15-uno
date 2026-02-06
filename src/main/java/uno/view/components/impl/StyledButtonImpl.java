package uno.view.components.impl;

import uno.view.components.api.StyledButton;
import uno.view.style.UnoTheme;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * A custom JButton that implements the application's visual theme.
 */
public final class StyledButtonImpl extends JButton implements StyledButton {

    private static final long serialVersionUID = 1L;
    private static final Dimension DEFAULT_DIMENSION = new Dimension(350, 60);
    private static final EmptyBorder DEFAULT_BORDER = new EmptyBorder(10, 20, 10, 20);

    private Color normalColor;
    private Color hoverColor;
    private int arc;

    /**
     * Constructs a StyledButton with default theme colors.
     * 
     * @param text The text to display on the button.
     */
    public StyledButtonImpl(final String text) {
        this(text, UnoTheme.BUTTON_COLOR, UnoTheme.BUTTON_HOVER_COLOR);
    }

    /**
     * Constructs a StyledButton with custom colors.
     * 
     * @param text        The text to display.
     * @param normalColor The background color in normal state.
     * @param hoverColor  The background color in hover state.
     */
    public StyledButtonImpl(final String text, final Color normalColor, final Color hoverColor) {
        super(text);
        this.normalColor = normalColor;
        this.hoverColor = hoverColor;
        this.arc = UnoTheme.ARC;

        setFont(UnoTheme.BUTTON_FONT);
        setForeground(UnoTheme.BUTTON_TEXT_COLOR);
        setBackground(normalColor);

        // Defaults
        setPreferredSize(DEFAULT_DIMENSION);
        setBorder(DEFAULT_BORDER);

        setFocusPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setAlignmentX(CENTER_ALIGNMENT);
    }

    /**
     * Implementation of paintComponent to draw rounded corners and handle hover
     * state.
     */
    @Override
    protected void paintComponent(final Graphics g) {
        final Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isRollover()) {
            g2.setColor(hoverColor);
        } else {
            g2.setColor(normalColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        super.paintComponent(g);
        g2.dispose();
    }

    /**
     * Sets the preferred dimensions of the button.
     * 
     * @param width  width
     * @param height height
     */
    @Override
    public void setSize(final int width, final int height) {
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
        setMinimumSize(new Dimension(width, height));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JComponent getComponent() {
        return this;
    }
}
