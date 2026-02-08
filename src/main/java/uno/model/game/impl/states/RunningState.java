package uno.model.game.impl.states;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.GameContext;
import uno.model.game.api.GameState;
import uno.model.game.impl.AbstractGameState;
import uno.model.players.api.AbstractPlayer;
import uno.model.game.impl.ScoreManagerImpl;
import uno.model.game.api.ScoreManager;

import java.util.Optional;

/**
 * State representing the main game loop where players can play cards or draw.
 */
public class RunningState extends AbstractGameState {

    public RunningState(GameContext game) {
        super(game);
    }

    @Override
    public GameState getEnum() {
        return GameState.RUNNING;
    }

    @Override
    public void playCard(Optional<Card> card) {
        // Validation logic logic moved here from GameImpl

        // 1. Check if it's a valid action in this state (Implicitly yes, since we are
        // in RunningState)

        final AbstractPlayer player = game.getCurrentPlayer();

        // New Rule: Skip After Draw
        if (game.getRules().isSkipAfterDrawEnabled() && game.hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("Regola: Skip After Draw. Hai pescato, quindi devi passare il turno.");
        }

        // 2. Controllo se il giocatore ha la carta
        if (!player.getHand().contains(card)) {
            throw new IllegalStateException("Il giocatore non ha questa carta!");
        }

        // 3. Controllo se la mossa è valida secondo le regole
        // Note: isValidMove is private in GameImpl. We will need to make it accessible
        // or move logic here.
        // For now, assume GameImpl will expose it or we call a package-private method.
        if (!game.isValidMove(card.get())) {
            throw new IllegalStateException("Mossa non valida! La carta " + card + " non può essere giocata.");
        }

        game.setCurrentPlayedCard(card.get());
        game.getLogger().logAction(player.getName(), "PLAY",
                card.getClass().getSimpleName(),
                card.get().getValue(game).toString());

        // --- FINE LOGICA DI VALIDAZIONE ---

        // Se la mossa è valida, aggiorna il currentColor.
        if (card.get().getColor(game) == CardColor.WILD) {
            game.setCurrentColorOptional(Optional.empty()); // Sarà impostato da onColorChosen()
        } else {
            // Se è una carta colorata, quello è il nuovo colore attivo.
            game.setCurrentColorOptional(Optional.of(card.get().getColor(game)));
        }

        // Esegui effetto carta (polimorfismo)
        if (card.get().getValue(game) == CardValue.WILD_FORCED_SWAP) {
            // Sposta la carta
            player.playCard(card);
            game.getDiscardPile().addCard(card.get());

            card.get().performEffect(game);
        } else {
            card.get().performEffect(game);

            // Sposta la carta
            player.playCard(card);
            game.getDiscardPile().addCard(card.get());
        }

        // --- CONTROLLO VITTORIA ---
        if (player.hasWon()) {
            final ScoreManager scoreManager = new ScoreManagerImpl();
            final int points = scoreManager.calculateRoundPoints(player, game.getPlayers(), game);
            player.addScore(points);

            String winType = "ROUND_WINNER";
            boolean scoringMode = game.getRules().isScoringModeEnabled();

            if (!scoringMode || player.getScore() >= 500) {
                winType = "MATCH_WINNER";
                game.setGameState(new GameOverState(game));
            } else {
                game.setGameState(new RoundOverState(game));
            }

            game.setWinner(player);
            game.getLogger().logAction("SYSTEM", "GAME_OVER", "N/A",
                    "Winner: " + player.getName() + " (" + winType + ") Points: " + points + " Total Score: "
                            + player.getScore());
            game.notifyObservers();
            return;
        }

        // --- LOGICA DI AVANZAMENTO TURNO ---
        // Se lo stato è cambiato (es. waiting for color), non avanzare qui.
        if (game.getGameState() == GameState.RUNNING) {
            game.getTurnManager().advanceTurn(game);
        }

        game.notifyObservers();
    }

    @Override
    public void playerInitiatesDraw() {
        final AbstractPlayer player = game.getCurrentPlayer();

        // 1. Regola: "Massimo una carta"
        if (game.hasCurrentPlayerDrawn(player)) {
            throw new IllegalStateException("Hai già pescato in questo turno. Devi giocare la carta o passare.");
        }

        // 2. Regola: "Non se hai carte da giocare"
        if (game.playerHasPlayableCard(player)) {
            throw new IllegalStateException("Mossa non valida! Hai una carta giocabile, non puoi pescare.");
        }

        // Ok, il giocatore deve pescare
        game.getTurnManager().setHasDrawnThisTurn(true);

        game.drawCardForPlayer(player);

        game.notifyObservers();
    }

    @Override
    public void playerPassTurn() {
        // Puoi passare solo se hai pescato (perché non avevi mosse)
        // Oppure se hai pescato e la regola "Skip After Draw" è attiva.
        if (!game.hasCurrentPlayerDrawn(game.getCurrentPlayer())) {
            // Potresti avere una mossa, quindi non puoi passare
            if (game.playerHasPlayableCard(game.getCurrentPlayer())) {
                throw new IllegalStateException("Non puoi passare, hai una mossa valida.");
            } else {
                throw new IllegalStateException("Non puoi passare, devi prima pescare una carta.");
            }
        }

        final AbstractPlayer currentPlayer = game.getCurrentPlayer();
        final String handSize = String.valueOf(currentPlayer.getHand().size());

        game.getLogger().logAction(currentPlayer.getName(), "PASS_TURN", "N/A", "HandSize: " + handSize);

        game.getTurnManager().advanceTurn(game);
        game.notifyObservers();
    }
}
