package uno.model.players;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;
import uno.model.game.GameState;

public class AIAllWild extends AIPlayer {

    public AIAllWild(String name) {
        super(name);
    }

    @Override
    public void takeTurn(Game game) {
        // Controllo di sicurezza: se non è il mio turno, esco
        if (game.getCurrentPlayer() != this) {
            return;
        }

        System.out.println(this.name + " (AI All Wild) sta pensando...");

        // 1. Sceglie la carta migliore
        Optional<Card> cardToPlay = chooseBestCard(game);

        if (cardToPlay.isPresent()) {
            playSelectedCard(game, cardToPlay.get());
        } else {
            // 2. Se non ha carte da giocare (raro in All Wild), pesca
            handleDrawSequence(game);
        }
    }

    private void playSelectedCard(Game game, Card card) {
        try {
            // Gioca la carta fisicamente
            game.playCard(card);
            System.out.println(this.name + " gioca " + card);

            // --- GESTIONE EFFETTI IMMEDIATI ---
            // Dopo aver giocato playCard, il gioco potrebbe essere entrato in pausa 
            // in attesa di un colore o di un giocatore bersaglio. L'IA deve risolverlo SUBITO.

            if (game.getGameState() == GameState.WAITING_FOR_COLOR) {
                // Scegliamo sempre il Rosso per semplicità in All Wild, o un colore random
                // Tanto in All Wild il colore conta poco, ma serve per sbloccare lo stato.
                game.setColor(null);
                game.AIAdvanceTurn();
            }

            if (game.getGameState() == GameState.WAITING_FOR_PLAYER) {
                // Dobbiamo scegliere un bersaglio per SWAP o TARGETED DRAW
                Player target = findBestTarget(game);
                if (target != null) {
                    game.choosenPlayer(target); // Nota: usa il metodo con il typo 'choosen' presente in Game.java
                    System.out.println(this.name + " ha scelto come bersaglio: " + target.getName());
                    game.AIAdvanceTurn();
                }
            }

            // Gestione chiamata UNO
            if (this.getHandSize() == 1) {
                try {
                    game.callUno(this);
                } catch (Exception e) {
                    // Ignora se non può chiamarlo
                }
            }

        } catch (Exception e) {
            System.err.println("Errore AI durante la giocata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleDrawSequence(Game game) {
        try {
            game.playerInitiatesDraw();
            Card drawnCard = hand.get(hand.size() - 1);
            System.out.println(this.name + " ha pescato: " + drawnCard);

            // In All Wild, la carta pescata è quasi sempre giocabile.
            // Se è valida, la gioca subito.
            if (isMoveValid(drawnCard, game)) {
                playSelectedCard(game, drawnCard);
            } else {
                System.out.println(this.name + " passa il turno.");
                game.playerPassTurn();
            }
        } catch (Exception e) {
            System.out.println(this.name + " non ha potuto pescare/giocare: " + e.getMessage());
            // Forza il passaggio del turno se bloccato
            try { game.playerPassTurn(); } catch (Exception ex) {}
        }
    }

    // --- LOGICA STRATEGICA ---

    private Optional<Card> chooseBestCard(Game game) {
        List<Card> hand = this.getHand();
        Player bestSwapTarget = findBestTarget(game);
        
        // Cerca se abbiamo una carta Scambio Forzato
        Optional<Card> swapCard = hand.stream()
            .filter(c -> c.getValue(game) == CardValue.WILD_FORCED_SWAP)
            .findFirst();

        // STRATEGIA SWAP: Se abbiamo molte carte e l'avversario poche, scambiamo!
        if (swapCard.isPresent() && bestSwapTarget != null) {
            if (this.getHandSize() > bestSwapTarget.getHandSize()) {
                return swapCard;
            }
        }

        // Priorità alle carte "cattive" (Draw 2, Draw 4, Skip)
        for (Card card : hand) {
            CardValue val = card.getValue(game);
            if (val == CardValue.WILD_TARGETED_DRAW_TWO || 
                val == CardValue.WILD_DRAW_FOUR || 
                val == CardValue.WILD_DRAW_TWO ||
                val == CardValue.WILD_SKIP_TWO) {
                return Optional.of(card);
            }
        }

        // Se non ci sono priorità, gioca la prima carta trovata
        // (Evitando lo swap se ci danneggia)
        for (Card card : hand) {
            if (card.getValue(game) == CardValue.WILD_FORCED_SWAP) {
                // Se ho meno carte del target, non voglio scambiare a meno che non sia l'unica carta
                if (bestSwapTarget != null && this.getHandSize() < bestSwapTarget.getHandSize()) {
                    continue; 
                }
            }
            return Optional.of(card);
        }

        // Se è rimasto solo lo swap "brutto", giocalo comunque
        if (!hand.isEmpty()) return Optional.of(hand.get(0));

        return Optional.empty();
    }

    /**
     * Trova il giocatore avversario con il minor numero di carte (il più pericoloso).
     */
    private Player findBestTarget(Game game) {
        return game.getPlayers().stream()
                .filter(p -> p != this) // Esclude se stesso
                .min(Comparator.comparingInt(Player::getHandSize))
                .orElse(null);
    }

    private boolean isMoveValid(Card card, Game game) {
        // In All Wild, quasi tutto è lecito.
        return true;
    }
}