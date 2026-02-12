package uno.model.players.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameState;

import java.util.Optional;
import java.util.Objects;

/**
 * Abstract AI Player class providing common functionality for AI players.
 */
public abstract class AbstractAIPlayer extends AbstractPlayer {

    /**
     * Constructor for AIPlayer.
     * 
     * @param name the name of the player
     */
    public AbstractAIPlayer(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void takeTurn(final Game game) {
        if (!game.getCurrentPlayer().equals(this)) {
            return;
        }

        // Try to find a valid move
        final Optional<Card> chosenCard = chooseCardToPlay(game);

        if (chosenCard.isPresent()) {
            // Before playing, check UNO condition
            if (getHandSize() == 2) {
                hasCalledUno();
            }

            game.playCard(chosenCard);

        } else {
            // No move? Draw a card.
            if (!game.hasCurrentPlayerDrawn(this)) {
                game.playerInitiatesDraw();

                // 6. Try to play the drawn card immediately (standard rule)
                if (!game.getRules().isSkipAfterDrawEnabled()) {
                    final Optional<Card> postDrawMove = chooseCardToPlay(game);
                    if (postDrawMove.isPresent()) {
                        if (getHandSize() == 2) {
                            hasCalledUno();
                        }
                        game.playCard(postDrawMove);
                    } else {
                        game.aiAdvanceTurn();
                    }
                } else {
                    game.aiAdvanceTurn();
                }
            } else {
                game.aiAdvanceTurn();
            }
        }

        if (game.getGameState() == GameState.WAITING_FOR_COLOR) {
            final CardColor chosenColor = chooseBestColor(game);
            game.requestColorChoice();
            game.setColor(chosenColor);
            game.aiAdvanceTurn();
        }
    }

    /**
     * Abstract Strategy: Each AI variant implements this differently.
     * 
     * @param game The current game state
     * @return An Optional containing the chosen card, or empty if no valid move
     *         exists.
     */
    protected abstract Optional<Card> chooseCardToPlay(Game game);

    /**
     * Abstract Strategy: Each AI variant implements this differently.
     * 
     * @param game The current game state
     * @return The best color to choose when playing a Wild card.
     */
    protected abstract CardColor chooseBestColor(Game game);

    /**
     * Helper to check validity using Game logic.
     * 
     * @param card The card to check
     * @param game The current game state
     * @return true if the move is valid, false otherwise
     */
    protected boolean isMoveValid(final Card card, final Game game) {
        final Optional<Card> topCard = game.getTopDiscardCard();
        return topCard.isEmpty() || card.canBePlayedOn(topCard.get(), game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AbstractPlayer player = (AbstractPlayer) o;
        return Objects.equals(this.getName(), player.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.getName());
    }
}
