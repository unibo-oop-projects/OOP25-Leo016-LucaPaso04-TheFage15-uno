package uno.controller.impl;

import uno.controller.api.GameController;
import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.game.Game;
import uno.model.game.GameState;
import uno.model.players.AIPlayer;
import uno.model.players.Player;
import uno.view.GameFrame;
import uno.view.scenes.GameScene;
import uno.view.scenes.MenuScene;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Concrete implementation of the GameController interface.
 * It manages the interaction logic between the GameScene (View) and the Game (Model).
 */

public class GameControllerImpl implements GameController {

    private static final int AI_DELAY = 5000;
    private static final int DIALOG_DELAY = 3000;

    private final Game gameModel;
    private final GameScene gameScene;
    private final GameFrame mainFrame;

    private Timer aiTimer; // Timer per il ritardo dell'IA

    /**
     * Costruttore del GameControllerImpl.
     * @param gameModel
     * @param gameScene
     * @param mainFrame
     */
    public GameControllerImpl(final Game gameModel, final GameScene gameScene, final GameFrame mainFrame) {
        this.gameModel = gameModel;
        this.gameScene = gameScene;
        this.mainFrame = mainFrame;

        this.gameModel.addObserver(this);
    }

    /**
     * Mostra un popup con il giocatore iniziale e avvia il gioco.
     */
    @Override
    public void showStartingPlayerPopupAndStartGame() {
        // 1. Otteniamo il giocatore che inizia (scelto a caso dal TurnManager)
        final Player startingPlayer = gameModel.getCurrentPlayer();
        final String msg = "Inizia: " + startingPlayer.getName();

        // 2. Creiamo il pannello del messaggio
        final JOptionPane pane = new JOptionPane(msg, JOptionPane.INFORMATION_MESSAGE);

        // 3. Creiamo il Dialog (modale = blocca il codice)
        final JDialog dialog = pane.createDialog(gameScene, "Inizio Partita");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Impedisce la chiusura manuale

        // 4. Creiamo un Thread per chiudere il dialog dopo 3 secondi
        new Thread(() -> {
            try {
                Thread.sleep(DIALOG_DELAY);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            dialog.dispose(); // Chiude il dialog e sblocca il codice qui sotto
        }).start();

        // 5. Mostriamo il dialog. Il codice SI FERMA QUI finché il dialog non si chiude.
        dialog.setVisible(true);

        // 6. ORA che il popup è sparito, facciamo partire il gioco
        onGameUpdate();
    }

    /**
     * Metodo di aggiornamento chiamato dal Modello (Game).
     * Questo è il "Game Loop" che attiva l'IA.
     */
    @Override
    public void onGameUpdate() {
        // --- CONTROLLO STATO PARTITA ---
        // Il Controller REAGISCE allo stato impostato dal Modello

        if (gameModel.getGameState() == GameState.GAME_OVER) {
            // Se il modello dice che il gioco è finito, fermiamo tutto.
            if (aiTimer != null) {
                aiTimer.stop(); // Ferma il timer dell'IA
            }
            gameScene.setHumanInputEnabled(false); // Disabilita tutti i bottoni

            // Mostra il messaggio di vittoria
            final Player winner = gameModel.getWinner();
            gameScene.showWinnerPopup(winner.getName());
            return; // Non fare nient'altro
        }

        // Ogni volta che il gioco si aggiorna, controlliamo chi sta giocando
        checkAndRunAITurn();
    }

    /**
     * Controlla se il giocatore corrente è un'IA. Se sì, avvia il suo turno.
     */
    private void checkAndRunAITurn() {
        // Se il gioco sta aspettando un input (colore) o è finito, non fare nulla.
        if (gameModel.getGameState() != GameState.RUNNING) {
            return;
        }

        final Player currentPlayer = gameModel.getCurrentPlayer();

        // Controlla se il giocatore è un'istanza di AIPlayer
        if (currentPlayer instanceof AIPlayer) {

            // Disabilita la UI umana per evitare input concorrenti
            gameScene.setHumanInputEnabled(false); 

            // L'IA non gioca subito. Creiamo un Timer per un breve ritardo.

            final ActionListener aiTask = new ActionListener() {
                @Override
                public void actionPerformed(final ActionEvent e) {
                    // Esegui la logica decisionale dell'IA
                    ((AIPlayer) currentPlayer).takeTurn(gameModel);
                    // (Questo chiamerà game.playCard o game.passTurn,
                    // che a sua volta chiamerà notifyObservers()
                    // e farà ripartire questo ciclo onGameUpdate())
                }
            };

            aiTimer = new Timer(AI_DELAY, aiTask);
            aiTimer.setRepeats(false); // Esegui solo una volta
            aiTimer.start();

        } else {
            // È il turno di un giocatore umano
            gameScene.setHumanInputEnabled(true);
        }
    }

    // --- Metodi chiamati dalla View (Input Umano) ---

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onPlayCard(final Card card) {
        System.out.println("Tentativo di giocare la carta: " + card);
        try {
            // Comanda al modello di giocare la carta
            gameModel.playCard(card);
            // Il modello notificherà la GameScene per l'aggiornamento
            // (grazie a gameModel.addObserver(gameScene) nel costruttore)

        } catch (final Exception e) {
            // Mostra un errore se la mossa non è valida
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), 
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onDrawCard() {
        System.out.println("L'utente clicca 'Pesca'");
        try {
            // Chiama il nuovo metodo con la logica di validazione
            gameModel.playerInitiatesDraw(); 
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), // Messaggio d'errore (es. "Hai già pescato")
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onCallUno() {
        System.out.println("L'utente clicca 'UNO!'");
        try {
            gameModel.callUno(gameModel.getPlayers().getFirst());
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), // Messaggio d'errore (es. "Non puoi chiamare UNO ora")
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onBackToMenu() {
        // Logica per tornare al menu
        final int choice = JOptionPane.showConfirmDialog(
            gameScene, 
            "Sei sicuro di voler tornare al menu? La partita sarà persa.",
            "Torna al Menu", 
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // Ricrea il controller e la scena del menu
            final MenuControllerImpl menuController = new MenuControllerImpl(mainFrame);
            final MenuScene menuScene = new MenuScene();
            menuScene.setObserver(menuController);
            mainFrame.showScene(menuScene);
        }
    }

    /**
     * Implementazione del nuovo metodo "Passa".
     */
    @Override
    public void onPassTurn() {
        System.out.println("L'utente clicca 'Passa'");
        try {
            gameModel.playerPassTurn();
        } catch (final Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), // Messaggio (es. "Non puoi passare se non hai pescato")
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     * Riceve il colore scelto dalla View e lo passa al Modello.
     * @param color Il colore scelto.
     */
    @Override
    public void onColorChosen(final CardColor color) {
        System.out.println("Colore scelto: " + color);
        // Il GameModel riceverà il colore, imposterà il suo stato
        // interno e notificherà la View (che si aggiornerà di nuovo).

        gameModel.setColor(color);
    }

    /**
     * Implementa il metodo dell'interfaccia.
     * Riceve il giocatore scelto dalla View e lo passa al Modello.
     * @param player Il giocatore scelto.
     */
    @Override
    public void onPlayerChosen(final Player player) {
        System.out.println("Giocatore scelto: " + player.getName());
        gameModel.choosenPlayer(player);
    }
}
