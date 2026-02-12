package uno.model.game.api;

import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;

/**
 * Interface responsible for deck and discard pile management.
 */
public interface DeckHandler {

    /**
     * Draws a card for a player, handling reshuffling if necessary.
     * 
     * @param player The player drawing the card.
     * @param game   The game context.
     * @return true if draw successful, false if game should end (e.g. empty deck no
     *         reshuffle).
     */
    boolean drawCardForPlayer(AbstractPlayer player, GameContext game);

    /**
     * Gets the draw deck.
     * 
     * @return The draw deck.
     */
    Deck<Card> getDrawDeck();

    /**
     * Gets the discard pile.
     * 
     * @return The discard pile.
     */
    DiscardPile getDiscardPile();
}
