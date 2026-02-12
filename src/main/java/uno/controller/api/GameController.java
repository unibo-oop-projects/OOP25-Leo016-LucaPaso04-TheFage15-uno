package uno.controller.api;

import uno.model.api.GameModelObserver;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.impl.AbstractPlayer;
import uno.view.api.GameViewObserver;

import java.util.Optional;

/**
 * Interface defining the operations for controlling a Uno! game session.
 * It extends GameViewObserver to receive input from the View and
 * GameModelObserver to react to Model changes.
 */
public interface GameController extends GameViewObserver, GameModelObserver {

    /**
     * Initializes the game sequence by displaying the starting player
     * and triggering the core game logic.
     */
    void showStartingPlayerPopupAndStartGame();

    /**
     * {@inheritDoc}
     */
    @Override
    void onPlayCard(Optional<Card> card);

    /**
     * {@inheritDoc}
     */
    @Override
    void onDrawCard();

    /**
     * {@inheritDoc}
     */
    @Override
    void onCallUno();

    /**
     * {@inheritDoc}
     */
    @Override
    void onPassTurn();

    /**
     * {@inheritDoc}
     */
    @Override
    void onColorChosen(CardColor color);

    /**
     * {@inheritDoc}
     */
    @Override
    void onPlayerChosen(AbstractPlayer player);

    /**
     * {@inheritDoc}
     */
    @Override
    void onBackToMenu();
}
