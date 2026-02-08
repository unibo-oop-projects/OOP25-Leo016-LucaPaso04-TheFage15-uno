package uno.model.game.impl.states;

import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;

/**
 * State representing the end of a round.
 * The game pauses here to show the round winner and scores before starting the
 * next round.
 */
public class RoundOverState extends AbstractGameState {

    public RoundOverState(GameContext game) {
        super(game);
    }

    @Override
    public GameState getEnum() {
        return GameState.ROUND_OVER;
    }
}
