package uno.Controller;

import uno.Model.Cards.Deck.StandardDeck;
import uno.Model.Game.Game;
import uno.Model.Game.GameSetup; // <-- IMPORTA
import uno.Model.Player.AIClassic;
import uno.Model.Player.Player;
import uno.View.GameFrame;
import uno.View.Scenes.GameScene;
import uno.View.Scenes.MenuScene;

import javax.swing.JOptionPane;
import java.util.ArrayList; // <-- IMPORTA
import java.util.List;     // <-- IMPORTA

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

        // --- IMPOSTAZIONE DELLA PARTITA ---

        // 1. Crea i giocatori
        List<Player> players = new ArrayList<>();
        players.add(new Player("Giocatore 1")); // Giocatore umano
        players.add(new AIClassic("IA-1")); // Avversario
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        StandardDeck deck = new StandardDeck();
        Game gameModel = new Game(deck, players);

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        GameSetup setup = new GameSetup(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.setupNewGame();

        // 4. Crea la View del Gioco (GameScene)
        GameScene gameScene = new GameScene(gameModel);

        // 5. Crea il Controller del Gioco
        GameController gameController = new GameController(gameModel, gameScene, frame);
        
        // 6. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);
        
        // 7. Mostra la nuova scena
        frame.showScene(gameScene);

        gameController.onGameUpdate();
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