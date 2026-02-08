package uno.model.game.impl;

import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DeckHandler;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameRules;
import uno.model.players.api.AbstractPlayer;
import uno.model.utils.api.GameLogger;

import java.util.List;
import java.util.Optional;

/**
 * Concrete implementation of DeckHandler.
 */
public class DeckHandlerImpl implements DeckHandler {

    private final Deck<Card> drawDeck;
    private final DiscardPile discardPile;
    private final GameRules rules;
    private final GameLogger logger;
    private final String loggerPlayerName;

    public DeckHandlerImpl(Deck<Card> drawDeck, DiscardPile discardPile, GameRules rules, GameLogger logger,
            String loggerPlayerName) {
        this.drawDeck = drawDeck;
        this.discardPile = discardPile;
        this.rules = rules;
        this.logger = logger;
        this.loggerPlayerName = loggerPlayerName;
    }

    @Override
    public boolean drawCardForPlayer(final AbstractPlayer player, final GameContext game) {
        if (drawDeck.isEmpty()) {
            // Regola: Mandatory Pass / No Reshuffle
            if (rules.isMandatoryPassEnabled()) {
                logger.logAction(loggerPlayerName, "DECK_EMPTY", "N/A", "No Reshuffle Rule Active. Game Ends.");
                return false;
            }

            reshuffleDiscardPile();
        }

        final Optional<Card> drawnCard = drawDeck.draw();
        if (drawnCard.isPresent()) {
            player.addCardToHand(drawnCard.get());
        }

        logger.logAction(player.getName(), "DRAW",
                drawnCard.isPresent() ? drawnCard.get().getClass().getSimpleName() : "NONE",
                drawnCard.isPresent() ? drawnCard.get().getValue(game).toString() : "NONE");

        return true;
    }

    private void reshuffleDiscardPile() {
        final List<Card> cardsToReshuffle = discardPile.takeAllExceptTop();

        if (cardsToReshuffle.isEmpty()) {
            return;
        }
        for (final Card card : cardsToReshuffle) {
            drawDeck.addCard(card);
        }
        drawDeck.shuffle();
    }

    @Override
    public Deck<Card> getDrawDeck() {
        return drawDeck;
    }

    @Override
    public DiscardPile getDiscardPile() {
        return discardPile;
    }
}
