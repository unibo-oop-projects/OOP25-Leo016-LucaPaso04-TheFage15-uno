package uno.view.scenes.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uno.model.cards.attributes.CardColor;
import uno.model.players.impl.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.model.game.api.GameState;
import uno.view.components.impl.ColorChooserPanelImpl;
import uno.view.components.impl.PlayerChooserPanelImpl;
import uno.view.components.api.StyledButton;
import uno.view.components.impl.StyledButtonImpl;
import uno.view.api.CardViewData;
import uno.view.api.GameViewData;
import uno.view.api.PlayerViewData;
import uno.view.scenes.api.GameScene;
import uno.view.utils.impl.CardImageLoaderImpl;
import uno.view.api.GameViewObserver;
import uno.view.components.api.ColorChooserPanel;
import uno.view.components.api.PlayerChooserPanel;
import uno.view.style.UnoTheme;
import java.util.Optional;

import javax.swing.ImageIcon;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.Box;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Cursor;
import java.awt.GridBagLayout;
import javax.swing.border.TitledBorder;
import java.util.List;
import javax.swing.border.EmptyBorder;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Implementation of the GameScene interface representing the main Game Board
 * view.
 * It defines how the game displays the state and handles user interaction
 * requests coming from the Controller.
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public final class GameSceneImpl extends JPanel implements GameScene {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(GameSceneImpl.class.getName());

    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;

    private static final int START_POPUP_DELAY = 3000;

    private static final String START_POINTS = "Punti: 0";

    private static final Insets GBC_INSETS = new Insets(0, 5, 0, 5);
    private static final int GRID_FIVE = 5;

    private static final Dimension SCROLL_PANE_DIMENSION = new Dimension(800, 180);
    private static final Dimension PANEL_DIMENSION = new Dimension(120, 110);
    private static final Dimension DISCARD_PILE_DIMENSION = new Dimension(100, 150);

    private static final int PASS_BUTTON_WIDTH = 100;
    private static final int PASS_BUTTON_HEIGHT = 40;
    private static final int UNO_BUTTON_WIDTH = 100;
    private static final int UNO_BUTTON_HEIGHT = 40;

    private static final int BOX_VERTICAL_HEIGHT = 5;
    private static final Color PASS_BUTTON_NORMAL_COLOR = new Color(244, 67, 54);
    private static final Color PASS_BUTTON_HOVER_COLOR = new Color(255, 100, 100);
    private static final Color MENU_BUTTON_NORMAL_COLOR = new Color(200, 50, 50);
    private static final Color MENU_BUTTON_HOVER_COLOR = new Color(220, 80, 80);
    private static final Dimension MENU_BUTTON_DIMENSION = new Dimension(100, 40);
    private static final EmptyBorder STATUS_LABEL_BORDER = new EmptyBorder(5, 5, 5, 5);
    private static final EmptyBorder INFO_LABEL_BORDER = new EmptyBorder(0, 5, 5, 5);
    private static final String POSITION_WEST = "West";
    private static final String POSITION_NORTH = "North";
    private static final String POSITION_EAST = "East";

    private final CardImageLoaderImpl cardImageLoader;

    private GameViewData currentData;
    private Optional<GameViewObserver> controllerObserver = Optional.empty();

    private final JPanel playerHandPanel;
    private final JPanel westAIPanel;
    private final JPanel northAIPanel;
    private final JPanel eastAIPanel;
    private JLabel westAILabel;
    private JLabel northAILabel;
    private JLabel eastAILabel;

    private JLabel westScoreLabel;
    private JLabel northScoreLabel;
    private JLabel eastScoreLabel;

    private final JPanel centerPanel;
    private JLabel discardPileCard;
    private JButton drawDeckButton;
    private StyledButton passButton;
    private JLabel statusLabel;
    private JLabel deckInfoLabel;
    private JLabel colorInfoLabel;
    private JLabel humanScoreLabel;
    private StyledButton unoButton;

    /**
     * Constructor for GameSceneImpl.
     */
    public GameSceneImpl() {
        super(new BorderLayout(10, 10));
        this.cardImageLoader = new CardImageLoaderImpl(CARD_WIDTH, CARD_HEIGHT);
        setBackground(UnoTheme.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        playerHandPanel = createPlayerHandPanel();
        centerPanel = createCenterPanel();

        westAIPanel = createOpponentPanel("AI-1", POSITION_WEST);
        northAIPanel = createOpponentPanel("AI-2", POSITION_NORTH);
        eastAIPanel = createOpponentPanel("AI-3", POSITION_EAST);

        add(northAIPanel, BorderLayout.NORTH);
        add(westAIPanel, BorderLayout.WEST);
        add(eastAIPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);

        final JPanel southPanel = new JPanel(new BorderLayout(10, 0));
        southPanel.setOpaque(false);

        final JScrollPane scrollPane = new JScrollPane(playerHandPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setPreferredSize(SCROLL_PANE_DIMENSION);

        southPanel.add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        drawDeckButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onDrawCard);
        });

        passButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onPassTurn);
        });

        unoButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onCallUno);
        });

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateView(final GameViewData data) {
        this.currentData = data;
        updateStatusLabel();
        updateDiscardPile();
        updateHumanHand();
        updateAIPanels();
        updateGameInfo();

        final boolean isHumanTurn = currentData.getCurrentPlayer().getModelPlayer() instanceof HumanPlayer;

        setHumanInputEnabled(isHumanTurn && currentData.getGameState() == GameState.RUNNING);

        revalidate();
        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setObserver(final GameViewObserver observer) {
        this.controllerObserver = Optional.ofNullable(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHumanInputEnabled(final boolean enabled) {
        if (currentData == null) {
            return;
        }
        final GameState currentState = currentData.getGameState();
        final boolean shouldDisableUno = currentState == GameState.WAITING_FOR_COLOR
                || currentState == GameState.WAITING_FOR_PLAYER;

        this.unoButton.setEnabled(!shouldDisableUno);

        final boolean hasDrawn = currentData.hasCurrentPlayerDrawn();

        this.drawDeckButton.setEnabled(enabled && !hasDrawn);
        this.passButton.setEnabled(enabled && hasDrawn);

        for (final Component comp : playerHandPanel.getComponents()) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showColorChooser(final boolean isDarkSide) {
        final ColorChooserPanel panel = new ColorChooserPanelImpl(this.controllerObserver, isDarkSide);

        final JOptionPane pane = new JOptionPane(
                panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, new Object[] {}, null);

        final JDialog dialog = pane.createDialog(this, "Choose a Color");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showPlayerChooser(final List<AbstractPlayer> opponents) {
        final PlayerChooserPanel panel = new PlayerChooserPanelImpl(this.controllerObserver, opponents);

        final JOptionPane pane = new JOptionPane(
                panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION,
                null, new Object[] {}, null);

        final JDialog dialog = pane.createDialog(this, "Choose a Player");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * Updates the game info labels such as deck count, current color, and human
     * score.
     */
    private void updateGameInfo() {
        if (currentData == null) {
            return;
        }

        final int deckSize = currentData.getDeckSize();
        deckInfoLabel.setText("Deck: " + deckSize + " cards");

        final Optional<CardColor> currentColor = currentData.getCurrentColor();
        if (currentColor.isPresent()) {
            colorInfoLabel.setText("Color: " + currentColor.get().name());
            colorInfoLabel.setForeground(convertCardColor(currentColor));
        } else {
            colorInfoLabel.setText("Color: None");
            colorInfoLabel.setForeground(UnoTheme.TEXT_COLOR);
        }

        if (!currentData.getPlayers().isEmpty()) {
            humanScoreLabel.setText("Points: " + currentData.getPlayers().get(0).getScore());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError(final String message, final String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showInfo(final String message, final String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showStartingPlayer(final String playerName) {
        final String msg = "Starts: " + playerName;
        final JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
        final JDialog dialog = pane.createDialog(this, "Inizio Partita");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setLocationRelativeTo(null);

        new Thread(() -> {
            try {
                Thread.sleep(START_POPUP_DELAY);
            } catch (final InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Error while showing starting player popup", e);
            }
            dialog.dispose();
        }).start();

        dialog.setVisible(true);
    }

    @Override
    public boolean confirmExit() {
        final int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to go back to the menu? The game will be lost.",
                "Back to Menu",
                JOptionPane.YES_NO_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    @Override
    public void showWinnerPopup(final String winnerName) {
        setHumanInputEnabled(false);

        final Object[] popupOptions = {"Back to Menu", "Close Game" };

        final int choice = JOptionPane.showOptionDialog(
                this,
                winnerName + " has won the game!\nWhat would you like to do?",
                "Game Over",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                popupOptions,
                popupOptions[0]);

        if (controllerObserver.isPresent()) {
            switch (choice) {
                case 0:
                    controllerObserver.ifPresent(GameViewObserver::onBackToMenu);
                    break;
                case 1:
                    closeApplication();
                    break;
                case JOptionPane.CLOSED_OPTION:
                    closeApplication();
                    break;
                default:
                    break;
            }
        }
    }

    // --- Methods for creating and updating UI components ---

    /**
     * Creates a panel for an AI opponent with a title and card count label.
     * 
     * @param title    The title of the opponent panel.
     * @param position The position of the panel (West, North, East).
     * @return The created JPanel.
     */
    private JPanel createOpponentPanel(final String title, final String position) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UnoTheme.PANEL_COLOR);

        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(UnoTheme.BORDER_COLOR, 2),
                UnoTheme.PANEL_INSETS));

        panel.setPreferredSize(PANEL_DIMENSION);

        final JLabel nameLabel = new JLabel(title);
        nameLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        nameLabel.setForeground(UnoTheme.DESC_COLOR);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        final JLabel cardLabel = new JLabel("X cards");
        cardLabel.setFont(UnoTheme.BUTTON_FONT);
        cardLabel.setForeground(UnoTheme.TEXT_COLOR);
        cardLabel.setAlignmentX(CENTER_ALIGNMENT);

        if (POSITION_WEST.equals(position)) {
            this.westAILabel = cardLabel;
            this.westScoreLabel = new JLabel(START_POINTS);
            this.westScoreLabel.setFont(UnoTheme.TEXT_FONT);
            this.westScoreLabel.setForeground(UnoTheme.DESC_COLOR);
            this.westScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        } else if (POSITION_NORTH.equals(position)) {
            this.northAILabel = cardLabel;
            this.northScoreLabel = new JLabel(START_POINTS);
            this.northScoreLabel.setFont(UnoTheme.TEXT_FONT);
            this.northScoreLabel.setForeground(UnoTheme.DESC_COLOR);
            this.northScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        } else if (POSITION_EAST.equals(position)) {
            this.eastAILabel = cardLabel;
            this.eastScoreLabel = new JLabel(START_POINTS);
            this.eastScoreLabel.setFont(UnoTheme.TEXT_FONT);
            this.eastScoreLabel.setForeground(UnoTheme.DESC_COLOR);
            this.eastScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        }

        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(BOX_VERTICAL_HEIGHT));
        panel.add(cardLabel);
        if (POSITION_WEST.equals(position)) {
            panel.add(this.westScoreLabel);
        } else if (POSITION_NORTH.equals(position)) {
            panel.add(this.northScoreLabel);
        } else if (POSITION_EAST.equals(position)) {
            panel.add(this.eastScoreLabel);
        }
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Updates an AI opponent panel with the current card count and highlights if
     * it's their turn.
     * 
     * @param panel      The panel to update.
     * @param label      The label within the panel to update.
     * @param ai         The AI player associated with the panel.
     * @param scoreLabel The label to update with the AI's score (can be null if not
     *                   used).
     */
    private void updateOpponentPanel(final JPanel panel, final JLabel label, final JLabel scoreLabel,
            final PlayerViewData ai) {
        label.setText(ai.getHandSize() + " cards");
        if (scoreLabel != null) {
            scoreLabel.setText("Points: " + ai.getScore());
        }

        if (ai.isCurrentPlayer()) {
            final Color activeColor = ai.getHandSize() <= 1 ? UnoTheme.BUTTON_COLOR : UnoTheme.ACTIVE_BORDER_COLOR;
            final int thickness = ai.getHandSize() <= 1 ? 5 : 4;

            panel.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(activeColor, thickness),
                    UnoTheme.PANEL_INSETS));
            panel.setBackground(UnoTheme.PANEL_COLOR);
        } else {
            panel.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(UnoTheme.BORDER_COLOR, 2),
                    UnoTheme.PANEL_INSETS));
            panel.setBackground(UnoTheme.PANEL_COLOR);
        }
    }

    /**
     * Creates the central panel containing the draw deck, discard pile, status
     * info, and action buttons.
     * 
     * @return The created JPanel.
     */
    private JPanel createCenterPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        this.drawDeckButton = new JButton();
        styleAsCardButton(this.drawDeckButton, "CARD_BACK");

        this.discardPileCard = new JLabel("DISCARDS");
        this.discardPileCard.setPreferredSize(DISCARD_PILE_DIMENSION);
        this.discardPileCard.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.discardPileCard.setHorizontalAlignment(JLabel.CENTER);
        this.discardPileCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.discardPileCard.setOpaque(true);

        this.passButton = new StyledButtonImpl("Pass", PASS_BUTTON_NORMAL_COLOR, PASS_BUTTON_HOVER_COLOR);
        this.passButton.setSize(PASS_BUTTON_WIDTH, PASS_BUTTON_HEIGHT);
        this.passButton.setFont(UnoTheme.TEXT_BOLD_FONT);

        this.unoButton = new StyledButtonImpl("UNO!", UnoTheme.YELLOW_COLOR, Color.ORANGE);
        this.unoButton.setForeground(Color.BLACK);
        this.unoButton.setSize(UNO_BUTTON_WIDTH, UNO_BUTTON_HEIGHT);
        this.unoButton.setFont(UnoTheme.TEXT_BOLD_FONT);

        final StyledButton menuButton;
        menuButton = new StyledButtonImpl("Menu", MENU_BUTTON_NORMAL_COLOR, MENU_BUTTON_HOVER_COLOR);
        menuButton.setPreferredSize(MENU_BUTTON_DIMENSION);
        menuButton.setFont(UnoTheme.TEXT_BOLD_FONT);
        menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        menuButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onBackToMenu);
        });

        final JPanel infoPanel = createInfoPanel();
        final JPanel horizontalSpacerLeft = new JPanel();
        horizontalSpacerLeft.setOpaque(false);
        final JPanel horizontalSpacerRight = new JPanel();
        horizontalSpacerRight.setOpaque(false);
        final JPanel verticalSpacerTop = new JPanel();
        verticalSpacerTop.setOpaque(false);

        // --- Row 0 (Top Row) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(menuButton.getComponent(), gbc);

        // --- Row 1 (Middle Row) ---
        // Top Spacer: Column 1, Row 0
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(verticalSpacerTop, gbc);

        // Left Spacer: Column 1, Row 1
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(horizontalSpacerLeft, gbc);

        // Draw Deck: Column 2, Row 1
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(drawDeckButton, gbc);

        // Discard Pile: Column 3, Row 1
        gbc.gridx = 3;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(discardPileCard, gbc);

        // Right Spacer: Column 4, Row 1
        gbc.gridx = 4;
        gbc.gridy = 1;
        gbc.weightx = 0.5;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(horizontalSpacerRight, gbc);

        // Info Panel: Column 5, Row 0
        gbc.gridx = GRID_FIVE;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(infoPanel, gbc);

        // --- Row 2 (Bottom Row) ---

        // Pass Button: Column 2, Row 2
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(passButton.getComponent(), gbc);

        // UNO Button: Column 3, Row 2
        gbc.gridx = GRID_FIVE;
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(unoButton.getComponent(), gbc);

        return panel;
    }

    /**
     * Creates the panel displaying the human player's hand of cards.
     * 
     * @return The created JPanel.
     */
    private JPanel createPlayerHandPanel() {
        final JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UnoTheme.PANEL_COLOR);

        panel.setBorder(new CompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, UnoTheme.BORDER_COLOR), // Top border only
                UnoTheme.PANEL_INSETS));

        return panel;
    }

    /**
     * Creates the info panel displaying current game status.
     * 
     * @return The created JPanel.
     */
    private JPanel createInfoPanel() {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UnoTheme.PANEL_COLOR);

        final TitledBorder border = BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Info Partita",
                TitledBorder.LEFT, TitledBorder.TOP, UnoTheme.TEXT_BOLD_FONT, UnoTheme.TEXT_COLOR);
        panel.setBorder(border);

        this.statusLabel = new JLabel("Turno di: ... \n Direzione: ...");
        this.statusLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.statusLabel.setForeground(UnoTheme.TEXT_COLOR);
        this.statusLabel.setBorder(STATUS_LABEL_BORDER);

        this.deckInfoLabel = new JLabel("Deck: ?");
        this.deckInfoLabel.setFont(UnoTheme.TEXT_FONT);
        this.deckInfoLabel.setForeground(UnoTheme.DESC_COLOR);
        this.deckInfoLabel.setBorder(INFO_LABEL_BORDER);

        this.colorInfoLabel = new JLabel("Color: ?");
        this.colorInfoLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.colorInfoLabel.setForeground(UnoTheme.TEXT_COLOR);
        this.colorInfoLabel.setBorder(INFO_LABEL_BORDER);

        this.humanScoreLabel = new JLabel(START_POINTS);
        this.humanScoreLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.humanScoreLabel.setForeground(UnoTheme.BUTTON_COLOR);
        this.humanScoreLabel.setBorder(INFO_LABEL_BORDER);

        panel.add(this.statusLabel);
        panel.add(Box.createVerticalStrut(BOX_VERTICAL_HEIGHT));
        panel.add(this.deckInfoLabel);
        panel.add(this.colorInfoLabel);
        panel.add(this.humanScoreLabel);

        return panel;
    }

    /**
     * Styles a JButton to look like a card using the card image loader.
     * 
     * @param button   The JButton to style.
     * @param cardName The name of the card image to load.
     */
    private void styleAsCardButton(final JButton button, final String cardName) {
        final Optional<ImageIcon> icon = Optional.of(cardImageLoader.getImage(cardName));
        final ImageIcon transparentIcon = cardImageLoader.getTransparentImage(cardName);
        if (icon.isPresent()) {
            button.setIcon(icon.get());
            button.setDisabledIcon(transparentIcon);
            button.setText(null);
        } else {
            button.setText(cardName.replace("", " "));
            button.setForeground(Color.WHITE);
        }

        button.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Creates a JButton representing a card in the player's hand.
     * 
     * @param cardData The card data to create a button for.
     * @return The created JButton.
     */
    private JButton createCardButton(final CardViewData cardData) {
        final String cardName = cardData.getImageKey();
        final JButton button = new JButton();
        styleAsCardButton(button, cardName);
        return button;
    }

    /**
     * Converts a CardColor to a corresponding Color for UI representation.
     * 
     * @param cardColor The CardColor to convert.
     * @return The corresponding Color.
     */
    private Color convertCardColor(final Optional<CardColor> cardColor) {
        if (!cardColor.isPresent()) {
            return Color.BLACK;
        }

        switch (cardColor.get()) {
            case RED:
                return UnoTheme.BUTTON_COLOR;
            case BLUE:
                return UnoTheme.BLUE_COLOR;
            case GREEN:
                return UnoTheme.GREEN_COLOR;
            case YELLOW:
                return UnoTheme.YELLOW_COLOR;
            case ORANGE:
                return UnoTheme.ORANGE_COLOR;
            case PURPLE:
                return UnoTheme.PURPLE_COLOR;
            case PINK:
                return UnoTheme.PINK_COLOR;
            case TEAL:
                return UnoTheme.TEAL_COLOR;
            default:
                return Color.DARK_GRAY;
        }
    }

    /**
     * Updates the visual representation of the discard pile,
     * including the top card image and the active color border.
     */
    private void updateDiscardPile() {
        if (currentData == null) {
            return;
        }
        if (currentData.isDiscardPileEmpty()) {
            discardPileCard.setText("Empty");
            discardPileCard.setIcon(null);
            discardPileCard.setBackground(Color.LIGHT_GRAY);
        } else {
            final Optional<CardViewData> topCardData = currentData.getTopDiscardCard();
            if (topCardData.isPresent()) {
                final CardViewData card = topCardData.get();
                final String cardName = card.getImageKey();
                final Optional<ImageIcon> icon = Optional.of(cardImageLoader.getImage(cardName));

                if (icon.isPresent()) {
                    discardPileCard.setIcon(icon.get());
                    discardPileCard.setText(null);
                } else {
                    discardPileCard.setIcon(null);
                    discardPileCard.setText("<html><div style='text-align: center;'>"
                            + card.getValue() + "<br>" + card.getColor()
                            + "</div></html>");
                }
            }

            applyActiveColorBorder(currentData.getCurrentColor());
        }
    }

    /**
     * Applies a double border to the discard pile card to show the current color in
     * play.
     * 
     * @param activeColor The current color set in the game model.
     */
    private void applyActiveColorBorder(final Optional<CardColor> activeColor) {
        final Color color = convertCardColor(activeColor);

        final Border innerBorder = BorderFactory.createLineBorder(UnoTheme.BACKGROUND_COLOR, 2);
        final Border outerBorder = BorderFactory.createLineBorder(color, 5);

        discardPileCard.setBorder(new CompoundBorder(outerBorder, innerBorder));
    }

    /**
     * Refreshes the human player's hand panel by recreating card buttons.
     */
    private void updateHumanHand() {
        if (currentData == null) {
            return;
        }
        playerHandPanel.removeAll();

        if (currentData.getPlayers().isEmpty()) {
            playerHandPanel.revalidate();
            playerHandPanel.repaint();
            return;
        }

        final PlayerViewData humanPlayer = currentData.getPlayers().get(0);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = GBC_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;

        for (final CardViewData cardData : humanPlayer.getHand()) {
            final JButton cardButton = createCardButton(cardData);

            cardButton.addActionListener(e -> {
                controllerObserver.ifPresent(observer -> observer.onPlayCard(cardData.getModelCard()));
            });

            playerHandPanel.add(cardButton, gbc);
            gbc.gridx++;
        }
        playerHandPanel.revalidate();
        playerHandPanel.repaint();
    }

    /**
     * Updates the central status label based on the current game state.
     * It shows whose turn it is, the rotation direction, or specific action
     * requests.
     */
    private void updateStatusLabel() {
        if (currentData == null) {
            return;
        }
        final GameState currentState = currentData.getGameState();

        switch (currentState) {
            case WAITING_FOR_COLOR:
                statusLabel.setText("Choose a color!");
                break;
            case WAITING_FOR_PLAYER:
                statusLabel.setText("Pick a target player!");
                break;
            case RUNNING:
                final String direction = currentData.isClockwise() ? "Clockwise" : "Counter-clockwise";
                statusLabel.setText("<html><div style='text-align: center;'>Turn: "
                        + currentData.getCurrentPlayer().getName()
                        + "<br>Direction: " + direction + "</div></html>");
                break;
            default:
                statusLabel.setText("Game Over!");
                break;
        }
    }

    /**
     * Refreshes all AI opponent panels (West, North, East).
     * Highlights the active player and updates card counts.
     */
    private void updateAIPanels() {
        if (currentData == null) {
            return;
        }
        final List<PlayerViewData> players = currentData.getPlayers();

        if (players.size() >= 4) {
            updateOpponentPanel(westAIPanel, westAILabel, westScoreLabel, players.get(1));
            updateOpponentPanel(northAIPanel, northAILabel, northScoreLabel, players.get(2));
            updateOpponentPanel(eastAIPanel, eastAILabel, eastScoreLabel, players.get(3));
        }
    }

    @SuppressFBWarnings("DM_EXIT")
    private void closeApplication() {
        System.exit(0);
    }
}
