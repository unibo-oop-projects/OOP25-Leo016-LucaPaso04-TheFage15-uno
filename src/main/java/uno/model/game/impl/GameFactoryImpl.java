package uno.model.game.impl;

import uno.model.game.api.GameFactory;
import uno.model.game.api.Game;
import uno.model.game.api.GameRules;
import uno.model.game.api.DiscardPile;
import uno.model.game.api.TurnManager;
import uno.model.players.impl.AbstractPlayer;
import uno.model.game.api.GameMode;
import uno.model.cards.deck.api.Deck;
import uno.model.cards.deck.impl.StandardDeck;
import uno.model.cards.deck.impl.FlipDeck;
import uno.model.cards.deck.impl.AllWildDeck;
import uno.model.cards.types.api.Card;
import uno.model.utils.api.GameLogger;
import uno.model.utils.impl.GameLoggerImpl;
import uno.model.game.api.GameSetup;

import java.util.List;

/**
 * Implementation of the GameFactory.
 */
public class GameFactoryImpl implements GameFactory {

    private final GameRules rules;
    private final GameLogger logger;

    /**
     * Constructor for GameFactoryImpl.
     * 
     * @param rules rules of the game.
     */
    public GameFactoryImpl(final GameRules rules) {
        this.rules = rules;
        this.logger = new GameLoggerImpl(String.valueOf(System.currentTimeMillis()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Game createGame(final String playerName, final GameMode gameMode, final List<AbstractPlayer> players) {
        final Deck<Card> deck;
        boolean isAllWild = false;

        switch (gameMode) {
            case FLIP:
                deck = new FlipDeck(logger);
                break;
            case ALL_WILD:
                deck = new AllWildDeck(logger);
                isAllWild = true;
                break;
            default:
                deck = new StandardDeck(logger);
                break;
        }

        final DiscardPile discardPile = new DiscardPileImpl();
        final TurnManager turnManager = new TurnManagerImpl(players, rules);
        final Game game = new GameImpl(deck, players, turnManager, discardPile, gameMode.name(), logger, rules);
        final GameSetup setup = new GameSetupImpl(game, deck, game.getDiscardPile(), players);
        setup.initializeGame(isAllWild);

        return game;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GameLogger getLogger() {
        return logger;
    }
}
