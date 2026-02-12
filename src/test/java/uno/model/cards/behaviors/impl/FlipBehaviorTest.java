package uno.model.cards.behaviors.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;
import uno.model.game.api.Game;
import uno.model.game.impl.GameImpl;
import uno.model.game.impl.GameSetupImpl;
import uno.model.players.impl.AIClassic;
import uno.model.players.impl.AbstractPlayer;
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
class FlipBehaviorTest {

        private Game game;
        private AIClassic aiClassic1;

        @BeforeEach
        void setUp() {
                final GameLogger logger = new uno.model.utils.impl.TestLogger();
                aiClassic1 = new AIClassic("AI-Bot-1");
                final AIClassic aiClassic2 = new AIClassic("AI-Bot-2");

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
        void testFlipBehavior() {

                final CardSideBehavior lightFlip = new FlipBehavior(CardColor.GREEN, CardValue.FLIP);
                final CardSideBehavior darkFlip = new FlipBehavior(CardColor.TEAL, CardValue.FLIP);
                final Card flipCard = new DoubleSidedCard(lightFlip, darkFlip);

                final CardSideBehavior lightNum = new NumericBehavior(CardColor.RED, CardValue.ONE);
                final CardSideBehavior darkNum = new NumericBehavior(CardColor.ORANGE, CardValue.FIVE);
                final Card testCard = new DoubleSidedCard(lightNum, darkNum);

                final List<Optional<Card>> hand = new LinkedList<>();
                hand.add(Optional.of(flipCard));
                hand.add(Optional.of(testCard));
                aiClassic1.setHand(hand);

                final Card discardTop = new DoubleSidedCard(
                                new NumericBehavior(CardColor.GREEN, CardValue.TWO),
                                new NumericBehavior(CardColor.TEAL, CardValue.NINE));
                game.getDiscardPile().addCard(discardTop);
                game.setCurrentColor(CardColor.GREEN);

                assertFalse(game.isDarkSide(), "Il gioco deve iniziare nel Lato Chiaro.");
                assertEquals(CardColor.RED, testCard.getColor(game),
                                "La carta in mano deve mostrare il colore chiaro (Rosso).");

                if (!game.getCurrentPlayer().equals(aiClassic1)) {
                        game.aiAdvanceTurn();
                }

                aiClassic1.takeTurn(game);

                assertTrue(game.isDarkSide(),
                                "Dopo aver giocato una carta FLIP, il gioco deve passare al Lato Oscuro.");

                assertTrue(game.getTopDiscardCard().isPresent());
                assertEquals(flipCard, game.getTopDiscardCard().get());

                assertEquals(CardColor.TEAL, flipCard.getColor(game),
                                "La carta Flip giocata deve ora comportarsi come Lato Oscuro (Teal).");

                assertEquals(1, aiClassic1.getHand().size());
                final Card remainingCard = aiClassic1.getHand().get(0).get();

                assertEquals(CardColor.ORANGE, remainingCard.getColor(game),
                                "Le carte in mano devono mostrare il colore del Lato Oscuro.");
                assertEquals(CardValue.FIVE, remainingCard.getValue(game),
                                "Le carte in mano devono mostrare il valore del Lato Oscuro.");
        }
}
