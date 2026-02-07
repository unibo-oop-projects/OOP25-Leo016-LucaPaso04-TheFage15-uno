package uno.controller.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uno.controller.api.GameViewObserver;
import uno.model.api.GameModelObserver;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.game.api.TurnManager;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;
import uno.view.api.GameFrame;
import uno.view.scenes.api.GameScene;

class GameControllerTest {

    private GameControllerImpl controller;
    private MockGame game;
    private MockGameScene scene;
    private MockGameFrame frame;

    @BeforeEach
    void setUp() {
        game = new MockGame();
        scene = new MockGameScene();
        frame = new MockGameFrame();
        controller = new GameControllerImpl(game, scene, frame);
    }

    @Test
    void testShowStartingPlayerPopupAndStartGame() {
        game.currentPlayer = new HumanPlayer("Test P1");
        controller.showStartingPlayerPopupAndStartGame();

        assertEquals("Test P1", scene.lastStartingPlayerName);
    }

    @Test
    void testOnGameUpdate_HumanTurn_WaitingForColor() {
        game.currentPlayer = new HumanPlayer("Human");
        game.gameState = GameState.WAITING_FOR_COLOR;
        game.isDarkSide = true;

        controller.onGameUpdate();

        assertTrue(scene.showColorChooserCalled);
        assertTrue(scene.lastIsDarkSide);
    }

    @Test
    void testOnGameUpdate_HumanTurn_WaitingForPlayer() {
        game.currentPlayer = new HumanPlayer("Human");
        game.gameState = GameState.WAITING_FOR_PLAYER;
        game.players = Collections.emptyList();

        controller.onGameUpdate();

        assertTrue(scene.showPlayerChooserCalled);
    }

    @Test
    void testOnGameUpdate_GameOver() {
        game.gameState = GameState.GAME_OVER;
        AbstractPlayer winner = new HumanPlayer("Winner");
        game.winner = winner;

        controller.onGameUpdate();

        assertEquals("Winner", scene.lastWinnerName);
        assertFalse(scene.humanInputEnabled);
    }

    @Test
    void testOnPlayCard_Success() {
        controller.onPlayCard(Optional.empty());
        assertTrue(game.playCardCalled);
    }

    @Test
    void testOnDrawCard_Success() {
        controller.onDrawCard();
        assertTrue(game.drawCardCalled);
    }

    @Test
    void testOnCallUno_Success() {
        game.players = List.of(new HumanPlayer("P1"));
        controller.onCallUno();
        assertTrue(game.callUnoCalled);
    }

    // --- MOCKS ---

    static class MockGame implements Game {
        AbstractPlayer currentPlayer;
        GameState gameState = GameState.RUNNING;
        boolean isDarkSide;
        List<AbstractPlayer> players;
        AbstractPlayer winner;

        boolean playCardCalled;
        boolean drawCardCalled;
        boolean callUnoCalled;
        boolean setColorCalled;
        boolean chosenPlayerCalled;

        @Override
        public AbstractPlayer getCurrentPlayer() {
            return currentPlayer;
        }

        @Override
        public GameState getGameState() {
            return gameState;
        }

        @Override
        public boolean isDarkSide() {
            return isDarkSide;
        }

        @Override
        public List<AbstractPlayer> getPlayers() {
            return players;
        }

        @Override
        public AbstractPlayer getWinner() {
            return winner;
        }

        @Override
        public void playCard(Optional<Card> card) {
            playCardCalled = true;
        }

        @Override
        public void playerInitiatesDraw() {
            drawCardCalled = true;
        }

        @Override
        public void callUno(AbstractPlayer p) {
            callUnoCalled = true;
        }

        @Override
        public void setColor(CardColor c) {
            setColorCalled = true;
        }

        @Override
        public void chosenPlayer(AbstractPlayer p) {
            chosenPlayerCalled = true;
        }

        @Override
        public void addObserver(GameModelObserver o) {
        }

        @Override
        public void notifyObservers() {
        }

        @Override
        public void playerPassTurn() {
        }

        @Override
        public void skipPlayers(int n) {
        }

        @Override
        public void makeNextPlayerDraw(int amount) {
        }

        @Override
        public void reversePlayOrder() {
        }

        @Override
        public void flipTheWorld() {
        }

        @Override
        public void requestColorChoice() {
        }

        @Override
        public void requestPlayerChoice() {
        }

        @Override
        public void drawCardForPlayer(AbstractPlayer player) {
        }

        @Override
        public void drawUntilColorChosenCard(CardColor color) {
        }

        @Override
        public Optional<Card> getTopDiscardCard() {
            return Optional.empty();
        }

        @Override
        public TurnManager getTurnManager() {
            return null;
        }

        @Override
        public DiscardPile getDiscardPile() {
            return null;
        }

        @Override
        public Deck<Card> getDrawDeck() {
            return null;
        }

        @Override
        public boolean isDiscardPileEmpty() {
            return false;
        }

        @Override
        public Optional<CardColor> getCurrentColor() {
            return Optional.empty();
        }

        @Override
        public boolean isClockwise() {
            return true;
        }

        @Override
        public boolean hasCurrentPlayerDrawn(AbstractPlayer player) {
            return false;
        }

        @Override
        public void setCurrentColor(CardColor color) {
            setColorCalled = true;
        }

        @Override
        public void aiAdvanceTurn() {
        }

        @Override
        public GameRules getRules() {
            return null;
        }

        @Override
        public void logSystemAction(String actionType, String cardDetails, String extraInfo) {
        }
    }

    static class MockGameScene implements GameScene {
        String lastStartingPlayerName;
        boolean showColorChooserCalled;
        boolean lastIsDarkSide;
        boolean showPlayerChooserCalled;
        String lastWinnerName;
        boolean humanInputEnabled = true;

        @Override
        public void showStartingPlayer(String playerName) {
            lastStartingPlayerName = playerName;
        }

        @Override
        public void showColorChooser(boolean isDarkSide) {
            showColorChooserCalled = true;
            lastIsDarkSide = isDarkSide;
        }

        @Override
        public void showPlayerChooser(List<AbstractPlayer> opponents) {
            showPlayerChooserCalled = true;
        }

        @Override
        public void showWinnerPopup(String winnerName) {
            lastWinnerName = winnerName;
        }

        @Override
        public void setHumanInputEnabled(boolean enabled) {
            humanInputEnabled = enabled;
        }

        @Override
        public void setObserver(GameViewObserver observer) {
        }

        @Override
        public void showError(String message, String title) {
        }

        @Override
        public void showInfo(String message, String title) {
        }

        @Override
        public boolean confirmExit() {
            return true;
        }

        @Override
        public void onGameUpdate() {
        }
    }

    static class MockGameFrame implements GameFrame {
        @Override
        public void showScene(JPanel scene) {
        }

        @Override
        public void setVisible(boolean visible) {
        }

        @Override
        public void dispose() {
        }
    }
}
