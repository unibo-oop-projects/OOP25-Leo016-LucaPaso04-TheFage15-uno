package uno.view.api;

import java.util.List;

import uno.model.players.impl.AbstractPlayer;

/**
 * DTO representing a Player for the View.
 */
public interface PlayerViewData {
    /**
     * Get the player's name.
     * 
     * @return the player's name.
     */
    String getName();

    /**
     * Get the number of cards in the player's hand.
     * 
     * @return the number of cards in the player's hand.
     */
    int getHandSize();

    /**
     * Get the player's current score.
     * 
     * @return the player's current score.
     */
    int getScore();

    /**
     * Check if this player is the current player.
     * 
     * @return true if this player is the current player, false otherwise.
     */
    boolean isCurrentPlayer();

    /**
     * Get the player's hand as a list of CardViewData.
     * 
     * @return the player's hand as a list of CardViewData.
     */
    List<CardViewData> getHand();

    /**
     * Get the underlying model player object. This is an opaque token that can be used
     * for advanced features or debugging, but should not be used for game logic in the view.
     * 
     * @return the underlying model player object.
     */
    AbstractPlayer getModelPlayer();
}
