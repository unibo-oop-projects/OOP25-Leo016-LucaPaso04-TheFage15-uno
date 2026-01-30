package uno.model.game;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.Deck;
import uno.model.players.Player;
import uno.model.utils.GameLogger;
import uno.view.GameModelObserver;

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
    private Card currentPlayedCard;

    private final GameLogger logger;

    private boolean isDarkSide = false; // <-- STATO FLIP

    /**
     * Costruisce una nuova partita.
     * @param deck Il mazzo da usare.
     * @param players La lista dei giocatori, già creata e in ordine.
     */
    public Game(Deck<Card> deck, List<Player> players, String gameMode) {
        this.drawDeck = deck;
        this.players = players;
        this.winner = null; // Inizializza il vincitore a null
        this.discardPile = new DiscardPile();
        
        // --- COLLEGAMENTO ---
        // Il TurnManager ora possiede la logica di chi sta giocando
        this.turnManager = new TurnManager(players); 

        this.logger = new GameLogger(String.valueOf(System.currentTimeMillis()));
        
        this.currentState = GameState.RUNNING;
        this.currentColor = null; // Nessun colore attivo all'inizio
        this.currentPlayedCard = null;

        logger.logAction("SYSTEM", "GAME_START", gameMode, "Players: " + players.size());
        
        // NOTA: La distribuzione delle carte ora è gestita da GameSetup
        // nel MenuController, non più qui.
    }
    
    // --- METODI OBSERVER ---
    
    public void addObserver(GameModelObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObservers() {
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

        this.currentPlayedCard = card;

        logger.logAction(player.getName(), "PLAY", 
                    card.getClass().getSimpleName(), 
                    card.getValue(this).toString());
        
        // --- FINE LOGICA DI VALIDAZIONE ---

        // Se la mossa è valida, aggiorna il currentColor.
        if (card.getColor(this) == CardColor.WILD) {
            this.currentColor = null; // Sarà impostato da onColorChosen()
        } else {
            // Se è una carta colorata, quello è il nuovo colore attivo.
            this.currentColor = card.getColor(this);
        }

        // Esegui effetto carta (polimorfismo)
        System.out.println("Svolgo l'effetto: " + card.getValue(this));
        System.out.println("La carta giocata è di classe: " + card.getClass().getSimpleName());

        if(card.getValue(this) == CardValue.WILD_FORCED_SWAP){
            // Sposta la carta
            player.playCard(card);
            discardPile.addCard(card);

            card.performEffect(this);
        } else {
            card.performEffect(this);
            
            // Sposta la carta
            player.playCard(card);
            discardPile.addCard(card);
        }

        // --- CONTROLLO VITTORIA ---
        // Controlla se il giocatore ha vinto DOPO aver giocato la carta
        if (player.hasWon()) {
            this.currentState = GameState.GAME_OVER;
            this.winner = player;
            logger.logAction("SYSTEM", "GAME_OVER", "N/A", "Winner: " + this.winner.getName());
            System.out.println("PARTITA FINITA! Il vincitore è " + this.winner.getName());
            notifyObservers(); // Notifica la View che la partita è finita
            return; // Non avanzare il turno, la partita è bloccata
        }
        // -------------------------
        
        // --- LOGICA DI AVANZAMENTO TURNO ---
        // Non passiamo il turno solo se stiamo aspettando una scelta di colore
        // (impostato da performEffect -> requestColorChoice).
        if (this.currentState != GameState.WAITING_FOR_COLOR && this.currentState != GameState.WAITING_FOR_PLAYER) {
            this.turnManager.advanceTurn(this);
        }
        
        notifyObservers();
    }

    /**
     * Controlla se la carta che si sta tentando di giocare è valida
     * in base allo stato attuale del gioco (carta in cima e colore attivo).
     */
    private boolean isValidMove(Card cardToPlay) {
        Card topCard = getTopDiscardCard();

        return cardToPlay.canBePlayedOn(topCard, this);
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

        Player currentPlayer = getCurrentPlayer();
        String handSize = String.valueOf(currentPlayer.getHand().size()); // Dimensione della mano dopo la pescata

        logger.logAction(currentPlayer.getName(), "PASS_TURN", "N/A", "HandSize: " + handSize);

        // Se sei qui, hai pescato e hai scelto di passare
        System.out.println(getCurrentPlayer().getName() + " passa il turno.");
        turnManager.advanceTurn(this); // Avanza al prossimo giocatore
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

        Card drawnCard = drawDeck.drawCard(); //
        player.addCardToHand(drawnCard);

        logger.logAction(player.getName(), "DRAW", 
                    drawnCard.getClass().getSimpleName(), 
                    drawnCard.getValue(this).toString());
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
            logger.logAction(player.getName(), "CALL_UNO_SUCCESS", "N/A", "HandSize: 1");
        } else {

            logger.logAction(player.getName(), "CALL_UNO_FAILED", "N/A", "Initial HandSize: " + player.getHandSize() + ". Penalty: Draw 2.");

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

    public TurnManager getTurnManager(){
        return this.turnManager;
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
    public void skipPlayers(int n) {
        this.turnManager.skipPlayers(n);
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

    /**
     * Chiamato dalla FlipCard.
     * Cambia lo stato del gioco e "traduce" tutte le carte.
     */
    public void flipTheWorld() {
        // 1. Inverti PRIMA lo stato del mondo
        // Questo è fondamentale affinché currentPlayedCard.getColor(this)
        // restituisca il colore del lato NUOVO (quello che sta per apparire).
        this.isDarkSide = !this.isDarkSide; 
        
        System.out.println("FLIP! Ora il gioco è sul lato: " + (isDarkSide ? "SCURO" : "CHIARO"));

        // 2. Aggiorna il colore attivo prendendolo dal lato appena rivelato della carta
        // Poiché abbiamo già fatto l'inversione al punto 1, getColor() leggerà il lato corretto.
        this.currentColor = this.currentPlayedCard.getColor(this); 

        // --- LOGICA DI SCELTA CASUALE PER WILD FLIPPATO ---
        // Se per caso il lato che appare è un JOLLY (raro, ma possibile), scegliamo un colore a caso
        // per non bloccare il gioco in attesa di un input che non può arrivare.
        if (this.currentColor == CardColor.WILD) {
            CardColor[] coloredValues = {CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW};
            java.util.Random random = new java.util.Random();
            CardColor chosenColor = coloredValues[random.nextInt(coloredValues.length)];

            this.currentColor = chosenColor;
            System.out.println("Il lato rivelato è WILD! Colore scelto casualmente: " + chosenColor);
        }
        
        System.out.println("Nuovo colore attivo dopo il FLIP: " + this.currentColor);
        
        // 3. Notifica la View per ridisegnare tutto (sfondi, carte, ecc.)
        notifyObservers();
    }

    /**
     * La View usa questo per sapere quale sfondo/dorso mostrare.
     */
    public boolean isDarkSide() {
        return this.isDarkSide;
    }

    // --- METODI PER I JOLLY ---

    public void requestColorChoice() {
        this.currentState = GameState.WAITING_FOR_COLOR;
        System.out.println("In attesa della scelta del colore...");
        notifyObservers();
    }

    public void requestPlayerChoice() {
        this.currentState = GameState.WAITING_FOR_PLAYER;
        System.out.println("In attesa della scelta del giocatore...");
        notifyObservers();
    }

    public void setColor(CardColor color) {
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }

        // Deve prendere il valore della carta giocata (NON quella nel mazzo degli scarti)
        Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "SET_COLOR", "N/A", color.toString());

        if (playedCard.getValue(this) == CardValue.WILD_DRAW_COLOR) {
            drawUntilColorChosenCard(color);
            return;
        }

        this.currentColor = color;
        this.currentState = GameState.RUNNING; 

        notifyObservers();
    }

    public void choosenPlayer(Player player) {
        if (this.currentState != GameState.WAITING_FOR_PLAYER) {
            return;
        }

        Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "CHOOSEN_PLAYER", "N/A", player.getName());

        if(playedCard.getValue(this) == CardValue.WILD_FORCED_SWAP){
            System.out.println("Scambio forzato con: " + player.getName());

            Player currentPlayer = getCurrentPlayer();

            // Scambia le mani
            List<Card> tempHand = new ArrayList<>(currentPlayer.getHand());
            currentPlayer.setHand(player.getHand());
            player.setHand(tempHand);
        }

        if(playedCard.getValue(this) == CardValue.WILD_TARGETED_DRAW_TWO){
            System.out.println(player.getName() + " deve pescare 2 carte.");
            drawCardForPlayer(player);
            drawCardForPlayer(player);
        }

        this.currentState = GameState.RUNNING;

        notifyObservers();
    }

    public void drawUntilColorChosenCard(CardColor color) {
        // Il prossimo giocatore  deve pescare fino a trovare il colore scelto
        Player nextPlayer = this.turnManager.peekNextPlayer();
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }

        System.out.println(nextPlayer.getName() + " deve pescare fino a trovare una carta del colore scelto: " + color);

        while (true) {
            Card drawnCard = drawDeck.drawCard();
            nextPlayer.addCardToHand(drawnCard);
            System.out.println(nextPlayer.getName() + " ha pescato: " + drawnCard);

            if (drawnCard.getColor(this) == color) {
                System.out.println(nextPlayer.getName() + " ha trovato una carta del colore scelto: " + drawnCard);
                break;
            }
        }

        this.currentColor = color;

        // Dopo aver trovato la carta, torna allo stato di gioco normale
        this.currentState = GameState.RUNNING;
        this.turnManager.advanceTurn(this);
        notifyObservers();
    }

        public void AIAdvanceTurn() {
        this.turnManager.advanceTurn(this);
        notifyObservers(); 
    }

    // Nel file Game.java
    // Assumendo che 'logger' sia il tuo campo GameLogger
    public void logSystemAction(String actionType, String cardDetails, String extraInfo) {
        this.logger.logAction("SYSTEM", actionType, cardDetails, extraInfo);
    }
}