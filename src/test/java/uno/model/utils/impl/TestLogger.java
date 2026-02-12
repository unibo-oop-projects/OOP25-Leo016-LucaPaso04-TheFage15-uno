package uno.model.utils.impl;

import uno.model.utils.api.GameLogger;

/**
 * A logger implementation for testing purposes that does not write to files.
 * This prevents the accumulation of log files during unit test execution.
 */
public class TestLogger implements GameLogger {

    @Override
    public void logAction(final String playerName, final String actionType, final String cardDetails, final String extraInfo) {
        // No-op or print to stdout
        // System.out.println("LOG: " + playerName + " performed " + 
        // actionType + " with " + cardDetails + ". Extra: " + extraInfo);
    }

    @Override
    public void logError(final String context, final Exception e) {
        // No-op or print to stderr
        // System.err.println("ERROR in " + context + ": " + e.getMessage());
    }
}
