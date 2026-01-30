package uno.model.game;

import java.util.List;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.Deck;
import uno.model.players.Player;

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
                game.drawCardForPlayer(player);
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
            game.logSystemAction("SETUP_FIRST_CARD", "ALL_WILD_START", "Color: WILD");
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

            game.logSystemAction("SETUP_CARD_SKIPPED", 
                            firstCard.getClass().getSimpleName(), 
                            "Drawn and discarded: " + firstCard.toString());
            
            System.out.println("Carta scartata: " + firstCard);
            discardPile.addCard(firstCard); // Metti la carta non valida nella pila degli scarti
            firstCard = deck.drawCard();
        }

        // Aggiunge la prima carta valida alla pila degli scarti
        discardPile.addCard(firstCard);
        System.out.println("Prima carta girata: " + firstCard);

        game.logSystemAction("SETUP_CARD_FLIPPED", 
                        firstCard.getClass().getSimpleName(), 
                        "Starting Color: " + firstCard.getColor(game));

        // Imposta il colore attivo in base alla prima carta
        game.setCurrentColor(firstCard.getColor(game));
    }
}