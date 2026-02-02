package uno.model.game.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.types.api.Card;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.GameSetup;
import uno.model.players.api.Player;
import uno.model.game.api.Game;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of the {@link GameSetup} interface.
 */
public class GameSetupImpl implements GameSetup {

    /** The size of the initial hand dealt to each player. */
    public static final int INITIAL_HAND_SIZE = 7;

    private final Game game;
    private final Deck<Card> deck;
    private final DiscardPile discardPile;
    private final List<Player> players;

    /**
     * Constructor for GameSetupImpl.
     * @param game game instance
     * @param deck deck of cards
     * @param discardPile discard pile
     * @param players list of players
     */
    public GameSetupImpl(final Game game, final Deck<Card> deck, final DiscardPile discardPile, final List<Player> players) {
        this.game = game;
        this.deck = deck;
        this.discardPile = discardPile;
        this.players = players;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeGame(final boolean isAllWild) {
        System.out.println("Starting Game Setup...");

        // 1. Deal cards
        dealInitialCards();

        // 2. Setup Discard Pile
        setupFirstCard(isAllWild);
    }

    /**
     * Deals initial cards to all players.
     */
    private void dealInitialCards() {
        for (int i = 0; i < INITIAL_HAND_SIZE; i++) {
            for (final Player player : this.players) {
                // We assume Game has a method to orchestrate the draw, 
                // or we can do it directly here:
                deck.draw().ifPresent(player::addCardToHand);
            }
        }
        game.logSystemAction("SETUP", "DEAL_CARDS", "Dealt 7 cards to " + players.size() + " players.");
    }

    /**
     * Sets up the first card on the discard pile.
     * @param isAllWild indicates if the game is in All Wild mode
     */
    private void setupFirstCard(final boolean isAllWild) {
        // CASE A: ALL WILD MODE
        // In All Wild, colors don't matter, and usually, any card can start.
        if (isAllWild) {
            drawAndPlaceAnyCard();
            game.setCurrentColor(CardColor.WILD);
            game.logSystemAction("SETUP", "FIRST_CARD", "Mode: All Wild. Color set to WILD.");
            return;
        }

        // CASE B: STANDARD / FLIP MODE
        // We must ensure the first card is a valid starter (usually a Number card).
        boolean validCardFound = false;

        while (!validCardFound) {
            final Optional<Card> drawnOpt = deck.draw();

            if (drawnOpt.isEmpty()) {
                throw new IllegalStateException("Critical Error: Deck is empty during setup!");
            }

            final Optional<Card> drawnCard = drawnOpt;

            if (isValidStartingCard(drawnCard)) {
                // Success: Valid card found
                discardPile.addCard(drawnCard.get());
                game.setCurrentColor(drawnCard.get().getColor(game));

                System.out.println("First Valid Card: " + drawnCard);
                game.logSystemAction("SETUP", "FIRST_CARD", "Card: " + drawnCard);
                validCardFound = true;
            } else {
                // Fail: Invalid card (Wild, Action, Flip), put in discard and draw again
                System.out.println("Invalid start card skipped: " + drawnCard);
                discardPile.addCard(drawnCard.get()); 
                // Note: Standard rules say "put back in deck", but putting in discard is a common variant
                // to avoid infinite reshuffling loops.
            }
        }
    }

    /**
     * Draws any card from the deck and places it on the discard pile.
     */
    private void drawAndPlaceAnyCard() {
        deck.draw().ifPresent(card -> discardPile.addCard(card));
    }

    /**
     * Checks if the drawn card is a valid starting card.
     * @param cardOpt the drawn card
     * @return true if valid, false otherwise
     */
    private boolean isValidStartingCard(final Optional<Card> cardOpt) {
        if (cardOpt.isEmpty()) {
            return false;
        }
        final Card card = cardOpt.get();
        final CardValue v = card.getValue(game);

        // List of prohibited starting cards
        return v != CardValue.WILD
                && v != CardValue.WILD_DRAW_FOUR 
                && v != CardValue.WILD_DRAW_COLOR
                && v != CardValue.DRAW_TWO 
                && v != CardValue.REVERSE 
                && v != CardValue.SKIP 
                && v != CardValue.FLIP;
    }
}
