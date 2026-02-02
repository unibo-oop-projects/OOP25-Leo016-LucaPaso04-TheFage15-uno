package uno.model.players.impl;

import uno.model.cards.types.api.Card;
import uno.model.players.api.Player;
import uno.model.game.api.Game;
import java.util.Optional;

/**
 * Abstract AI Player class providing common functionality for AI players.
 */
public abstract class AIPlayer extends Player {

    /**
     * Constructor for AIPlayer.
     * @param name the name of the player
     */
    public AIPlayer(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void takeTurn(final Game game) {
        // 1. Simulate "thinking" time (optional, handled by Thread/Timer in Controller usually)

        // 2. Try to find a valid move
        final Optional<Card> chosenCard = chooseCardToPlay(game);

        if (chosenCard.isPresent()) {
            // 3. Before playing, check UNO condition
            if (getHandSize() == 2) { // Will have 1 after playing
                System.out.println(getName() + " calls UNO!");
                hasCalledUno();
                game.callUno(this); // Notify game
            }

            // 4. Play the card
            game.playCard(chosenCard);

        } else {
            // 5. No move? Draw a card.
            if (!game.hasCurrentPlayerDrawn(this)) {
                System.out.println(getName() + " has no moves. Drawing...");
                game.playerInitiatesDraw();

                // 6. Try to play the drawn card immediately (standard rule)
                // Since drawing is async/state-changing in Game, usually we wait.
                // But if your game logic allows immediate replay:
                // Optional<Card> drawnCard = getDrawnCard();
                // if (isValid(drawnCard)) game.playCard(drawnCard);
                // else game.playerPassTurn();

                // For simplicity, let's assume the Controller calls AI again 
                // or handles the post-draw logic. 
                // If you need to handle it here, you need to re-evaluate the hand.

                // Re-evaluate immediately after draw:
                final Optional<Card> postDrawMove = chooseCardToPlay(game);
                if (postDrawMove.isPresent()) {
                    game.playCard(postDrawMove);
                } else {
                    game.playerPassTurn();
                }

            } else {
                game.playerPassTurn();
            }
        }
    }

    /**
     * Abstract Strategy: Each AI variant implements this differently.
     * @param game The current game state
     * @return An Optional containing the chosen card, or empty if no valid move exists.
     */
    protected abstract Optional<Card> chooseCardToPlay(Game game);

    /**
     * Abstract Strategy: Each AI variant implements this differently.
     * @param game The current game state
     * @return The best color to choose when playing a Wild card.
     */
    protected abstract uno.model.cards.attributes.CardColor chooseBestColor(Game game);

    /**
     * Helper to check validity using Game logic.
     * @param card The card to check
     * @param game The current game state
     * @return true if the move is valid, false otherwise
     */
    protected boolean isMoveValid(final Card card, final Game game) {
        final Optional<Card> topCard = game.getTopDiscardCard();
        return topCard.isPresent() && card.canBePlayedOn(topCard.get(), game);
    }
}
