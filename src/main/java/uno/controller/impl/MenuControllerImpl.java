package uno.controller.impl;

import uno.controller.api.MenuController;
import uno.controller.api.GameController;
import uno.model.game.api.Game;
import uno.model.game.api.GameMode;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameFactory;
import uno.model.game.impl.GameRulesImpl;
import uno.model.game.impl.GameFactoryImpl;
import uno.model.players.impl.AIAllWild;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AIFlip;
import uno.model.players.impl.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.view.api.GameFrame;
import uno.view.scenes.api.GameScene;
import uno.view.scenes.api.RulesScene;
import uno.view.scenes.api.MenuScene;
import uno.view.scenes.impl.GameSceneImpl;
import uno.view.scenes.impl.MenuSceneImpl;
import uno.view.scenes.impl.RulesSceneImpl;

import java.util.ArrayList;
import java.util.List;
import java.awt.Container;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete implementation of the MenuController.
 * It manages the transitions from the Menu Scene to the Game Scene based on
 * user selection, and handles Rules configuration.
 */
public class MenuControllerImpl implements MenuController {

    private static final String HUMAN_NAME = "Player";
    private static final String AI_ONE_NAME = "IA-1";
    private static final String AI_TWO_NAME = "IA-2";
    private static final String AI_THREE_NAME = "IA-3";

    private final GameFrame frame;
    private GameRules currentRules;

    /**
     * Constructor for MenuControllerImpl.
     * 
     * @param frame the main application frame to control scene transitions
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public MenuControllerImpl(final GameFrame frame) {
        this.frame = frame;
        this.currentRules = GameRulesImpl.defaultRules();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartClassicGame() {
        startGame(GameMode.STANDARD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartFlipGame() {
        startGame(GameMode.FLIP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartAllWildGame() {
        startGame(GameMode.ALL_WILD);
    }

    /**
     * Starts a new game based on the selected Game Mode, initializing the Model,
     * View, and Controller accordingly.
     * 
     * @param gameMode the selected Game Mode
     */
    private void startGame(final GameMode gameMode) {
        final List<AbstractPlayer> players = createPlayers(gameMode);

        final GameFactory factory = new GameFactoryImpl(currentRules);
        final Game gameModel = factory.createGame(HUMAN_NAME, gameMode, players);
        final GameScene gameScene = new GameSceneImpl();
        final GameController gameController = new GameControllerImpl(gameModel, gameScene, frame);

        gameScene.setObserver(gameController);

        frame.showScene((Container) gameScene);

        gameController.showStartingPlayerPopupAndStartGame();
    }

    /**
     * Creates a list of players based on the selected Game Mode. The first player
     * is always a Human Player, followed by three AI Players of the appropriate type.
     * 
     * @param gameMode the selected Game Mode
     * @return a list of players for the game
     */
    private List<AbstractPlayer> createPlayers(final GameMode gameMode) {
        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(new HumanPlayer(HUMAN_NAME));

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
            default:
                players.add(new AIClassic(AI_ONE_NAME));
                players.add(new AIClassic(AI_TWO_NAME));
                players.add(new AIClassic(AI_THREE_NAME));
                break;
        }
        return players;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("DM_EXIT")
    public void onQuit() {
        System.exit(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onOpenRules() {
        final RulesScene rulesScene = new RulesSceneImpl(currentRules);
        rulesScene.setObserver(this);
        frame.showScene((Container) rulesScene);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSaveRules(final GameRules rules) {
        this.currentRules = rules;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackToMenu() {
        final MenuScene menuScene = new MenuSceneImpl();
        menuScene.setObserver(this);
        frame.showScene((Container) menuScene);
    }
}
