package uno.Model.Game;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Player.Player;

import java.util.List;

/**
 * Classe di utilità responsabile della logica di 
 * impostazione e avvio di una partita a UNO.
 */
public class GameSetup {

    public static final int INITIAL_HAND_SIZE = 7;

    private final Deck<Card> deck;
    private final DiscardPile discardPile;
    private final List<Player> players;
    private final Game game;

    /**
     * Costruisce un gestore per l'impostazione della partita.
     * @param game La partita da impostare.
     * @param deck Il mazzo da cui pescare.
     * @param discardPile La pila degli scarti su cui girare la prima carta.
     * @param players La lista dei giocatori.
     */
    public GameSetup(Game game, Deck<Card> deck, DiscardPile discardPile, List<Player> players) {
        this.game = game;
        this.deck = deck;
        this.discardPile = discardPile;
        this.players = players;
    }

    /**
     * Esegue tutti i passaggi per avviare la partita.
     */
    public void setupNewGame() {
        // 1. Distribuisci le carte
        dealInitialCards();
        
        // 2. Gira la prima carta
        flipFirstCard();
    }

    /**
     * Distribuisce 7 carte a ciascun giocatore.
     */
    private void dealInitialCards() {
        for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
            for (Player player : this.players) {
                // Aggiunge una carta alla mano del giocatore (presuppone che getHand() esista)
                player.addCardToHand(deck.drawCard());
            }
        }
    }

    /**
     * Gira la prima carta dal mazzo di pesca alla pila degli scarti.
     * Gestisce i casi speciali (es. se è un +4 o un Jolly).
     */
    private void flipFirstCard() {
        Card firstCard = deck.drawCard();

        // Regola ufficiale: se la prima carta è un +4, 
        // rimettila nel mazzo, mischia e gira un'altra carta.
        while (firstCard.getValue() == CardValue.WILD_DRAW_FOUR) {
            deck.addCard(firstCard); // Rimette la carta in fondo (o dove 'addCard' la mette)
            deck.shuffle();
            firstCard = deck.drawCard();
        }

        // Aggiunge la prima carta valida alla pila degli scarti
        discardPile.addCard(firstCard);

        // Se la prima carta è un Jolly, la logica di scelta colore
        // verrà attivata da performEffect.
        // Se è una carta colorata, impostiamo quello come colore iniziale.
        if (firstCard.getColor() != CardColor.WILD) {
            // Imposta il colore attivo iniziale nella partita
            game.setColor(firstCard.getColor());
        }

        // Applica l'effetto della prima carta (es. Salta, Inverti, Pesca 2, Jolly)
        // La classe Game gestirà questo effetto sul "primo" giocatore.
        firstCard.performEffect(game);
        
        // Se è un Jolly, imposta uno stato per la scelta del colore
        if (firstCard.getValue() == CardValue.WILD) {
            game.requestColorChoice();
        }
    }
}