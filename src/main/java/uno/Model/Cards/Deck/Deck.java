package uno.Model.Cards.Deck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import uno.Model.Cards.Card;

/**
 * Classe astratta che rappresenta un mazzo di carte generico.
 * <T extends Card> assicura che il mazzo possa contenere solo oggetti di tipo Card.
 */
public abstract class Deck<T extends Card> {

    // Lista protetta per contenere le carte del mazzo. 
    // "protected" permette alle sottoclassi (es. UnoStandardDeck) di accedervi.
    protected List<T> cards;

    /**
     * Costruttore della classe Deck. 
     * Inizializza la lista e chiama il metodo astratto per popolare il mazzo.
     */
    public Deck() {
        this.cards = new ArrayList<>();
        createDeck(); // Ogni sottoclasse DEVE implementare questo metodo.
        shuffle();    // Mescola il mazzo subito dopo la creazione.
    }

    /**
     * Metodo astratto che deve essere implementato dalle sottoclassi 
     * per definire la composizione del mazzo (es. 108 carte standard, 
     * 112 carte variante, ecc.).
     */
    protected abstract void createDeck();

    /**
     * Mescola casualmente le carte nel mazzo.
     */
    public void shuffle() {
        System.out.println("Mischiando il mazzo...");
        Collections.shuffle(cards);
    }

    /**
     * Pesca e rimuove la carta in cima al mazzo.
     * * @return La prima carta del mazzo.
     * @throws NoSuchElementException se il mazzo è vuoto.
     */
    public T drawCard() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("Impossibile pescare: il mazzo è vuoto.");
        }
        // Rimuove e restituisce l'ultima carta (la cima del mazzo).
        return cards.remove(cards.size() - 1);
    }

    /**
     * Restituisce la carta in cima al mazzo senza rimuoverla (per l'ispezione).
     * * @return La prima carta del mazzo.
     * @throws NoSuchElementException se il mazzo è vuoto.
     */
    public T peek() {
        if (cards.isEmpty()) {
            throw new NoSuchElementException("Impossibile ispezionare: il mazzo è vuoto.");
        }
        return cards.get(cards.size() - 1);
    }

    /**
     * Aggiunge una singola carta al mazzo.
     * * @param card La carta da aggiungere.
     */
    public void addCard(T card) {
        cards.add(card);
    }

    /**
     * Restituisce il numero di carte rimanenti nel mazzo.
     * * @return La dimensione attuale del mazzo.
     */
    public int size() {
        return cards.size();
    }

    /**
     * Verifica se il mazzo è vuoto.
     * * @return true se il mazzo non contiene carte, altrimenti false.
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
