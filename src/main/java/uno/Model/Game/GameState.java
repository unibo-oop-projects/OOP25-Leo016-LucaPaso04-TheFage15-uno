package uno.Model.Game;

/**
 * Rappresenta i possibili stati in cui la partita si può trovare.
 * Questo è fondamentale per gestire logiche complesse come la scelta
 * del colore dopo aver giocato un Jolly.
 */
public enum GameState {
    /**
     * Il gioco sta procedendo normalmente, un giocatore dopo l'altro.
     */
    RUNNING,

    /**
     * Un giocatore ha giocato una carta Jolly (Wild) e il gioco
     * è in pausa in attesa che scelga un nuovo colore.
     */
    WAITING_FOR_COLOR,
    WAITING_FOR_PLAYER,

    /**
     * La partita è terminata.
     */
    GAME_OVER
}