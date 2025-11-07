package uno.Model.Game;

import uno.Model.Player.Player;
import java.util.List;

/**
 * Gestisce la logica di avanzamento dei turni, inclusa la direzione
 * di gioco e il salto dei giocatori.
 */
public class TurnManager {

    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean isClockwise; // true = senso orario, false = antiorario
    private boolean skipNext;
    private boolean hasDrawnThisTurn;

    /**
     * Crea un nuovo gestore dei turni.
     * @param players La lista dei giocatori, in ordine di gioco iniziale.
     */
    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentPlayerIndex = 0; // Inizia sempre dal primo giocatore
        this.isClockwise = true;
        this.skipNext = false;
        this.hasDrawnThisTurn = false;
    }

    /**
     * Restituisce il giocatore attualmente di turno.
     * @return Il giocatore corrente.
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Calcola e passa al giocatore successivo.
     * Applica qualsiasi effetto "salta" in sospeso.
     */
    public void advanceTurn() {
        // --- MODIFICA ---
        // Resetta i flag per il *nuovo* turno
        this.skipNext = false; 
        this.hasDrawnThisTurn = false; 

        int increment = skipNext ? 2 : 1;
        // skipNext = false; // Spostato sopra

        int direction = isClockwise ? 1 : -1;
        int nextIndex = (currentPlayerIndex + (increment * direction));

        if (nextIndex < 0) {
            currentPlayerIndex = players.size() + nextIndex;
        } else {
            currentPlayerIndex = nextIndex % players.size();
        }
    }

    /**
     * Restituisce il giocatore che giocherebbe DOPO quello corrente,
     * senza far avanzare il turno.
     * @return Il prossimo giocatore.
     */
    public Player peekNextPlayer() {
        int increment = skipNext ? 2 : 1;
        int direction = isClockwise ? 1 : -1;
        int nextIndex = (currentPlayerIndex + (increment * direction));

        if (nextIndex < 0) {
            return players.get(players.size() + nextIndex);
        } else {
            return players.get(nextIndex % players.size());
        }
    }

    /**
     * Inverte la direzione di gioco.
     */
    public void reverseDirection() {
        isClockwise = !isClockwise;
    }

    /**
     * Imposta un flag per saltare il prossimo giocatore.
     * L'avanzamento effettivo avverrà alla chiamata di advanceTurn().
     */
    public void skipNextPlayer() {
        this.skipNext = true;
    }

    // --- NUOVI METODI GETTER/SETTER ---

    /**
     * @return true se il giocatore corrente ha già pescato.
     */
    public boolean hasDrawnThisTurn() {
        return this.hasDrawnThisTurn;
    }

    /**
     * Imposta il flag per la pesca.
     */
    public void setHasDrawnThisTurn(boolean value) {
        this.hasDrawnThisTurn = value;
    }
}