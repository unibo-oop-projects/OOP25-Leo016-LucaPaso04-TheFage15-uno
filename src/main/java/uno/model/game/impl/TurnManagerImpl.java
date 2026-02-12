package uno.model.game.impl;

import uno.model.game.api.Game;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AbstractPlayer;
import uno.model.game.api.GameRules;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Concrete implementation of the Turn Manager logic.
 */
public class TurnManagerImpl implements TurnManager {

    private static final Random RANDOM = new Random();

    private final List<AbstractPlayer> players;
    private final GameRules rules;
    private int currentPlayerIndex;
    private boolean isClockwise;
    private boolean hasDrawnThisTurn;
    private int skipSize;

    /**
     * Initializes the turn manager with default rules.
     * 
     * @param players The list of participants.
     */
    public TurnManagerImpl(final List<AbstractPlayer> players) {
        this(players, GameRulesImpl.defaultRules());
    }

    /**
     * Initializes the turn manager with custom rules.
     * 
     * @param players The list of participants.
     * @param rules   The game rules.
     */
    public TurnManagerImpl(final List<AbstractPlayer> players, final GameRules rules) {
        this.players = new ArrayList<>(players);
        this.rules = rules;
        this.currentPlayerIndex = RANDOM.nextInt(players.size());
        this.isClockwise = true;
        this.hasDrawnThisTurn = false;
        this.skipSize = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPlayer getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void advanceTurn(final Game game) {
        final int n = players.size();

        final int totalSteps = this.skipSize + 1;

        this.skipSize = 0;
        this.hasDrawnThisTurn = false;

        final int direction = isClockwise ? 1 : -1;

        final int nextIndex = currentPlayerIndex + (totalSteps * direction);

        currentPlayerIndex = (nextIndex % n + n) % n;

        checkAndApplyStartTurnPenalty(game);
    }

    /**
     * Checks and applies any penalties at the start of the turn.
     * 
     * @param game The current game instance.
     */
    private void checkAndApplyStartTurnPenalty(final Game game) {
        if (!rules.isUnoPenaltyEnabled()) {
            return;
        }

        final AbstractPlayer player = getCurrentPlayer();

        if (player.getHandSize() == 1 && !player.isHasCalledUno()) {
            player.unoPenalty(game);
        }

        player.resetUnoStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractPlayer peekNextPlayer() {
        final int totalSteps = this.skipSize + 1;
        final int n = players.size();
        final int direction = isClockwise ? 1 : -1;
        final int nextIndex = currentPlayerIndex + (totalSteps * direction);
        final int nextPlayerIndex = (nextIndex % n + n) % n;

        return players.get(nextPlayerIndex);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reverseDirection() {
        if (players.size() == 2) {
            skipPlayers(1);
        }
        isClockwise = !isClockwise;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void skipPlayers(final int n) {
        this.skipSize = n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDrawnThisTurn() {
        return this.hasDrawnThisTurn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHasDrawnThisTurn(final boolean value) {
        this.hasDrawnThisTurn = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClockwise() {
        return isClockwise;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        this.isClockwise = true;
        this.hasDrawnThisTurn = false;
        this.skipSize = 0;
        this.currentPlayerIndex = RANDOM.nextInt(players.size());
    }
}
