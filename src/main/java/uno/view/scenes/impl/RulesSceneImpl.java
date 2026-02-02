package uno.view.scenes.impl;

import uno.controller.api.MenuObserver;
import uno.view.scenes.api.RulesScene;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.util.Optional;

/**
 * The panel (JPanel) representing the Rules Configuration screen.
 * Maintains the same modern graphical style as the MenuScene.
 */
public class RulesSceneImpl extends JPanel implements RulesScene {

    // Color Palette (Consistent with MenuScene)
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color DESC_COLOR = new Color(180, 180, 180); // Light gray for descriptions
    private static final Color BUTTON_COLOR = new Color(211, 47, 47);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;
    private static final EmptyBorder PADDING_BORDER = new EmptyBorder(20, 40, 20, 40);
    private static final int TITLE_FONT_SIZE = 48;
    private static final Dimension RIGID_AREA_1 = new Dimension(0, 40);
    private static final Dimension RIGID_AREA_2 = new Dimension(0, 20);
    private static final Dimension RIGID_AREA_3 = new Dimension(0, 50);
    private static final Dimension PANEL_DIMENSION = new Dimension(600, 80);
    private static final Color PANEL_COLOR = new Color(80, 80, 80);
    private static final int LBL_TITLE_FONT_SIZE = 18;
    private static final int BUTTON_FONT_SIZE = 18;
    private static final int LBL_DESC_FONT_SIZE = 12;
    private static final Dimension TEXT_PANEL_RIGID_AREA = new Dimension(0, 5);
    private static final Color BUTTON_OVER_COLOR = new Color(255, 82, 82);
    private static final int ARC = 20;
    private static final Dimension BUTTON_DIMENSION = new Dimension(350, 60);
    private static final EmptyBorder BUTTON_BORDER = new EmptyBorder(10, 20, 10, 20);

    // Input components
    private final JCheckBox unoPenaltyCheck;
    private final JCheckBox skipAfterDrawCheck;
    private final JCheckBox mandatoryPassCheck;

    private Optional<MenuObserver> observer;

    /**
     * Constructs the RulesSceneImpl panel with all GUI components.
     */
    public RulesSceneImpl() {
        super(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(PADDING_BORDER);

        // 1. Title
        final JLabel title = new JLabel("House Rules");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, TITLE_FONT_SIZE));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. Create Rule Panels

        // Rule 1: UNO Penalty (Default: On)
        final JPanel rule1 = createRulePanel(
            "UNO Penalty", 
            "If DISABLED, players are not required to shout 'UNO!' when holding one card.",
            true 
        );
        unoPenaltyCheck = (JCheckBox) rule1.getClientProperty("checkbox");

        // Rule 2: Skip after Draw (Default: Off)
        final JPanel rule2 = createRulePanel(
            "Skip After Draw", 
            "If ENABLED, a player cannot play a card immediately after drawing it.",
            false
        );
        skipAfterDrawCheck = (JCheckBox) rule2.getClientProperty("checkbox");

        // Rule 3: Mandatory Pass / No Reshuffle (Default: Off)
        final JPanel rule3 = createRulePanel(
            "No Reshuffle (Hardcore)", 
            "If the draw deck is empty, the game ends in a draw (discard pile is not reshuffled).",
            false
        );
        mandatoryPassCheck = (JCheckBox) rule3.getClientProperty("checkbox");

        // 3. "Save and Back" Button
        final JButton backButton = createStyledButton("Save & Back to Menu");
        backButton.setMnemonic(KeyEvent.VK_B);

        backButton.addActionListener(e -> {
            if (observer.isPresent()) {
                // Determine logic to go back. 
                // Assuming MenuObserver has a method for this, 
                // otherwise the Controller handles scene switching manually.

                // For now, we assume the controller will read the values via the interface getters
                // when starting the next game.

                // Example call: observer.onBackToMenu(); 
                // Since onBackToMenu isn't in your snippet of MenuObserver, 
                // you might need to add it or use an existing method.
                System.out.println("Settings saved. Returning to menu...");

                // Temporary fallback if method is missing in interface:
                // observer.onQuit(); // Or specific navigation method
            }
        });

        // 4. Assembly
        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_1)); 
        contentPanel.add(rule1);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_2));
        contentPanel.add(rule2);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_2));
        contentPanel.add(rule3);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_3)); 
        contentPanel.add(backButton);

        add(contentPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObserver(final MenuObserver observer) {
        this.observer = Optional.of(observer);
    }

    // --- API Implementation: Getters for the Controller ---

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnoPenaltyEnabled() {
        return unoPenaltyCheck.isSelected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSkipAfterDrawEnabled() {
        return skipAfterDrawCheck.isSelected();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMandatoryPassEnabled() {
        return mandatoryPassCheck.isSelected();
    }

    // --- GUI Helper Methods ---

    /**
     * Creates a horizontal panel containing Title+Description on the left and a Checkbox on the right.
     * @param titleText The title of the rule.
     * @param descText The description of the rule.
     * @param defaultState The default state of the checkbox.
     * @return The constructed JPanel.
     */
    private JPanel createRulePanel(final String titleText, final String descText, final boolean defaultState) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(PANEL_DIMENSION);
        panel.setPreferredSize(PANEL_DIMENSION);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, PANEL_COLOR)); // Separator line

        // Left Side: Text
        final JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        final JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Helvetica Neue", Font.BOLD, LBL_TITLE_FONT_SIZE));
        lblTitle.setForeground(TEXT_COLOR);

        final JLabel lblDesc = new JLabel("<html><body style='width: 450px'>" + descText + "</body></html>");
        lblDesc.setFont(new Font("Helvetica Neue", Font.PLAIN, LBL_DESC_FONT_SIZE));
        lblDesc.setForeground(DESC_COLOR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createRigidArea(TEXT_PANEL_RIGID_AREA));
        textPanel.add(lblDesc);

        // Right Side: Checkbox
        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setSelected(defaultState);

        // Store checkbox reference in the panel for retrieval during construction
        panel.putClientProperty("checkbox", checkBox);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(checkBox, BorderLayout.EAST);

        return panel;
    }

    /**
     * Creates a styled button consistent with MenuScene.
     * @param text The button text.
     * @return The styled JButton.
     */
    private JButton createStyledButton(final String text) {
        final JButton button = new JButton(text) {
            @Override
            protected void paintComponent(final Graphics g) {
                final Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(BUTTON_OVER_COLOR);
                } else {
                    g2.setColor(BUTTON_COLOR);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), ARC, ARC);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setFont(new Font("Helvetica Neue", Font.BOLD, BUTTON_FONT_SIZE));
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(BUTTON_DIMENSION);
        button.setPreferredSize(BUTTON_DIMENSION);
        button.setBorder(BUTTON_BORDER);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
