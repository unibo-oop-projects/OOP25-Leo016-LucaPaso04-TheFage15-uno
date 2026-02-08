package uno.model.game.api;

import java.util.List;

import uno.model.players.api.AbstractPlayer;

public interface ScoreManager {
    /**
     * Calculates the total points from all opponents' hands.
     * 
     * @param winner  The player who won the round.
     * @param players The list of all players in the game.
     * @param game    The current game context.
     * @return The total points calculated from opponents' hands.
     */
    int calculateRoundPoints(final AbstractPlayer winner, final List<AbstractPlayer> players, final Game game);
}
