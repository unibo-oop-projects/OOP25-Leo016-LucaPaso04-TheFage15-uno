package uno.model.game.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

class GameSetupTest {

    private GameSetupImpl setup;
    private MockGame game;
    private MockDiscardPile discardPile;
    private Deck<Card> deck;
    private List<AbstractPlayer> players;

    @BeforeEach
    void setUp() {
        game = new MockGame();
        discardPile = new MockDiscardPile();

        players = new ArrayList<>();
        players.add(new HumanPlayer("P1"));
        players.add(new HumanPlayer("P2"));

        deck = new MockDeck();

        setup = new GameSetupImpl(game, deck, discardPile, players);
    }

    @Test
    void testInitializeGame_DealsCards() {
        setup.initializeGame(false);

        assertEquals(7, players.get(0).getHandSize());
        assertEquals(7, players.get(1).getHandSize());
    }

    @Test
    void testInitializeGame_Standard_FirstCard() {
        setup.initializeGame(false);

        assertEquals(1, discardPile.cards.size());
        assertTrue(game.setCurrentColorCalled);
    }

    @Test
    void testInitializeGame_AllWild() {
        setup.initializeGame(true);
        assertTrue(game.setCurrentColorCalled);
    }

    static class MockGame implements Game {
        boolean setCurrentColorCalled;

        @Override
        public void logSystemAction(String type, String action, String description) {
        }

        @Override
        public void setCurrentColor(CardColor c) {
            setCurrentColorCalled = true;
        }

        // Complete interface implementation
        @Override
        public void addObserver(GameModelObserver observer) {
        }

        @Override
        public void notifyObservers() {
        }

        @Override
        public void playCard(Optional<Card> card) {
        }

        @Override
        public void playerInitiatesDraw() {
        }

        @Override
        public void playerPassTurn() {
        }

        @Override
        public void callUno(AbstractPlayer player) {
        }

        @Override
        public void setColor(CardColor color) {
            setCurrentColorCalled = true;
        }

        @Override
        public void chosenPlayer(AbstractPlayer player) {
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
        public AbstractPlayer getCurrentPlayer() {
            return null;
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
        public GameState getGameState() {
            return null;
        }

        @Override
        public Optional<CardColor> getCurrentColor() {
            return Optional.empty();
        }

        @Override
        public List<AbstractPlayer> getPlayers() {
            return null;
        }

        @Override
        public AbstractPlayer getWinner() {
            return null;
        }

        @Override
        public boolean isDarkSide() {
            return false;
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
        public void aiAdvanceTurn() {
        }

        @Override
        public GameRules getRules() {
            return null;
        }
    }

    static class MockDiscardPile implements DiscardPile {
        List<Card> cards = new ArrayList<>();

        @Override
        public void addCard(Card card) {
            cards.add(card);
        }

        @Override
        public Optional<Card> getTopCard() {
            return cards.isEmpty() ? Optional.empty() : Optional.of(cards.get(cards.size() - 1));
        }

        @Override
        public List<Card> takeAllExceptTop() {
            return new ArrayList<>();
        }

        @Override
        public List<Card> getSnapshot() {
            return new ArrayList<>(cards);
        }

        @Override
        public boolean isEmpty() {
            return cards.isEmpty();
        }

        @Override
        public int size() {
            return cards.size();
        }
    }

    static class MockDeck implements Deck<Card> {
        private final List<Card> cards = new ArrayList<>();

        @Override
        public void addCard(Card c) {
            cards.add(c);
        }

        @Override
        public void shuffle() {
        }

        @Override
        public Optional<Card> draw() {
            if (cards.isEmpty()) {
                // Return a safe card to start game: Red 1
                return Optional.of(new uno.model.cards.types.impl.DoubleSidedCard(
                        new uno.model.cards.behaviors.impl.NumericBehavior(
                                CardColor.RED, uno.model.cards.attributes.CardValue.ONE),
                        uno.model.cards.behaviors.impl.BackSideBehavior.getInstance()));
            }
            return Optional.of(cards.remove(cards.size() - 1));
        }

        @Override
        public Optional<Card> peek() {
            if (cards.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(cards.get(cards.size() - 1));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public void refill(List<Card> newCards) {
            cards.addAll(newCards);
        }

        @Override
        public int size() {
            return cards.size();
        }

        @Override
        public uno.model.utils.api.GameLogger getLogger() {
            return null;
        }
    }
}
