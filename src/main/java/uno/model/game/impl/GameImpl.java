package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameState;
import uno.model.players.api.Player;
import uno.model.utils.api.GameLogger;
import uno.view.api.GameModelObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Optional;

/**
 * Implementation of the UNO Game Model.
 * It maintains the game state, orchestrates turn flow, and manages rules execution.
 */
public class GameImpl implements Game {

    private final List<GameModelObserver> observers = new ArrayList<>();
    private final Deck<Card> drawDeck;
    private final DiscardPileImpl discardPile;
    private final List<Player> players;
    private Player winner;

    private final TurnManagerImpl turnManager;
    private GameState currentState;
    private Optional<CardColor> currentColor;
    private Card currentPlayedCard;

    private final GameLogger logger;

    private boolean isDarkSide;

    /**
     * Constructor for GameImpl.
     * @param deck deck of cards
     * @param players list of players
     * @param gameMode game mode
     * @param logger logger
     */
    public GameImpl(final Deck<Card> deck, final List<Player> players, final String gameMode, final GameLogger logger) {
        this.drawDeck = deck;
        this.players = players;
        this.logger = logger;
        this.winner = null;
        this.discardPile = new DiscardPileImpl();

        this.turnManager = new TurnManagerImpl(players);

        this.currentState = GameState.RUNNING;
        this.currentColor = Optional.empty(); 
        this.currentPlayedCard = null;

        logger.logAction("SYSTEM", "GAME_START", gameMode, "Players: " + players.size());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addObserver(final GameModelObserver observer) {
        this.observers.add(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifyObservers() {
        for (final GameModelObserver obs : observers) {
            obs.onGameUpdate();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playCard(final Optional<Card> card) {
        final Player player = getCurrentPlayer();

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
        if (!isValidMove(card.get())) {
            throw new IllegalStateException("Mossa non valida! La carta " + card + " non può essere giocata.");
        }

        this.currentPlayedCard = card.get();
        logger.logAction(player.getName(), "PLAY", 
                    card.getClass().getSimpleName(), 
                    card.get().getValue(this).toString());

        // --- FINE LOGICA DI VALIDAZIONE ---

        // Se la mossa è valida, aggiorna il currentColor.
        if (card.get().getColor(this) == CardColor.WILD) {
            this.currentColor = Optional.empty(); // Sarà impostato da onColorChosen()
        } else {
            // Se è una carta colorata, quello è il nuovo colore attivo.
            this.currentColor = Optional.of(card.get().getColor(this));
        }

        // Esegui effetto carta (polimorfismo)
        System.out.println("Svolgo l'effetto: " + card.get().getValue(this));
        System.out.println("La carta giocata è di classe: " + card.get().getClass().getSimpleName());
        if (card.get().getValue(this) == CardValue.WILD_FORCED_SWAP) {
            // Sposta la carta
            player.playCard(card);
            discardPile.addCard(card.get());

            card.get().performEffect(this);
        } else {
            card.get().performEffect(this);

            // Sposta la carta
            player.playCard(card);
            discardPile.addCard(card.get());
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
     * Check if the played card is a valid move.
     * @param cardToPlay The card the player wants to play.
     * @return true if the move is valid, false otherwise.
     */
    private boolean isValidMove(final Card cardToPlay) {
        final Optional<Card> topCard = getTopDiscardCard();
        return topCard.isPresent() && cardToPlay.canBePlayedOn(topCard.get(), this);
    }

    /**
     * Verify if the player has at least one playable card.
     * @param player The player to check.
     * @return true if the player has a playable card, false otherwise.
     */
    private boolean playerHasPlayableCard(final Player player) {
        for (final Optional<Card> card : player.getHand()) {
            if (card.isPresent() && isValidMove(card.get())) {
                return true; // Trovata una carta giocabile
            }
        }
        return false; // Nessuna carta giocabile
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasCurrentPlayerDrawn(final Player player) {
        return turnManager.hasDrawnThisTurn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerInitiatesDraw() {
        final Player player = getCurrentPlayer();

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
     * {@inheritDoc}
     */
    @Override
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

        final Player currentPlayer = getCurrentPlayer();
        final String handSize = String.valueOf(currentPlayer.getHand().size()); // Dimensione della mano dopo la pescata

        logger.logAction(currentPlayer.getName(), "PASS_TURN", "N/A", "HandSize: " + handSize);

        // Se sei qui, hai pescato e hai scelto di passare
        System.out.println(getCurrentPlayer().getName() + " passa il turno.");
        turnManager.advanceTurn(this); // Avanza al prossimo giocatore
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawCardForPlayer(final Player player) {
        if (drawDeck.isEmpty()) {
            System.out.println("Mazzo di pesca vuoto. Rimescolo gli scarti...");
            final List<Card> cardsToReshuffle = discardPile.takeAllExceptTop();

            if (cardsToReshuffle.isEmpty()) {
                System.out.println("Nessuna carta da rimescolare. Impossibile pescare.");
                return; 
            }
            for (final Card card : cardsToReshuffle) {
                drawDeck.addCard(card);
            }
            drawDeck.shuffle();
            System.out.println("Mazzo rimescolato con " + cardsToReshuffle.size() + " carte.");
        }

        final Optional<Card> drawnCard = drawDeck.draw();
        if (drawnCard.isPresent()) {
            player.addCardToHand(drawnCard.get());
        }

        logger.logAction(player.getName(), "DRAW", 
                    drawnCard.isPresent() ? drawnCard.get().getClass().getSimpleName() : "NONE", 
                    drawnCard.isPresent() ? drawnCard.get().getValue(this).toString() : "NONE");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callUno(final Player player) {
        if (player.getHandSize() == 1) {
            player.hasCalledUno();
            logger.logAction(player.getName(), "CALL_UNO_SUCCESS", "N/A", "HandSize: 1");
        } else {

            logger.logAction(player.getName(), "CALL_UNO_FAILED", 
            "N/A", "Initial HandSize: " + player.getHandSize() + ". Penalty: Draw 2.");

            drawCardForPlayer(player);
            drawCardForPlayer(player);

            notifyObservers();

            throw new IllegalStateException("Non puoi chiamare UNO ora! Hai " 
            + player.getHandSize() + " carte. Penalità applicata: hai pescato 2 carte.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getCurrentPlayer() {
        return turnManager.getCurrentPlayer();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Card> getTopDiscardCard() {
        try {
            return discardPile.getTopCard();
        } catch (final NoSuchElementException e) {
            return Optional.empty(); // Pila scarti vuota
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiscardPileEmpty() {
        return discardPile.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameState getGameState() {
        return this.currentState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<CardColor> getCurrentColor() {
        return this.currentColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Deck<Card> getDrawDeck() {
        return this.drawDeck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscardPileImpl getDiscardPile() {
        return this.discardPile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Player> getPlayers() {
        return this.players;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TurnManagerImpl getTurnManager() {
        return this.turnManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Player getWinner() {
        return this.winner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentColor(final CardColor color) {
        this.currentColor = Optional.of(color);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipPlayers(final int n) {
        this.turnManager.skipPlayers(n);
        System.out.println("Giocatore saltato!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeNextPlayerDraw(final int amount) {
        final Player nextPlayer = this.turnManager.peekNextPlayer();
        System.out.println(nextPlayer.getName() + " pesca " + amount);
        for (int i = 0; i < amount; i++) {
            drawCardForPlayer(nextPlayer);
        }
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reversePlayOrder() {
        this.turnManager.reverseDirection();
        System.out.println("Ordine invertito!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClockwise() {
        return this.turnManager.isClockwise();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void flipTheWorld() {
        this.isDarkSide = !this.isDarkSide; 

        System.out.println("FLIP! Ora il gioco è sul lato: " + (isDarkSide ? "SCURO" : "CHIARO"));

        this.currentColor = Optional.of(this.currentPlayedCard.getColor(this)); 

        if (this.currentColor.get() == CardColor.WILD) {
            final CardColor[] coloredValues = {CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW};
            final Random random = new java.util.Random();
            final CardColor chosenColor = coloredValues[random.nextInt(coloredValues.length)];

            this.currentColor = Optional.of(chosenColor);
            System.out.println("Il lato rivelato è WILD! Colore scelto casualmente: " + chosenColor);
        }

        System.out.println("Nuovo colore attivo dopo il FLIP: " + this.currentColor);

        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDarkSide() {
        return this.isDarkSide;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestColorChoice() {
        this.currentState = GameState.WAITING_FOR_COLOR;
        System.out.println("In attesa della scelta del colore...");
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestPlayerChoice() {
        this.currentState = GameState.WAITING_FOR_PLAYER;
        System.out.println("In attesa della scelta del giocatore...");
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setColor(final CardColor color) {
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }

        // Deve prendere il valore della carta giocata (NON quella nel mazzo degli scarti)
        final Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "SET_COLOR", "N/A", color.toString());

        if (playedCard.getValue(this) == CardValue.WILD_DRAW_COLOR) {
            drawUntilColorChosenCard(color);
            return;
        }

        this.currentColor = Optional.of(color);
        this.currentState = GameState.RUNNING; 

        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void chosenPlayer(final Player player) {
        if (this.currentState != GameState.WAITING_FOR_PLAYER) {
            return;
        }

        final Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "CHOOSEN_PLAYER", "N/A", player.getName());

        if (playedCard.getValue(this) == CardValue.WILD_FORCED_SWAP) {
            System.out.println("Scambio forzato con: " + player.getName());

            final Player currentPlayer = getCurrentPlayer();

            // Scambia le mani
            final List<Optional<Card>> tempHand = new ArrayList<>(currentPlayer.getHand());
            currentPlayer.setHand(player.getHand());
            player.setHand(tempHand);
        }

        if (playedCard.getValue(this) == CardValue.WILD_TARGETED_DRAW_TWO) {
            System.out.println(player.getName() + " deve pescare 2 carte.");
            drawCardForPlayer(player);
            drawCardForPlayer(player);
        }

        this.currentState = GameState.RUNNING;

        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawUntilColorChosenCard(final CardColor color) {
        final Player nextPlayer = this.turnManager.peekNextPlayer();
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }

        System.out.println(nextPlayer.getName() + " deve pescare fino a trovare una carta del colore scelto: " + color);

        while (true) {
            final Optional<Card> drawnCard = drawDeck.draw();
            if (drawnCard.isPresent()) {
                nextPlayer.addCardToHand(drawnCard.get());
                System.out.println(nextPlayer.getName() + " ha pescato: " + drawnCard.get());

                if (drawnCard.get().getColor(this) == color) {
                    System.out.println(nextPlayer.getName() + " ha trovato una carta del colore scelto: " + drawnCard);
                    break;
                }
            }
        }

        this.currentColor = Optional.of(color);

        this.currentState = GameState.RUNNING;
        this.turnManager.advanceTurn(this);
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void aiAdvanceTurn() {
        this.turnManager.advanceTurn(this);
        notifyObservers(); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logSystemAction(final String actionType, final String cardDetails, final String extraInfo) {
        this.logger.logAction("SYSTEM", actionType, cardDetails, extraInfo);
    }
}
