package uno.model.players.impl;

import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.players.api.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class representing a generic player in the UNO game.
 * Manages the hand state and basic player properties.
 */
public abstract class AbstractPlayer implements Player {

    private final String name;
    private final List<Optional<Card>> hand;
    private boolean hasCalledUno;
    private int score;

    /**
     * Constructor to initialize a player with a name.
     * 
     * @param name The name of the player.
     */
    public AbstractPlayer(final String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.hasCalledUno = false;
        this.score = 0;
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
    public void setScore(final int score) {
        this.score = score;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addScore(final int points) {
        this.score += points;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public abstract void takeTurn(Game game);

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
    public List<Optional<Card>> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getHandSize() {
        return hand.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHand(final List<Optional<Card>> newHand) {
        this.hand.clear();
        this.hand.addAll(newHand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCardToHand(final Card card) {
        this.hand.add(Optional.of(card));
        if (hand.size() > 1) {
            this.hasCalledUno = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean playCard(final Optional<Card> card) {
        return !card.isEmpty() && this.hand.remove(card);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasWon() {
        return hand.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unoPenalty(final Game game) {
        game.drawCardForPlayer(this);
        game.drawCardForPlayer(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hasCalledUno() {
        this.hasCalledUno = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isHasCalledUno() {
        return this.hasCalledUno;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resetUnoStatus() {
        this.hasCalledUno = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHasCalledUno(final boolean status) {
        this.hasCalledUno = status;
    }
}
