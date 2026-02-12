package uno.model.game.api;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;

import java.util.Optional;

/**
 * Interface representing the behavior of a specific Game State.
 * Methods in this interface correspond to actions that depend on the game
 * state.
 */
public interface GameStateBehavior {

    /**
     * Gets the enum representation of this game state.
     * 
     * @return The enum representation of this state.
     */
    GameState getEnum();

    /**
     * Handles the player playing a card.
     * 
     * @param card The card to play.
     */
    void playCard(Optional<Card> card);

    /**
     * Handles the player initiating a draw.
     */
    void playerInitiatesDraw();

    /**
     * Handles the player passing their turn.
     */
    void playerPassTurn();

    /**
     * Handles setting a color (for Wild cards).
     * 
     * @param color The color to set.
     */
    void setColor(CardColor color);

    /**
     * Handles choosing a player (for specific Wild cards).
     * 
     * @param player The chosen player.
     */
    void chosenPlayer(AbstractPlayer player);

    /**
     * Handles drawing until a specific color is found (specific Wild card effect).
     * 
     * @param color The target color.
     */
    void drawUntilColorChosenCard(CardColor color);
}
