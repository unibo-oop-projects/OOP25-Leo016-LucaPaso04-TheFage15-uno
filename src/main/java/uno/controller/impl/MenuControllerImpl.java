package uno.controller.impl;

import uno.controller.api.MenuController;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.AIAllWild;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AIFlip;
import uno.model.players.impl.HumanPlayer;
import uno.view.scenes.impl.GameSceneImpl;
import uno.view.scenes.impl.MenuSceneImpl;
import uno.view.scenes.impl.RulesSceneImpl;
import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;
import uno.view.api.GameFrame;

import uno.model.game.impl.GameFactoryImpl;
import uno.model.game.api.GameMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of the MenuController.
 * It manages the transitions from the Menu Scene to the Game Scene based on
 * user selection, and handles Rules configuration.
 */
public class MenuControllerImpl implements MenuController {

    private static final String HUMAN_NAME = "Giocatore 1";
    private static final String AI_ONE_NAME = "IA-1";
    private static final String AI_TWO_NAME = "IA-2";
    private static final String AI_THREE_NAME = "IA-3";
    private final GameFrame frame;
    private GameRules currentRules;

    /**
     * Constructor for MenuControllerImpl.
     * 
     * @param frame frame
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("EI_EXPOSE_REP2")
    public MenuControllerImpl(final GameFrame frame) {
        this.frame = frame;
        this.currentRules = GameRulesImpl.defaultRules();
    }

    /**
     * Handles the action of starting a Classic game.
     */
    @Override
    public void onStartClassicGame() {
        startGame(GameMode.STANDARD);
    }

    /**
     * Handles the action of starting a Flip game.
     */
    @Override
    public void onStartFlipGame() {
        startGame(GameMode.FLIP);
    }

    /**
     * Handles the action of starting an All Wild game.
     */
    @Override
    public void onStartAllWildGame() {
        startGame(GameMode.ALL_WILD);
    }

    private void startGame(final GameMode gameMode) {
        // 1. Crea i giocatori
        final List<AbstractPlayer> players = createPlayers(gameMode);

        // 2. Crea la Factory e il Gioco
        // La factory gestisce Deck, GameImpl e Setup
        final GameFactoryImpl factory = new GameFactoryImpl(currentRules);
        final Game gameModel = factory.createGame(HUMAN_NAME, gameMode, players);

        // 3. Crea la View del Gioco (GameScene)
        final GameSceneImpl gameScene = new GameSceneImpl(gameModel);

        // 4. Crea il Controller del Gioco
        // Use factory.getLogger() to share the logger
        final GameControllerImpl gameController = new GameControllerImpl(gameModel, gameScene, frame);

        // 5. Collega la Scena al suo Controller
        gameScene.setObserver(gameController);

        // 6. Mostra la nuova scena
        frame.showScene(gameScene);

        gameController.showStartingPlayerPopupAndStartGame();
    }

    private List<AbstractPlayer> createPlayers(final GameMode gameMode) {
        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(new HumanPlayer(HUMAN_NAME)); // Giocatore umano

        switch (gameMode) {
            case FLIP:
                players.add(new AIFlip(AI_ONE_NAME));
                players.add(new AIFlip(AI_TWO_NAME));
                players.add(new AIFlip(AI_THREE_NAME));
                break;
            case ALL_WILD:
                players.add(new AIAllWild(AI_ONE_NAME));
                players.add(new AIAllWild(AI_TWO_NAME));
                players.add(new AIAllWild(AI_THREE_NAME));
                break;
            default: // Classic
                players.add(new AIClassic(AI_ONE_NAME));
                players.add(new AIClassic(AI_TWO_NAME));
                players.add(new AIClassic(AI_THREE_NAME));
                break;
        }
        return players;
    }

    /**
     * Handles the action of quitting the application.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DM_EXIT")
    public void onQuit() {
        System.exit(0);
    }

    /**
     * Handles the action of opening the rules scene.
     */
    @Override
    public void onOpenRules() {
        final RulesSceneImpl rulesScene = new RulesSceneImpl(currentRules);
        rulesScene.setObserver(this);
        frame.showScene(rulesScene);
    }

    /**
     * Handles saving the custom rules.
     */
    @Override
    public void onSaveRules(final GameRules rules) {
        this.currentRules = rules;
    }

    /**
     * Handles returning to the main menu.
     */
    @Override
    public void onBackToMenu() {
        // Re-create the Menu Scene (stateless usually)
        final MenuSceneImpl menuScene = new MenuSceneImpl();
        menuScene.setObserver(this);
        frame.showScene(menuScene);
    }
}
