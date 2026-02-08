package uno.controller.api;

import java.util.Optional;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.api.AbstractPlayer;
import uno.view.api.GameViewObserver;
import uno.model.api.GameModelObserver;

/**
 * Interface defining the operations for controlling a Uno! game session.
 * It extends GameViewObserver to receive input from the View and
 * GameModelObserver
 * to react to Model changes.
 */
public interface GameController extends GameViewObserver, GameModelObserver {

    /**
     * Initializes the game sequence by displaying the starting player
     * and triggering the core game logic.
     */
    void showStartingPlayerPopupAndStartGame();

    /**
     * Handles the action of a player attempting to play a specific card.
     * 
     * @param card The card the player intends to play.
     */
    @Override
    void onPlayCard(Optional<Card> card);

    /**
     * Handles the request to draw a card from the deck.
     */
    @Override
    void onDrawCard();

    /**
     * Handles the invocation of the "UNO!" command (when a player has one card
     * left).
     */
    @Override
    void onCallUno();

    /**
     * Handles the action of passing the turn to the next player
     * (usually allowed after drawing a card that cannot be played).
     */
    @Override
    void onPassTurn();

    /**
     * Handles the event where a color has been selected from the UI
     * (e.g., after playing a Wild card).
     * 
     * @param color The selected color.
     */
    @Override
    void onColorChosen(CardColor color);

    /**
     * Handles the selection of a target player
     * (e.g., for specific card effects in variants).
     * 
     * @param player The selected target player.
     */
    @Override
    void onPlayerChosen(AbstractPlayer player);

    /**
     * Handles the logic for exiting the current game session and returning to the
     * main menu.
     */
    @Override
    void onBackToMenu();
}
