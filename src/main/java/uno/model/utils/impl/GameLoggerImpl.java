package uno.model.utils.impl;

import uno.model.utils.api.GameLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Concrete implementation of GameLogger that writes events to a text file.
 * The logs are stored in a "logs" directory within the project root.
 */
public class GameLoggerImpl implements GameLogger {

    private final String filePath;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Initializes the logger and ensures the directory structure exists.
     *
     * @param matchId A unique identifier for the current match (used in the filename).
     */
    public GameLoggerImpl(final String matchId) {
        // Retrieve current working directory
        final String userDir = System.getProperty("user.dir");

        // Construct the file path using safe separators
        this.filePath = userDir + File.separator + "logs" + File.separator + "log_match_" + matchId + ".txt"; 

        initializeLogDirectory();
    }

    /**
     * Helper method to create the directory structure using Optional to avoid null checks.
     */
    private void initializeLogDirectory() {
        try {
            final File logFile = new File(this.filePath);

            Optional.ofNullable(logFile.getParentFile())
                    .filter(parent -> !parent.exists())
                    .ifPresent(parent -> {
                        if (parent.mkdirs()) {
                            System.out.println("Log directory created at: " + parent.getAbsolutePath());
                        } else {
                            System.err.println("CRITICAL ERROR: Could not create 'logs/' directory.");
                        }
                    });

        } catch (final Exception e) {
            System.err.println("Logger initialization error: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logAction(final String playerName, final String actionType, final String cardDetails, final String extraInfo) {
        final String timestamp = dtf.format(LocalDateTime.now());

        final String logEntry = String.format("%s;%s;%s;%s;%s", 
            timestamp, playerName, actionType, cardDetails, extraInfo);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (final IOException e) {
            System.err.println("Error writing to game log: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logError(final String context, final Exception e) {
        this.logAction("SYSTEM_ERROR", context, e.getClass().getSimpleName(), e.getMessage());

        java.util.logging.Logger.getLogger("UNO")
            .log(java.util.logging.Level.SEVERE, context, e);
    }
}
