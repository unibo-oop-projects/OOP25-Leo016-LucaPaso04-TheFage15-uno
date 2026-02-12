package uno.view.api;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;

import java.util.Optional;

/**
 * Interface for observing user interactions in the game view. This interface defines
 * methods that are called when the user performs certain actions in the game view, such as
 * playing a card, drawing a card, calling "UNO", etc.
 */
public interface GameViewObserver {

    /**
     * Called when the user attempts to play a card.
     * 
     * @param card The card that the user attempted to play.
     */
    void onPlayCard(Optional<Card> card);

    /**
     * Called when the user clicks on the draw pile.
     */
    void onDrawCard();

    /**
     * Called when the user clicks the "UNO" button.
     */
    void onCallUno();

    /**
     * Called when the user wants to go back to the main menu.
     */
    void onBackToMenu();

    /**
     * Called when the user clicks the "Pass" button.
     */
    void onPassTurn();

    /**
     * Called when the user clicks on one of the player buttons to choose a player for a card effect.
     * 
     * @param player The chosen player.
     */
    void onPlayerChosen(AbstractPlayer player);

    /**
     * Called when the user clicks on one of the color buttons
     * after playing a Jolly card.
     * 
     * @param color The chosen color.
     */
    void onColorChosen(CardColor color);
}
