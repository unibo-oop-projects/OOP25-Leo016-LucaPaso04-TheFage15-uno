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
import uno.model.players.impl.AbstractPlayer;

/**
 * Unit tests for the {@link DoubleSidedCard} class.
 */
class DoubleSidedCardTest {

    private DoubleSidedCard card;
    private MockGame game;

    @BeforeEach
    void setUp() {
        final CardSideBehavior lightSide = new NumericBehavior(CardColor.RED, CardValue.ONE);
        final CardSideBehavior darkSide = new NumericBehavior(CardColor.BLUE, CardValue.TWO);
        card = new DoubleSidedCard(lightSide, darkSide);
        game = new MockGame();
    }

    @Test
    void testGetColorLightSide() {
        game.isDarkSide = false;
        assertEquals(CardColor.RED, card.getColor(game));
    }

    @Test
    void testGetColorDarkSide() {
        game.isDarkSide = true;
        assertEquals(CardColor.BLUE, card.getColor(game));
    }

    @Test
    void testGetValueLightSide() {
        game.isDarkSide = false;
        assertEquals(CardValue.ONE, card.getValue(game));
    }

    @Test
    void testGetValueDarkSide() {
        game.isDarkSide = true;
        assertEquals(CardValue.TWO, card.getValue(game));
    }

    @Test
    void testPerformEffectDelegation() {
        card.performEffect(game);
    }

    @Test
    void testCanBePlayedOn() {
        game.isDarkSide = false;
        final CardSideBehavior back = uno.model.cards.behaviors.impl.BackSideBehavior.getInstance();
        assertTrue(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.RED, CardValue.NINE), back),
                game));
        assertTrue(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.GREEN, CardValue.ONE), back),
                game));
        assertFalse(card.canBePlayedOn(new DoubleSidedCard(new NumericBehavior(CardColor.GREEN, CardValue.NINE), back),
                game));
    }

    static class MockGame implements Game {
        private boolean isDarkSide;

        @Override
        public boolean isDarkSide() {
            return isDarkSide;
        }

        @Override
        public Optional<CardColor> getCurrentColor() {
            return Optional.empty();
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
        public List<AbstractPlayer> getPlayers() {
            return List.of();
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
        public boolean hasCurrentPlayerDrawn(final AbstractPlayer player) {
            return false;
        }

        @Override
        public void setCurrentColor(final CardColor color) {
        }

        @Override
        public void aiAdvanceTurn() {
        }

        @Override
        public GameRules getRules() {
            return null;
        }

        @Override
        public void logSystemAction(final String actionType, final String cardDetails, final String extraInfo) {
        }

        @Override
        public void startNewRound() {
        }
    }
}
