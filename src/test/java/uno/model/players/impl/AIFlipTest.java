package uno.model.players.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.impl.FlipBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.FlipDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.api.AbstractPlayer;
import uno.model.utils.api.GameLogger;

class AIFlipTest {

    private Game game;
    private AIFlip aiFlip;

    @BeforeEach
    void setUp() {
        aiFlip = new AIFlip("AI-Flip");

        final List<AbstractPlayer> players = new ArrayList<>();
        players.add(aiFlip);

        final GameLogger logger = new uno.model.utils.impl.TestLogger();
        final Deck<Card> deck = new FlipDeck(logger);
        game = new GameImpl(deck, players, "FLIP", logger);

        final GameSetupImpl setup = new GameSetupImpl(
                game,
                deck,
                game.getDiscardPile(),
                players);
        setup.initializeGame(false);
    }

    private Card createCard(final CardColor color, final CardValue value, final boolean isDarkSide) {
        // Semplicificazione: creiamo una carta DoubleSided dove conta il lato attivo.
        // Se isDarkSide è true, mettiamo il comportamento sul back.
        if (value == CardValue.FLIP) {
            return new DoubleSidedCard(
                    new FlipBehavior(color, value), // Front
                    new FlipBehavior(CardColor.ORANGE, value) // Back (placeholder)
            );
        } else if (value == CardValue.WILD_DRAW_COLOR) {
            // Dark side power card
            return new DoubleSidedCard(
                    new NumericBehavior(CardColor.RED, CardValue.ONE), // Front dummy
                    new WildBehavior(value, 0) // Back
            );
        } else if (value == CardValue.DRAW_FIVE) {
            // Dark side power card
            final int drawAmount = 5;
            return new DoubleSidedCard(
                    new NumericBehavior(CardColor.RED, CardValue.ONE), // Front dummy
                    new WildBehavior(value, drawAmount) // Back
            );
        } else {
            return new DoubleSidedCard(
                    new NumericBehavior(color, value),
                    new NumericBehavior(isDarkSide ? color : CardColor.TEAL, value));
        }
    }

    @Test
    void testAIPrioritizesFlipCard() {
        // Scenario: AI ha una carta FLIP. Dovrebbe giocarla subito (Score 100).

        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.ONE, false));
        game.setCurrentColor(CardColor.RED);

        final Card flipCard = createCard(CardColor.RED, CardValue.FLIP, false);
        final Card redNine = createCard(CardColor.RED, CardValue.NINE, false);

        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(redNine));
        listcard.add(Optional.of(flipCard));
        aiFlip.setHand(listcard);

        // Forza turno IA
        if (!game.getCurrentPlayer().equals(aiFlip)) {
            game.aiAdvanceTurn();
        }

        aiFlip.takeTurn(game);

        assertEquals(CardValue.FLIP, game.getTopDiscardCard().get().getValue(game),
                "L'IA dovrebbe dare priorità alla carta FLIP.");
    }

    @Test
    @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
    void testAIDarkSidePowerCards() {
        // Scenario: Siamo nel Dark Side. L'IA ha una WILD_DRAW_COLOR.

        // --- FIX NPE & State (Reflection approach) ---
        // game.flipTheWorld() cerca currentPlayedCard.
        // Impostiamo il campo currentPlayedCard tramite Reflection per evitare
        // side-effects di game.playCard() che fa avanzare il gioco o controlla regole.

        final Card dummyCard = createCard(CardColor.RED, CardValue.ONE, false);
        try {
            final java.lang.reflect.Field field = GameImpl.class.getDeclaredField("currentPlayedCard");
            field.setAccessible(true);
            field.set(game, dummyCard);
        } catch (final ReflectiveOperationException e) {
            org.junit.jupiter.api.Assertions.fail("Reflection failed: " + e.getMessage());
        }

        // Setup mazzo e mano normale
        game.getDiscardPile().addCard(createCard(CardColor.RED, CardValue.NINE, false));
        game.setCurrentColor(CardColor.RED);

        game.flipTheWorld(); // Passa al Dark Side
        assertTrue(game.isDarkSide());

        game.getDiscardPile().addCard(createCard(CardColor.TEAL, CardValue.ONE, true));
        game.setCurrentColor(CardColor.TEAL); // Colore Dark

        final Card darkWild = createCard(CardColor.WILD, CardValue.WILD_DRAW_COLOR, true);
        // Nota: createCard mette WILD_DRAW_COLOR sul retro

        final Card tealNine = createCard(CardColor.TEAL, CardValue.NINE, true);

        // SET HAND PRIMA DI TUTTO
        final List<Optional<Card>> listcard = new LinkedList<>();
        listcard.add(Optional.of(tealNine));
        listcard.add(Optional.of(darkWild));
        aiFlip.setHand(listcard);

        // Ora assicuriamoci che sia il turno di AI
        while (!game.getCurrentPlayer().equals(aiFlip)) {
            game.aiAdvanceTurn();
        }

        aiFlip.takeTurn(game);

        // Verifica che abbia giocato la carta potente
        assertEquals(CardValue.WILD_DRAW_COLOR, game.getTopDiscardCard().get().getValue(game));
    }
}
