package uno.view.scenes.impl;

import uno.controller.api.GameViewObserver;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameState;
import uno.model.players.api.AbstractPlayer;
import uno.view.components.impl.ColorChooserPanelImpl;
import uno.view.components.impl.PlayerChooserPanelImpl;
import uno.view.components.api.StyledButton;
import uno.view.components.impl.StyledButtonImpl;
import uno.view.scenes.api.GameScene;
import uno.view.utils.impl.CardImageLoaderImpl;
import uno.view.components.api.ColorChooserPanel;
import uno.view.components.api.PlayerChooserPanel;
import uno.view.style.UnoTheme;
import uno.model.game.api.Game;
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
 * requests
 * coming from the Controller.
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public final class GameSceneImpl extends JPanel implements GameScene {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(GameSceneImpl.class.getName());

    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;

    private static final int START_POPUP_DELAY = 3000;

    private static final Insets GBC_INSETS = new Insets(0, 5, 0, 5);
    private static final int GRID_FIVE = 5;

    private static final Dimension SCROLL_PANE_DIMENSION = new Dimension(800, 180);
    private static final Dimension PANEL_DIMENSION = new Dimension(120, 110); // Slightly taller for better spacing
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

    private final CardImageLoaderImpl cardImageLoader;

    private final Game gameModel;
    private Optional<GameViewObserver> controllerObserver = Optional.empty();

    private final JPanel playerHandPanel;
    private final JPanel westAIPanel;
    private final JPanel northAIPanel;
    private final JPanel eastAIPanel;
    private JLabel westAILabel;
    private JLabel northAILabel;
    private JLabel eastAILabel;

    private final JPanel centerPanel;
    private JLabel discardPileCard;
    private JButton drawDeckButton; // Keep as JButton for Card Image
    private StyledButton passButton;
    private JLabel statusLabel;
    private JLabel deckInfoLabel; // New Label for Deck Count
    private JLabel colorInfoLabel; // New Label for Current Color
    private StyledButton unoButton;

    /**
     * Constructor for GameSceneImpl.
     * 
     * @param gameModel The game model instance to observe and represent.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GameSceneImpl(final Game gameModel) {
        super(new BorderLayout(10, 10));
        this.gameModel = gameModel;
        this.gameModel.addObserver(this);
        this.cardImageLoader = new CardImageLoaderImpl(CARD_WIDTH, CARD_HEIGHT);
        setBackground(UnoTheme.BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Creation of Panels ---
        playerHandPanel = createPlayerHandPanel();
        centerPanel = createCenterPanel();

        westAIPanel = createOpponentPanel("IA-Ovest (1)");
        northAIPanel = createOpponentPanel("IA-Nord (2)");
        eastAIPanel = createOpponentPanel("IA-Est (3)");

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

        // --- Event Listeners ---
        drawDeckButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onDrawCard);
        });

        passButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onPassTurn);
        });

        unoButton.addActionListener(e -> {
            controllerObserver.ifPresent(GameViewObserver::onCallUno);
        });

        onGameUpdate();
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
        final GameState currentState = gameModel.getGameState();
        final boolean shouldDisableUno = currentState == GameState.WAITING_FOR_COLOR
                || currentState == GameState.WAITING_FOR_PLAYER;

        this.unoButton.setEnabled(!shouldDisableUno);

        final boolean hasDrawn = gameModel.hasCurrentPlayerDrawn(gameModel.getCurrentPlayer());

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

        final JDialog dialog = pane.createDialog(this, "Scegli un Colore");
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

        final JDialog dialog = pane.createDialog(this, "Scegli un Giocatore");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }

    /**
     * Metodo chiamato dal Modello (Game) quando lo stato cambia.
     */
    @Override
    public void onGameUpdate() {
        updateStatusLabel();
        updateDiscardPile();
        updateHumanHand();
        updateAIPanels();
        updateGameInfo(); // Update new info labels

        final boolean isHumanTurn = gameModel.getCurrentPlayer().getClass() == AbstractPlayer.class;
        setHumanInputEnabled(isHumanTurn && gameModel.getGameState() == GameState.RUNNING);

        revalidate();
        repaint();
    }

    /**
     * New method to update game info.
     */
    private void updateGameInfo() {
        // Update Deck Count
        if (gameModel.getDrawDeck() != null) {
            final int deckSize = gameModel.getDrawDeck().size();
            deckInfoLabel.setText("Deck: " + deckSize + " cards");
        }

        // Update Current Color
        final Optional<CardColor> currentColor = gameModel.getCurrentColor();
        if (currentColor.isPresent()) {
            colorInfoLabel.setText("Color: " + currentColor.get().name());
            colorInfoLabel.setForeground(convertCardColor(currentColor));
        } else {
            colorInfoLabel.setText("Color: None");
            colorInfoLabel.setForeground(UnoTheme.TEXT_COLOR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showError(final String message, final String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void showInfo(final String message, final String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void showStartingPlayer(final String playerName) {
        final String msg = "Inizia: " + playerName;
        final JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);
        final JDialog dialog = pane.createDialog(this, "Inizio Partita");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        // Force center on screen as requested
        dialog.setLocationRelativeTo(null);

        new Thread(() -> {
            try {
                Thread.sleep(START_POPUP_DELAY);
            } catch (final InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Si è verificato un errore imprevisto", e);
            }
            dialog.dispose();
        }).start();

        dialog.setVisible(true);
    }

    @Override
    public boolean confirmExit() {
        final int choice = JOptionPane.showConfirmDialog(
                this,
                "Sei sicuro di voler tornare al menu? La partita sarà persa.",
                "Torna al Menu",
                JOptionPane.YES_NO_OPTION);
        return choice == JOptionPane.YES_OPTION;
    }

    @Override
    public void showWinnerPopup(final String winnerName) {
        setHumanInputEnabled(false);

        final Object[] options = {"Torna al Menu", "Chiudi Gioco" };

        final int choice = JOptionPane.showOptionDialog(
                this,
                winnerName + " ha vinto la partita!\nCosa vuoi fare?",
                "Partita Terminata",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);

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
     * @param title The title of the opponent panel.
     * @return The created JPanel.
     */
    private JPanel createOpponentPanel(final String title) {
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UnoTheme.PANEL_COLOR);

        // Modern styling: LineBorder + Padding
        panel.setBorder(new CompoundBorder(
                BorderFactory.createLineBorder(UnoTheme.BORDER_COLOR, 2),
                UnoTheme.PANEL_INSETS));

        panel.setPreferredSize(PANEL_DIMENSION);

        // Name Label
        final JLabel nameLabel = new JLabel(title);
        nameLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        nameLabel.setForeground(UnoTheme.DESC_COLOR);
        nameLabel.setAlignmentX(CENTER_ALIGNMENT);

        final JLabel cardLabel = new JLabel("X carte");
        cardLabel.setFont(UnoTheme.BUTTON_FONT); // Larger font for count
        cardLabel.setForeground(UnoTheme.TEXT_COLOR);
        cardLabel.setAlignmentX(CENTER_ALIGNMENT);

        if (title.contains("Ovest")) {
            this.westAILabel = cardLabel;
        } else if (title.contains("Nord")) {
            this.northAILabel = cardLabel;
        } else if (title.contains("Est")) {
            this.eastAILabel = cardLabel;
        }

        panel.add(Box.createVerticalGlue());
        panel.add(nameLabel);
        panel.add(Box.createVerticalStrut(BOX_VERTICAL_HEIGHT));
        panel.add(cardLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }

    /**
     * Updates an AI opponent panel with the current card count and highlights if
     * it's their turn.
     * 
     * @param panel The panel to update.
     * @param label The label within the panel to update.
     * @param ai    The AI player associated with the panel.
     */
    private void updateOpponentPanel(final JPanel panel, final JLabel label, final AbstractPlayer ai) {
        label.setText(ai.getHandSize() + " carte");

        if (gameModel.getCurrentPlayer().equals(ai)) {
            // Active Turn: Thicker Gold (or Red) Border
            final Color activeColor = ai.getHandSize() <= 1 ? UnoTheme.BUTTON_COLOR : UnoTheme.ACTIVE_BORDER_COLOR;
            final int thickness = ai.getHandSize() <= 1 ? 5 : 4;

            panel.setBorder(new CompoundBorder(
                    BorderFactory.createLineBorder(activeColor, thickness),
                    UnoTheme.PANEL_INSETS));
            panel.setBackground(UnoTheme.PANEL_COLOR); // Simpler background for now
        } else {
            // Inactive
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

        this.discardPileCard = new JLabel("SCARTI");
        this.discardPileCard.setPreferredSize(DISCARD_PILE_DIMENSION);
        this.discardPileCard.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.discardPileCard.setHorizontalAlignment(JLabel.CENTER);
        this.discardPileCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.discardPileCard.setOpaque(true);

        // Use StyledButton for Pass and UNO
        this.passButton = new StyledButtonImpl("Passa", PASS_BUTTON_NORMAL_COLOR, PASS_BUTTON_HOVER_COLOR); // Custom Red
        this.passButton.setSize(PASS_BUTTON_WIDTH, PASS_BUTTON_HEIGHT);
        this.passButton.setFont(UnoTheme.TEXT_BOLD_FONT);

        this.unoButton = new StyledButtonImpl("UNO!", UnoTheme.YELLOW_COLOR, Color.ORANGE);
        this.unoButton.setForeground(Color.BLACK); // Text black for yellow button
        this.unoButton.setSize(UNO_BUTTON_WIDTH, UNO_BUTTON_HEIGHT);
        this.unoButton.setFont(UnoTheme.TEXT_BOLD_FONT);

        // Initialize MENU BUTTON here
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

        // Layout using GridBagLayout

        // --- Row 0 (Top Row) ---
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.weightx = 0.5; // Give it weight so it expands
        gbc.weighty = 0.0;
        gbc.fill = GridBagConstraints.NONE; // Do NOT fill, keep distinct size
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

        // Modern simple border
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
        // this.statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.statusLabel.setBorder(STATUS_LABEL_BORDER);

        this.deckInfoLabel = new JLabel("Deck: ?");
        this.deckInfoLabel.setFont(UnoTheme.TEXT_FONT);
        this.deckInfoLabel.setForeground(UnoTheme.DESC_COLOR);
        this.deckInfoLabel.setBorder(INFO_LABEL_BORDER);

        this.colorInfoLabel = new JLabel("Color: ?");
        this.colorInfoLabel.setFont(UnoTheme.TEXT_BOLD_FONT);
        this.colorInfoLabel.setForeground(UnoTheme.TEXT_COLOR);
        this.colorInfoLabel.setBorder(INFO_LABEL_BORDER);

        panel.add(this.statusLabel);
        panel.add(Box.createVerticalStrut(BOX_VERTICAL_HEIGHT));
        panel.add(this.deckInfoLabel);
        panel.add(this.colorInfoLabel);

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
     * @param card The card to create a button for.
     * @return The created JButton.
     */
    private JButton createCardButton(final Optional<Card> card) {
        final String cardName = card.get().getColor(gameModel).name() + "_" + card.get().getValue(gameModel).name();
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
                return UnoTheme.BUTTON_COLOR; // Use theme red
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
        if (gameModel.isDiscardPileEmpty()) {
            discardPileCard.setText("Empty");
            discardPileCard.setIcon(null);
            discardPileCard.setBackground(Color.LIGHT_GRAY);
        } else {
            final Optional<Card> topCard = gameModel.getTopDiscardCard();
            final String cardName = topCard.get().getColor(gameModel).name() + "_"
                    + topCard.get().getValue(gameModel).name();
            final Optional<ImageIcon> icon = Optional.of(cardImageLoader.getImage(cardName));

            if (icon.isPresent()) {
                discardPileCard.setIcon(icon.get());
                discardPileCard.setText(null);
            } else {
                discardPileCard.setIcon(null);
                discardPileCard.setText("<html><div style='text-align: center;'>"
                        + topCard.get().getValue(gameModel) + "<br>" + topCard.get().getColor(gameModel)
                        + "</div></html>");
            }

            applyActiveColorBorder(gameModel.getCurrentColor());
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
        playerHandPanel.removeAll();

        final AbstractPlayer humanPlayer = gameModel.getPlayers().get(0);

        final GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = GBC_INSETS;
        gbc.anchor = GridBagConstraints.CENTER;

        for (final Optional<Card> card : humanPlayer.getHand()) {
            final JButton cardButton = createCardButton(card);

            cardButton.addActionListener(e -> {
                controllerObserver.ifPresent(observer -> observer.onPlayCard(card));
            });

            playerHandPanel.add(cardButton, gbc);
            gbc.gridx++;
        }
    }

    /**
     * Updates the central status label based on the current game state.
     * It shows whose turn it is, the rotation direction, or specific action
     * requests.
     */
    private void updateStatusLabel() {
        final GameState currentState = gameModel.getGameState();

        switch (currentState) {
            case WAITING_FOR_COLOR:
                statusLabel.setText("Choose a color!");
                break;
            case WAITING_FOR_PLAYER:
                statusLabel.setText("Pick a target player!");
                break;
            case RUNNING:
                final String direction = gameModel.isClockwise() ? "Clockwise" : "Counter-clockwise";
                statusLabel.setText("<html><div style='text-align: center;'>Turn: "
                        + gameModel.getCurrentPlayer().getName()
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

        final List<AbstractPlayer> players = gameModel.getPlayers();

        if (players.size() >= 4) {
            updateOpponentPanel(westAIPanel, westAILabel, players.get(1));
            updateOpponentPanel(northAIPanel, northAILabel, players.get(2));
            updateOpponentPanel(eastAIPanel, eastAILabel, players.get(3));
        }

    }

    @SuppressFBWarnings("DM_EXIT")
    private void closeApplication() {
        System.exit(0);
    }
}
