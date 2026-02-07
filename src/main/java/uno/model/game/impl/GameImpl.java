package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.players.api.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.api.GameModelObserver;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.TurnManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of the UNO Game Model.
 * It maintains the game state, orchestrates turn flow, and manages rules
 * execution.
 */
public class GameImpl implements Game {

    private static final String CARD_DETAIL = "N/A";
    private static final String SUPPRESS_EI_EXPOSE_REP = "EI_EXPOSE_REP";
    private static final String LOGGER_PLAYER_NAME = "SYSTEM";
    private static final Random RANDOM = new Random();

    private final List<GameModelObserver> observers = new ArrayList<>();
    private final Deck<Card> drawDeck;
    private final DiscardPile discardPile;
    private final List<AbstractPlayer> players;
    private AbstractPlayer winner;

    private final TurnManager turnManager;
    private GameState currentState;
    private Optional<CardColor> currentColor;
    private Card currentPlayedCard;

    private final GameLogger logger;
    private final GameRules rules;

    private boolean isDarkSide;

    /**
     * Constructor for GameImpl with custom rules.
     * 
     * @param deck        deck of cards
     * @param players     list of players
     * @param turnManager turn manager
     * @param discardPile discard pile
     * @param gameMode    game mode
     * @param logger      logger
     * @param rules       game rules
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GameImpl(final Deck<Card> deck, final List<AbstractPlayer> players, final TurnManager turnManager,
            final DiscardPile discardPile, final String gameMode,
            final GameLogger logger, final GameRules rules) {
        this.drawDeck = deck;
        this.players = new ArrayList<>(players);
        this.logger = logger;
        this.rules = rules;
        this.winner = null;
        this.discardPile = discardPile;
        this.turnManager = turnManager;

        this.currentState = GameState.RUNNING;
        this.currentColor = Optional.empty();
        this.currentPlayedCard = null;

        logger.logAction(LOGGER_PLAYER_NAME, "GAME_START", gameMode,
                "Players: " + players.size() + ". Rules: " + rules);
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
        // --- INIZIO LOGICA DI VALIDAZIONE ---

        // 1. Controlla lo stato del gioco
        if (this.currentState != GameState.RUNNING) {
            throw new IllegalStateException("Non è possibile giocare una carta ora (Stato: " + this.currentState + ")");
        }

        final AbstractPlayer player = getCurrentPlayer();

        // New Rule: Skip After Draw
        // If enabled, and the player has drawn this turn, they cannot play immediately.
        // The rule description says: "If ENABLED, a player cannot play a card
        // immediately after drawing it."
        if (rules.isSkipAfterDrawEnabled() && hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("Regola: Skip After Draw. Hai pescato, quindi devi passare il turno.");
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
            logger.logAction(LOGGER_PLAYER_NAME, "GAME_OVER", CARD_DETAIL, "Winner: " + this.winner.getName());
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
     * 
     * @param cardToPlay The card the player wants to play.
     * @return true if the move is valid, false otherwise.
     */
    private boolean isValidMove(final Card cardToPlay) {
        final Optional<Card> topCard = getTopDiscardCard();
        return topCard.isPresent() && cardToPlay.canBePlayedOn(topCard.get(), this);
    }

    /**
     * Verify if the player has at least one playable card.
     * 
     * @param player The player to check.
     * @return true if the player has a playable card, false otherwise.
     */
    private boolean playerHasPlayableCard(final AbstractPlayer player) {
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
    public boolean hasCurrentPlayerDrawn(final AbstractPlayer player) {
        return turnManager.hasDrawnThisTurn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerInitiatesDraw() {

        if (currentState != GameState.RUNNING) {
            throw new IllegalStateException("Non puoi pescare ora.");
        }

        final AbstractPlayer player = getCurrentPlayer();
        // 1. Regola: "Massimo una carta"
        if (hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("Hai già pescato in questo turno. Devi giocare la carta o passare.");
        }

        // 2. Regola: "Non se hai carte da giocare"
        if (playerHasPlayableCard(player)) {
            throw new IllegalStateException("Mossa non valida! Hai una carta giocabile, non puoi pescare.");
        }

        // Ok, il giocatore deve pescare
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
        // Oppure se hai pescato e la regola "Skip After Draw" è attiva.
        if (!hasCurrentPlayerDrawn(getCurrentPlayer())) {
            // Potresti avere una mossa, quindi non puoi passare
            if (playerHasPlayableCard(getCurrentPlayer())) {
                throw new IllegalStateException("Non puoi passare, hai una mossa valida.");
            } else {
                throw new IllegalStateException("Non puoi passare, devi prima pescare una carta.");
            }
        }

        final AbstractPlayer currentPlayer = getCurrentPlayer();
        final String handSize = String.valueOf(currentPlayer.getHand().size()); // Dimensione della mano dopo la pescata

        logger.logAction(currentPlayer.getName(), "PASS_TURN", CARD_DETAIL, "HandSize: " + handSize);

        // Se sei qui, hai pescato e hai scelto di passare
        turnManager.advanceTurn(this); // Avanza al prossimo giocatore
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawCardForPlayer(final AbstractPlayer player) {
        if (drawDeck.isEmpty()) {

            // Regola: Mandatory Pass / No Reshuffle
            if (rules.isMandatoryPassEnabled()) {
                logger.logAction(LOGGER_PLAYER_NAME, "DECK_EMPTY", CARD_DETAIL, "No Reshuffle Rule Active. Game Ends.");
                this.currentState = GameState.GAME_OVER;
                notifyObservers();
                return;
            }

            final List<Card> cardsToReshuffle = discardPile.takeAllExceptTop();

            if (cardsToReshuffle.isEmpty()) {
                return;
            }
            for (final Card card : cardsToReshuffle) {
                drawDeck.addCard(card);
            }
            drawDeck.shuffle();
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
    public void callUno(final AbstractPlayer player) {

        // Rule: UNO Penalty
        // If disabled, players don't need to call UNO.
        if (!rules.isUnoPenaltyEnabled()) {
            // Se la penalità è disabilitata, non facciamo nulla se il giocatore tenta di
            // chiamare UNO.
            // Ma se lo chiama quando ha 1 carta, va bene e lo logghiamo come successo.
            // Se sbaglia, tecnicamente non dovrebbe succedere nulla se la regola è "No
            // Penalty".
            // Tuttavia, permettiamo comunque di chiamarlo correttamente.
            if (player.getHandSize() == 1) {
                player.hasCalledUno();
                logger.logAction(player.getName(), "CALL_UNO_SUCCESS", CARD_DETAIL, "HandSize: 1");
            }
            return;
        }

        if (player.getHandSize() == 1) {
            player.hasCalledUno();
            logger.logAction(player.getName(), "CALL_UNO_SUCCESS", CARD_DETAIL, "HandSize: 1");
        } else {
            logger.logAction(player.getName(), "CALL_UNO_FAILED",
                    CARD_DETAIL, "Initial HandSize: " + player.getHandSize() + ". Penalty: Draw 2.");

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
    public AbstractPlayer getCurrentPlayer() {
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
    @SuppressFBWarnings(SUPPRESS_EI_EXPOSE_REP)
    public Deck<Card> getDrawDeck() {
        return this.drawDeck;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(SUPPRESS_EI_EXPOSE_REP)
    public DiscardPile getDiscardPile() {
        return this.discardPile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(SUPPRESS_EI_EXPOSE_REP)
    public List<AbstractPlayer> getPlayers() {
        return this.players;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(SUPPRESS_EI_EXPOSE_REP)
    public TurnManager getTurnManager() {
        return this.turnManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings(SUPPRESS_EI_EXPOSE_REP)
    public AbstractPlayer getWinner() {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeNextPlayerDraw(final int amount) {
        final AbstractPlayer nextPlayer = this.turnManager.peekNextPlayer();
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

        this.currentColor = Optional.of(this.currentPlayedCard.getColor(this));

        if (this.currentColor.get() == CardColor.WILD) {
            final CardColor[] coloredValues = { CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW };
            final CardColor chosenColor = coloredValues[RANDOM.nextInt(coloredValues.length)];

            this.currentColor = Optional.of(chosenColor);
        }

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
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestPlayerChoice() {
        this.currentState = GameState.WAITING_FOR_PLAYER;
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

        // Deve prendere il valore della carta giocata (NON quella nel mazzo degli
        // scarti)
        final Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "SET_COLOR", CARD_DETAIL, color.toString());

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
    public void chosenPlayer(final AbstractPlayer player) {
        if (this.currentState != GameState.WAITING_FOR_PLAYER) {
            return;
        }

        final Card playedCard = this.currentPlayedCard;

        logger.logAction(getCurrentPlayer().getName(), "CHOOSEN_PLAYER", CARD_DETAIL, player.getName());

        if (playedCard.getValue(this) == CardValue.WILD_FORCED_SWAP) {

            final AbstractPlayer currentPlayer = getCurrentPlayer();

            // Scambia le mani
            final List<Optional<Card>> tempHand = new ArrayList<>(currentPlayer.getHand());
            currentPlayer.setHand(player.getHand());
            player.setHand(tempHand);
        }

        if (playedCard.getValue(this) == CardValue.WILD_TARGETED_DRAW_TWO) {
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
        final AbstractPlayer nextPlayer = this.turnManager.peekNextPlayer();
        if (this.currentState != GameState.WAITING_FOR_COLOR) {
            return;
        }

        while (true) {
            final Optional<Card> drawnCard = drawDeck.draw();
            if (drawnCard.isPresent()) {
                nextPlayer.addCardToHand(drawnCard.get());

                if (drawnCard.get().getColor(this) == color) {
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
        this.logger.logAction(LOGGER_PLAYER_NAME, actionType, cardDetails, extraInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameRules getRules() {
        return this.rules;
    }
}
