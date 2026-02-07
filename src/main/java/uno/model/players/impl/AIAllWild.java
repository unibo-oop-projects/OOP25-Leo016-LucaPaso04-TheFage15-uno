package uno.model.players.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.types.api.Card;
import uno.model.game.api.Game;
import uno.model.game.api.GameState;
import uno.model.players.api.AbstractPlayer;

/**
 * AI implementation for UNO All Wild.
 * Focuses on aggressive targeting and strategic use of "Forced Swap".
 */
public class AIAllWild extends AbstractAIPlayer {

    /**
     * Constructor for AIAllWild.
     * 
     * @param name The name of the player.
     */
    public AIAllWild(final String name) {
        super(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void takeTurn(final Game game) {
        // 1. Esegui il flusso standard (Scegli carta -> Gioca -> Scegli Colore -> Pesca
        // se serve)
        super.takeTurn(game);

        // 2. Controllo Extra: Se il gioco aspetta un giocatore (es. per Swap o Targeted
        // Draw)
        if (game.getGameState() == GameState.WAITING_FOR_PLAYER) {
            findBestTarget(game).ifPresent(target -> {
                game.chosenPlayer(target);
                game.aiAdvanceTurn();
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Optional<Card> chooseCardToPlay(final Game game) {
        final List<Card> hand = getPlayableCards(game);

        if (hand.isEmpty()) {
            return Optional.empty();
        }

        // Analisi bersaglio migliore (chi ha meno carte)
        final Optional<AbstractPlayer> bestTargetOpt = findBestTarget(game);

        // --- 1. LOGICA SWAP (Scambio Forzato) ---
        // Cerchiamo se abbiamo la carta scambio
        final Optional<Card> swapCard = hand.stream()
                .filter(c -> c.getValue(game) == CardValue.WILD_FORCED_SWAP)
                .findFirst();

        if (swapCard.isPresent() && bestTargetOpt.isPresent()) {
            // CONVIENE SCAMBIARE?
            // Sì, se io ho PIÙ carte del bersaglio (gli rifilo il mio mazzo grosso)
            if (this.getHandSize() > bestTargetOpt.get().getHandSize()) {
                return swapCard;
            }
        }

        // --- 2. LOGICA ATTACCO (Priorità alle carte cattive) ---
        // Cerchiamo Targeted Draw 2, Draw 4, Skip Two, Draw Two
        final Optional<Card> attackCard = hand.stream()
                .filter(c -> isAggressiveCard(c.getValue(game)))
                .findFirst();

        if (attackCard.isPresent()) {
            return attackCard;
        }

        // --- 3. LOGICA DI SCARTO SICURO ---
        // Se non attacco e non scambio vantaggiosamente, gioca una carta qualsiasi.
        // MA: Evita di giocare lo Swap se mi danneggerebbe (ho meno carte del target).
        for (final Card card : hand) {
            boolean isBadSwap = false;
            // Se è uno swap e abbiamo un target, controlliamo se ci conviene
            if (card.getValue(game) == CardValue.WILD_FORCED_SWAP && bestTargetOpt.isPresent()) {
                if (this.getHandSize() < bestTargetOpt.get().getHandSize()) {
                    isBadSwap = true;
                }
            }
            // Se non è uno swap svantaggioso, giocala
            if (!isBadSwap) {
                return Optional.of(card);
            }
        }

        // Se sono arrivato qui, ho solo carte Swap svantaggiose. Devo giocarne una per
        // forza.
        return Optional.of(hand.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected CardColor chooseBestColor(final Game game) {
        return CardColor.WILD;
    }

    // --- HELPER PRIVATI ---

    /**
     * Find the best target player (the one with the fewest cards).
     * 
     * @param game Current game instance.
     * @return The best target player.
     */
    private Optional<AbstractPlayer> findBestTarget(final Game game) {
        return game.getPlayers().stream()
                .filter(p -> !p.equals(this))
                .min(Comparator.comparingInt(AbstractPlayer::getHandSize));
    }

    /**
     * Check if the card is an aggressive type.
     * 
     * @param val Card value to check.
     * @return true if aggressive.
     */
    private boolean isAggressiveCard(final CardValue val) {
        return val == CardValue.WILD_TARGETED_DRAW_TWO
                || val == CardValue.WILD_DRAW_FOUR_ALLWILD
                || val == CardValue.WILD_DRAW_TWO_ALLWILD
                || val == CardValue.WILD_SKIP_TWO
                || val == CardValue.WILD_SKIP;
    }

    /**
     * Get playable cards from hand.
     * 
     * @param game Current game instance.
     * @return List of playable cards.
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
}
