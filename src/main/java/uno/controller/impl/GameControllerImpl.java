package uno.controller.impl;

import uno.controller.api.GameController;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameState;
import uno.model.players.impl.AbstractAIPlayer;
import uno.model.players.api.AbstractPlayer;
import uno.view.scenes.impl.MenuSceneImpl;
import uno.model.game.api.Game;
import uno.view.scenes.api.GameScene;
import uno.view.api.GameFrame;
import uno.model.players.impl.HumanPlayer;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete implementation of the GameController interface.
 * It manages the interaction logic between the GameScene (View) and the Game
 * (Model).
 */

public class GameControllerImpl implements GameController {

    private static final int AI_DELAY = 5000;

    private final Game gameModel;
    private final GameScene gameScene;
    private final GameFrame mainFrame;

    private Optional<Timer> aiTimer = Optional.empty();

    /**
     * Costruttore del GameControllerImpl.
     * 
     * @param gameModel model
     * @param gameScene scene
     * @param mainFrame frame
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", 
        justification = "Il controller deve operare sulle istanze condivise di Model e View (MVC Pattern)")
    public GameControllerImpl(final Game gameModel, final GameScene gameScene,
            final GameFrame mainFrame) {
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
        final AbstractPlayer startingPlayer = gameModel.getCurrentPlayer();

        // 2. Usiamo la View per mostrare il popup
        gameScene.showStartingPlayer(startingPlayer.getName());

        // 3. Facciamo partire il gioco
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
            final AbstractPlayer winner = gameModel.getWinner();
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

        final AbstractPlayer currentPlayer = gameModel.getCurrentPlayer();

        // Controlla se il giocatore è un'istanza di AIPlayer
        if (currentPlayer instanceof AbstractAIPlayer) {

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
        } catch (final IllegalStateException e) {
            // Mostra un errore se la mossa non è valida
            gameScene.showError(e.getMessage(), "Carta non giocabile!");
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onDrawCard() {
        try {
            // Chiama il nuovo metodo con la logica di validazione
            gameModel.playerInitiatesDraw();
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Non puoi pescare!");
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onCallUno() {
        try {
            gameModel.callUno(gameModel.getPlayers().getFirst());
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Non puoi chiamare UNO!");
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     */
    @Override
    public void onBackToMenu() {
        // Logica per tornare al menu
        if (gameScene.confirmExit()) {
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
        try {
            gameModel.playerPassTurn();
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Non puoi passare!");
        }
    }

    /**
     * Implementa il metodo dell'interfaccia.
     * Riceve il colore scelto dalla View e lo passa al Modello.
     * 
     * @param color Il colore scelto.
     */
    @Override
    public void onColorChosen(final CardColor color) {
        gameModel.setColor(color);
    }

    /**
     * Implementa il metodo dell'interfaccia.
     * Riceve il giocatore scelto dalla View e lo passa al Modello.
     * 
     * @param player Il giocatore scelto.
     */
    @Override
    public void onPlayerChosen(final AbstractPlayer player) {
        gameModel.chosenPlayer(player);
    }
}
