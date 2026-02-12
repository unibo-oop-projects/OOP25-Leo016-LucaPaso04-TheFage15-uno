package uno.controller.impl;

import uno.controller.api.GameController;
import uno.controller.api.MenuController;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameState;
import uno.model.game.api.Game;
import uno.model.players.impl.AbstractAIPlayer;
import uno.model.players.impl.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.view.api.GameFrame;
import uno.view.api.CardViewData;
import uno.view.api.GameViewData;
import uno.view.api.PlayerViewData;
import uno.view.impl.CardViewDataImpl;
import uno.view.impl.GameViewDataImpl;
import uno.view.impl.PlayerViewDataImpl;
import uno.view.scenes.api.GameScene;
import uno.view.scenes.api.MenuScene;
import uno.view.scenes.impl.MenuSceneImpl;

import java.util.List;
import java.util.stream.Collectors;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Optional;
import java.awt.Container;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Concrete implementation of the GameController interface.
 * It manages the interaction logic between the GameScene (View) and the Game
 * (Model).
 */
public class GameControllerImpl implements GameController {

    private static final int AI_DELAY = 4000;

    private final Game gameModel;
    private final GameScene gameScene;
    private final GameFrame mainFrame;

    private Optional<Timer> aiTimer = Optional.empty();

    /**
     * Constructs the GameControllerImpl with the given Model, View, and Main Frame.
     * 
     * @param gameModel the game logic and state.
     * @param gameScene the view representing the game board and player
     *                  interactions.
     * @param mainFrame the main application window to control scene transitions and
     *                  popups.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GameControllerImpl(final Game gameModel, final GameScene gameScene,
            final GameFrame mainFrame) {
        this.gameModel = gameModel;
        this.gameScene = gameScene;
        this.mainFrame = mainFrame;
        this.gameModel.addObserver(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showStartingPlayerPopupAndStartGame() {
        final AbstractPlayer startingPlayer = gameModel.getCurrentPlayer();
        gameScene.showStartingPlayer(startingPlayer.getName());
        onGameUpdate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onGameUpdate() {
        if (gameModel.getGameState() == GameState.GAME_OVER) {
            if (aiTimer.isPresent()) {
                aiTimer.get().stop();
            }

            gameScene.setHumanInputEnabled(false);
            final AbstractPlayer winner = gameModel.getWinner();
            gameScene.showWinnerPopup(winner.getName());
            return;
        }

        if (gameModel.getGameState() == GameState.ROUND_OVER) {
            if (aiTimer.isPresent()) {
                aiTimer.get().stop();
            }

            gameScene.setHumanInputEnabled(false);
            final AbstractPlayer roundWinner = gameModel.getWinner();
            gameScene.showInfo("Round Winner: " + roundWinner.getName() + "!\nScore: " + roundWinner.getScore(),
                    "Round Over");
            gameModel.startNewRound();
            return;
        }

        final GameViewData viewData = createGameViewData();
        gameScene.updateView(viewData);
        final boolean isHumanTurn = gameModel.getCurrentPlayer().getClass() == HumanPlayer.class;

        if (isHumanTurn) {
            if (gameModel.getGameState() == GameState.WAITING_FOR_COLOR) {
                gameScene.showColorChooser(gameModel.isDarkSide());
            }

            if (gameModel.getGameState() == GameState.WAITING_FOR_PLAYER) {
                gameScene.showPlayerChooser(gameModel.getPlayers());
            }
        }

        checkAndRunAITurn();
    }

    /**
     * Creates a GameViewData object that encapsulates all the necessary information
     * about the current game state, players, and cards to be displayed in the view.
     * 
     * @return a GameViewData instance with the current game information.
     */
    private GameViewData createGameViewData() {
        final GameState state = gameModel.getGameState();

        final List<PlayerViewData> playerViewDataList = gameModel.getPlayers().stream()
                .map(this::createPlayerViewData)
                .collect(Collectors.toList());

        final PlayerViewData currentPlayer = createPlayerViewData(gameModel.getCurrentPlayer());

        final PlayerViewData winner = gameModel.getGameState() == GameState.GAME_OVER 
                                    || gameModel.getGameState() == GameState.ROUND_OVER
                ? createPlayerViewData(gameModel.getWinner())
                : null;

        final Optional<CardViewData> topCard = gameModel.getTopDiscardCard()
                .map(this::createCardViewData);

        return new GameViewDataImpl(
                state,
                playerViewDataList,
                currentPlayer,
                topCard,
                gameModel.isDiscardPileEmpty(),
                gameModel.getDrawDeck().size(),
                gameModel.getCurrentColor(),
                gameModel.isDarkSide(),
                gameModel.hasCurrentPlayerDrawn(gameModel.getCurrentPlayer()),
                winner,
                gameModel.isClockwise());
    }

    /**
     * Creates a PlayerViewData object for a given player, including their name, hand
     * size, score, and whether they are the current player.
     * 
     * @param player the player for whom to create the view data.
     * @return a PlayerViewData instance with the player's information and hand cards.
     */
    private PlayerViewData createPlayerViewData(final AbstractPlayer player) {
        final List<CardViewData> hand = player.getHand().stream()
                .filter(Optional::isPresent)
                .map(cardOpt -> createCardViewData(cardOpt.get()))
                .collect(Collectors.toList());

        return new PlayerViewDataImpl(
                player.getName(),
                player.getHandSize(),
                player.getScore(),
                gameModel.getCurrentPlayer().equals(player),
                hand,
                player);
    }

    /**
     * Creates a CardViewData object for a given card, including its color, value, and
     * an image key for display purposes.
     * 
     * @param card the card for which to create the view data.
     * @return a CardViewData instance with the card's information and image key.
     */
    private CardViewData createCardViewData(final Card card) {
        final String imageKey = card.getColor(gameModel).name() + "_" + card.getValue(gameModel).name();
        return new CardViewDataImpl(
                card.getColor(gameModel),
                card.getValue(gameModel),
                imageKey,
                Optional.of(card));
    }

    /**
     * Check if the current player is an AI and, if so, schedule its turn after a
     * short delay.
     */
    private void checkAndRunAITurn() {
        if (gameModel.getGameState() != GameState.RUNNING) {
            return;
        }

        final AbstractPlayer currentPlayer = gameModel.getCurrentPlayer();

        if (currentPlayer instanceof AbstractAIPlayer) {
            gameScene.setHumanInputEnabled(false);

            final ActionListener aiTask = new ActionListener() {
                /**
                 * When the timer triggers, this method is called to execute the AI player's turn.
                 */
                @Override
                public void actionPerformed(final ActionEvent e) {
                    ((AbstractAIPlayer) currentPlayer).takeTurn(gameModel);
                }
            };

            aiTimer = Optional.of(new Timer(AI_DELAY, aiTask));
            aiTimer.get().setRepeats(false);
            aiTimer.get().start();
        } else {
            gameScene.setHumanInputEnabled(true);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayCard(final Optional<Card> card) {
        try {
            gameModel.playCard(card);
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Can't play this card!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDrawCard() {
        try {
            gameModel.playerInitiatesDraw();
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Can't draw a card!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCallUno() {
        try {
            gameModel.callUno(gameModel.getPlayers().getFirst());
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Can't call UNO!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackToMenu() {
        if (gameScene.confirmExit()) {
            final MenuController menuController = new MenuControllerImpl(mainFrame);
            final MenuScene menuScene = new MenuSceneImpl();
            menuScene.setObserver(menuController);
            mainFrame.showScene((Container) menuScene);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPassTurn() {
        try {
            gameModel.playerPassTurn();
        } catch (final IllegalStateException e) {
            gameScene.showError(e.getMessage(), "Can't pass turn!");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onColorChosen(final CardColor color) {
        gameModel.setColor(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerChosen(final AbstractPlayer player) {
        gameModel.chosenPlayer(player);
    }
}
