package uno.model.cards.types.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import uno.model.api.GameModelObserver;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.game.api.TurnManager;
import uno.model.players.api.AbstractPlayer;

class DoubleSidedCardTest {

    private DoubleSidedCard card;
    private MockGame game;
    private CardSideBehavior lightSide;
    private CardSideBehavior darkSide;

    @BeforeEach
    void setUp() {
        lightSide = new NumericBehavior(CardColor.RED, CardValue.ONE);
        darkSide = new NumericBehavior(CardColor.BLUE, CardValue.TWO);
        card = new DoubleSidedCard(lightSide, darkSide);
        game = new MockGame();
    }

    @Test
    void testGetColor_LightSide() {
        game.isDarkSide = false;
        assertEquals(CardColor.RED, card.getColor(game));
    }

    @Test
    void testGetColor_DarkSide() {
        game.isDarkSide = true;
        assertEquals(CardColor.BLUE, card.getColor(game));
    }

    @Test
    void testGetValue_LightSide() {
        game.isDarkSide = false;
        assertEquals(CardValue.ONE, card.getValue(game));
    }

    @Test
    void testGetValue_DarkSide() {
        game.isDarkSide = true;
        assertEquals(CardValue.TWO, card.getValue(game));
    }

    @Test
    void testPerformEffect_Delegation() {
        card.performEffect(game);
    }

    @Test
    void testCanBePlayedOn() {
        game.isDarkSide = false;
        CardSideBehavior back = uno.model.cards.behaviors.impl.BackSideBehavior.getInstance();
        assertTrue(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.RED, CardValue.NINE), back),
                game));
        assertTrue(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.GREEN, CardValue.ONE), back),
                game));
        assertFalse(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.GREEN, CardValue.NINE), back),
                game));
    }

    static class MockGame implements Game {
        boolean isDarkSide;

        @Override
        public boolean isDarkSide() {
            return isDarkSide;
        }

        @Override
        public Optional<CardColor> getCurrentColor() {
            return Optional.empty();
        }

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
        public List<AbstractPlayer> getPlayers() {
            return null;
        }

        @Override
        public AbstractPlayer getWinner() {
            return null;
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
}
