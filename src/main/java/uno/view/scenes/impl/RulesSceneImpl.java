package uno.view.scenes.impl;

import uno.controller.api.MenuObserver;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;
import uno.view.components.api.StyledButton;
import uno.view.components.impl.StyledButtonImpl;
import uno.view.scenes.api.RulesScene;
import uno.view.style.UnoTheme;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.KeyEvent;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * The panel (JPanel) representing the Rules Configuration screen.
 * Maintains the same modern graphical style as the MenuScene.
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public final class RulesSceneImpl extends JPanel implements RulesScene {

    private static final long serialVersionUID = 1L;

    private static final Dimension RIGID_AREA_1 = new Dimension(0, 40);
    private static final Dimension RIGID_AREA_2 = new Dimension(0, 20);
    private static final Dimension RIGID_AREA_3 = new Dimension(0, 50);
    private static final Dimension PANEL_DIMENSION = new Dimension(600, 80);
    private static final Dimension TEXT_PANEL_RIGID_AREA = new Dimension(0, 5);
    private static final float FONT_SIZE = 12f;
    private static final String CHECKBOX = "checkbox";

    private final JCheckBox unoPenaltyCheck;
    private final JCheckBox skipAfterDrawCheck;
    private final JCheckBox mandatoryPassCheck;
    private final JCheckBox scoringModeCheck;

    private Optional<MenuObserver> observer;

    /**
     * Constructs the RulesSceneImpl panel with all GUI components.
     * 
     * @param currentRules The current game rules to display.
     */
    public RulesSceneImpl(final GameRules currentRules) {
        super(new GridBagLayout());
        setBackground(UnoTheme.BACKGROUND_COLOR);

        final JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(UnoTheme.PADDING_BORDER);

        final JLabel title = new JLabel("Game Rules");
        title.setFont(UnoTheme.SUBTITLE_FONT);
        title.setForeground(UnoTheme.TEXT_COLOR);
        title.setAlignmentX(CENTER_ALIGNMENT);

        // Rule 1: UNO Penalty (Default: On)
        final JPanel rule1 = createRulePanel(
                "UNO Penalty",
                "If DISABLED, players are not required to shout 'UNO!' when holding one card.",
                currentRules.isUnoPenaltyEnabled());
        unoPenaltyCheck = (JCheckBox) rule1.getClientProperty(CHECKBOX);

        // Rule 2: Skip After Draw (Default: Off)
        final JPanel rule2 = createRulePanel(
                "Skip After Draw",
                "If ENABLED, a player cannot play a card immediately after drawing it.",
                currentRules.isSkipAfterDrawEnabled());
        skipAfterDrawCheck = (JCheckBox) rule2.getClientProperty(CHECKBOX);

        // Rule 3: No Reshuffle (Default: Off)
        final JPanel rule3 = createRulePanel(
                "No Reshuffle",
                "If the draw deck is empty, the game ends in a draw (discard pile is not reshuffled).",
                currentRules.isMandatoryPassEnabled());
        mandatoryPassCheck = (JCheckBox) rule3.getClientProperty(CHECKBOX);

        // Rule 4: Scoring Mode (Default: Off -> Single Round)
        final JPanel rule4 = createRulePanel(
                "Scoring Mode",
                "If ENABLED, play until 500 points. If DISABLED, the first player to win a round wins the match.",
                currentRules.isScoringModeEnabled());
        scoringModeCheck = (JCheckBox) rule4.getClientProperty(CHECKBOX);

        final StyledButton backButton = new StyledButtonImpl("Save & Back to Menu");
        backButton.setMnemonic(KeyEvent.VK_B);

        backButton.addActionListener(e -> {
            if (observer.isPresent()) {
                final GameRules newRules = new GameRulesImpl(
                        unoPenaltyCheck.isSelected(),
                        skipAfterDrawCheck.isSelected(),
                        mandatoryPassCheck.isSelected(),
                        scoringModeCheck.isSelected());
                observer.get().onSaveRules(newRules);
                observer.get().onBackToMenu();
            }
        });

        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_1));
        contentPanel.add(rule1);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_2));
        contentPanel.add(rule2);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_2));
        contentPanel.add(rule3);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_2));
        contentPanel.add(rule4);
        contentPanel.add(Box.createRigidArea(RIGID_AREA_3));
        contentPanel.add(backButton.getComponent());
        add(contentPanel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObserver(final MenuObserver observer) {
        this.observer = Optional.of(observer);
    }

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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isScoringModeEnabled() {
        return scoringModeCheck.isSelected();
    }

    /**
     * Creates a horizontal panel containing Title+Description on the left and a
     * Checkbox on the right.
     * 
     * @param titleText    The title of the rule.
     * @param descText     The description of the rule.
     * @param defaultState The default state of the checkbox.
     * @return The constructed JPanel.
     */
    private JPanel createRulePanel(final String titleText, final String descText, final boolean defaultState) {
        final JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(PANEL_DIMENSION);
        panel.setPreferredSize(PANEL_DIMENSION);
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UnoTheme.PANEL_COLOR));

        final JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        final JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(UnoTheme.BUTTON_FONT);
        lblTitle.setForeground(UnoTheme.TEXT_COLOR);

        final JLabel lblDesc = new JLabel("<html><body style='width: 450px'>" + descText + "</body></html>");

        lblDesc.setFont(UnoTheme.TEXT_FONT.deriveFont(FONT_SIZE));
        lblDesc.setForeground(UnoTheme.DESC_COLOR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createRigidArea(TEXT_PANEL_RIGID_AREA));
        textPanel.add(lblDesc);

        final JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setSelected(defaultState);

        panel.putClientProperty(CHECKBOX, checkBox);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(checkBox, BorderLayout.EAST);

        return panel;
    }
}
