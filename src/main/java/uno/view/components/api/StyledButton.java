package uno.view.components.api;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionListener;
import javax.swing.JComponent;

/**
 * Interface defining the behavior of a StyledButton.
 */
public interface StyledButton {

    /**
     * Sets the mnemonic key.
     * 
     * @param mnemonic key code
     */
    void setMnemonic(int mnemonic);

    /**
     * Sets the text of the button.
     * 
     * @param text text
     */
    void setText(String text);

    /**
     * Adds an action listener.
     * 
     * @param l listener
     */
    void addActionListener(ActionListener l);

    /**
     * Sets enabled state.
     * 
     * @param b enabled
     */
    void setEnabled(boolean b);

    /**
     * Sets visible state.
     * 
     * @param b visible
     */
    void setVisible(boolean b);

    /**
     * Sets preferred size.
     * 
     * @param d dimension
     */
    void setPreferredSize(Dimension d);

    /**
     * Sets font.
     * 
     * @param f font
     */
    void setFont(Font f);

    /**
     * Sets foreground color.
     * 
     * @param c color
     */
    void setForeground(Color c);

    /**
     * Sets cursor.
     * 
     * @param c cursor
     */
    void setCursor(Cursor c);

    /**
     * Sets margin.
     * 
     * @param m insets
     */
    void setMargin(Insets m);

    /**
     * Sets the size explicitly.
     * 
     * @param width  width
     * @param height height
     */
    void setSize(int width, int height);

    /**
     * Returns the underlying Swing component.
     * 
     * @return JComponent
     */
    JComponent getComponent();
}
