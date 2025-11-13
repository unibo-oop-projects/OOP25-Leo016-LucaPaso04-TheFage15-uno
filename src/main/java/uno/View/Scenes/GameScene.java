package uno.View.Scenes;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Game.GameState;
import uno.Model.Players.Player;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Controller.GameViewObserver;
import uno.View.GameModelObserver;
import uno.View.Components.ColorChooserPanel;
import uno.View.Utils.CardImageLoader;
import uno.View.Components.PlayerChooserPanel;

// Imports per le Immagini
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
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

    // --- Dimensioni Carte ---
    private static final int CARD_WIDTH = 80;
    private static final int CARD_HEIGHT = 120;

    // --- Pannelli Giocatori ---
    private JPanel playerHandPanel; // Sud (Umano)
    private JPanel westAIPanel, northAIPanel, eastAIPanel; // Ovest, Nord, Est (IA)
    private JLabel westAILabel, northAILabel, eastAILabel;

    private final CardImageLoader cardImageLoader;

    // --- Pannelli Centrali ---
    private JPanel centerPanel;
    private JLabel discardPileCard;
    private JButton drawDeckButton;
    private JButton passButton;
    private JButton settingsButton;
    private JLabel statusLabel;
    private boolean isColorDialogShowing = false;
    private boolean isPlayerDialogShowing = false;
    private JButton unoButton;

    public GameScene(Game gameModel) {
        super(new BorderLayout(10, 10));
        this.gameModel = gameModel;
        this.gameModel.addObserver(this); 

        // 1. Inizializza la cache e carica le immagini
        this.cardImageLoader = new CardImageLoader(CARD_WIDTH, CARD_HEIGHT);
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

        // --- Assemblaggio Layout ---
        add(northAIPanel, BorderLayout.NORTH);
        add(westAIPanel, BorderLayout.WEST);
        add(eastAIPanel, BorderLayout.EAST);
        add(centerPanel, BorderLayout.CENTER);
        
        // Pannello Sud (Mano Umano + Bottone UNO)
        JPanel southPanel = new JPanel(new BorderLayout(10, 0));
        southPanel.setOpaque(false);
        southPanel.add(new JScrollPane(playerHandPanel), BorderLayout.CENTER);
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
        settingsButton.addActionListener(e -> {
            if (controllerObserver != null) { 
                // Chiama l'azione di ritorno al menu
                controllerObserver.onBackToMenu(); 
            }
        });
        onGameUpdate(); // Prima visualizzazione
    }

    /**
     * Imposta l'observer (controller) che ascolterà gli eventi di questa scena.
     * @param observer L'observer del controller.
     */
    public void setObserver(GameViewObserver observer) {
        this.controllerObserver = observer;
        //this.colorChooserPanel.setObserver(observer);
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
    }

    private void showColorChooser() {
        boolean isDarkSide = gameModel.isDarkSide();
        
        // 1. Crea l'istanza del pannello ColorChooser
        ColorChooserPanel panel = new ColorChooserPanel(this.controllerObserver, isDarkSide); 
        
        // 2. Utilizza showOptionDialog per rimuovere i pulsanti di default 
        // e impedire la chiusura tramite il tasto 'X'.
        JOptionPane pane = new JOptionPane(
            panel, 
            JOptionPane.PLAIN_MESSAGE, // Tipo di messaggio (senza icone)
            JOptionPane.DEFAULT_OPTION, // Nessun pulsante 'OK'/'Cancel'
            null, // Nessuna icona
            new Object[]{}, // Array vuoto per le opzioni (rimuove i pulsanti standard)
            null
        );
        
        // 3. Imposta l'azione predefinita per la chiusura della finestra:
        // impedisce la chiusura tramite il tasto 'X' e Alt+F4.
        pane.setInitialValue(null);
        pane.setWantsInput(false);
        
        // Crea la finestra di dialogo modale che ospita il JOptionPane
        JDialog dialog = pane.createDialog(this, "Scegli un Colore");
        
        // Imposta la chiusura su DO_NOTHING_ON_CLOSE per bloccare il tasto 'X'
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Mostra la finestra (blocca il thread fino alla chiusura)
        dialog.setVisible(true);
    }

    private void showPlayerChooser() {
        // 1. Crea l'istanza del pannello ColorChooser
        PlayerChooserPanel panel = new PlayerChooserPanel(this.controllerObserver, gameModel.getPlayers()); 
        
        // 2. Utilizza showOptionDialog per rimuovere i pulsanti di default 
        // e impedire la chiusura tramite il tasto 'X'.
        JOptionPane pane = new JOptionPane(
            panel, 
            JOptionPane.PLAIN_MESSAGE, // Tipo di messaggio (senza icone)
            JOptionPane.DEFAULT_OPTION, // Nessun pulsante 'OK'/'Cancel'
            null, // Nessuna icona
            new Object[]{}, // Array vuoto per le opzioni (rimuove i pulsanti standard)
            null
        );
        
        // 3. Imposta l'azione predefinita per la chiusura della finestra:
        // impedisce la chiusura tramite il tasto 'X' e Alt+F4.
        pane.setInitialValue(null);
        pane.setWantsInput(false);
        
        // Crea la finestra di dialogo modale che ospita il JOptionPane
        JDialog dialog = pane.createDialog(this, "Scegli un Giocatore");
        
        // Imposta la chiusura su DO_NOTHING_ON_CLOSE per bloccare il tasto 'X'
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        
        // Mostra la finestra (blocca il thread fino alla chiusura)
        dialog.setVisible(true);
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
            if (isHumanTurn && !isColorDialogShowing) { 
                System.out.println("Mostro il pannello di scelta colore.");
                
                this.isColorDialogShowing = true;

                showColorChooser();

                this.isColorDialogShowing = false;
            }
            statusLabel.setText("Scegli un colore!");
        } else if (gameModel.getGameState() == GameState.WAITING_FOR_PLAYER){
            setHumanInputEnabled(false); 
            if (isHumanTurn && !isPlayerDialogShowing) { 
                System.out.println("Mostro il pannello di scelta colore.");
                
                this.isPlayerDialogShowing = true;

                showPlayerChooser();

                this.isPlayerDialogShowing = false;
            }
            statusLabel.setText("Scegli un giocatore!");
        } else if (gameModel.getGameState() == GameState.RUNNING) {
            this.isColorDialogShowing = false;
            this.isPlayerDialogShowing = false;

            String direction = gameModel.isClockwise() ? "Orario" : "Antiorario";
            statusLabel.setText("<html><div style='text-align: center;'>Turno di: " 
                + gameModel.getCurrentPlayer().getName() 
                + "<br>Direzione: " + direction + "</div></html>"); // <-- USA <br> PER ANDARE A CAPO
        }
        
        // --- Aggiornamento Pila Scarti ---
        if (gameModel.isDiscardPileEmpty()) {
            discardPileCard.setText("Vuota");
            discardPileCard.setIcon(null);
            discardPileCard.setBackground(Color.LIGHT_GRAY);
        } else {
            Card topCard = gameModel.getTopDiscardCard();
            String cardName = topCard.getColor(gameModel).name() + "_" + topCard.getValue(gameModel).name();
            ImageIcon icon = cardImageLoader.getImage(cardName);
            
            CardColor activeColor = gameModel.getCurrentColor();
            System.out.println("Colore attivo: " + activeColor);

            if (icon != null) {
                discardPileCard.setIcon(icon);
                discardPileCard.setText(null); // Rimuovi testo di fallback
            } else {
                discardPileCard.setIcon(null);
                discardPileCard.setText("<html><div style='text-align: center;'>" + topCard.getValue(gameModel) + "<br>" + topCard.getColor(gameModel) + "</div></html>");
            }

            discardPileCard.setBorder(BorderFactory.createLineBorder(convertCardColor(activeColor), 4));
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

        // 1. Definisce le opzioni dei pulsanti
        Object[] options = {
            "Torna al Menu",  // Opzione 0
            "Chiudi Gioco"   // Opzione 1
        };

        // 2. Mostra il popup personalizzato
        int choice = JOptionPane.showOptionDialog(
            this,                                     // Parent component
            winnerName + " ha vinto la partita!\nCosa vuoi fare?", // Messaggio
            "Partita Terminata",                      // Titolo
            JOptionPane.YES_NO_CANCEL_OPTION,         // Tipo di opzioni (anche se usiamo opzioni custom)
            JOptionPane.INFORMATION_MESSAGE,          // Tipo di icona
            null,                                     // Icona custom (null per default)
            options,                                  // Le opzioni definite sopra
            options[0]                                // Opzione di default
        );

        // 3. Gestisce la scelta dell'utente
        if (controllerObserver != null) { // Controlla sempre che il controller sia impostato
            switch (choice) {
                case 0: // Torna al Menu
                    // Richiama la logica per tornare al menu, gestita nel Controller
                    controllerObserver.onBackToMenu(); 
                    break;
                case 1: // Chiudi Gioco
                    System.exit(0);
                    break;
                case JOptionPane.CLOSED_OPTION:
                    // Se la finestra viene chiusa (X), esce dal gioco.
                    System.exit(0); 
                    break;
                default:
                    break;
            }
        }
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
        
        // --- Preparazione degli elementi ---
        this.drawDeckButton = new JButton();
        styleAsCardButton(this.drawDeckButton, "CARD_BACK"); // Usa l'immagine del dorso
        
        this.discardPileCard = new JLabel("SCARTI");
        this.discardPileCard.setPreferredSize(new Dimension(100, 150));
        this.discardPileCard.setFont(BOLD_FONT);
        this.discardPileCard.setHorizontalAlignment(JLabel.CENTER);
        this.discardPileCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.discardPileCard.setOpaque(true); 
        
        this.passButton = createStyledButton("Passa", BUTTON_COLOR_PASS, Color.WHITE, 100, 40);

        this.unoButton = createStyledButton("UNO!", new Color(255, 193, 7), Color.BLACK, 100, 40); // Giallo

        // Nota: Assumiamo che drawDeckButton, discardPileCard e passButton
        // siano stati inizializzati correttamente prima di questo metodo.

        // Crea i pannelli ausiliari (Assumiamo che i metodi esistano e siano validi)
        JPanel settingsPanel = createSettingsPanel(); 
        JPanel infoPanel = createInfoPanel();

        // Pannelli di riempimento (Spacer) per il centraggio orizzontale
        JPanel horizontalSpacerLeft = new JPanel();
        horizontalSpacerLeft.setOpaque(false);
        JPanel horizontalSpacerRight = new JPanel();
        horizontalSpacerRight.setOpaque(false);
        JPanel verticalSpacerTop = new JPanel();
        verticalSpacerTop.setOpaque(false);

        // ----------------------------------------------------------------------
        // LAYOUT GBC: 6 colonne
        // ----------------------------------------------------------------------

        // 1. SETTINGS PANEL (Menu): Posizione (0, 0) - Alto a Sinistra
        gbc.gridx = 0; 
        gbc.gridy = 0;
        gbc.gridwidth = 1; 
        gbc.anchor = GridBagConstraints.NORTHWEST; 
        gbc.weightx = 0.0; 
        gbc.weighty = 0.0; 
        gbc.fill = GridBagConstraints.NONE;
        panel.add(settingsPanel, gbc);
        
        // --- Riga 1 (Contenuto Centrale) ---

        // 1. SPACER SUPERIORE: Colonna 1, Row 0
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5; // Prende spazio verticale
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(verticalSpacerTop, gbc);
        
        // 2. SPACER SINISTRO: Colonna 1, Row 1. Prende metà dello spazio extra
        gbc.gridx = 1; 
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0.5; // Peso per spingere il contenuto centrale
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(horizontalSpacerLeft, gbc);

        // 3. DRAW DECK (Pesca): Colonna 2, Row 1
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.weightx = 0.0; // Non prende spazio extra
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(drawDeckButton, gbc); 
        
        // 4. DISCARD PILE (Scarti): Colonna 3, Row 1
        gbc.gridx = 3; 
        gbc.gridy = 1;
        gbc.weightx = 0.0; 
        gbc.weighty = 0.0; 
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(discardPileCard, gbc); 
        
        // 5. SPACER DESTRO: Colonna 4, Row 1. Prende l'altra metà dello spazio extra
        gbc.gridx = 4; 
        gbc.gridy = 1;
        gbc.weightx = 0.5; // Peso uguale a Spacer L per centrare
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(horizontalSpacerRight, gbc);

        // 6. INFO PANEL: Colonna 5, Row 1
        gbc.gridx = 5; 
        gbc.gridy = 0;
        gbc.weightx = 0.0; 
        gbc.weighty = 0.0; 
        gbc.fill = GridBagConstraints.NONE; 
        gbc.anchor = GridBagConstraints.NORTHEAST;
        panel.add(infoPanel, gbc);
        
        // --- Riga 2 (Pulsante Passa) ---
        
        // 7. PASS BUTTON: Posizione (5, 2) - Basso a Destra
        // Usiamo l'ultima colonna (5) per l'allineamento a destra.
        gbc.gridx = 2; 
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0; // IMPORTANTE: Spinge il bottone verso il basso
        gbc.anchor = GridBagConstraints.CENTER; // Ancoraggio in basso a destra
        gbc.fill = GridBagConstraints.NONE;
        panel.add(passButton, gbc); 

        // 7. UNO BUTTON: Posizione (5, 2) - Basso a Destra
        // Usiamo l'ultima colonna (5) per l'allineamento a destra.
        gbc.gridx = 3; 
        gbc.gridy = 2;
        gbc.weightx = 0.0;
        gbc.weighty = 1.0; // IMPORTANTE: Spinge il bottone verso il basso
        gbc.anchor = GridBagConstraints.CENTER; // Ancoraggio in basso a destra
        gbc.fill = GridBagConstraints.NONE;
        panel.add(unoButton, gbc); 
        
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

    private JPanel createSettingsPanel() {
        // Usa FlowLayout allineato a destra per spingere il pulsante verso l'angolo
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);
        
        // Crea il pulsante usando lo stile esistente, con dimensioni ridotte
        this.settingsButton = createStyledButton("Menu", new Color(70, 70, 70), Color.WHITE, 80, 30);
        
        panel.add(this.settingsButton);
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

    /**
     * NUOVO: Metodo per applicare stile-carta a un JButton.
     */
    private void styleAsCardButton(JButton button, String cardName) {
        ImageIcon icon = cardImageLoader.getImage(cardName);
        ImageIcon transparentIcon = cardImageLoader.getTransparentImage(cardName);
        if (icon != null) {
            button.setIcon(icon);
            button.setDisabledIcon(transparentIcon);
            button.setText(null);
        } else {
            // Fallback se l'immagine non è trovata
            button.setText(cardName.replace("_", " "));
            button.setForeground(Color.WHITE); //Imposta il testo bianco
        }
        
        button.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    private JButton createCardButton(Card card) {
        String cardName = card.getColor(gameModel).name() + "_" + card.getValue(gameModel).name();
        JButton button = new JButton();
        styleAsCardButton(button, cardName); // Applica lo stile
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
            case ORANGE: return new Color(255, 140, 0);
            case PURPLE: return new Color(128, 0, 128);
            case PINK: return new Color(255, 105, 180);
            case TEAL: return new Color(0, 128, 128);
            case WILD: default: return Color.DARK_GRAY;
        }
    }
}