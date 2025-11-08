package uno.Model.Game;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Deck.Deck;
import uno.Model.Player.Player;
import uno.View.GameModelObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Classe principale del Modello. Contiene la logica e lo stato della partita.
 * Delega la gestione dei turni al TurnManager.
 */
public class Game {

    private final List<GameModelObserver> observers = new ArrayList<>();
    private final Deck<Card> drawDeck;
    private final DiscardPile discardPile;
    private final List<Player> players;
    private Player winner; // <-- NUOVO CAMPO PER IL VINCITORE
    
    // --- NUOVI CAMPI ---
    private final TurnManager turnManager; // Delega la gestione dei turni
    private GameState currentState;
    private CardColor currentColor;

    /**
     * Costruisce una nuova partita.
     * @param deck Il mazzo da usare.
     * @param players La lista dei giocatori, già creata e in ordine.
     */
    public Game(Deck<Card> deck, List<Player> players) {
        this.drawDeck = deck;
        this.players = players;
        this.winner = null; // Inizializza il vincitore a null
        this.discardPile = new DiscardPile();
        
        // --- COLLEGAMENTO ---
        // Il TurnManager ora possiede la logica di chi sta giocando
        this.turnManager = new TurnManager(players); 
        
        this.currentState = GameState.RUNNING;
        this.currentColor = null; // Nessun colore attivo all'inizio
        
        // NOTA: La distribuzione delle carte ora è gestita da GameSetup
        // nel MenuController, non più qui.
    }
    
    // --- METODI OBSERVER ---
    
    public void addObserver(GameModelObserver observer) {
        this.observers.add(observer);
    }

    private void notifyObservers() {
        for (GameModelObserver obs : observers) {
            obs.onGameUpdate();
        }
    }
    
    // --- METODI DI GIOCO ---
    
    public void playCard(Card card) {
        Player player = getCurrentPlayer();

        // --- INIZIO LOGICA DI VALIDAZIONE ---
        
        // 1. Controlla lo stato del gioco
        if (this.currentState != GameState.RUNNING) {
            throw new IllegalStateException("Non è possibile giocare una carta ora (Stato: " + this.currentState + ")");
        }
        
        // 2. Controlla se il giocatore ha la carta
        if (!player.getHand().contains(card)) {
            throw new IllegalStateException("Il giocatore non ha questa carta!");
        }

        // 3. Controlla se la mossa è valida secondo le regole
        if (!isValidMove(card)) {
            throw new IllegalStateException("Mossa non valida! La carta " + card + " non può essere giocata.");
        }
        
        // --- FINE LOGICA DI VALIDAZIONE ---

        // Se la mossa è valida, aggiorna il currentColor.
        if (card.getColor() == CardColor.WILD) {
            this.currentColor = null; // Sarà impostato da onColorChosen()
        } else {
            // Se è una carta colorata, quello è il nuovo colore attivo.
            this.currentColor = card.getColor();
        }

        // Esegui effetto carta (polimorfismo)
        card.performEffect(this);
        
        // Sposta la carta
        player.playCard(card);
        discardPile.addCard(card);

        // --- CONTROLLO VITTORIA ---
        // Controlla se il giocatore ha vinto DOPO aver giocato la carta
        if (player.hasWon()) {

            //Controlla che il giocatore abbia effettivamente chiamato UNO
            if (!player.getHasCalledUno()) {
                // Penalità: pesca 2 carte
                drawCardForPlayer(player);
                drawCardForPlayer(player);
                notifyObservers(); // Notifica la View per mostrare le nuove carte
                throw new IllegalStateException("Non hai chiamato UNO! Penalità applicata: hai pescato 2 carte.");
            }

            this.currentState = GameState.GAME_OVER;
            this.winner = player;
            System.out.println("PARTITA FINITA! Il vincitore è " + this.winner.getName());
            notifyObservers(); // Notifica la View che la partita è finita
            return; // Non avanzare il turno, la partita è bloccata
        }
        // -------------------------
        
        // --- LOGICA DI AVANZAMENTO TURNO ---
        // Non passiamo il turno solo se stiamo aspettando una scelta di colore
        // (impostato da performEffect -> requestColorChoice).
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            this.turnManager.advanceTurn();
        }
        
        notifyObservers();
    }

    /**
     * Controlla se la carta che si sta tentando di giocare è valida
     * in base allo stato attuale del gioco (carta in cima e colore attivo).
     */
    private boolean isValidMove(Card cardToPlay) {
        Card topCard = getTopDiscardCard(); //
        
        // Determina il colore attivo. Se currentColor è impostato (da un Jolly),
        // usa quello. Altrimenti, usa il colore della carta in cima.
        CardColor activeColor = (this.currentColor != null) ? this.currentColor : topCard.getColor();

        // 1. Regola Jolly Standard (WILD)
        if (cardToPlay.getValue() == CardValue.WILD) {
            return true;
        }

        // 2. Regola Jolly +4 (WILD_DRAW_FOUR)
        if (cardToPlay.getValue() == CardValue.WILD_DRAW_FOUR) {
            // Regola ufficiale: puoi giocarla solo se NON hai
            // altre carte che corrispondono al COLORE ATTIVO.
            for (Card cardInHand : getCurrentPlayer().getHand()) {
                if (cardInHand.getColor() == activeColor) {
                    return false; // Mossa illegale: hai un'altra carta giocabile
                }
            }
            return true; // Mossa legale
        }

        // 3. Regole Standard (non-Jolly)
        // La carta è valida se corrisponde al colore ATTIVO...
        if (cardToPlay.getColor() == activeColor) {
            return true;
        }
        
        // ...o se corrisponde al VALORE della carta in cima.
        if (cardToPlay.getValue() == topCard.getValue()) {
            return true;
        }

        // Se nessuna regola è soddisfatta, la mossa non è valida
        return false;
    }
    
    /**
     * Metodo helper per controllare se il giocatore ha mosse valide.
     */
    private boolean playerHasPlayableCard(Player player) {
        for (Card card : player.getHand()) {
            if (isValidMove(card)) {
                return true; // Trovata una carta giocabile
            }
        }
        return false; // Nessuna carta giocabile
    }

    /**
     * Metodo helper per vedere se il giocatore ha pescato in questo turno.
     * @return true se ha pescato, false altrimenti.
     */

    public boolean hasCurrentPlayerDrawn(Player player) {
        return turnManager.hasDrawnThisTurn();
    }

    /**
     * Azione logica chiamata quando il giocatore clicca "Pesca".
     * Contiene le regole "non puoi pescare se hai mosse" e "pesca solo 1".
     */
    public void playerInitiatesDraw() {
        Player player = getCurrentPlayer();

        if (currentState != GameState.RUNNING) {
            throw new IllegalStateException("Non puoi pescare ora.");
        }

        // 1. Regola: "Massimo una carta"
        if (hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("Hai già pescato in questo turno. Devi giocare la carta o passare.");
        }

        // 2. Regola: "Non se hai carte da giocare"
        if (playerHasPlayableCard(player)) {
            throw new IllegalStateException("Mossa non valida! Hai una carta giocabile, non puoi pescare.");
        }

        // Ok, il giocatore deve pescare
        System.out.println(player.getName() + " non ha mosse, pesca una carta.");
        turnManager.setHasDrawnThisTurn(true); // Imposta il flag!
        
        drawCardForPlayer(player); // Pesca la carta

        // Notifica la View per mostrare la nuova carta
        notifyObservers();
    }

    /**
     * Il giocatore sceglie di passare il turno.
     * Valido solo se il giocatore è obbligato a farlo (ha pescato e non può/vuole giocare).
     */
    public void playerPassTurn() {
        if (currentState != GameState.RUNNING) {
            throw new IllegalStateException("Non puoi passare ora.");
        }

        // Puoi passare solo se hai pescato (perché non avevi mosse)
        if (!hasCurrentPlayerDrawn(getCurrentPlayer())) {
            // Potresti avere una mossa, quindi non puoi passare
            if (playerHasPlayableCard(getCurrentPlayer())) {
                throw new IllegalStateException("Non puoi passare, hai una mossa valida.");
            } else {
                throw new IllegalStateException("Non puoi passare, devi prima pescare una carta.");
            }
        }

        // Se sei qui, hai pescato e hai scelto di passare
        System.out.println(getCurrentPlayer().getName() + " passa il turno.");
        turnManager.advanceTurn(); // Avanza al prossimo giocatore
        notifyObservers();
    }
    
    /**
     * Metodo fisico di pesca (precedentemente playerDrawCard).
     * Rimescola il mazzo se necessario.
     * NON notifica gli observer.
     */
    public void drawCardForPlayer(Player player) {
        if (drawDeck.isEmpty()) {
            System.out.println("Mazzo di pesca vuoto. Rimescolo gli scarti...");
            List<Card> cardsToReshuffle = discardPile.takeAllExceptTop();
            
            if (cardsToReshuffle.isEmpty()) {
                System.out.println("Nessuna carta da rimescolare. Impossibile pescare.");
                return; 
            }
            for (Card card : cardsToReshuffle) {
                drawDeck.addCard(card);
            }
            drawDeck.shuffle();
            System.out.println("Mazzo rimescolato con " + cardsToReshuffle.size() + " carte.");
        }

        player.addCardToHand(drawDeck.drawCard());
        // Rimosso notifyObservers() - sarà gestito dal metodo chiamante
    }

    /**
     * Metodo chiamato dal Controller quando il giocatore
     * preme il bottone "UNO!".
     * Imposta lo stato del giocatore come "sicuro".
     */
    public void callUno(Player player) {
        // Un giocatore può validamente chiamare UNO solo se ha 1 carta.
        if (player.getHandSize() == 1) {
            player.hasCalledUno();
        } else {

            drawCardForPlayer(player);
            drawCardForPlayer(player);

            notifyObservers();

            throw new IllegalStateException("Non puoi chiamare UNO ora! Hai " + player.getHandSize() + " carte. Penalità applicata: hai pescato 2 carte.");
        }
    }

    // --- METODI GETTER ---

    /**
     * Ora chiede al TurnManager chi è il giocatore corrente.
     */
    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }
    
    public Card getTopDiscardCard() {
        try {
            return discardPile.getTopCard();
        } catch (NoSuchElementException e) {
            return null; // Pila scarti vuota
        }
    }

    public boolean isDiscardPileEmpty() {
        return discardPile.isEmpty();
    }

    public GameState getGameState() {
        return this.currentState;
    }
    
    public CardColor getCurrentColor() {
        return this.currentColor;
    }

    public Deck<Card> getDrawDeck() {
        return this.drawDeck;
    }

    public DiscardPile getDiscardPile() {
        return this.discardPile;
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * Restituisce il giocatore che ha vinto la partita.
     * @return Il giocatore vincitore, o null se la partita è ancora in corso.
     */
    public Player getWinner() {
        return this.winner;
    }

    // --- METODI SETTER ---

    public void setCurrentColor(CardColor color) {
        this.currentColor = color;
    }
    
    // --- METODI PER GLI EFFETTI DELLE CARTE (Delegano al TurnManager) ---
    
    public void skipNextPlayer() {
        this.turnManager.skipNextPlayer();
        System.out.println("Giocatore saltato!");
    }
    
    public void makeNextPlayerDraw(int amount) {
        Player nextPlayer = this.turnManager.peekNextPlayer();
        System.out.println(nextPlayer.getName() + " pesca " + amount);
        for(int i = 0; i < amount; i++) {
            drawCardForPlayer(nextPlayer); // <-- Usa il metodo rinominato
        }
        notifyObservers(); // <-- Aggiunto notify perché drawCardForPlayer non lo fa più
    }
    
    public void reversePlayOrder() {
        this.turnManager.reverseDirection();
        System.out.println("Ordine invertito!");
    }

    public boolean isClockwise() {
        return this.turnManager.getIsClockwise();
    }

    // --- METODI PER I JOLLY ---

    public void requestColorChoice() {
        this.currentState = GameState.WAITING_FOR_COLOR;
        System.out.println("In attesa della scelta del colore...");
        notifyObservers();
    }

    public void setColor(CardColor color) {
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }
        this.currentColor = color;
        this.currentState = GameState.RUNNING; 
        
        // Ora che il colore è stato scelto, passiamo il turno.
        this.turnManager.advanceTurn();

        notifyObservers();
    }
}