package uno.model.game.api;

/**
 * Enum representing the various states a UNO game can be in.
 */
public enum GameState {
    /**
     * The game is in progress.
     */
    RUNNING,

    /**
     * The game is waiting for a player to choose a color (after playing a Wild
     * card).
     */
    WAITING_FOR_COLOR,

    /**
     * The game is waiting for a player to be chosen (after playing a "Choose
     * Player" card).
     */
    WAITING_FOR_PLAYER,

    /**
     * The round has ended, but the match continues.
     */
    ROUND_OVER,

    /**
     * The game has ended.
     */
    GAME_OVER
}
