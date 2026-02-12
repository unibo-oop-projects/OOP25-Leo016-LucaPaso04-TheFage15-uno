package uno.model.players.impl;

import uno.model.game.api.Game;

/**
 * Implementation of a human player in the UNO game.
 * This class serves as a placeholder for human interaction
 */
public class HumanPlayer extends AbstractPlayer {

    /**
     * Constructor for HumanPlayer.
     * 
     * @param name the name of the human player
     */
    public HumanPlayer(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void takeTurn(final Game game) {
        // Human logic is passive: we wait for UI events.
    }
}
