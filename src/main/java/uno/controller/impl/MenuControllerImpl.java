package uno.controller.impl;

import uno.controller.api.MenuController;
import uno.model.cards.deck.AllWildDeck;
import uno.model.cards.deck.FlipDeck;
import uno.model.cards.deck.StandardDeck;
import uno.model.game.Game;
import uno.model.game.GameSetup;
import uno.model.players.AIAllWild;
import uno.model.players.AIClassic;
import uno.model.players.AIFlip;
import uno.model.players.Player;
import uno.view.GameFrame;
import uno.view.scenes.GameScene;
import uno.view.scenes.RulesScene;

import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the MenuController.
 * It manages the transitions from the Menu Scene to the Game Scene based on user selection.
 */
public class MenuControllerImpl implements MenuController {

    private final GameFrame frame;
    private final boolean isAllWild = true;

    /**
     * Constructor for MenuControllerImpl.
     * @param frame
     */
    public MenuControllerImpl(final GameFrame frame) {
        this.frame = frame;
    }

    /** 
     * Handles the action of starting a Classic game.
    */
    @Override
    public void onStartClassicGame() {
        System.out.println("Avvio modalità classica...");

        // --- IMPOSTAZIONE DELLA PARTITA ---

        // 1. Crea i giocatori
        final List<Player> players = new ArrayList<>();
        players.add(new Player("Giocatore 1")); // Giocatore umano
        players.add(new AIClassic("IA-1")); // Avversario
        players.add(new AIClassic("IA-2"));
        players.add(new AIClassic("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final StandardDeck deck = new StandardDeck();
        final Game gameModel = new Game(deck, players, "CLASSIC");

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetup setup = new GameSetup(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.setupNewGame(!isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameScene gameScene = new GameScene(gameModel);

        // 5. Crea il Controller del Gioco
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame);

        // 6. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);

        // 7. Mostra la nuova scena
        frame.showScene(gameScene);

        gameController.showStartingPlayerPopupAndStartGame();
    }

    /** 
     * Handles the action of starting a Flip game.
    */
    @Override
    public void onStartFlipGame() {
        System.out.println("Avvio modalità flip...");

        // --- IMPOSTAZIONE DELLA PARTITA ---

        // 1. Crea i giocatori
        final List<Player> players = new ArrayList<>();
        players.add(new Player("Giocatore 1")); // Giocatore umano
        players.add(new AIFlip("IA-1")); // Avversario
        players.add(new AIFlip("IA-2"));
        players.add(new AIFlip("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final FlipDeck deck = new FlipDeck();
        final Game gameModel = new Game(deck, players, "FLIP");

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetup setup = new GameSetup(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.setupNewGame(!isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameScene gameScene = new GameScene(gameModel);

        // 5. Crea il Controller del Gioco
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame);

        // 6. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);

        // 7. Mostra la nuova scena
        frame.showScene(gameScene);

        gameController.showStartingPlayerPopupAndStartGame();
    }


    /** 
     * Handles the action of starting an All Wild game.
    */
    @Override
    public void onStartAllWildGame() {
        System.out.println("Avvio modalità All Wild...");
        // --- IMPOSTAZIONE DELLA PARTITA ---

        // 1. Crea i giocatori
        final List<Player> players = new ArrayList<>();
        players.add(new Player("Giocatore 1")); // Giocatore umano
        players.add(new AIAllWild("IA-1")); // Avversario
        players.add(new AIAllWild("IA-2"));
        players.add(new AIAllWild("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final AllWildDeck deck = new AllWildDeck();
        final Game gameModel = new Game(deck, players, "ALL_WILD");

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetup setup = new GameSetup(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.setupNewGame(isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameScene gameScene = new GameScene(gameModel);

        // 5. Crea il Controller del Gioco
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame);

        // 6. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);

        // 7. Mostra la nuova scena
        frame.showScene(gameScene);

        gameController.showStartingPlayerPopupAndStartGame();
    }

    /** 
     * Handles the action of quitting the application.
    */
    @Override
    public void onQuit() {
        System.out.println("Uscita dall'applicazione.");
        System.exit(0);
    }

    /** 
     * Handles the action of opening the rules scene.
    */
    @Override
    public void onOpenRules() {
        final RulesScene rulesScene = new RulesScene();
        frame.showScene(rulesScene);
    }
}
