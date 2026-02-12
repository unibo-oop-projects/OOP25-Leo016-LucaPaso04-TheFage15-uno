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
import uno.model.players.impl.AbstractPlayer;
import uno.model.players.impl.HumanPlayer;

/**
 * Unit tests for the GameSetupImpl class.
 */
class GameSetupTest {

    private static final int INITIAL_HAND_SIZE = 7;

    private GameSetupImpl setup;
    private MockGame game;
    private MockDiscardPile discardPile;
    private List<AbstractPlayer> players;

    @BeforeEach
    void setUp() {
        game = new MockGame();
        discardPile = new MockDiscardPile();

        players = new ArrayList<>();
        players.add(new HumanPlayer("P1"));
        players.add(new HumanPlayer("P2"));

        final Deck<Card> deck = new MockDeck();

        setup = new GameSetupImpl(game, deck, discardPile, players);
    }

    @Test
    void testInitializeGameDealsCards() {
        setup.initializeGame(false);

        assertEquals(INITIAL_HAND_SIZE, players.get(0).getHandSize());
        assertEquals(INITIAL_HAND_SIZE, players.get(1).getHandSize());
    }

    @Test
    void testInitializeGameStandardFirstCard() {
        setup.initializeGame(false);

        assertEquals(1, discardPile.cards.size());
        assertTrue(game.setCurrentColorCalled);
    }

    @Test
    void testInitializeGameAllWild() {
        setup.initializeGame(true);
        assertTrue(game.setCurrentColorCalled);
    }

    static class MockGame implements Game {
        private boolean setCurrentColorCalled;

        @Override
        public void logSystemAction(final String type, final String action, final String description) {
        }

        @Override
        public void setCurrentColor(final CardColor c) {
            setCurrentColorCalled = true;
        }

        @Override
        public void addObserver(final GameModelObserver observer) {
        }

        @Override
        public void notifyObservers() {
        }

        @Override
        public void playCard(final Optional<Card> card) {
        }

        @Override
        public void playerInitiatesDraw() {
        }

        @Override
        public void playerPassTurn() {
        }

        @Override
        public void callUno(final AbstractPlayer player) {
        }

        @Override
        public void setColor(final CardColor color) {
            setCurrentColorCalled = true;
        }

        @Override
        public void chosenPlayer(final AbstractPlayer player) {
        }

        @Override
        public void skipPlayers(final int n) {
        }

        @Override
        public void makeNextPlayerDraw(final int amount) {
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
        public void drawCardForPlayer(final AbstractPlayer player) {
        }

        @Override
        public void drawUntilColorChosenCard(final CardColor color) {
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
            return List.of();
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
        public boolean hasCurrentPlayerDrawn(final AbstractPlayer player) {
            return false;
        }

        @Override
        public void aiAdvanceTurn() {
        }

        @Override
        public GameRules getRules() {
            return null;
        }

        @Override
        public void startNewRound() {
        }
    }

    static class MockDiscardPile implements DiscardPile {
        private final List<Card> cards = new ArrayList<>();

        @Override
        public void addCard(final Card card) {
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
        public List<Card> takeAll() {
            final List<Card> allCards = new ArrayList<>(cards);
            cards.clear();
            return allCards;
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
        public void addCard(final Card c) {
            cards.add(c);
        }

        @Override
        public void shuffle() {
        }

        @Override
        public Optional<Card> draw() {
            if (cards.isEmpty()) {
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
        public void refill(final List<Card> newCards) {
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
