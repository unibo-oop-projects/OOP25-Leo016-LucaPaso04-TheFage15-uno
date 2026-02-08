package uno.model.players.api;

import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class representing a generic player in the UNO game.
 * Manages the hand state and basic player properties.
 */
public abstract class AbstractPlayer {

    private final String name;
    private final List<Optional<Card>> hand;
    private boolean hasCalledUno;
    private int score;

    /**
     * Constructor to initialize a player with a name.
     * 
     * @param name The name of the player.
     */
    public AbstractPlayer(final String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.hasCalledUno = false;
        this.score = 0;
    }

    /**
     * Gets the current score of the player.
     * 
     * @return The player's score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Sets the player's score.
     * 
     * @param score The new score.
     */
    public void setScore(final int score) {
        this.score = score;
    }

    /**
     * Adds points to the player's current score.
     * 
     * @param points The points to add.
     */
    public void addScore(final int points) {
        this.score += points;
    }

    /**
     * Executes the player's turn logic.
     * For Human players, this might notify the UI to enable controls.
     * For AI players, this triggers the algorithmic decision making.
     * 
     * @param game The current game instance (interface).
     */
    public abstract void takeTurn(Game game);

    /**
     * Gets the player's name.
     * 
     * @return The name of the player.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets a copy of the player's current hand.
     * 
     * @return A list of Card representing the player's hand.
     */
    public List<Optional<Card>> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * Gets the current size of the player's hand.
     * 
     * @return The number of cards in hand.
     */
    public int getHandSize() {
        return hand.size();
    }

    /**
     * Sets the player's hand to a new list of cards.
     * 
     * @param newHand The new hand to set.
     */
    public void setHand(final List<Optional<Card>> newHand) {
        this.hand.clear();
        this.hand.addAll(newHand);
    }

    /**
     * Adds a card to the player's hand.
     * 
     * @param card The card to add.
     */
    public void addCardToHand(final Card card) {
        this.hand.add(Optional.of(card));
        // Reset UNO status if they draw cards (they are safe now or need to call it
        // again later)
        if (hand.size() > 1) {
            this.hasCalledUno = false;
        }
    }

    /**
     * Removes a card from hand.
     * 
     * @param card The card to play (remove).
     * @return true if successful.
     */
    public boolean playCard(final Optional<Card> card) {
        return !card.isEmpty() && this.hand.remove(card);
    }

    /**
     * Checks if the player has won (i.e., has no cards left).
     * 
     * @return true if the player has won.
     */
    public boolean hasWon() {
        return hand.isEmpty();
    }

    /**
     * Applies the UNO penalty for forgetting to call UNO.
     * 
     * @param game The current game instance (interface).
     */
    public void unoPenalty(final Game game) {
        game.drawCardForPlayer(this);
        game.drawCardForPlayer(this);
    }

    /**
     * Marks that the player has called UNO.
     */
    public void hasCalledUno() {
        this.hasCalledUno = true;
    }

    /**
     * Checks if the player has called UNO.
     * 
     * @return true if the player has called UNO.
     */
    public boolean isHasCalledUno() {
        return this.hasCalledUno;
    }

    /**
     * Resets the UNO call status, typically after the player's turn ends.
     */
    public void resetUnoStatus() {
        this.hasCalledUno = false;
    }

    /**
     * Explicitly sets the UNO call status (TESTING ONLY).
     * 
     * @param status The new status.
     */
    public void setHasCalledUno(final boolean status) {
        this.hasCalledUno = status;
    }
}
