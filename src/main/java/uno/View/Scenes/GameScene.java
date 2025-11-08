package uno.View.Scenes;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Player.*;
import uno.Model.Game.GameState; // <-- IMPORTA
import uno.Model.Cards.Attributes.CardColor;
import uno.Controller.GameViewObserver;
import uno.View.GameModelObserver;
import uno.View.Components.ColorChooserPanel; // <-- IMPORTA


import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;


/**
 * Pannello (JPanel) che rappresenta la schermata di gioco principale.
 * Implementa GameModelObserver per essere aggiornata dal modello Game.
 */
public class GameScene extends JPanel implements GameModelObserver {

    private  Game gameModel;
    private GameViewObserver controllerObserver;

    // Pannelli principali
    private  JPanel opponentsPanel;
    private  JPanel centerPanel;
    private  JPanel playerHandPanel;
    private  JPanel infoPanel;
    private  ColorChooserPanel colorChooserPanel; // <-- NUOVO CAMPO

    // Componenti dinamici
    private  JLabel discardPileCard;
    private  JButton drawDeckButton;
    private JButton passButton;
    private  JLabel statusLabel;
    private  JButton unoButton;

    public GameScene(Game gameModel) {
        super(new BorderLayout(10, 10));
        this.gameModel = gameModel;
        this.gameModel.addObserver(this); // Si registra al modello

        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Creazione Pannelli ---
        opponentsPanel = createOpponentsPanel();
        centerPanel = createCenterPanel(); 
        playerHandPanel = createPlayerHandPanel();
        
        // --- Pannello Est (Info + Scelta Colore) ---
        // Usiamo un pannello contenitore per impilare info e scelta colore
        JPanel eastPanel = new JPanel();
        eastPanel.setLayout(new BoxLayout(eastPanel, BoxLayout.Y_AXIS));
        
        infoPanel = createInfoPanel();
        colorChooserPanel = new ColorChooserPanel(); // <-- CREA IL PANNELLO
        
        eastPanel.add(infoPanel);
        eastPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spaziatore
        eastPanel.add(colorChooserPanel); // <-- AGGIUNGI
        eastPanel.add(Box.createVerticalGlue()); // Spinge in alto i componenti

        // --- Assemblaggio Layout ---
        add(opponentsPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        
        // Pannello per la mano e il bottone UNO
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JScrollPane(playerHandPanel), BorderLayout.CENTER);
        
        JPanel unoButtonPanel = new JPanel(); // Pannello per centrare il bottone UNO
        unoButton = new JButton("UNO!");
        unoButton.setFont(new Font("Arial", Font.BOLD, 24));
        unoButton.setPreferredSize(new Dimension(100, 50));
        unoButtonPanel.add(unoButton);
        unoButtonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        southPanel.add(unoButtonPanel, BorderLayout.EAST);
        add(southPanel, BorderLayout.SOUTH);
        
        add(eastPanel, BorderLayout.EAST); // <-- AGGIUNGI IL PANNELLO 'east'

        // --- Collegamento Azioni -> Controller ---
        drawDeckButton.addActionListener(e -> {
            if (controllerObserver != null) {
                controllerObserver.onDrawCard();
            }
        });

        passButton.addActionListener(e -> {
            if (controllerObserver != null) {
                controllerObserver.onPassTurn();
            }
        });

        unoButton.addActionListener(e -> {
            if (controllerObserver != null) {
                controllerObserver.onCallUno();
            }
        });
        
        // Prima visualizzazione
        onGameUpdate();
    }

    /**
     * Imposta il controller che ascolterà gli eventi di questa scena.
     * Passa l'observer anche al pannello figlio.
     */
    public void setObserver(GameViewObserver observer) {
        this.controllerObserver = observer;
        this.colorChooserPanel.setObserver(observer); // <-- COLLEGA IL SOTTO-PANNELLO
    }

    /**
     * NUOVO METODO: Abilita o disabilita tutti i controlli di input umano.
     * Chiamato dal GameController.
     * @param enabled true per abilitare, false per disabilitare.
     */
    public void setHumanInputEnabled(boolean enabled) {
        this.unoButton.setEnabled(enabled);
        
        // Logica specifica: i bottoni di gioco sono attivi
        // SOLO se è il turno dell'umano E non ha ancora pescato.
        boolean hasDrawn = gameModel.hasCurrentPlayerDrawn(gameModel.getCurrentPlayer());
        
        this.drawDeckButton.setEnabled(enabled && !hasDrawn);
        this.passButton.setEnabled(enabled && hasDrawn);

        // Abilita/Disabilita le carte in mano
        for (Component comp : playerHandPanel.getComponents()) {
            if (comp instanceof JButton) {
                // Le carte si possono giocare solo se non hai pescato
                comp.setEnabled(enabled && !hasDrawn);
            }
        }
        
        if (!enabled) {
            colorChooserPanel.setVisible(false); // Nascondi se l'IA sta giocando
        }
    }

    /**
     * Metodo chiamato dal Modello (Game) quando lo stato cambia.
     */
    @Override
    public void onGameUpdate() {
        
        // --- LOGICA PER MOSTRARE/NASCONDERE LA SCELTA COLORE ---
        if (gameModel.getGameState() == GameState.WAITING_FOR_COLOR) {
            colorChooserPanel.setVisible(true);
            statusLabel.setText("Scegli un colore!");
            // Disabilita gli altri bottoni
            drawDeckButton.setEnabled(false);
            unoButton.setEnabled(false);
        } else {
            colorChooserPanel.setVisible(false);
            statusLabel.setText("Turno di: " + gameModel.getCurrentPlayer().getName());
            // Ri-abilita i bottoni
            drawDeckButton.setEnabled(true);
            unoButton.setEnabled(true);
        }

        // --- LOGICA AGGIUNTA PER IL BOTTONE PASSA ---
        if (gameModel.getGameState() == GameState.RUNNING && gameModel.hasCurrentPlayerDrawn(gameModel.getCurrentPlayer())) {
            passButton.setEnabled(true);
        } else {
            passButton.setEnabled(false);
        }
        
        // 2. Aggiorna Pila degli Scarti
        if (gameModel.isDiscardPileEmpty()) {
            discardPileCard.setText("Vuota");
            discardPileCard.setBackground(Color.LIGHT_GRAY);
        } else {
            Card topCard = gameModel.getTopDiscardCard();
            discardPileCard.setText(topCard.toString()); 
            
            // Imposta il colore di sfondo della label
            discardPileCard.setBackground(convertCardColor(gameModel.getCurrentColor()));
            discardPileCard.setForeground(Color.BLACK); // Resetta il testo a nero
            
            if(gameModel.getCurrentColor() == CardColor.BLUE || 
               gameModel.getCurrentColor() == CardColor.RED ||
               gameModel.getCurrentColor() == CardColor.WILD) {
                discardPileCard.setForeground(Color.WHITE);
            }
        }

        // 3. Aggiorna Mano del Giocatore (Umano)
        playerHandPanel.removeAll(); 
        
        // Assumiamo che il giocatore umano sia il primo
        Player humanPlayer = gameModel.getPlayers().get(0); // Meglio un metodo game.getHumanPlayer()
        
        for (Card card : humanPlayer.getHand()) {
            JButton cardButton = new JButton(card.toString());
            cardButton.setPreferredSize(new Dimension(80, 120));
            // TODO: Aggiungere stile al bottone della carta
            
            // Disabilita i bottoni delle carte se non è il tuo turno
            // o se stai scegliendo un colore
            if (gameModel.getGameState() != GameState.RUNNING || 
                gameModel.getCurrentPlayer() != humanPlayer) {
                cardButton.setEnabled(false);
            }
            
            cardButton.addActionListener(e -> {
                if (controllerObserver != null) {
                    controllerObserver.onPlayCard(card);
                }
            });
            playerHandPanel.add(cardButton);
        }

        // 4. Aggiorna Pannello Avversari
        opponentsPanel.removeAll();
        for (int i = 1; i < gameModel.getPlayers().size(); i++) {
            Player ai = gameModel.getPlayers().get(i);
            JLabel aiLabel = new JLabel(ai.getName() + ": " + ai.getHandSize() + " carte");
            aiLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
            
            // Evidenzia il giocatore di turno
            if(gameModel.getCurrentPlayer() == ai) {
                aiLabel.setFont(new Font("Arial", Font.BOLD, 14));
                aiLabel.setBorder(BorderFactory.createLineBorder(Color.ORANGE, 2));
            }
            
            opponentsPanel.add(aiLabel);
        }

        // Forza il ridisegno dei pannelli aggiornati
        revalidate();
        repaint();
    }

    /**
     * Mostra un popup che annuncia il vincitore e blocca la scena.
     * @param winnerName Il nome del giocatore che ha vinto.
     */
    public void showWinnerPopup(String winnerName) {
        // Assicura che l'input sia disabilitato
        setHumanInputEnabled(false);
        
        // Mostra un popup modale (blocca l'interazione)
        JOptionPane.showMessageDialog(
            this, // Il genitore è questa GameScene
            winnerName + " ha vinto la partita!", 
            "Partita Terminata", 
            JOptionPane.INFORMATION_MESSAGE
        );
        
        // TODO: A questo punto potresti voler mostrare un bottone "Torna al Menu"
        // che chiama controllerObserver.onBackToMenu()
    }

    // --- Metodi di Inizializzazione GUI ---

    private JPanel createOpponentsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Avversari"));
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        this.drawDeckButton = new JButton("[MAZZO]");
        this.drawDeckButton.setPreferredSize(new Dimension(80, 120));
        
        this.discardPileCard = new JLabel("SCARTI");
        this.discardPileCard.setPreferredSize(new Dimension(80, 120));
        this.discardPileCard.setHorizontalAlignment(JLabel.CENTER);
        this.discardPileCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        this.discardPileCard.setOpaque(true); // Per mostrare il colore di sfondo

        this.passButton = new JButton("Passa"); // <-- INIZIALIZZA IL BOTTONE
        this.passButton.setPreferredSize(new Dimension(80, 50));
        this.passButton.setEnabled(false); // Disabilitato di default, si abilita dopo aver pescato.
        
        panel.add(this.drawDeckButton);
        panel.add(this.discardPileCard);
        panel.add(this.passButton);

        return panel;
    }

    private JPanel createPlayerHandPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("La tua Mano"));
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Info Partita"));
        
        this.statusLabel = new JLabel("Turno di: ..."); 
        this.statusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        this.statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.statusLabel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        panel.add(this.statusLabel);
        return panel;
    }
    
    /**
     * Converte un CardColor in un Color di Swing.
     */
    private Color convertCardColor(CardColor cardColor) {
        if (cardColor == null) return Color.LIGHT_GRAY;
        
        switch (cardColor) {
            case RED: return new Color(211, 47, 47);
            case BLUE: return new Color(33, 150, 243);
            case GREEN: return new Color(76, 175, 80);
            case YELLOW: return new Color(255, 235, 59);
            case WILD: // Se il colore è WILD, significa che è stata appena giocata
            default:
                return Color.DARK_GRAY;
        }
    }
}