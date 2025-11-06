package uno.View.Scenes;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Controller.GameViewObserver;
import uno.View.GameModelObserver;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

/**
 * Pannello (JPanel) che rappresenta la schermata di gioco principale.
 * Implementa GameModelObserver per essere aggiornata dal modello Game.
 */
public class GameScene extends JPanel implements GameModelObserver {

    private final Game gameModel;
    private GameViewObserver controllerObserver;

    // Pannelli principali
    private final JPanel opponentsPanel;
    private final JPanel centerPanel;
    private final JPanel playerHandPanel;
    private final JPanel infoPanel;

    // Componenti dinamici
    private final JLabel discardPileCard;
    private final JButton drawDeckButton;
    private final JLabel statusLabel;
    private final JButton unoButton;

    public GameScene(Game gameModel) {
        super(new BorderLayout(10, 10));
        this.gameModel = gameModel;
        this.gameModel.addObserver(this); // Si registra al modello

        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Creazione Pannelli ---
        opponentsPanel = createOpponentsPanel();
        centerPanel = createCenterPanel();
        playerHandPanel = createPlayerHandPanel();
        infoPanel = createInfoPanel();

        // --- Assemblaggio Layout ---
        add(opponentsPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(new JScrollPane(playerHandPanel), BorderLayout.SOUTH); // Mano del giocatore scrollabile
        add(infoPanel, BorderLayout.EAST);

        // --- Componenti dinamici (per riferimento) ---
        discardPileCard = (JLabel) centerPanel.getComponent(1);
        drawDeckButton = (JButton) centerPanel.getComponent(0);
        statusLabel = (JLabel) infoPanel.getComponent(0);
        unoButton = (JButton) ((JPanel) playerHandPanel.getParent().getParent().getComponent(0)).getComponent(1); // Complesso, ma prende il bottone UNO

        // --- Collegamento Azioni -> Controller ---
        drawDeckButton.addActionListener(e -> {
            if (controllerObserver != null) {
                controllerObserver.onDrawCard();
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
     */
    public void setObserver(GameViewObserver observer) {
        this.controllerObserver = observer;
    }

    /**
     * Metodo chiamato dal Modello (Game) quando lo stato cambia.
     */
    @Override
    public void onGameUpdate() {
        // 1. Aggiorna etichetta di stato
        statusLabel.setText("Turno di: " + gameModel.getCurrentPlayer().getName()); 
        // TODO: Aggiungere info colore corrente, ecc.

        // 2. Aggiorna Pila degli Scarti
        if (gameModel.getDiscardPile().isEmpty()) {
            discardPileCard.setText("Vuota");
        } else {
            Card topCard = gameModel.getDiscardPile().get(gameModel.getDiscardPile().size() - 1);
            discardPileCard.setText(topCard.toString()); // Usiamo toString() per ora
        }

        // 3. Aggiorna Mano del Giocatore (Umano)
        playerHandPanel.removeAll(); // Rimuove le vecchie carte
        
        // Assumiamo che il giocatore umano sia il primo
        for (Card card : gameModel.getCurrentPlayer().getHand()) {
            JButton cardButton = new JButton(card.toString());
            cardButton.setPreferredSize(new Dimension(80, 120));
            cardButton.addActionListener(e -> {
                if (controllerObserver != null) {
                    controllerObserver.onPlayCard(card);
                }
            });
            playerHandPanel.add(cardButton);
        }

        // 4. Aggiorna Pannello Avversari
        opponentsPanel.removeAll();
        // TODO: Aggiungere logica per mostrare gli altri giocatori
        opponentsPanel.add(new JLabel("Avversario 1: " + "X carte"));

        // Forza il ridisegno dei pannelli aggiornati
        revalidate();
        repaint();
    }

    // --- Metodi di Inizializzazione GUI ---

    private JPanel createOpponentsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Avversari"));
        panel.add(new JLabel("Avversario 1: X carte"));
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        JButton deckBtn = new JButton("[MAZZO]");
        deckBtn.setPreferredSize(new Dimension(80, 120));
        
        JLabel discardLabel = new JLabel("SCARTI");
        discardLabel.setPreferredSize(new Dimension(80, 120));
        discardLabel.setHorizontalAlignment(JLabel.CENTER);
        discardLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        panel.add(deckBtn);
        panel.add(discardLabel);
        return panel;
    }

    private JPanel createPlayerHandPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("La tua Mano"));
        // I bottoni delle carte verranno aggiunti dinamicamente in onGameUpdate()
        return panel;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("Info Partita"));
        
        panel.add(new JLabel("Turno di: ...")); // Sarà aggiornato
        
        JButton unoBtn = new JButton("UNO!");
        unoBtn.setPreferredSize(new Dimension(100, 40));
        panel.add(unoBtn);
        
        return panel;
    }
}