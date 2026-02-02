package uno.model.players.impl;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;

/**
 * AI player implementation for classic UNO strategy.
 */
public class AIClassic extends AIPlayer {

    private static final int NUMBER_ONE = 1;
    private static final int NUMBER_TWO = 1;
    private static final int NUMBER_THREE = 1;
    private static final int NUMBER_FOUR = 1;
    private static final int NUMBER_FIVE = 1;
    private static final int NUMBER_SIX = 1;
    private static final int NUMBER_SEVEN = 1;
    private static final int NUMBER_EIGHT = 1;
    private static final int NUMBER_NINE = 1;

    /**
     * Constructor for AIClassic.
     * @param name The name of the AI player.
     */
    public AIClassic(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Card> chooseCardToPlay(final Game game) {
        final List<Card> playableCards = getPlayableCards(game);

        if (playableCards.isEmpty()) {
            return Optional.empty();
        }

        final boolean opponentHasUno = hasOpponentWithUno(game);
        final boolean iAmClose = this.getHandSize() <= 3;

        final List<Card> actionCards = new ArrayList<>();
        final List<Card> numberCards = new ArrayList<>();
        final List<Card> wildCards = new ArrayList<>();

        for (final Card card : playableCards) {
            final CardValue value = card.getValue(game);
            if (value == CardValue.WILD || value == CardValue.WILD_DRAW_FOUR 
                || value == CardValue.WILD_DRAW_COLOR || value == CardValue.WILD_FORCED_SWAP 
                || value == CardValue.WILD_TARGETED_DRAW_TWO) {
                wildCards.add(card);
            } else if (isActionCard(value)) {
                actionCards.add(card);
            } else {
                numberCards.add(card);
            }
        }

        if (opponentHasUno) {
            final Card offensiveCard = findBestOffensiveCard(actionCards, wildCards, game);
            if (offensiveCard != null) {
                return Optional.of(offensiveCard);
            }
        }

        if (iAmClose) {
            if (!actionCards.isEmpty()) {
                actionCards.sort((c1, c2) -> getActionCardPriority(c2.getValue(game)) 
                - getActionCardPriority(c1.getValue(game)));
                return Optional.of(actionCards.get(0));
            }
            if (!numberCards.isEmpty()) {
                numberCards.sort((c1, c2) -> getNumericValue(c2, game) - getNumericValue(c1, game));
                return Optional.of(numberCards.get(0));
            }
        }

        if (!actionCards.isEmpty()) {
            actionCards.sort((c1, c2) -> getActionCardPriority(c2.getValue(game)) 
            - getActionCardPriority(c1.getValue(game)));
            return Optional.of(actionCards.get(0));
        }

        if (!numberCards.isEmpty()) {
            numberCards.sort((c1, c2) -> getNumericValue(c2, game) - getNumericValue(c1, game));
            return Optional.of(numberCards.get(0));
        }

        if (!wildCards.isEmpty()) {
            wildCards.sort((c1, c2) -> {
                final boolean is1DrawFour = c1.getValue(game) == CardValue.WILD_DRAW_FOUR;
                final boolean is2DrawFour = c2.getValue(game) == CardValue.WILD_DRAW_FOUR;
                if (is1DrawFour && !is2DrawFour) {
                    return 1;
                }
                if (!is1DrawFour && is2DrawFour) {
                    return -1;
                }
                return 0;
            });
            return Optional.of(wildCards.get(0));
        }

        return Optional.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardColor chooseBestColor(final Game game) {
        final Map<CardColor, Integer> colorScores = new EnumMap<>(CardColor.class);

        colorScores.put(CardColor.RED, 0);
        colorScores.put(CardColor.GREEN, 0);
        colorScores.put(CardColor.BLUE, 0);
        colorScores.put(CardColor.YELLOW, 0);

        for (final Optional<Card> cardOpt : this.getHand()) {
            if (cardOpt.isPresent()) {
                final Card card = cardOpt.get();
                final CardColor color = card.getColor(game);

                if (colorScores.containsKey(color)) {
                    int score = 1;

                    if (isActionCard(card.getValue(game))) {
                        score = 3;
                    }

                    colorScores.put(color, colorScores.get(color) + score);
                }
            }
        }

        CardColor bestColor = CardColor.RED;
        int maxScore = -1;

        for (final Map.Entry<CardColor, Integer> entry : colorScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestColor = entry.getKey();
            }
        }

        return bestColor;
    }

    /**
     * Find the best offensive card to play when an opponent has UNO.
     * @param actionCards available action cards
     * @param wildCards  available wild cards
     * @param game The current game instance.
     * @return The best offensive card, or null if none found.
     */
    private Card findBestOffensiveCard(final List<Card> actionCards, final List<Card> wildCards, final Game game) {

        for (final Card card : wildCards) {
            if (card.getValue(game) == CardValue.WILD_DRAW_FOUR) {
                return card;
            }
        }


        for (final Card card : actionCards) {
            if (card.getValue(game) == CardValue.DRAW_TWO) {
                return card;
            }
        }

        for (final Card card : actionCards) {
            if (card.getValue(game) == CardValue.SKIP) {
                return card;
            }
        }

        if (!actionCards.isEmpty()) {
            return actionCards.get(0);
        }

        return null;
    }

    /**
     * Check if any opponent has only one card left (UNO).
     * @param game The current game instance.
     * @return True if any opponent has UNO, false otherwise.
     */
    private boolean hasOpponentWithUno(final Game game) {
        for (final var player : game.getPlayers()) {
            if (player != this && player.getHandSize() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all playable cards from hand.
     * @param game The current game instance.
     * @return List of playable cards.
     */
    private List<Card> getPlayableCards(final Game game) {
        final List<Card> playable = new ArrayList<>();
        for (final Optional<Card> cardOpt : this.getHand()) {
            if (cardOpt.isPresent() && isMoveValid(cardOpt.get(), game)) {
                playable.add(cardOpt.get());
            }
        }
        return playable;
    }

    /**
     * Check if a card is an action card.
     * @param value The card value.
     * @return True if it's an action card, false otherwise.
     */
    private boolean isActionCard(final CardValue value) {
        return value == CardValue.SKIP 
                || value == CardValue.REVERSE 
                || value == CardValue.DRAW_TWO 
                || value == CardValue.FLIP 
                || value == CardValue.SKIP_EVERYONE;
    }

    /**
     * Get priority of action cards for decision making.
     * @param value The card value.
     * @return The priority score.
     */
    private int getActionCardPriority(final CardValue value) {
        switch (value) {
            case DRAW_TWO: return NUMBER_FIVE;
            case SKIP: return NUMBER_FOUR;
            case SKIP_EVERYONE: return NUMBER_FOUR;
            case REVERSE: return NUMBER_THREE;
            case FLIP: return NUMBER_TWO;
            default: return 0;
        }
    }

    /**
     * Get numeric value of a card for comparison.
     * @param card The card to evaluate.
     * @param game The current game instance.
     * @return The numeric value of the card.
     */
    private int getNumericValue(final Card card, final Game game) {
        final CardValue value = card.getValue(game);
        switch (value) {
            case ZERO: return 0;
            case ONE: return NUMBER_ONE;
            case TWO: return NUMBER_TWO;
            case THREE: return NUMBER_THREE;
            case FOUR: return NUMBER_FOUR;
            case FIVE: return NUMBER_FIVE;
            case SIX: return NUMBER_SIX;
            case SEVEN: return NUMBER_SEVEN;
            case EIGHT: return NUMBER_EIGHT;
            case NINE: return NUMBER_NINE;
            default: return 0;
        }
    }
}
