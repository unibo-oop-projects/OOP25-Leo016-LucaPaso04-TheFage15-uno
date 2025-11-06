package uno.Model.Game;

import uno.Model.Cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Rappresenta la pila degli scarti nel gioco UNO.
 * Gestisce la collezione di carte giocate e fornisce metodi
 * per interagire con esse (es. vedere la carta in cima).
 */
public class DiscardPile {

    private final List<Card> cards;

    /**
     * Crea una nuova pila degli scarti, inizialmente vuota.
     */
    public DiscardPile() {
        this.cards = new ArrayList<>();
    }

    /**
     * Aggiunge una carta in cima alla pila degli scarti.
     * @param card La carta da aggiungere.
     */
    public void addCard(Card card) {
        if (card == null) {
            throw new IllegalArgumentException("Non è possibile aggiungere una carta nulla alla pila degli scarti.");
        }
        this.cards.add(card);
    }

    /**
     * Restituisce la carta in cima alla pila degli scarti (l'ultima giocata)
     * senza rimuoverla.
     *
     * @return La carta in cima alla pila.
     * @throws NoSuchElementException se la pila è vuota.
     */
    public Card getTopCard() {
        if (isEmpty()) {
            throw new NoSuchElementException("La pila degli scarti è vuota.");
        }
        // L'ultima carta nella lista è quella in cima alla pila
        return this.cards.get(this.cards.size() - 1);
    }

    /**
     * Controlla se la pila degli scarti è vuota.
     * @return true se la pila è vuota, false altrimenti.
     */
    public boolean isEmpty() {
        return this.cards.isEmpty();
    }

    /**
     * Rimuove e restituisce tutte le carte dalla pila degli scarti
     * tranne quella in cima (l'ultima giocata).
     * Questo metodo è usato per rimescolare il mazzo di pesca.
     *
     * @return Una lista di carte da rimettere nel mazzo di pesca.
     */
    public List<Card> takeAllExceptTop() {
        if (this.cards.size() <= 1) {
            // Se c'è 0 o 1 carta, non c'è nulla da rimescolare
            return new ArrayList<>();
        }

        // Prende la carta in cima
        Card topCard = getTopCard();
        
        // Crea una nuova lista con tutte le carte tranne l'ultima
        List<Card> cardsToReshuffle = new ArrayList<>(this.cards.subList(0, this.cards.size() - 1));

        // Pulisce la pila attuale e ri-aggiunge solo la carta in cima
        this.cards.clear();
        this.cards.add(topCard);

        return cardsToReshuffle;
    }

    /**
     * Restituisce una copia dell'intera pila di scarti (principalmente per la View).
     * @return Una lista di carte nella pila.
     */
    public List<Card> getCards() {
        // Restituisce una copia per evitare modifiche esterne (incapsulamento)
        return new ArrayList<>(this.cards);
    }
}