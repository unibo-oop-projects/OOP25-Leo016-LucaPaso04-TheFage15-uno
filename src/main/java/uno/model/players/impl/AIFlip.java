package uno.model.players.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.EnumMap;
import java.util.ArrayList;

/**
 * AI implementation specifically for UNO Flip.
 * It understands the dual-sided nature of the game and adjusts its strategy
 * based on whether the game is currently on the Light Side or Dark Side.
 */
public class AIFlip extends AIPlayer {

    private static final int SCORE_FIVE = 5;
    private static final int SCORE_TEN = 10;
    private static final int SCORE_FOURTY = 40;
    private static final int SCORE_FIFTY = 50;
    private static final int SCORE_EIGHTY_FIVE = 85;
    private static final int SCORE_NINETY = 90;
    private static final int SCORE_ONE_HUNDRED = 100;

    /**
     * Constructor for AIFlip player.
     * @param name the name of the player
     */
    public AIFlip(final String name) {
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

        final Optional<Card> flipCard = playableCards.stream()
                .filter(c -> c.getValue(game) == CardValue.FLIP)
                .findFirst();

        if (flipCard.isPresent()) {
            return flipCard;
        }

        if (game.isDarkSide()) {
            final Optional<Card> powerCard = playableCards.stream()
                    .filter(c -> c.getValue(game) == CardValue.DRAW_FIVE 
                    || c.getValue(game) == CardValue.WILD_DRAW_COLOR)
                    .findFirst();
            if (powerCard.isPresent()) {
                return powerCard;
            }
        }

        playableCards.sort((c1, c2) -> getCardScore(c2, game) - getCardScore(c1, game));

        return Optional.of(playableCards.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardColor chooseBestColor(final Game game) {
        final CardColor[] validColors;

        if (game.isDarkSide()) {
            validColors = new CardColor[] { CardColor.TEAL, CardColor.PINK, CardColor.PURPLE, CardColor.ORANGE };
        } else {
            validColors = new CardColor[] { CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW };
        }

        final Map<CardColor, Integer> colorCounts = new EnumMap<>(CardColor.class);
        for (final CardColor c : validColors) {
            colorCounts.put(c, 0);
        }

        for (final Optional<Card> cardOpt : this.getHand()) {
            if (cardOpt.isPresent()) {
                final CardColor c = cardOpt.get().getColor(game);
                if (colorCounts.containsKey(c)) {
                    colorCounts.put(c, colorCounts.get(c) + 1);
                }
            }
        }

        return colorCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(validColors[0]);
    }

    /**
     * Get all playable cards from hand based on current game state.
     * @param game the current game state
     * @return list of playable cards
     */
    private List<Card> getPlayableCards(final Game game) {
        final List<Card> list = new ArrayList<>();
        for (final Optional<Card> opt : getHand()) {
            if (opt.isPresent() && isMoveValid(opt.get(), game)) {
                list.add(opt.get());
            }
        }
        return list;
    }

    /**
     * Calculate a score for a card to determine its priority.
     * Higher score means higher priority to play.
     * @param c the card to score
     * @param game the current game state
     * @return the score of the card
     */
    private int getCardScore(final Card c, final Game game) {
        final CardValue v = c.getValue(game);

        if (v == CardValue.FLIP) {
            return SCORE_ONE_HUNDRED;
        }

        if (v == CardValue.WILD_DRAW_COLOR) {
            return SCORE_NINETY;
        }

        if (v == CardValue.DRAW_FIVE) {
            return SCORE_EIGHTY_FIVE;
        }

        if (v == CardValue.SKIP || v == CardValue.REVERSE || v == CardValue.SKIP_EVERYONE) {
            return SCORE_FIFTY;
        }

        if (v == CardValue.DRAW_TWO || v == CardValue.DRAW_ONE) {
            return SCORE_FOURTY;
        }

        if (v == CardValue.WILD) {
            return SCORE_FIVE;
        }

        return SCORE_TEN; 
    }
}
