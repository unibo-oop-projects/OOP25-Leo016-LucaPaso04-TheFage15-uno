package uno.Controller;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.View.GameFrame;
import uno.View.Scenes.GameScene;
import uno.View.Scenes.MenuScene;

import javax.swing.JOptionPane;

/**
 * Controller che gestisce la logica di interazione
 * tra la GameScene (View) e il Game (Model).
 */
public class GameController implements GameViewObserver {

    private final Game gameModel;
    private final GameScene gameScene;
    private final GameFrame mainFrame;

    public GameController(Game gameModel, GameScene gameScene, GameFrame mainFrame) {
        this.gameModel = gameModel;
        this.gameScene = gameScene;
        this.mainFrame = mainFrame;
    }

    @Override
    public void onPlayCard(Card card) {
        System.out.println("Tentativo di giocare la carta: " + card);
        try {
            // Comanda al modello di giocare la carta
            gameModel.playCard(card);
            // Il modello notificherà la GameScene per l'aggiornamento
            // (grazie a gameModel.addObserver(gameScene) nel costruttore)

            // TODO: Aggiungere logica per turno IA
            
        } catch (Exception e) {
            // Mostra un errore se la mossa non è valida
            JOptionPane.showMessageDialog(gameScene, 
                "Mossa non valida! " + e.getMessage(), 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onDrawCard() {
        System.out.println("L'utente pesca una carta");
        try {
            gameModel.playerDrawCard(gameModel.getCurrentPlayer());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(gameScene, 
                "Errore durante la pesca: " + e.getMessage(), 
                "Errore", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void onCallUno() {
        System.out.println("L'utente ha chiamato UNO!");
        // TODO: gameModel.callUno(gameModel.getCurrentPlayer());
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
}