package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameSetup;
import uno.model.players.impl.AbstractPlayer;
import uno.model.game.api.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Implementation of the {@link GameSetup} interface.
 */
public class GameSetupImpl implements GameSetup {

    private static final int INITIAL_HAND_SIZE = 7;
    private static final String LOGGER_ACTION_TYPE = "SETUP";

    private final Game game;
    private final Deck<Card> deck;
    private final DiscardPile discardPile;
    private final List<AbstractPlayer> players;

    /**
     * Constructor for GameSetupImpl.
     * 
     * @param game        game instance.
     * @param deck        deck of cards.
     * @param discardPile discard pile.
     * @param players     list of players.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public GameSetupImpl(final Game game, final Deck<Card> deck, final DiscardPile discardPile,
            final List<AbstractPlayer> players) {
        this.game = game;
        this.deck = deck;
        this.discardPile = discardPile;
        this.players = new ArrayList<>(players);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeGame(final boolean isAllWild) {
        dealInitialCards();
        setupFirstCard(isAllWild);
    }

    /**
     * Deals initial cards to all players.
     */
    private void dealInitialCards() {
        for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
            for (final AbstractPlayer player : this.players) {
                deck.draw().ifPresent(player::addCardToHand);
            }
        }
        game.logSystemAction(LOGGER_ACTION_TYPE, "DEAL_CARDS", "Dealt 7 cards to " + players.size() + " players.");
    }

    /**
     * Sets up the first card on the discard pile.
     * 
     * @param isAllWild indicates if the game is in All Wild mode.
     */
    private void setupFirstCard(final boolean isAllWild) {
        if (isAllWild) {
            drawAndPlaceAnyCard();
            game.setCurrentColor(CardColor.WILD);
            game.logSystemAction(LOGGER_ACTION_TYPE, "FIRST_CARD", "Mode: All Wild. Color set to WILD.");
            return;
        }

        boolean validCardFound = false;
        while (!validCardFound) {
            final Optional<Card> drawnOpt = deck.draw();

            if (drawnOpt.isEmpty()) {
                throw new IllegalStateException("Critical Error: Deck is empty during setup!");
            }

            final Optional<Card> drawnCard = drawnOpt;

            if (isValidStartingCard(drawnCard)) {
                discardPile.addCard(drawnCard.get());
                game.setCurrentColor(drawnCard.get().getColor(game));

                game.logSystemAction(LOGGER_ACTION_TYPE, "FIRST_CARD", "Card: " + drawnCard);
                validCardFound = true;
            } else {
                discardPile.addCard(drawnCard.get());
            }
        }
    }

    /**
     * Draws any card from the deck and places it on the discard pile.
     */
    private void drawAndPlaceAnyCard() {
        deck.draw().ifPresent(discardPile::addCard);
    }

    /**
     * Checks if the drawn card is a valid starting card.
     * 
     * @param cardOpt the drawn card.
     * @return true if valid, false otherwise.
     */
    private boolean isValidStartingCard(final Optional<Card> cardOpt) {
        if (cardOpt.isEmpty()) {
            return false;
        }
        final Card card = cardOpt.get();
        final CardValue v = card.getValue(game);

        return v != CardValue.WILD
                && v != CardValue.WILD_DRAW_FOUR
                && v != CardValue.WILD_DRAW_TWO
                && v != CardValue.DRAW_TWO
                && v != CardValue.DRAW_ONE
                && v != CardValue.REVERSE
                && v != CardValue.SKIP
                && v != CardValue.FLIP;
    }
}
