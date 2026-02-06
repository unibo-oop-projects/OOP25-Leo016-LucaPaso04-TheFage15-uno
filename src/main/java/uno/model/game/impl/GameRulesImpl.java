package uno.model.game.impl;

import uno.model.game.api.GameRules;

/**
 * Immutable implementation of the GameRules interface.
 */
public class GameRulesImpl implements GameRules {

    private final boolean unoPenaltyEnabled;
    private final boolean skipAfterDrawEnabled;
    private final boolean mandatoryPassEnabled;

    /**
     * Creates a new GameRulesImpl instance.
     *
     * @param unoPenaltyEnabled    If true, players must say UNO when they have 1
     *                             card.
     * @param skipAfterDrawEnabled If true, a player cannot play a card immediately
     *                             after drawing.
     * @param mandatoryPassEnabled If true, when draw deck is empty, game ends (no
     *                             reshuffle).
     */
    public GameRulesImpl(final boolean unoPenaltyEnabled, final boolean skipAfterDrawEnabled,
            final boolean mandatoryPassEnabled) {
        this.unoPenaltyEnabled = unoPenaltyEnabled;
        this.skipAfterDrawEnabled = skipAfterDrawEnabled;
        this.mandatoryPassEnabled = mandatoryPassEnabled;
    }

    /**
     * Default rules: Penalty YES, Skip NO, Reshuffle YES (mandatory pass NO).
     * 
     * @return default GameRules implementation.
     */
    public static GameRules defaultRules() {
        return new GameRulesImpl(true, false, false);
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
    public String toString() {
        return "GameRulesImpl{"
                + "unoPenaltyEnabled=" + unoPenaltyEnabled
                + ", skipAfterDrawEnabled=" + skipAfterDrawEnabled
                + ", mandatoryPassEnabled=" + mandatoryPassEnabled
                + '}';
    }
}
