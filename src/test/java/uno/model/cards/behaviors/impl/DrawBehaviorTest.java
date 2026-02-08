package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.players.impl.AIClassic;
import uno.model.utils.api.GameLogger;

import uno.model.game.api.DiscardPile;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.game.api.GameRules;
import uno.model.game.impl.GameRulesImpl;

/**
 * Test class for verification of Card Behaviors using the Strategy Pattern.
 * Uses a MockGame to intercept and verify calls made by the behaviors.
 */
class DrawBehaviorTest {

    private Game game;
    private AIClassic aiClassic1;
    private AIClassic aiClassic2;

    @BeforeEach
    void setUp() {
        // Creiamo un logger fittizio (mock) poiché non ci interessa testare il logging
        // qui.
        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        // Setup base
        aiClassic1 = new AIClassic("AI-Bot-1");
        aiClassic2 = new AIClassic("AI-Bot-2");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiClassic1);
        players.add(aiClassic2);
        final Deck<Card> deck = new StandardDeck(logger);

        final GameRules rules = new GameRulesImpl(false, false, false, false);
        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        game = new GameImpl(deck, players, turnManager, discardPile, "CLASSIC", logger, rules);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    @Test
    void testDrawBehavior() {

        final Card blueDrawTwo = createCard(CardColor.BLUE, CardValue.DRAW_TWO);
        final Card redFive = createCard(CardColor.RED, CardValue.FIVE);
        final List<Optional<Card>> hand = new LinkedList<>();
        hand.add(Optional.of(blueDrawTwo));
        hand.add(Optional.of(redFive));
        aiClassic1.setHand(hand);

        final Card discardCard = createCard(CardColor.BLUE, CardValue.ONE);
        game.setCurrentColor(CardColor.BLUE);

        game.getDiscardPile().addCard(discardCard);

        final int initialHandSizeP2 = aiClassic2.getHand().size();

        if (!game.getCurrentPlayer().equals(aiClassic1)) {
            game.aiAdvanceTurn();
        }
        assertEquals(aiClassic1, game.getCurrentPlayer(), "Deve essere il turno di AI 1");

        aiClassic1.takeTurn(game);

        final int finalHandSizeP2 = aiClassic2.getHand().size();

        // CHECK 1: Il giocatore successivo deve aver ricevuto 2 carte
        assertEquals(initialHandSizeP2 + 2, finalHandSizeP2,
                "Il giocatore successivo dovrebbe avere 2 carte in più (effetto Draw Two).");

        // CHECK 2: La carta in cima agli scarti deve essere il Draw Two
        assertTrue(game.getTopDiscardCard().isPresent());
        assertEquals(blueDrawTwo, game.getTopDiscardCard().get(), "Il Draw Two deve essere in cima agli scarti.");

        // CHECK 3: Verifica Salto Turno
        assertTrue(game.isClockwise(), "La direzione del gioco non deve cambiare.");
        assertEquals(aiClassic1, game.getCurrentPlayer(),
                "In partita a 2, dopo un +2 l'avversario salta e tocca di nuovo a chi ha giocato.");
    }

    /**
     * Helper per creare una carta semplice al volo per i test.
     *
     * @param color colore carta
     * @param value valore carta
     * @return carta creata
     */
    private Card createCard(final CardColor color, final CardValue value) {
        if (value == CardValue.WILD) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 0), // Fronte
                    BackSideBehavior.getInstance());
        } else if (value == CardValue.WILD_DRAW_FOUR) {
            return new DoubleSidedCard(
                    new WildBehavior(value, 4), // Fronte
                    BackSideBehavior.getInstance());
        } else if (isAction(value)) {
            return new DoubleSidedCard(
                    new ActionBehavior(color, value, correctAction(value)),
                    BackSideBehavior.getInstance());
        } else if (value == CardValue.DRAW_TWO) {
            return new DoubleSidedCard(
                    new DrawBehavior(color, value, 2),
                    BackSideBehavior.getInstance());
        } else {
            return new DoubleSidedCard(
                    new NumericBehavior(color, value),
                    BackSideBehavior.getInstance());
        }
    }

    private boolean isAction(final CardValue value) {
        return value == CardValue.SKIP || value == CardValue.REVERSE;
    }

    private Consumer<Game> correctAction(final CardValue value) {
        if (value == CardValue.SKIP) {
            return g -> g.skipPlayers(1);
        } else if (value == CardValue.REVERSE) {
            return Game::reversePlayOrder;
        }

        return g -> {
        };
    }
}
