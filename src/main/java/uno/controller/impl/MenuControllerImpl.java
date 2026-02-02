package uno.controller.impl;

import uno.controller.api.MenuController;
import uno.model.cards.deck.impl.AllWildDeck;
import uno.model.cards.deck.impl.FlipDeck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.api.Player;
import uno.model.players.impl.AIAllWild;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AIFlip;
import uno.model.players.impl.HumanPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.utils.impl.GameLoggerImpl;
import uno.view.scenes.impl.GameSceneImpl;
import uno.view.scenes.impl.RulesSceneImpl;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameSetup;
import uno.controller.api.GameController;
import uno.view.scenes.api.GameScene;
import uno.view.api.GameFrame;

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
        players.add(new HumanPlayer("Giocatore 1")); // Giocatore umano
        players.add(new AIClassic("IA-1")); // Avversario
        players.add(new AIClassic("IA-2"));
        players.add(new AIClassic("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final StandardDeck deck = new StandardDeck();
        final GameLogger logger = new GameLoggerImpl(String.valueOf(System.currentTimeMillis()));
        final GameImpl gameModel = new GameImpl(deck, players, "CLASSIC", logger);

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetupImpl setup = new GameSetupImpl(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.initializeGame(!isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameSceneImpl gameScene = new GameSceneImpl(gameModel);

        // 5. Crea il Controller del Gioco
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame, logger);

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
        players.add(new HumanPlayer("Giocatore 1")); // Giocatore umano
        players.add(new AIFlip("IA-1")); // Avversario
        players.add(new AIFlip("IA-2"));
        players.add(new AIFlip("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final Deck<Card> deck = new FlipDeck();
        final GameLogger logger = new GameLoggerImpl(String.valueOf(System.currentTimeMillis()));
        final Game gameModel = new GameImpl(deck, players, "FLIP", logger);

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetup setup = new GameSetupImpl(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.initializeGame(!isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameScene gameScene = new GameSceneImpl(gameModel);

        // 5. Crea il Controller del Gioco
        final GameController gameController = new GameControllerImpl(gameModel, gameScene, frame, logger);

        // 6. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);

        // 7. Mostra la nuova scena
        frame.showScene((javax.swing.JPanel) gameScene);

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
        players.add(new HumanPlayer("Giocatore 1")); // Giocatore umano
        players.add(new AIAllWild("IA-1")); // Avversario
        players.add(new AIAllWild("IA-2"));
        players.add(new AIAllWild("IA-3"));
        // puoi aggiungere altri giocatori qui...

        // 2. Crea il Model (Mazzo e Partita)
        final AllWildDeck deck = new AllWildDeck();
        final GameLogger logger = new GameLoggerImpl(String.valueOf(System.currentTimeMillis()));
        final GameImpl gameModel = new GameImpl(deck, players, "ALL_WILD", logger);

        // 3. Esegui il setup (distribuisci carte, gira la prima carta)
        // Questo popola le mani dei giocatori e la pila degli scarti.
        final GameSetupImpl setup = new GameSetupImpl(
            gameModel, 
            deck, 
            gameModel.getDiscardPile(), 
            players
        );
        setup.initializeGame(isAllWild);

        // 4. Crea la View del Gioco (GameScene)
        final GameSceneImpl gameScene = new GameSceneImpl(gameModel);

        // 5. Crea il Controller del Gioco
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame, logger);

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
        final RulesSceneImpl rulesScene = new RulesSceneImpl();
        frame.showScene(rulesScene);
    }
}
