package uno.model.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class GameLogger {
    private final String filePath;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public GameLogger(String matchId) {
        // RECUPERA IL PERCORSO DELLA DIRECTORY DI LAVORO CORRENTE (es. C:/Users/tuo_nome/progetti/uno)
        String userDir = System.getProperty("user.dir");
        
        // Combina la directory di lavoro con il percorso relativo "logs/"
        this.filePath = userDir + File.separator + "logs" + File.separator + "log_partita_" + matchId + ".txt"; 
        
        // --- LOGICA DI CREAZIONE DELLA DIRECTORY ---
        try {
            File logFile = new File(this.filePath);
            File parentDir = logFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                if (parentDir.mkdirs()) {
                    System.out.println("Directory 'logs/' creata con successo in: " + parentDir.getAbsolutePath());
                } else {
                    System.err.println("ERRORE CRITICO: Impossibile creare la directory 'logs/'.");
                }
            }
        } catch (Exception e) {
            System.err.println("Errore di inizializzazione del logger: " + e.getMessage());
        }
    }

    public void logAction(String playerName, String actionType, String cardDetails, String extraInfo) {
        String timestamp = dtf.format(LocalDateTime.now());
        // Formato: TIMESTAMP;PLAYER;ACTION;CARD;EXTRA
        String logEntry = String.format("%s;%s;%s;%s;%s", 
            timestamp, playerName, actionType, cardDetails, extraInfo);
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(logEntry);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Errore durante la scrittura del log: " + e.getMessage());
        }
    }
}