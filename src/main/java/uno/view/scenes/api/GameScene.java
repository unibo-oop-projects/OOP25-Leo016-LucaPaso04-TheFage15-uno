package uno.view.scenes.api;

import uno.view.api.GameViewObserver;
import uno.model.players.impl.AbstractPlayer;
import uno.view.api.GameViewData;

import java.util.List;

/**
 * Interface representing the main Game Board view.
 * It defines how the game displays the state and handles user interaction
 * requests coming from the Controller.
 */
public interface GameScene {

    /**
     * Updates the view with the latest game data.
     * 
     * @param data The snapshot of the game state.
     */
    void updateView(GameViewData data);

    /**
     * Registers the controller to handle user inputs (clicks on cards, buttons).
     *
     * @param observer The controller instance.
     */
    void setObserver(GameViewObserver observer);

    /**
     * Enables or disables the interactive elements for the human player.
     * Used to lock the UI during AI turns or animations.
     *
     * @param enabled true to enable buttons/cards, false to disable them.
     */
    void setHumanInputEnabled(boolean enabled);

    /**
     * Displays a popup dialog announcing the winner of the game.
     *
     * @param winnerName The name of the winning player.
     */
    void showWinnerPopup(String winnerName);

    /**
     * Shows an error message to the user.
     * 
     * @param message The message to display.
     * @param title   The title of the dialog.
     */
    void showError(String message, String title);

    /**
     * Shows an informational message to the user.
     * 
     * @param message The message to display.
     * @param title   The title of the dialog.
     */
    void showInfo(String message, String title);

    /**
     * Shows the starting player popup.
     * 
     * @param playerName The name of the starting player.
     */
    void showStartingPlayer(String playerName);

    /**
     * Asks the user to confirm exiting to the menu.
     * 
     * @return true if the user confirms, false otherwise.
     */
    boolean confirmExit();

    /**
     * Triggers the display of the color selection panel (e.g., after a Wild card).
     *
     * @param isDarkSide True if the game is on the "Dark" side (Uno Flip),
     *                   affecting the palette.
     */
    void showColorChooser(boolean isDarkSide);

    /**
     * Triggers the display of the player selection panel (e.g., for targeted
     * effects).
     *
     * @param opponents The list of available opponents to choose from.
     */
    void showPlayerChooser(List<AbstractPlayer> opponents);
}
