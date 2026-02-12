package uno.view.impl;

import uno.model.players.impl.AbstractPlayer;
import uno.view.api.CardViewData;
import uno.view.api.PlayerViewData;

import java.util.List;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.Collections;

/**
 * DTO representing a Player for the View.
 */
public class PlayerViewDataImpl implements PlayerViewData {
    private final String name;
    private final int handSize;
    private final int score;
    private final boolean isCurrentPlayer;
    private final List<CardViewData> hand;
    private final AbstractPlayer modelPlayer;

    /**
     * Constructor for PlayerViewDataImpl.
     * 
     * @param name The player's name.
     * @param handSize The number of cards in the player's hand.
     * @param score The player's current score.
     * @param isCurrentPlayer Whether this player is the current player.
     * @param hand The player's hand as a list of CardViewData.
     * @param modelPlayer The underlying model player object.
     */
    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public PlayerViewDataImpl(final String name, final int handSize, final int score, final boolean isCurrentPlayer,
            final List<CardViewData> hand, final AbstractPlayer modelPlayer) {
        this.name = name;
        this.handSize = handSize;
        this.score = score;
        this.isCurrentPlayer = isCurrentPlayer;
        this.hand = hand != null ? hand : Collections.emptyList();
        this.modelPlayer = modelPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHandSize() {
        return handSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getScore() {
        return score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCurrentPlayer() {
        return isCurrentPlayer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public List<CardViewData> getHand() {
        return hand;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressFBWarnings("EI_EXPOSE_REP")
    public AbstractPlayer getModelPlayer() {
        return modelPlayer;
    }
}
