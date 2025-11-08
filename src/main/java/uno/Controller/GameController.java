package uno.Controller;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Game.Game;
import uno.Model.Game.GameState;
import uno.Model.Player.AIPlayer; // <-- IMPORTA
import uno.Model.Player.Player;   // <-- IMPORTA
import uno.View.GameFrame;
import uno.View.Scenes.GameScene;
import uno.View.Scenes.MenuScene;
import uno.View.GameModelObserver; // <-- IMPORTA

import javax.swing.JOptionPane;
import javax.swing.Timer; // <-- IMPORTA PER IL RITARDO
import java.awt.event.ActionEvent; // <-- IMPORTA
import java.awt.event.ActionListener; // <-- IMPORTA

/**
 * Controller che gestisce la logica di interazione
 * tra la GameScene (View) e il Game (Model).
 */
public class GameController implements GameViewObserver, GameModelObserver {

    private final Game gameModel;
    private final GameScene gameScene;
    private final GameFrame mainFrame;

    private Timer aiTimer; // Timer per il ritardo dell'IA

    public GameController(Game gameModel, GameScene gameScene, GameFrame mainFrame) {
        this.gameModel = gameModel;
        this.gameScene = gameScene;
        this.mainFrame = mainFrame;

        this.gameModel.addObserver(this);
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
            Player winner = gameModel.getWinner();
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

        Player currentPlayer = gameModel.getCurrentPlayer();

        // Controlla se il giocatore è un'istanza di AIPlayer
        if (currentPlayer instanceof AIPlayer) {
            
            // Disabilita la UI umana per evitare input concorrenti
            gameScene.setHumanInputEnabled(false); 
            
            // L'IA non gioca subito. Creiamo un Timer per un breve ritardo.
            int AI_DELAY = 1000; // 1 secondo (1000ms)
            
            ActionListener aiTask = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
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

    @Override
    public void onPlayCard(Card card) {
        System.out.println("Tentativo di giocare la carta: " + card);
        try {
            // Comanda al modello di giocare la carta
            gameModel.playCard(card);
            // Il modello notificherà la GameScene per l'aggiornamento
            // (grazie a gameModel.addObserver(gameScene) nel costruttore)
            
        } catch (Exception e) {
            // Mostra un errore se la mossa non è valida
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), 
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onDrawCard() {
        System.out.println("L'utente clicca 'Pesca'");
        try {
            // Chiama il nuovo metodo con la logica di validazione
            gameModel.playerInitiatesDraw(); 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), // Messaggio d'errore (es. "Hai già pescato")
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }


    @Override
    public void onCallUno() {
        System.out.println("L'utente clicca 'UNO!'");
        try {
            gameModel.callUno(gameModel.getCurrentPlayer());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                e.getMessage(), // Messaggio d'errore (es. "Non puoi chiamare UNO ora")
                "Mossa non valida", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onBackToMenu() {
        // Logica per tornare al menu
        int choice = JOptionPane.showConfirmDialog(
            gameScene, 
            "Sei sicuro di voler tornare al menu? La partita sarà persa.",
            "Torna al Menu", 
            JOptionPane.YES_NO_OPTION);
            
        if (choice == JOptionPane.YES_OPTION) {
            // Ricrea il controller e la scena del menu
            MenuController menuController = new MenuController(mainFrame);
            MenuScene menuScene = new MenuScene();
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
        } catch (Exception e) {
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
    public void onColorChosen(CardColor color) {
        System.out.println("Colore scelto: " + color);
        // Il GameModel riceverà il colore, imposterà il suo stato
        // interno e notificherà la View (che si aggiornerà di nuovo).
        gameModel.setColor(color);
    }
}