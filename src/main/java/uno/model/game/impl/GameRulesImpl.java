package uno.model.game.impl;

import uno.model.game.api.GameRules;

/**
 * Immutable implementation of the GameRules interface.
 */
public class GameRulesImpl implements GameRules {

    private final boolean unoPenaltyEnabled;
    private final boolean skipAfterDrawEnabled;
    private final boolean mandatoryPassEnabled;
    private final boolean scoringModeEnabled;

    /**
     * Creates a new GameRulesImpl instance.
     *
     * @param unoPenaltyEnabled    If true, players must say UNO when they have 1
     *                             card.
     * @param skipAfterDrawEnabled If true, a player cannot play a card immediately
     *                             after drawing.
     * @param mandatoryPassEnabled If true, when draw deck is empty, game ends (no
     *                             reshuffle).
     * @param scoringModeEnabled   If true, game ends when a player reaches a score
     *                             limit.
     */
    public GameRulesImpl(final boolean unoPenaltyEnabled, final boolean skipAfterDrawEnabled,
            final boolean mandatoryPassEnabled, final boolean scoringModeEnabled) {
        this.unoPenaltyEnabled = unoPenaltyEnabled;
        this.skipAfterDrawEnabled = skipAfterDrawEnabled;
        this.mandatoryPassEnabled = mandatoryPassEnabled;
        this.scoringModeEnabled = scoringModeEnabled;
    }

    /**
     * Default rules: Penalty YES, Skip NO, Reshuffle YES (mandatory pass NO),
     * Scoring YES.
     * 
     * @return default GameRules implementation.
     */
    public static GameRules defaultRules() {
        return new GameRulesImpl(true, false, false, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUnoPenaltyEnabled() {
        return unoPenaltyEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSkipAfterDrawEnabled() {
        return skipAfterDrawEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isMandatoryPassEnabled() {
        return mandatoryPassEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isScoringModeEnabled() {
        return scoringModeEnabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GameRulesImpl{"
                + "unoPenaltyEnabled=" + unoPenaltyEnabled
                + ", skipAfterDrawEnabled=" + skipAfterDrawEnabled
                + ", mandatoryPassEnabled=" + mandatoryPassEnabled
                + ", scoringModeEnabled=" + scoringModeEnabled
                + '}';
    }
}
