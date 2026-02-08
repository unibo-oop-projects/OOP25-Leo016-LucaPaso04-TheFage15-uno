package uno.model.game.impl;

import uno.model.game.api.Game;
import uno.model.game.api.TurnManager;
import uno.model.players.api.AbstractPlayer;
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
    private boolean isClockwise; // true = Clockwise, false = Counter-Clockwise
    private boolean hasDrawnThisTurn;
    private int skipSize; // How many players to jump over (0 = normal turn)

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

        // Randomly choose the starting player
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

        // 1. Calculate total steps:
        // skipSize = 0 -> step 1 (Next player)
        // skipSize = 1 -> step 2 (Skip one player)
        final int totalSteps = this.skipSize + 1;

        // 2. Reset flags for the NEW turn
        this.skipSize = 0;
        this.hasDrawnThisTurn = false;

        // 3. Calculate direction and new index
        final int direction = isClockwise ? 1 : -1;

        // Raw index calculation (can be negative or greater than size)
        final int nextIndex = currentPlayerIndex + (totalSteps * direction);

        // 4. Wrap-around logic (Circular Buffer)
        // Formula: (a % n + n) % n handles negative results correctly in Java
        currentPlayerIndex = (nextIndex % n + n) % n;

        // 5. Apply rules for the start of the turn
        checkAndApplyStartTurnPenalty(game);
    }

    /**
     * Checks and applies any penalties at the start of the turn.
     * 
     * @param game The current game instance.
     */
    private void checkAndApplyStartTurnPenalty(final Game game) {
        // If UNO Penalty is disabled, skip this check completely.
        if (!rules.isUnoPenaltyEnabled()) {
            return;
        }

        final AbstractPlayer player = getCurrentPlayer();

        // If player starts turn with 1 card and didn't call UNO -> Penalty
        if (player.getHandSize() == 1 && !player.isHasCalledUno()) {
            // Apply penalty (Draw 2 usually)
            // Note: game.makeNextPlayerDraw usually affects the *next* player relative to
            // current,
            // but here we want to penalize 'player'.
            // Assuming player.unoPenalty() handles the drawing logic directly or calls game
            // methods.
            player.unoPenalty(game);

            // The penalty is applied. The UI should successfully observe this event via
            // NotificationCenter/Observer.
            // No exception needed for control flow.
        }

        // Reset status for the new turn
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
        // Pick a new random starting player
        this.currentPlayerIndex = RANDOM.nextInt(players.size());
    }
}
