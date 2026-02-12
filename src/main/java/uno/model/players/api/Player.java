package uno.model.players.api;

import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;

import java.util.List;
import java.util.Optional;

/**
 * Interface representing a player in the UNO game.
 */
public interface Player {
    /**
     * Gets the current score of the player.
     * 
     * @return The player's score.
     */
    int getScore();

    /**
     * Sets the player's score.
     * 
     * @param score The new score.
     */
    void setScore(int score);

    /**
     * Adds points to the player's current score.
     * 
     * @param points The points to add.
     */
    void addScore(int points);

    /**
     * Executes the player's turn logic.
     * 
     * @param game The current game instance (interface).
     */
    void takeTurn(Game game);

    /**
     * Gets the player's name.
     * 
     * @return The name of the player.
     */
    String getName();

    /**
     * Gets a copy of the player's current hand.
     * 
     * @return A list of Card representing the player's hand.
     */
    List<Optional<Card>> getHand();

    /**
     * Gets the current size of the player's hand.
     * 
     * @return The number of cards in hand.
     */
    int getHandSize();

    /**
     * Sets the player's hand to a new list of cards.
     * 
     * @param newHand The new hand to set.
     */
    void setHand(List<Optional<Card>> newHand);

    /**
     * Adds a card to the player's hand.
     * 
     * @param card The card to add.
     */
    void addCardToHand(Card card);

    /**
     * Removes a card from hand.
     * 
     * @param card The card to play (remove).
     * @return true if successful.
     */
    boolean playCard(Optional<Card> card);

    /**
     * Checks if the player has won (i.e., has no cards left).
     * 
     * @return true if the player has won.
     */
    boolean hasWon();

    /**
     * Applies the UNO penalty for forgetting to call UNO.
     * 
     * @param game The current game instance (interface).
     */
    void unoPenalty(Game game);

    /**
     * Marks that the player has called UNO.
     */
    void hasCalledUno();

    /**
     * Checks if the player has called UNO.
     * 
     * @return true if the player has called UNO.
     */
    boolean isHasCalledUno();

    /**
     * Resets the UNO call status, typically after the player's turn ends.
     */
    void resetUnoStatus();

    /**
     * Explicitly sets the UNO call status (TESTING ONLY).
     * 
     * @param status The new status.
     */
    void setHasCalledUno(boolean status);
}
