package uno.model.game.impl.states;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.game.impl.ScoreManagerImpl;
import uno.model.players.impl.AbstractPlayer;
import uno.model.game.api.ScoreManager;

import java.util.Optional;

/**
 * State representing the main game loop where players can play cards or draw.
 */
public class RunningState extends AbstractGameState {

    private static final int FINAL_SCORE = 500;

    /**
     * Constructor for RunningState.
     * 
     * @param game The game context to which this state belongs.
     */
    public RunningState(final GameContext game) {
        super(game);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameState getEnum() {
        return GameState.RUNNING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playCard(final Optional<Card> card) {
        final AbstractPlayer player = this.getGame().getCurrentPlayer();

        if (this.getGame().getRules().isSkipAfterDrawEnabled() && this.getGame().hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("RULE: Skip After Draw. You drawn a card, so you must pass your turn.");
        }

        if (!player.getHand().contains(card)) {
            throw new IllegalStateException("Player " + player.getName() + " does not have the card");
        }

        if (!this.getGame().isValidMove(card.get())) {
            throw new IllegalStateException("Move not valid! The card " + card + " cannot be played.");
        }

        this.getGame().setCurrentPlayedCard(card.get());
        this.getGame().getLogger().logAction(player.getName(), "PLAY",
                card.getClass().getSimpleName(),
                card.get().getValue(this.getGame()).toString());

        if (card.get().getColor(this.getGame()) == CardColor.WILD) {
            this.getGame().setCurrentColorOptional(Optional.empty());
        } else {
            this.getGame().setCurrentColorOptional(Optional.of(card.get().getColor(this.getGame())));
        }

        if (card.get().getValue(this.getGame()) == CardValue.WILD_FORCED_SWAP) {
            player.playCard(card);
            this.getGame().getDiscardPile().addCard(card.get());
            card.get().performEffect(this.getGame());
        } else {
            card.get().performEffect(this.getGame());
            player.playCard(card);
            this.getGame().getDiscardPile().addCard(card.get());
        }

        if (player.hasWon()) {
            final ScoreManager scoreManager = new ScoreManagerImpl();
            final int points = scoreManager.calculateRoundPoints(player, this.getGame().getPlayers(), this.getGame());
            player.addScore(points);

            String winType = "ROUND_WINNER";
            final boolean scoringMode = this.getGame().getRules().isScoringModeEnabled();

            if (!scoringMode || player.getScore() >= FINAL_SCORE) {
                winType = "MATCH_WINNER";
                this.getGame().setGameState(new GameOverState(this.getGame()));
            } else {
                this.getGame().setGameState(new RoundOverState(this.getGame()));
            }

            this.getGame().setWinner(player);
            this.getGame().getLogger().logAction("SYSTEM", "GAME_OVER", "N/A",
                    "Winner: " + player.getName() + " (" + winType + ") Points: " + points + " Total Score: "
                            + player.getScore());
            this.getGame().notifyObservers();
            return;
        }

        if (this.getGame().getGameState() == GameState.RUNNING) {
            this.getGame().getTurnManager().advanceTurn(this.getGame());
        }

        this.getGame().notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerInitiatesDraw() {
        final AbstractPlayer player = this.getGame().getCurrentPlayer();

        if (this.getGame().hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("You have already drawn in this turn. You must play the card or pass.");
        }

        if (this.getGame().playerHasPlayableCard(player)) {
            throw new IllegalStateException("Move not valid! You have a playable card, you cannot draw.");
        }

        this.getGame().getTurnManager().setHasDrawnThisTurn(true);

        this.getGame().drawCardForPlayer(player);

        this.getGame().notifyObservers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void playerPassTurn() {
        if (!this.getGame().hasCurrentPlayerDrawn(this.getGame().getCurrentPlayer())) {
            if (this.getGame().playerHasPlayableCard(this.getGame().getCurrentPlayer())) {
                throw new IllegalStateException("You cannot pass, you have a playable card.");
            } else {
                throw new IllegalStateException("You cannot pass, you must draw a card first.");
            }
        }

        final AbstractPlayer currentPlayer = this.getGame().getCurrentPlayer();
        final String handSize = String.valueOf(currentPlayer.getHand().size());

        this.getGame().getLogger().logAction(currentPlayer.getName(), "PASS_TURN", "N/A", "HandSize: " + handSize);
        this.getGame().getTurnManager().advanceTurn(this.getGame());
        this.getGame().notifyObservers();
    }
}
