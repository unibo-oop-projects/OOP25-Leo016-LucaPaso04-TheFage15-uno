package uno.Model.Game;

import uno.Model.Cards.Card;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Player.Player; // Assumendo che esista
import uno.View.GameModelObserver; // <-- IMPORTA L'OBSERVER

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Classe principale del Modello. Contiene la logica e lo stato della partita.
 * Ora è "Observable": notifica gli Observer (View) quando cambia.
 */
public class Game {

    private final List<GameModelObserver> observers = new ArrayList<>(); // <-- LISTA OBSERVER
    private final Deck<Card> drawDeck;
    private final DiscardPile discardPile; // <-- USA LA TUA CLASSE
    private final List<Player> players;
    private int currentPlayerIndex;

    public Game(Deck<Card> deck) {
        this.drawDeck = deck;
        this.discardPile = new DiscardPile(); // <-- INIZIALIZZA
        this.players = new ArrayList<>();
        this.currentPlayerIndex = 0;
        // Aggiungi giocatori di default (esempio)
        players.add(new Player("Giocatore 1"));
        players.add(new Player("IA")); // Assumendo che Player esista
        // TODO: Distribuire carte iniziali
    }
    
    // --- METODI OBSERVER ---
    
    /**
     * Aggiunge un observer (la View) da notificare.
     */
    public void addObserver(GameModelObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Notifica tutti gli observer che lo stato è cambiato.
     */
    private void notifyObservers() {
        for (GameModelObserver obs : observers) {
            obs.onGameUpdate();
        }
    }
    
    // --- METODI DI GIOCO (Esempio) ---
    
    public void playCard(Card card) {
        // TODO: Aggiungere la logica di validazione della mossa
        // (es. if (!isValidMove(card))) { throw new IllegalStateException("Mossa non valida!"); }
        
        // Esegui effetto carta (polimorfismo)
        card.performEffect(this); // <-- ORA FUNZIONA (vedi modifiche sotto)
        
        // Sposta la carta
        players.get(currentPlayerIndex).getHand().remove(card);
        discardPile.addCard(card); // <-- USA IL METODO DELLA CLASSE
        
        // Passa il turno (logica base)
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        
        // Notifica la view!
        notifyObservers();
    }
    
    public void playerDrawCard(Player player) {
        if (drawDeck.isEmpty()) {
            // Logica per rimescolare gli scarti (non implementata)
            notifyObservers(); // Notifica comunque per aggiornare la UI
            return;
        }
        player.getHand().add(drawDeck.drawCard());
        
        // Notifica la view!
        notifyObservers();
    }

    // --- METODI GETTER (per la View) ---

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    public Card getTopDiscardCard() {
        try {
            return discardPile.getTopCard();
        } catch (NoSuchElementException e) {
            return null; // O gestisci diversamente se la pila è vuota
        }
    }

    public boolean isDiscardPileEmpty() {
        return discardPile.isEmpty();
    }
    
    // TODO: Aggiungere altri getter (getPlayers, ecc.)
    
    // METODI PER GLI EFFETTI DELLE CARTE (chiamati da card.performEffect(this))
    public void skipNextPlayer() {
        // Logica per saltare il giocatore
        System.out.println("Giocatore saltato!");
    }
    
    public void makeNextPlayerDraw(int amount) {
        // Logica per far pescare il prossimo giocatore
        System.out.println("Il prossimo giocatore pesca " + amount);
    }
    
    public void reversePlayOrder() {
        // Logica per invertire l'ordine
        System.out.println("Ordine invertito!");
    }
}