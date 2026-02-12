package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DeckHandler;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameRules;
import uno.model.game.api.GameState;
import uno.model.game.api.GameStateBehavior;
import uno.model.game.api.MoveValidator;
import uno.model.game.api.TurnManager;
import uno.model.game.impl.states.GameOverState;
import uno.model.game.impl.states.RunningState;
import uno.model.game.impl.states.WaitingForColorState;
import uno.model.game.impl.states.WaitingForPlayerState;
import uno.model.players.impl.AbstractPlayer;
import uno.model.utils.api.GameLogger;
import uno.model.api.GameModelObserver;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Random;

/**
 * Implementation of the UNO Game Model.
 * It maintains the game state, orchestrates turn flow, and manages rules
 * execution.
 */
public class GameImpl implements GameContext {

    private static final String CARD_DETAIL = "N/A";
    private static final String SUPPRESS_EI_EXPOSE_REP = "EI_EXPOSE_REP";
    private static final String LOGGER_PLAYER_NAME = "SYSTEM";
    private static final Random RANDOM = new Random();
    private static final int START_HAND_SIZE = 7;

    private final List<GameModelObserver> observers = new ArrayList<>();
    private final List<AbstractPlayer> players;
    private AbstractPlayer winner;

    private final TurnManager turnManager;
    private final DeckHandler deckHandler;
    private final MoveValidator moveValidator;

    private GameStateBehavior currentState;
    private Optional<CardColor> currentColor;
    private Card currentPlayedCard;

    private final GameLogger logger;
    private final GameRules rules;

    private boolean isDarkSide;

    /**
     * Constructor for GameImpl with custom rules.
     * 
     * @param deck        deck of cards.
     * @param players     list of players.
     * @param turnManager turn manager.
     * @param discardPile discard pile.
     * @param gameMode    game mode.
     * @param logger      logger.
     * @param rules       game rules.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GameImpl(final Deck<Card> deck, final List<AbstractPlayer> players, final TurnManager turnManager,
            final DiscardPile discardPile, final String gameMode,
            final GameLogger logger, final GameRules rules) {
        this.players = new ArrayList<>(players);
        this.logger = logger;
        this.rules = rules;
        this.winner = null;
        this.turnManager = turnManager;
        this.deckHandler = new DeckHandlerImpl(deck, discardPile, rules, logger, LOGGER_PLAYER_NAME);
        this.moveValidator = new MoveValidatorImpl(this);
        this.currentState = new RunningState(this);
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
        currentState.playCard(card);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidMove(final Card cardToPlay) {
        return moveValidator.isValidMove(cardToPlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean playerHasPlayableCard(final AbstractPlayer player) {
        return moveValidator.playerHasPlayableCard(player);
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
        currentState.playerInitiatesDraw();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerPassTurn() {
        currentState.playerPassTurn();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawCardForPlayer(final AbstractPlayer player) {
        final boolean success = deckHandler.drawCardForPlayer(player, this);
        if (!success) {
            this.currentState = new GameOverState(this);
            notifyObservers();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void callUno(final AbstractPlayer player) {
        if (!rules.isUnoPenaltyEnabled()) {
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

            throw new IllegalStateException("You can't call UNO now! You have "
                    + player.getHandSize() + " cards. Penalty applied: you drew 2 cards.");
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
            return deckHandler.getDiscardPile().getTopCard();
        } catch (final NoSuchElementException e) {
            return Optional.empty();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDiscardPileEmpty() {
        return deckHandler.getDiscardPile().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameState getGameState() {
        return this.currentState.getEnum();
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
        return deckHandler.getDrawDeck();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DiscardPile getDiscardPile() {
        return deckHandler.getDiscardPile();
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
    public void setColor(final CardColor color) {
        currentState.setColor(color);
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
            final CardColor[] coloredValues;
            if (this.isDarkSide) {
                coloredValues = new CardColor[]{CardColor.PINK, CardColor.TEAL, CardColor.PURPLE, CardColor.ORANGE};
            } else {
                coloredValues = new CardColor[]{CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW};
            }
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
        this.currentState = new WaitingForColorState(this);
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestPlayerChoice() {
        this.currentState = new WaitingForPlayerState(this);
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void chosenPlayer(final AbstractPlayer player) {
        currentState.chosenPlayer(player);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void drawUntilColorChosenCard(final CardColor color) {
        currentState.drawUntilColorChosenCard(color);
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

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    @Override
    public void setGameState(final GameStateBehavior newState) {
        this.currentState = newState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameLogger getLogger() {
        return this.logger;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    @Override
    public void setWinner(final AbstractPlayer winner) {
        this.winner = winner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startNewRound() {
        logger.logAction(LOGGER_PLAYER_NAME, "ROUND_START", "N/A", "Starting new round...");

        final List<Card> cardsToRecycle = new ArrayList<>();

        for (final AbstractPlayer player : players) {
            for (final Optional<Card> cardOpt : player.getHand()) {
                cardOpt.ifPresent(cardsToRecycle::add);
            }
            player.setHand(new ArrayList<>());
        }

        cardsToRecycle.addAll(deckHandler.getDiscardPile().takeAll());

        deckHandler.getDrawDeck().refill(cardsToRecycle);
        deckHandler.getDrawDeck().shuffle();

        turnManager.reset();

        for (final AbstractPlayer player : players) {
            for (int i = 0; i < START_HAND_SIZE; i++) {
                drawCardForPlayer(player);
            }
        }

        final Optional<Card> firstCardOpt = deckHandler.getDrawDeck().draw();
        if (firstCardOpt.isPresent()) {
            final Card firstCard = firstCardOpt.get();
            deckHandler.getDiscardPile().addCard(firstCard);
            this.currentPlayedCard = firstCard;

            if (firstCard.getColor(this) == CardColor.WILD) {
                final CardColor[] coloredValues = {CardColor.RED, CardColor.BLUE, CardColor.GREEN, CardColor.YELLOW };
                final CardColor chosenColor = coloredValues[RANDOM.nextInt(coloredValues.length)];
                this.currentColor = Optional.of(chosenColor);
            } else {
                this.currentColor = Optional.of(firstCard.getColor(this));
            }

            logger.logAction(LOGGER_PLAYER_NAME, "FIRST_CARD", firstCard.getClass().getSimpleName(),
                    firstCard.toString());
        } else {
            throw new IllegalStateException("Deck empty after refill!");
        }

        this.currentState = new RunningState(this);
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentPlayedCard(final Card card) {
        this.currentPlayedCard = card;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Card getCurrentPlayedCard() {
        return this.currentPlayedCard;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentColorOptional(final Optional<CardColor> color) {
        this.currentColor = color;
    }
}
