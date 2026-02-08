package uno.model.cards.attributes;

/**
 * Enum representing the possible values of UNO cards.
 */
public enum CardValue {
    /**
     * Value representing the number zero.
     */
    ZERO(0),
    /**
     * Value representing the number one.
     */
    ONE(1),
    /**
     * Value representing the number two.
     */
    TWO(2),
    /**
     * Value representing the number three.
     */
    THREE(3),
    /**
     * Value representing the number four.
     */
    FOUR(4),
    /**
     * Value representing the number five.
     */
    FIVE(5),
    /**
     * Value representing the number six.
     */
    SIX(6),
    /**
     * Value representing the number seven.
     */
    SEVEN(7),
    /**
     * Value representing the number eight.
     */
    EIGHT(8),
    /**
     * Value representing the number nine.
     */
    NINE(9),
    /**
     * Value representing the card skip.
     */
    SKIP(20),
    /**
     * Value representing the card reverse.
     */
    REVERSE(20),
    /**
     * Value representing the card draw two.
     */
    DRAW_TWO(20),
    /**
     * Value representing the card wild.
     */
    WILD(50),
    /**
     * Value representing the card wild draw four.
     */
    WILD_DRAW_FOUR(50),
    /**
     * Value representing the card flip.
     */
    FLIP(20),
    /**
     * Value representing the card wild (dark side).
     */
    WILD_DARK(50),
    /**
     * Value representing the card wild draw two.
     */
    WILD_DRAW_TWO(50),
    /**
     * Value representing the card draw five.
     */
    DRAW_FIVE(20),
    /**
     * Value representing the card draw one.
     */
    DRAW_ONE(10),
    /**
     * Value representing the card skip everyone.
     */
    SKIP_EVERYONE(30),
    /**
     * Value representing the card wild draw color.
     */
    WILD_DRAW_COLOR(60),
    /**
     * Value representing the card wild (all wild).
     */
    WILD_ALLWILD(50),
    /**
     * Value representing the card wild draw four (all wild).
     */
    WILD_DRAW_FOUR_ALLWILD(50),
    /**
     * Value representing the card wild draw two (all wild).
     */
    WILD_DRAW_TWO_ALLWILD(50),
    /**
     * Value representing the card targeted draw two.
     */
    WILD_TARGETED_DRAW_TWO(50),
    /**
     * Value representing the card forced swap.
     */
    WILD_FORCED_SWAP(50),
    /**
     * Value representing the card wild reverse.
     */
    WILD_REVERSE(50),
    /**
     * Value representing the card wild skip.
     */
    WILD_SKIP(50),
    /**
     * Value representing the card wild skip two.
     */
    WILD_SKIP_TWO(50);

    private final int pointValue;

    CardValue(final int pointValue) {
        this.pointValue = pointValue;
    }

    /**
     * Gets the point value of the card.
     * 
     * @return The point value.
     */
    public int getPointValue() {
        return pointValue;
    }
}
