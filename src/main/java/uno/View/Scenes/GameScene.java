package uno.View.Scenes;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Game.GameState;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Player.Player;
import uno.Controller.GameViewObserver;
import uno.View.GameModelObserver;
import uno.View.Components.ColorChooserPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.Box;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component; 
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font; 
import java.awt.Cursor;
import java.awt.GridBagLayout;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Pannello (JPanel) che rappresenta la schermata di gioco principale.
 * Implementa GameModelObserver per essere aggiornata dal modello Game.
 * Versione con grafica moderna e layout per 4 giocatori.
 */
public class GameScene extends JPanel implements GameModelObserver {

    // --- Colori e Font (come MenuScene) ---
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(50, 50, 50);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BUTTON_COLOR_DRAW = new Color(33, 150, 243); // Blu
    private static final Color BUTTON_COLOR_PASS = new Color(244, 67, 54); // Rosso
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 14);
    private static final Font NORMAL_FONT = new Font("Arial", Font.PLAIN, 12);
    private static final Border HIGHLIGHT_BORDER = BorderFactory.createLineBorder(Color.ORANGE, 3);
    private static final Border NORMAL_BORDER = BorderFactory.createEmptyBorder(3, 3, 3, 3); // Spessore per allineamento

    private final Game gameModel;
    private GameViewObserver controllerObserver;

    // --- Pannelli Giocatori ---
    private JPanel playerHandPanel; // Sud (Umano)
    private JPanel westAIPanel, northAIPanel, eastAIPanel; // Ovest, Nord, Est (IA)
    private JLabel westAILabel, northAILabel, eastAILabel;

    // --- Pannelli Centrali ---
    private JPanel centerPanel;
    private JLabel discardPileCard;
    private JButton drawDeckButton;
    private JButton passButton;
    
    // --- Pannelli Laterali (Est) ---
    private JLabel statusLabel;
    private JButton unoButton;
    private ColorChooserPanel colorChooserPanel; 

    public GameScene(Game gameModel) {
        super(new BorderLayout(10, 10));
        this.gameModel = gameModel;
        this.gameModel.addObserver(this); 
        
        setBackground(BACKGROUND_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Creazione Pannelli ---
        // (I metodi helper inizializzano i campi)
        playerHandPanel = createPlayerHandPanel();
        centerPanel = createCenterPanel(); 
        
        // Pannelli IA (Ovest, Nord, Est)
        westAIPanel = createOpponentPanel("IA-Ovest (1)");
        northAIPanel = createOpponentPanel("IA-Nord (2)");
        eastAIPanel = createOpponentPanel("IA-Est (3)");

        // Pannello Est (Info + Scelta Colore + IA Est)
        JPanel eastContainer = createEastContainer();

        // --- Assemblaggio Layout ---
        add(northAIPanel, BorderLayout.NORTH);
        add(westAIPanel, BorderLayout.WEST);
        add(eastContainer, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
        
        // Pannello Sud (Mano Umano + Bottone UNO)
        JPanel southPanel = new JPanel(new BorderLayout(10, 0));
        southPanel.setOpaque(false);
        southPanel.add(new JScrollPane(playerHandPanel), BorderLayout.CENTER);
        
        JPanel unoButtonPanel = new JPanel(new BorderLayout());
        unoButtonPanel.setOpaque(false);
        unoButtonPanel.setBorder(new EmptyBorder(0, 0, 10, 10));
        this.unoButton = createStyledButton("UNO!", new Color(255, 193, 7), Color.BLACK, 100, 80); // Giallo
        unoButtonPanel.add(this.unoButton, BorderLayout.CENTER);
        
        southPanel.add(unoButtonPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);

        // --- Collegamento Azioni -> Controller ---
        drawDeckButton.addActionListener(e -> {
            if (controllerObserver != null) { controllerObserver.onDrawCard(); }
        });
        passButton.addActionListener(e -> { 
            if (controllerObserver != null) { controllerObserver.onPassTurn(); }
        });
        unoButton.addActionListener(e -> {
            if (controllerObserver != null) { controllerObserver.onCallUno(); }
        });
        
        onGameUpdate(); // Prima visualizzazione
    }

    /**
     * Imposta l'observer (controller) che ascolterà gli eventi di questa scena.
     * @param observer L'observer del controller.
     */
    public void setObserver(GameViewObserver observer) {
        this.controllerObserver = observer;
        this.colorChooserPanel.setObserver(observer);
    }

    /**
     * Abilita o disabilita tutti i controlli di input umano.
     */
    public void setHumanInputEnabled(boolean enabled) {
        this.unoButton.setEnabled(enabled);
        
        boolean isHumanTurn = gameModel.getCurrentPlayer().getClass() == Player.class;
        boolean hasDrawn = gameModel.hasCurrentPlayerDrawn(gameModel.getCurrentPlayer());
        // Il bottone Pesca è attivo solo se è il tuo turno E non hai ancora pescato
        this.drawDeckButton.setEnabled(enabled && !hasDrawn);
        // Il bottone Passa è attivo solo se è il tuo turno E hai già pescato
        this.passButton.setEnabled(enabled && hasDrawn);

        // --- CORREZIONE LOGICA ---
        // Le carte in mano sono abilitate se è il turno dell'umano.
        // La logica "isMoveValid" in Game.java impedirà di giocare
        // una carta non valida.
        for (Component comp : playerHandPanel.getComponents()) {
            if (comp instanceof JButton) {
                // Rimuoviamo "!hasDrawn". Le carte sono sempre giocabili
                // se è il turno dell'umano.
                comp.setEnabled(enabled); 
            }
        }
        
        if (!enabled && !isHumanTurn) {
            System.out.println("Disabilito input umano.");
            colorChooserPanel.setVisible(false);
        }
    }

    /**
     * Metodo chiamato dal Modello (Game) quando lo stato cambia.
     */
    @Override
    public void onGameUpdate() {
        boolean isHumanTurn = gameModel.getCurrentPlayer().getClass() == Player.class;
        
        // --- Gestione Stato (Visibilità) ---
        if (gameModel.getGameState() == GameState.WAITING_FOR_COLOR) {
            setHumanInputEnabled(false); 
            if (isHumanTurn) { 
                System.out.println("Mostro il pannello di scelta colore.");
                colorChooserPanel.setVisible(true);
            }
            statusLabel.setText("Scegli un colore!");
        } else if (gameModel.getGameState() == GameState.RUNNING) {
            colorChooserPanel.setVisible(false);

            String direction = gameModel.isClockwise() ? "Orario" : "Antiorario";
            statusLabel.setText("<html><div style='text-align: center;'>Turno di: " 
                + gameModel.getCurrentPlayer().getName() 
                + "<br>Direzione: " + direction + "</div></html>"); // <-- USA <br> PER ANDARE A CAPO
        }
        
        // --- Aggiornamento Pila Scarti ---
        if (gameModel.isDiscardPileEmpty()) {
            discardPileCard.setText("Vuota");
            discardPileCard.setBackground(Color.LIGHT_GRAY);
        } else {
            Card topCard = gameModel.getTopDiscardCard();
            discardPileCard.setText("<html><div style='text-align: center;'>" + topCard.getValue() + "<br>" + topCard.getColor() + "</div></html>");
            
            CardColor activeColor = gameModel.getCurrentColor();
            System.out.println("Colore attivo: " + activeColor);

            discardPileCard.setBackground(convertCardColor(activeColor));
            discardPileCard.setForeground(Color.BLACK);
            if(activeColor == CardColor.BLUE || activeColor == CardColor.RED || activeColor == CardColor.WILD || activeColor == null) {
                discardPileCard.setForeground(Color.WHITE);
            }
        }

        // --- Aggiornamento Mano Umano ---
        playerHandPanel.removeAll(); 
        Player humanPlayer = gameModel.getPlayers().get(0); 
        for (Card card : humanPlayer.getHand()) {
            JButton cardButton = createCardButton(card);
            cardButton.addActionListener(e -> {
                if (controllerObserver != null) { controllerObserver.onPlayCard(card); }
            });
            playerHandPanel.add(cardButton);
        }

        setHumanInputEnabled(isHumanTurn && gameModel.getGameState() == GameState.RUNNING);
        
        // --- Aggiornamento Pannelli IA ---
        updateOpponentPanel(westAIPanel, westAILabel, gameModel.getPlayers().get(1));
        updateOpponentPanel(northAIPanel, northAILabel, gameModel.getPlayers().get(2));
        updateOpponentPanel(eastAIPanel, eastAILabel, gameModel.getPlayers().get(3));

        revalidate();
        repaint();
    }
    
    /**
     * Mostra un popup che annuncia il vincitore.
     */

    public void showWinnerPopup(String winnerName) {
        setHumanInputEnabled(false);
        JOptionPane.showMessageDialog(this, winnerName + " ha vinto la partita!", 
            "Partita Terminata", JOptionPane.INFORMATION_MESSAGE);
    }
    
    // --- Metodi Helper per la Creazione GUI ---

    private JPanel createOpponentPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), title, 
            TitledBorder.CENTER, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
        ));
        panel.setPreferredSize(new Dimension(120, 100));

        JLabel cardLabel = new JLabel("X carte");
        cardLabel.setFont(BOLD_FONT);
        cardLabel.setForeground(TEXT_COLOR);
        cardLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Salva il riferimento alla label corretta
        if (title.contains("Ovest")) { this.westAILabel = cardLabel; }
        else if (title.contains("Nord")) { this.northAILabel = cardLabel; }
        else if (title.contains("Est")) { this.eastAILabel = cardLabel; }
        
        panel.add(Box.createVerticalGlue());
        panel.add(cardLabel);
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    
    private void updateOpponentPanel(JPanel panel, JLabel label, Player ai) {
        label.setText(ai.getHandSize() + " carte");
        if (gameModel.getCurrentPlayer() == ai) {
            panel.setBorder(BorderFactory.createTitledBorder(
                HIGHLIGHT_BORDER, ai.getName(),
                TitledBorder.CENTER, TitledBorder.TOP, BOLD_FONT, Color.ORANGE
            ));
        } else {
            panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), ai.getName(),
                TitledBorder.CENTER, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
            ));
        }
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        this.drawDeckButton = createStyledButton("[MAZZO]", BUTTON_COLOR_DRAW, Color.WHITE, 100, 150);
        
        this.discardPileCard = new JLabel("SCARTI");
        this.discardPileCard.setPreferredSize(new Dimension(100, 150));
        this.discardPileCard.setFont(BOLD_FONT);
        this.discardPileCard.setHorizontalAlignment(JLabel.CENTER);
        this.discardPileCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.discardPileCard.setOpaque(true); 
        
        this.passButton = createStyledButton("Passa", BUTTON_COLOR_PASS, Color.WHITE, 100, 40);
        
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(this.drawDeckButton, gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        panel.add(this.discardPileCard, gbc);
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(this.passButton, gbc);
        
        return panel;
    }

    private JPanel createPlayerHandPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        panel.setBackground(PANEL_COLOR);
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "La tua Mano",
            TitledBorder.CENTER, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
        );
        panel.setBorder(border);
        panel.setPreferredSize(new Dimension(100, 150)); // Altezza per le carte
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Info Partita",
            TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
        ));

        this.statusLabel = new JLabel("Turno di: ... \n Direzione: ...");
        this.statusLabel.setFont(BOLD_FONT);
        this.statusLabel.setForeground(TEXT_COLOR);
        this.statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.statusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));

        panel.add(this.statusLabel);

        return panel;
    }
    
    private JPanel createEastContainer() {
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        eastPanel.setOpaque(false);
        
        JPanel infoPanel = createInfoPanel();
        colorChooserPanel = new ColorChooserPanel();
        
        eastPanel.add(eastAIPanel); // Pannello IA Est in cima
        eastPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        eastPanel.add(infoPanel);
        eastPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        eastPanel.add(colorChooserPanel);
        eastPanel.add(Box.createVerticalGlue()); // Spinge tutto in alto
        return eastPanel;
    }
    
    private JButton createCardButton(Card card) {
        JButton button = new JButton("<html><div style='text-align: center;'>" + card.getValue() + "<br>" + card.getColor() + "</div></html>");
        button.setPreferredSize(new Dimension(80, 120));
        button.setBackground(convertCardColor(card.getColor()));
        button.setFont(new Font("Arial", Font.BOLD, 10));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        if (card.getColor() == CardColor.YELLOW) {
            button.setForeground(Color.BLACK);
        } else {
            button.setForeground(Color.WHITE);
        }
        return button;
    }
    
    private JButton createStyledButton(String text, Color bg, Color fg, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(BOLD_FONT);
        button.setBackground(bg);
        button.setForeground(fg);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(width, height));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private Color convertCardColor(CardColor cardColor) {
        if (cardColor == null) return Color.BLACK; // Colore Jolly attivo
        
        switch (cardColor) {
            case RED: return new Color(211, 47, 47);
            case BLUE: return new Color(33, 150, 243);
            case GREEN: return new Color(76, 175, 80);
            case YELLOW: return new Color(255, 235, 59);
            case WILD:
            default:
                return Color.DARK_GRAY;
        }
    }
}