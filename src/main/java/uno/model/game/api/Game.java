package uno.model.game.api;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.impl.DiscardPileImpl;
import uno.model.game.impl.TurnManagerImpl;
import uno.model.players.impl.AbstractPlayer;
import uno.model.api.GameModelObserver;

import java.util.List;
import java.util.Optional;

/**
 * Main interface for the UNO Game Model.
 * It maintains the game state, orchestrates turn flow, and manages rules
 * execution.
 */
public interface Game {

    /**
     * Registers an observer to receive game state updates.
     * 
     * @param observer The observer to be registered.
     */
    void addObserver(GameModelObserver observer);

    /**
     * Notifies all registered observers about a state change.
     */
    void notifyObservers();

    /**
     * Processes a player's attempt to play a card.
     * 
     * @param card The card the player wishes to play.
     */
    void playCard(Optional<Card> card);

    /**
     * Initiates the draw action for the current player.
     */
    void playerInitiatesDraw();

    /**
     * Processes the passing of a player's turn.
     */
    void playerPassTurn();

    /**
     * Called when a player declares "UNO".
     * 
     * @param player The player who is calling UNO.
     */
    void callUno(AbstractPlayer player);

    /**
     * Sets the current color in play (used after Wild cards).
     * 
     * @param color The color to set as current.
     */
    void setColor(CardColor color);

    /**
     * Called when a player is chosen (e.g., for 'Wild Draw Player' card).
     * 
     * @param player The player who has been chosen.
     */
    void chosenPlayer(AbstractPlayer player);

    /**
     * Skips the next n players' turns.
     * 
     * @param n Number of players to skip.
     */
    void skipPlayers(int n);

    /**
     * Causes the next player to draw a specified number of cards.
     * 
     * @param amount Number of cards the next player must draw.
     */
    void makeNextPlayerDraw(int amount);

    /**
     * Reverses the current play order.
     */
    void reversePlayOrder();

    /**
     * Flips the game world between Light and Dark sides.
     */
    void flipTheWorld();

    /**
     * Requests the current player to choose a color (for Wild cards).
     */
    void requestColorChoice();

    /**
     * Requests the current player to choose another player (for 'Wild Draw Player'
     * cards).
     */
    void requestPlayerChoice();

    /**
     * Draws a card for the specified player.
     * 
     * @param player The player who will draw a card.
     */
    void drawCardForPlayer(AbstractPlayer player);

    /**
     * Causes the next player to draw cards until they pick one of the specified
     * color.
     * 
     * @param color The color that the next player must draw until they find.
     */
    void drawUntilColorChosenCard(CardColor color);

    /**
     * Returns the current player in the game.
     * 
     * @return The current {@link AbstractPlayer}.
     */
    AbstractPlayer getCurrentPlayer();

    /**
     * Gets the top card of the discard pile.
     * 
     * @return An {@link Optional} containing the top {@link Card} if present.
     */
    Optional<Card> getTopDiscardCard();

    /**
     * Gets the TurnManager responsible for managing player turns.
     * 
     * @return The {@link TurnManagerImpl} instance.
     */
    TurnManager getTurnManager();

    /**
     * Gets the discard pile of the game.
     * 
     * @return The {@link DiscardPileImpl} instance.
     */
    DiscardPile getDiscardPile();

    /**
     * Gets the draw deck of the game.
     * 
     * @return The {@link Deck} containing {@link Card} objects.
     */
    Deck<Card> getDrawDeck();

    /**
     * Checks if the discard pile is empty.
     * 
     * @return true if the discard pile has no cards, false otherwise.
     */
    boolean isDiscardPileEmpty();

    /**
     * Gets the current state of the game.
     * 
     * @return The current {@link GameState}.
     */
    GameState getGameState();

    /**
     * Gets the current color in play.
     * 
     * @return An {@link Optional} containing the current {@link CardColor} if set.
     */
    Optional<CardColor> getCurrentColor();

    /**
     * Gets the list of players in the game.
     * 
     * @return A {@link List} of {@link AbstractPlayer} objects.
     */
    List<AbstractPlayer> getPlayers();

    /**
     * Gets the winner of the game, if there is one.
     * 
     * @return The winning {@link AbstractPlayer}, or null if the game is still
     *         ongoing.
     */
    AbstractPlayer getWinner();

    /**
     * Indicates if the game is currently in Dark Side mode.
     * 
     * @return true if Dark Side is active, false otherwise.
     */
    boolean isDarkSide();

    /**
     * Indicates the current play direction.
     * 
     * @return true if play is clockwise, false if counter-clockwise.
     */
    boolean isClockwise();

    /**
     * Checks if the specified player has drawn cards during their current turn.
     * 
     * @param player The player to check.
     * @return true if the player has drawn cards this turn, false otherwise.
     */
    boolean hasCurrentPlayerDrawn(AbstractPlayer player);

    /**
     * Advances the turn to the next player, considering the current game state.
     * 
     * @param color The color to set as current after a Wild card is played.
     */
    void setCurrentColor(CardColor color);

    /**
     * Advances the turn for AI players.
     */
    void aiAdvanceTurn();

    /**
     * Gets the rules currently active in the game.
     * 
     * @return The {@link GameRules} object.
     */
    GameRules getRules();

    /**
     * Logs a system action for auditing or debugging purposes.
     * 
     * @param actionType  The type of action being logged.
     * @param cardDetails Details about the card involved in the action.
     * @param extraInfo   Any additional information relevant to the action.
     */
    void logSystemAction(String actionType, String cardDetails, String extraInfo);

    /**
     * Resets the game state for a new round (keeping scores).
     */
    void startNewRound();
}
