package uno.Model.Game;

// Import di JUnit
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

// Import delle tue classi
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.SkipCard;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Cards.Deck.FlipDeck;
import uno.Model.Cards.Types.NumberedCard;
import uno.Model.Cards.Types.WildCard;
import uno.Model.Player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        gameSetup.setupNewGame();

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

        // ASSERT 1: Controlla lo stato iniziale
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe iniziare sul Lato Chiaro");

        // ACT (Agisci): Flippa il mondo
        game.flipTheWorld(); // Ora questa riga NON dovrebbe fallire.

        // ASSERT 2: Controlla che lo stato sia cambiato
        assertTrue(game.isDarkSide(), "Il gioco dovrebbe essere passato al Lato Scuro");

        // ACT 2: Flippa di nuovo
        game.flipTheWorld();

        // ASSERT 3: Controlla che lo stato sia tornato a quello iniziale
        assertFalse(game.isDarkSide(), "Il gioco dovrebbe essere tornato al Lato Chiaro");
    }
}