package uno.Model.Game;

// Import di JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import delle tue classi
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.SkipCard;
import uno.Model.Cards.Types.FlipCard;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Cards.Deck.FlipDeck;
import uno.Model.Cards.Types.NumberedCard;
import uno.Model.Players.Player;
import uno.Model.Cards.Attributes.CardFace;


import java.util.ArrayList;
import java.util.List;

/**
 * Classe di test (nello stile di arcaym) per la logica di UNO Flip.
 * Testa la logica del Modello (Game.java), non la View.
 */
class GameFlipTest {

    private Game game;
    private GameSetup gameSetup;
    private Deck<Card> flipDeck;
    private Player player1;
    private List<Player> players;

    /**
     * Questo metodo @BeforeEach viene eseguito PRIMA di ogni
     * singolo test (@Test), assicurando che ogni test sia isolato.
     */
    @BeforeEach
    void setUp() {
        // ARRANGE (Prepara)
        player1 = new Player("Test Player");
        players = new ArrayList<>(List.of(player1));
        flipDeck = new FlipDeck();
        
        // Il costruttore di Game popola le mappe
        game = new Game(flipDeck, players);
        
        // Prepara il setup del gioco
        gameSetup = new GameSetup(game, flipDeck, game.getDiscardPile(), players);
        
        // Esegui il setup: distribuisce le carte, mette la prima carta sullo scarto.
        // Se questa chiamata non mette una carta sulla discardPile, la riga sotto la risolve.
        gameSetup.setupNewGame(false);

        if (game.getDiscardPile().isEmpty()) {
            // Forza l'aggiunta di una carta se per qualche motivo non c'è
            game.getDiscardPile().addCard(flipDeck.drawCard());
        }
    }

    /**
     * Test 1: Verifica che il gioco inizi sul Lato Chiaro
     * e che il metodo flipTheWorld() cambi correttamente lo stato.
     */
    @Test
    void testFlipWorldStateChange() {
        // Il setup è stato fatto in @BeforeEach. La discardPile ha una carta.

        // ARRANGE: Crea una carta Flip specifica
        CardFace lightFace = new CardFace(CardColor.GREEN, CardValue.FLIP);
        CardFace darkFace = new CardFace(CardColor.YELLOW, CardValue.FLIP);
        Card flipCard = new FlipCard(lightFace, darkFace);

        // ASSERT 1: Controlla lo stato iniziale
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe iniziare sul Lato Chiaro");

        // ACT (Agisci): Flippa il mondo
        game.flipTheWorld(flipCard); // Ora questa riga NON dovrebbe fallire.

        // ASSERT 2: Controlla che lo stato sia cambiato
        assertTrue(game.isDarkSide(), "Il gioco dovrebbe essere passato al Lato Scuro");

        // ACT 2: Flippa di nuovo
        game.flipTheWorld(flipCard);

        // ASSERT 3: Controlla che lo stato sia tornato a quello iniziale
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe essere tornato al Lato Chiaro");
    }

    /**
     * Test 2: Verifica che le carte vengano interpretate correttamente
     * in base al lato attivo del gioco.
     */
    @Test
    void testCardFaceInterpretation() {
        // ARRANGE: Crea una carta Flip specifica
        CardFace lightFace = new CardFace(CardColor.RED, CardValue.SKIP);
        CardFace darkFace = new CardFace(CardColor.BLUE, CardValue.FIVE);
        Card flipCard = new SkipCard(lightFace, darkFace);

        // ACT & ASSERT 1: Con il gioco sul Lato Chiaro
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe essere sul Lato Chiaro");
        assertEquals(CardColor.RED, flipCard.getColor(game), "La carta dovrebbe essere ROSSA sul Lato Chiaro");
        assertEquals(CardValue.SKIP, flipCard.getValue(game), "La carta dovrebbe essere SKIP sul Lato Chiaro");

        // ACT: Flippa il mondo
        game.flipTheWorld(flipCard);

        // ASSERT 2: Con il gioco sul Lato Scuro
        assertTrue(game.isDarkSide(), "Il gioco dovrebbe essere sul Lato Scuro");
        assertEquals(CardColor.BLUE, flipCard.getColor(game), "La carta dovrebbe essere BLU sul Lato Scuro");
        assertEquals(CardValue.FIVE, flipCard.getValue(game), "La carta dovrebbe essere FIVE sul Lato Scuro");
    }

    /**
     * Test 3: Verifica che le regole di gioco rispettino il lato attivo.
     */
    @Test
    void testPlayabilityRulesBasedOnActiveSide() {
        // Prendi la carta in cima allo scarto;
        Card topCard = game.getTopDiscardCard();

        // ARRANGE: Crea una carta Flip specifica
        CardFace lightFace = new CardFace(topCard.getColor(game), CardValue.THREE);
        // ACT: Flippa il mondo
        game.flipTheWorld(topCard);
        CardFace darkFace = new CardFace(CardColor.PINK, topCard.getValue(game));
        // ACT: Flippa il mondo
        game.flipTheWorld(topCard);
        Card flipCard = new NumberedCard(lightFace, darkFace);

        // ACT & ASSERT 1: Con il gioco sul Lato Chiaro
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe essere sul Lato Chiaro");
        // La carta dovrebbe essere giocabile per colore (VERDE)
        assertTrue(flipCard.canBePlayedOn(topCard, game), "La carta dovrebbe essere giocabile per COLORE sul Lato Chiaro");

        // ACT: Flippa il mondo
        game.flipTheWorld(topCard);

        // ASSERT 2: Con il gioco sul Lato Scuro
        assertTrue(game.isDarkSide(), "Il gioco dovrebbe essere sul Lato Scuro");
        // La carta dovrebbe essere giocabile per valore (SETTE)
        assertTrue(flipCard.canBePlayedOn(topCard, game), "La carta dovrebbe essere giocabile per VALORE sul Lato Scuro");
    }

    /**
     * Test 4: Verifica che l'effetto della carta DRAW_FIVE funzioni correttamente.
     */
    @Test
    void testDrawFiveEffect() {
        Card topCard = game.getTopDiscardCard();
        game.flipTheWorld(topCard); // Assicuriamoci di essere sul Lato Scuro

        // ARRANGE: Crea una carta Draw Five specifica
        CardFace lightFace = new CardFace(CardColor.YELLOW, CardValue.DRAW_FIVE);
        CardFace darkFace = new CardFace(topCard.getColor(game), CardValue.DRAW_FIVE);
        Card drawFiveCard = new uno.Model.Cards.Types.DrawFiveCard(lightFace, darkFace);

        //Mano del giocatore
        Player humanPlayer = players.get(0);
        humanPlayer.addCardToHand(drawFiveCard);


        int initialHandSize = player1.getHand().size();

        System.out.println("Mano iniziale del giocatore: " + initialHandSize + " carte.");

        // ACT: Esegui l'effetto della carta
        game.playCard(drawFiveCard);

        // ASSERT: Verifica che il giocatore abbia pescato 5 carte
        assertEquals(initialHandSize + 6, player1.getHand().size(), "Il giocatore dovrebbe aver pescato 5 carte");
        System.out.println("Mano del giocatore dopo Draw Five: " + player1.getHand().size() + " carte.");
    }
}