package uno.controller.impl;

import uno.controller.api.GameController;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameState;
import uno.model.players.impl.AIPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.model.players.api.Player;
import uno.view.scenes.impl.MenuSceneImpl;
import uno.model.game.api.Game;
import uno.view.scenes.api.GameScene;
import uno.view.api.GameFrame;
import uno.model.utils.api.GameLogger;

import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;

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
    private final GameLogger logger;

    private Optional<Timer> aiTimer = Optional.empty();

    /**
     * Costruttore del GameControllerImpl.
     * @param gameModel
     * @param gameScene
     * @param mainFrame
     */
    public GameControllerImpl(final Game gameModel, final GameScene gameScene, 
        final GameFrame mainFrame, final GameLogger logger) {
        this.gameModel = gameModel;
        this.gameScene = gameScene;
        this.mainFrame = mainFrame;
        this.logger = logger;

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
        final JDialog dialog = pane.createDialog((javax.swing.JPanel) gameScene, "Inizio Partita");
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // Impedisce la chiusura manuale

        // 4. Creiamo un Thread per chiudere il dialog dopo 3 secondi
        new Thread(() -> {
            try {
                Thread.sleep(DIALOG_DELAY);
            } catch (final InterruptedException e) {
                logger.logAction("ERROR", "EXCEPTION_CAUGHT", e.getClass().getSimpleName(), e.getMessage());
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
        final boolean isHumanTurn = gameModel.getCurrentPlayer().getClass() == HumanPlayer.class;

        if (gameModel.getGameState() == GameState.GAME_OVER) {
            // Se il modello dice che il gioco è finito, fermiamo tutto.
            if (aiTimer.isPresent()) {
                aiTimer.get().stop(); // Ferma il timer dell'IA
            }
            gameScene.setHumanInputEnabled(false); // Disabilita tutti i bottoni

            // Mostra il messaggio di vittoria
            final Player winner = gameModel.getWinner();
            gameScene.showWinnerPopup(winner.getName());
            return; // Non fare nient'altro
        }

        if (isHumanTurn) {
            // Il modello dice che serve un colore?
            if (gameModel.getGameState() == GameState.WAITING_FOR_COLOR) {
                // IL CONTROLLER COMANDA LA VIEW
                gameScene.showColorChooser(gameModel.isDarkSide());
            }

            // Il modello dice che serve scegliere un giocatore?
            if (gameModel.getGameState() == GameState.WAITING_FOR_PLAYER) {
                // IL CONTROLLER COMANDA LA VIEW
                gameScene.showPlayerChooser(gameModel.getPlayers());
            }
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
                    currentPlayer.takeTurn(gameModel);
                    // (Questo chiamerà game.playCard o game.passTurn,
                    // che a sua volta chiamerà notifyObservers()
                    // e farà ripartire questo ciclo onGameUpdate())
                }
            };

            aiTimer = Optional.of(new Timer(AI_DELAY, aiTask));
            aiTimer.get().setRepeats(false); // Esegui solo una volta
            aiTimer.get().start();
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
    public void onPlayCard(final Optional<Card> card) {
        try {
            gameModel.playCard(card);
        } catch (final Exception e) {
            // Mostra un errore se la mossa non è valida
            JOptionPane.showMessageDialog((javax.swing.JPanel) gameScene, 
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
            JOptionPane.showMessageDialog((javax.swing.JPanel) gameScene, 
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
            JOptionPane.showMessageDialog((javax.swing.JPanel) gameScene, 
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
            (javax.swing.JPanel) gameScene, 
            "Sei sicuro di voler tornare al menu? La partita sarà persa.",
            "Torna al Menu", 
            JOptionPane.YES_NO_OPTION);

        if (choice == JOptionPane.YES_OPTION) {
            // Ricrea il controller e la scena del menu
            final MenuControllerImpl menuController = new MenuControllerImpl(mainFrame);
            final MenuSceneImpl menuScene = new MenuSceneImpl();
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
            JOptionPane.showMessageDialog((javax.swing.JPanel) gameScene, 
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
        gameModel.chosenPlayer(player);
    }
}
