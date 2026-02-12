package uno.model.game.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.BackSideBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameRules;

/**
 * Unit tests for the TurnManagerImpl class, covering turn advancement, direction changes, player skipping,
 * and Uno penalty enforcement. Tests ensure correct player sequencing and state management across various scenarios.
 */
class TurnManagerTest {

    private TurnManager turnManager;
    private List<AbstractPlayer> players;
    private Game game;

    @BeforeEach
    void setUp() {

        players = new ArrayList<>();
        players.add(new AIClassic("P1"));
        players.add(new AIClassic("P2"));
        players.add(new AIClassic("P3"));
        players.add(new AIClassic("P4"));

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck = new StandardDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();

        final TurnManager tm = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, tm, discardPile, "CLASSIC", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);

        turnManager = game.getTurnManager();
    }

    @Test
    void testInitialization() {
        assertTrue(turnManager.isClockwise(), "Il gioco deve iniziare in senso orario.");
        assertFalse(turnManager.hasDrawnThisTurn(), "Il flag hasDrawn deve essere falso all'inizio.");
        assertNotNull(turnManager.getCurrentPlayer(), "Deve esserci un giocatore attivo.");
        assertTrue(players.contains(turnManager.getCurrentPlayer()), "Il giocatore attivo deve essere nella lista.");
    }

    @Test
    void testAdvanceTurnClockwise() {
        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final int startIndex = players.indexOf(current);

        turnManager.advanceTurn(game);

        final AbstractPlayer next = turnManager.getCurrentPlayer();
        final int nextIndex = players.indexOf(next);

        final int expectedIndex = (startIndex + 1) % players.size();
        assertEquals(expectedIndex, nextIndex, "In senso orario, l'indice deve avanzare di 1 (modulo N).");
    }

    @Test
    void testAdvanceTurnCounterClockwise() {
        turnManager.reverseDirection();
        assertFalse(turnManager.isClockwise());

        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final int startIndex = players.indexOf(current);

        turnManager.advanceTurn(game);

        final AbstractPlayer next = turnManager.getCurrentPlayer();
        final int nextIndex = players.indexOf(next);

        final int expectedIndex = (startIndex - 1 + players.size()) % players.size();
        assertEquals(expectedIndex, nextIndex, "In senso antiorario, l'indice deve diminuire di 1.");
    }

    @Test
    void testSkipPlayer() {
        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final int startIndex = players.indexOf(current);

        turnManager.skipPlayers(1);

        final int expectedIndex = (startIndex + 2) % players.size();
        assertEquals(players.get(expectedIndex), turnManager.peekNextPlayer(),
                "Peek deve vedere oltre il giocatore saltato.");

        turnManager.advanceTurn(game);

        assertEquals(players.get(expectedIndex), turnManager.getCurrentPlayer(),
                "Il turno deve avanzare di 2 posizioni (1 attuale + 1 salto).");
    }

    @Test
    void testSkipTwoPlayers() {
        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final int startIndex = players.indexOf(current);

        turnManager.skipPlayers(2);
        turnManager.advanceTurn(game);

        final int expectedIndex = (startIndex + 3) % players.size();
        assertEquals(players.get(expectedIndex), turnManager.getCurrentPlayer(),
                "Il turno deve avanzare di 3 posizioni.");
    }

    @Test
    void testReverseAndSkipCombined() {
        turnManager.reverseDirection();
        turnManager.skipPlayers(1);

        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final int startIndex = players.indexOf(current);

        turnManager.advanceTurn(game);

        final int expectedIndex = (startIndex - 2 + players.size()) % players.size();
        assertEquals(players.get(expectedIndex), turnManager.getCurrentPlayer());
    }

    @Test
    void testStateResetOnTurnAdvance() {
        turnManager.setHasDrawnThisTurn(true);
        assertTrue(turnManager.hasDrawnThisTurn());

        turnManager.advanceTurn(game);

        assertFalse(turnManager.hasDrawnThisTurn(), "Il flag hasDrawn deve resettarsi al cambio turno.");
    }

    @Test
    void testPeekDoesNotAlterState() {
        final AbstractPlayer current = turnManager.getCurrentPlayer();
        final boolean direction = turnManager.isClockwise();

        final AbstractPlayer peeked = turnManager.peekNextPlayer();

        assertNotNull(peeked);
        assertNotEquals(current, peeked);

        assertEquals(current, turnManager.getCurrentPlayer());
        assertEquals(direction, turnManager.isClockwise());
    }

    @Test
    void testUnoPenaltyCheck() {
        final GameRules rules = new GameRulesImpl(true, false, false, false);
        turnManager = new TurnManagerImpl(players, rules);

        game = new GameImpl(new StandardDeck(new uno.model.utils.impl.TestLogger()),
                players, turnManager, new DiscardPileImpl(), "CLASSIC", new uno.model.utils.impl.TestLogger(), rules);

        final AbstractPlayer playerToPenalize = turnManager.peekNextPlayer(); // P2

        final Card redNine = new DoubleSidedCard(
                new NumericBehavior(CardColor.RED, CardValue.NINE),
                BackSideBehavior.getInstance());
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        playerToPenalize.setHand(listcard);

        assertFalse(playerToPenalize.isHasCalledUno());
        assertEquals(1, playerToPenalize.getHandSize());

        turnManager.advanceTurn(game);

        assertEquals(3, playerToPenalize.getHandSize(),
                "Il giocatore deve avere 3 carte in mano dopo la penalità (1 + 2 penalità)");
    }
}
