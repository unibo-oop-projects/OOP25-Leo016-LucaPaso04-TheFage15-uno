package uno.Model.Game;

import java.util.List;
import java.util.Random;

import uno.Model.Players.Player;

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
    private int skipSize;

    /**
     * Crea un nuovo gestore dei turni.
     * @param players La lista dei giocatori, in ordine di gioco iniziale.
     */
    public TurnManager(List<Player> players) {
        this.players = players;

        Random rand = new Random();
        this.currentPlayerIndex = rand.nextInt(players.size());

        this.isClockwise = true;
        this.skipNext = false;
        this.hasDrawnThisTurn = false;
        this.skipSize = 0;
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
    public void advanceTurn(Game game) {
        int N = players.size();
    
        // 1. Calcola lo step: N posizioni (salta tutti) o 1/2 (skip singolo)
        int step;
        if (skipSize != 0) {
            step = skipSize; // Torna al giocatore corrente
        } else {
            step = this.skipNext ? 2 : 1; // Logica esistente per Skip/Normale
        }

        // 2. Resetta i flag per il *nuovo* turno
        this.skipNext = false;         // Il flag "skip" è stato "consumato"
        this.skipSize = 0;    // <-- Reset del nuovo flag
        this.hasDrawnThisTurn = false; // Il nuovo giocatore non ha ancora pescato

        // 3. Calcola la direzione e il prossimo indice
        int direction = isClockwise ? 1 : -1;
        int nextIndex = (currentPlayerIndex + (step * direction));

        // 4. Gestisci il "giro" (wrap-around)
        // Usa l'aritmetica modulare per gestire grandi salti negativi (N passi)
        currentPlayerIndex = (nextIndex % N + N) % N; 
        System.out.println("Ora tocca al giocatore: " + currentPlayerIndex);

        checkAndApplyStartTurnPenalty(game);
    }

    public void checkAndApplyStartTurnPenalty(Game game){
        Player player = getCurrentPlayer();

        if (player.getHandSize() == 1 && !player.getHasCalledUno()) {
            player.unoPenalty(game);
            throw new IllegalStateException("Non hai chiamato UNO! Penalità assegnata");
        }

        player.resetUnoStatus();
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

    public void skipPlayers(int n) {
        skipSize = n;
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

    public boolean getIsClockwise() {
        return isClockwise;
    }
}