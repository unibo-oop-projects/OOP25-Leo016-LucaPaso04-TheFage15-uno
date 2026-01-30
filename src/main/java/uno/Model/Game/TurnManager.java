package uno.model.game;

import java.util.List;
import java.util.Random;

import uno.model.players.Player;

/**
 * Gestisce la logica di avanzamento dei turni, inclusa la direzione
 * di gioco e il salto dei giocatori.
 */
public class TurnManager {

    private final List<Player> players;
    private int currentPlayerIndex;
    private boolean isClockwise; // true = senso orario, false = antiorario
    private boolean hasDrawnThisTurn;
    private int skipSize; // Indica QUANTI giocatori saltare (0 per un turno normale).

    /**
     * Crea un nuovo gestore dei turni.
     * @param players La lista dei giocatori, in ordine di gioco iniziale.
     */
    public TurnManager(List<Player> players) {
        this.players = players;

        Random rand = new Random();
        this.currentPlayerIndex = rand.nextInt(players.size());

        this.isClockwise = true;
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
     * Applica l'avanzamento basato su skipSize.
     */
    public void advanceTurn(Game game) {
        int N = players.size();
    
        // 1. Calcola il passo totale: 
        // Se skipSize è N (vuol dire saltare N giocatori), il passo totale è N + 1.
        int totalSteps = this.skipSize + 1; 

        // 2. Resetta i flag per il *nuovo* turno
        this.skipSize = 0;    // Reset per la prossima carta/effetto
        this.hasDrawnThisTurn = false; // Il nuovo giocatore non ha ancora pescato

        // 3. Calcola la direzione e il prossimo indice
        int direction = isClockwise ? 1 : -1;
        // La posizione si sposta di 'totalSteps' nella direzione 'direction'.
        int nextIndex = (currentPlayerIndex + (totalSteps * direction));

        // 4. Gestisci il "giro" (wrap-around)
        // Usa l'aritmetica modulare (aggiunge N per gestire numeri negativi)
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
     * tenendo conto di eventuali skipSize in sospeso.
     * @return Il prossimo giocatore.
     */
    public Player peekNextPlayer() {
        // Calcola il passo totale (come in advanceTurn)
        int totalSteps = this.skipSize + 1; 
        
        int N = players.size();
        int direction = isClockwise ? 1 : -1;
        
        // Calcola l'indice futuro
        int nextIndex = (currentPlayerIndex + (totalSteps * direction));

        // Gestisci il "giro" con il modulo
        int nextPlayerIndex = (nextIndex % N + N) % N; 
        
        return players.get(nextPlayerIndex);
    }

    /**
     * Inverte la direzione di gioco.
     */
    public void reverseDirection() {
        isClockwise = !isClockwise;
    }

    /**
     * Imposta il numero di giocatori da saltare nel prossimo turno.
     * @param n Il numero di giocatori da saltare (es. 1 per Skip, 2 per Skip Two).
     */
    public void skipPlayers(int n) {
        this.skipSize = n;
    }

    // --- METODI GETTER/SETTER ---

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