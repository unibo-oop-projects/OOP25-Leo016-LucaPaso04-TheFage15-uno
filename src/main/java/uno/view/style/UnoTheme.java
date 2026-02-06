package uno.view.style;

import java.awt.Color;
import java.awt.Font;

import javax.swing.border.EmptyBorder;

/**
 * Utility class to hold common styling constants for the application.
 */
public final class UnoTheme {

    // Colors
    public static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    public static final Color PANEL_COLOR = new Color(50, 50, 50);
    public static final Color TEXT_COLOR = Color.WHITE;
    public static final Color DESC_COLOR = new Color(180, 180, 180);
    public static final Color BUTTON_COLOR = new Color(211, 47, 47); // UNO Red
    public static final Color BUTTON_HOVER_COLOR = new Color(255, 82, 82);
    public static final Color BUTTON_TEXT_COLOR = Color.WHITE;

    // Additional Colors
    public static final Color YELLOW_COLOR = new Color(255, 193, 7);
    public static final Color BLUE_COLOR = new Color(33, 150, 243);
    public static final Color GREEN_COLOR = new Color(76, 175, 80);
    public static final Color BLACK_COLOR = Color.BLACK;
    public static final Color PINK_COLOR = new Color(255, 105, 180);
    public static final Color TEAL_COLOR = new Color(0, 128, 128);
    public static final Color ORANGE_COLOR = new Color(255, 140, 0);
    public static final Color PURPLE_COLOR = new Color(153, 50, 204);

    // Fonts
    public static final String FONT_NAME = "Helvetica Neue";
    public static final Font TITLE_FONT = new Font(FONT_NAME, Font.BOLD, 82);
    public static final Font SUBTITLE_FONT = new Font(FONT_NAME, Font.BOLD, 48);
    public static final Font BUTTON_FONT = new Font(FONT_NAME, Font.BOLD, 18);
    public static final Font TEXT_FONT = new Font(FONT_NAME, Font.PLAIN, 14);
    public static final Font TEXT_BOLD_FONT = new Font(FONT_NAME, Font.BOLD, 14);

    // Borders & Dimensions
    public static final int ARC = 20;
    public static final Color BORDER_COLOR = new Color(80, 80, 80);
    public static final Color ACTIVE_BORDER_COLOR = YELLOW_COLOR; // Gold for active turn

    public static final EmptyBorder PADDING_BORDER = new EmptyBorder(20, 40, 20, 40);
    public static final EmptyBorder PANEL_INSETS = new EmptyBorder(10, 10, 10, 10);

    private UnoTheme() {
        // Prevent instantiation
    }
}
