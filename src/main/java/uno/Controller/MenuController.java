package uno.Controller;

import uno.Model.Cards.Deck.StandardDeck;
import uno.Model.Game.Game;
import uno.View.GameFrame;
import uno.View.Scenes.GameScene;
import uno.View.Scenes.MenuScene;

import javax.swing.JOptionPane;

/**
 * Controller che gestisce la logica per la schermata del menu.
 * Implementa l'interfaccia MenuObserver.
 */
public class MenuController implements MenuObserver {

    private final GameFrame frame;

    public MenuController(GameFrame frame) {
        this.frame = frame;
    }

    @Override
    public void onStartClassicGame() {
        System.out.println("Avvio modalità classica...");

        // 1. Crea il Model (Gioco)
        Game gameModel = new Game(new StandardDeck());
        // TODO: Aggiungere giocatori al modello...

        // 2. Crea la View del Gioco (GameScene)
        GameScene gameScene = new GameScene(gameModel);

        // 3. Crea il Controller del Gioco
        GameController gameController = new GameController(gameModel, gameScene, frame);
        
        // 4. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);
        
        // 5. Mostra la nuova scena
        frame.showScene(gameScene);
    }

    @Override
    public void onStartFlipGame() {
        System.out.println("Avvio modalità Flip...");
        JOptionPane.showMessageDialog(frame, "Modalità Flip non ancora implementata");
    }

    @Override
    public void onQuit() {
        System.out.println("Uscita dall'applicazione.");
        System.exit(0);
    }
}