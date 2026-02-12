package uno.model.game.api;

import java.util.List;

import uno.model.players.impl.AbstractPlayer;

/**
 * Interface for managing the scoring system in the Uno game. It defines the method
 * to calculate the points for a round based on the winner and the opponents' hands.
 */
@FunctionalInterface
public interface ScoreManager {
    /**
     * Calculates the total points from all opponents' hands.
     * 
     * @param winner  The player who won the round.
     * @param players The list of all players in the game.
     * @param game    The current game context.
     * @return The total points calculated from opponents' hands.
     */
    int calculateRoundPoints(AbstractPlayer winner, List<AbstractPlayer> players, Game game);
}
