package uno.Model.Game;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Players.Player;

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
    public void setupNewGame(boolean isAllWild) {
        // 1. Distribuisci le carte
        dealInitialCards();
        
        // 2. Gira la prima carta
        flipFirstCard(isAllWild);
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
    private void flipFirstCard(boolean isAllWild) {
        if(isAllWild){
            game.setCurrentColor(CardColor.WILD);
            return;
        }

        Card firstCard = deck.drawCard();

        // Regola ufficiale: se la prima carta è diversa da un numero,
        // bisogna continuare a scartare carte finchè non ne esce una valida.
        while ((firstCard.getValue(game) == CardValue.WILD_DRAW_FOUR ||
                firstCard.getValue(game) == CardValue.WILD ||
                firstCard.getValue(game) == CardValue.DRAW_TWO ||
                firstCard.getValue(game) == CardValue.REVERSE ||
                firstCard.getValue(game) == CardValue.SKIP ||
                firstCard.getValue(game) == CardValue.FLIP) && !isAllWild) {
            System.out.println("Carta scartata: " + firstCard);
            discardPile.addCard(firstCard); // Metti la carta non valida nella pila degli scarti
            firstCard = deck.drawCard();
        }

        // Aggiunge la prima carta valida alla pila degli scarti
        discardPile.addCard(firstCard);
        System.out.println("Prima carta girata: " + firstCard);

        // Imposta il colore attivo in base alla prima carta
        game.setCurrentColor(firstCard.getColor(game));
    }
}