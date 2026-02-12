package uno.model.game.impl;

import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.ScoreManager;
import uno.model.players.impl.AbstractPlayer;

import java.util.List;
import java.util.Optional;

/**
 * Helper class responsible for calculating scores at the end of a round.
 */
public class ScoreManagerImpl implements ScoreManager {

    /**
     * {@inheritDoc}
     */
    @Override
    public int calculateRoundPoints(final AbstractPlayer winner, final List<AbstractPlayer> players, final Game game) {
        int totalPoints = 0;

        for (final AbstractPlayer player : players) {
            if (!player.equals(winner)) {
                for (final Optional<Card> cardOpt : player.getHand()) {
                    if (cardOpt.isPresent()) {
                        totalPoints += cardOpt.get().getPointValue(game);
                    }
                }
            }
        }

        return totalPoints;
    }
}
