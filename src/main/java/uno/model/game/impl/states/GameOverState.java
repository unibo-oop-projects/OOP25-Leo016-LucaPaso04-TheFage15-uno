package uno.model.game.impl.states;

import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;

/**
 * State representing the end of the game.
 */
public class GameOverState extends AbstractGameState {

    public GameOverState(GameContext game) {
        super(game);
    }

    @Override
    public GameState getEnum() {
        return GameState.GAME_OVER;
    }
}
