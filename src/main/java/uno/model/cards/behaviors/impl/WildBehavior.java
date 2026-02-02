package uno.model.cards.behaviors.impl;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.game.api.Game;
/**
 * Implementation of {@link CardSideBehavior} for Wild cards.
 * This class handles all "Jolly" type cards, including Standard Wilds,
 * Uno Flip Wilds, and "Uno All Wild" variants.
 * It uses boolean flags to determine specific interactions (drawing, targeting, skipping).
 */
public class WildBehavior implements CardSideBehavior {

    private final CardValue value;
    private final int drawAmount;
    private final boolean requiresColorChoice;
    private final boolean requiresTargetPlayer;
    private final int skipAmount;
    private final boolean reversesGame;

    /**
     * Master Constructor.
     * Allows full configuration of a Wild Card's behavior.
     * @param value                The card value (name).
     * @param drawAmount           How many cards the next player draws (0 if none).
     * @param requiresColorChoice  True if the player must choose a color (Standard Wilds).
     * @param requiresTargetPlayer True if the player must choose an opponent (Swap, Target Draw).
     * @param skipAmount           How many players to skip (usually 0 or 1, sometimes 2).
     * @param reversesGame         True if the card changes turn direction (Reverse).
     */
    public WildBehavior(final CardValue value, 
                        final int drawAmount, 
                        final boolean requiresColorChoice, 
                        final boolean requiresTargetPlayer,
                        final int skipAmount,
                        final boolean reversesGame) {
        this.value = value;
        this.drawAmount = drawAmount;
        this.requiresColorChoice = requiresColorChoice;
        this.requiresTargetPlayer = requiresTargetPlayer;
        this.skipAmount = skipAmount;
        this.reversesGame = reversesGame;
    }

    // --- CONSTRUCTOR OVERLOADS (Per facilitare la creazione nella Factory) ---

    /**
     * Constructor for Standard Wilds (Wild, Wild Draw 4).
     * Automatically assumes color choice is needed and no specific target player.
     * @param value      The card value (name).
     * @param drawAmount How many cards the next player draws (0 if none).
     */
    public WildBehavior(final CardValue value, final int drawAmount) {
        this(value, drawAmount, true, false, (drawAmount >= 4 ? 1 : 0), false);
    }

    /**
     * Constructor for "All Wild" variants or special cards.
     * Allows defining if color choice is needed explicitly.
     * @param value                The card value (name).
     * @param drawAmount           How many cards the next player draws (0 if none).
     * @param requiresColorChoice  True if the player must choose a color.
     */
    public WildBehavior(final CardValue value, final int drawAmount, final boolean requiresColorChoice) {
        this(value, drawAmount, requiresColorChoice, false, (drawAmount > 0 ? 1 : 0), false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeEffect(final Game game) {
        // 1. Draw Logic
        if (drawAmount > 0) {
            game.makeNextPlayerDraw(drawAmount);
        }

        // 2. Color Selection (Standard Wilds)
        if (requiresColorChoice) {
            game.requestColorChoice();
        }

        // 3. Target Selection (Swap Hands, Targeted Draw)
        if (requiresTargetPlayer) {
            game.requestPlayerChoice();
        }

        // 4. Flow Control (Skip)
        if (skipAmount > 0) {
            game.skipPlayers(skipAmount);
        }

        // 5. Flow Control (Reverse)
        if (reversesGame) {
            game.reversePlayOrder();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardColor getColor() {
        return CardColor.WILD;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardValue getValue() {
        return value;
    }

    /**
     * Returns the number of cards this Wild card forces the next player to draw.
     * @return The draw amount.
     */
    public int getDrawAmount() { 
        return drawAmount; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isWild() { 
        return true; 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return String.format("WILD %s (Draw: %d, Skip: %d)", value, drawAmount, skipAmount);
    }
}
