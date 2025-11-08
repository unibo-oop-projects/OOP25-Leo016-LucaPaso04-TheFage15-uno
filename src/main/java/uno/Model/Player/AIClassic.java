package uno.Model.Player;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor; // Importa CardColor
import uno.Model.Cards.Attributes.CardValue;

import java.util.Optional; // Importa Optional

/**
 * Implementazione di una IA di livello "classico" o base.
 * Estende AIPlayer e implementa la logica decisionale.
 */
public class AIClassic extends AIPlayer {

    public AIClassic(String name) {
        super(name);
    }

    /**
     * Logica decisionale principale dell'IA.
     * Chiamato dal GameController quando è il turno di questa IA.
     * @param game L'istanza corrente del modello Game.
     */
    @Override
    public void takeTurn(Game game) {
        System.out.println(this.name + " sta pensando...");

        // 1. Trova una carta giocabile
        Optional<Card> cardToPlay = findFirstPlayableCard(game);

        if (cardToPlay.isPresent()) {
            // 2. Se trova una carta, la gioca
            Card card = cardToPlay.get();
            
            // Gioca la carta
            game.playCard(card);
            System.out.println(this.name + " gioca " + card);

            // Se la carta è un Jolly, l'IA deve anche scegliere un colore
            if (card.getColor() == CardColor.WILD) {
                CardColor chosenColor = chooseBestColor();
                game.setColor(chosenColor); // Imposta il colore scelto
            }
            
            // 3. Logica per chiamare UNO
            if (this.getHandSize() == 1) {
                try {
                    game.callUno(this);
                } catch (IllegalStateException e) {
                    // L'IA non farà mai una falsa chiamata
                }
            }
            
        } else {
            // 4. Se non trova carte giocabili, pesca una carta
            game.playerInitiatesDraw();
            Card drawnCard = hand.get(hand.size() - 1); // L'ultima carta è quella pescata

            System.out.println(this.name + " pesca una carta.");

            // 5. Controlla se la carta pescata è giocabile
            if (isMoveValid(drawnCard, game)) {
                
                game.playCard(drawnCard);
                System.out.println(this.name + " gioca " + drawnCard + " dopo averla pescata.");

                // Se è giocabile, la gioca immediatamente
                if (drawnCard.getColor() == CardColor.WILD) {
                    CardColor chosenColor = chooseBestColor();
                    game.setColor(chosenColor); // Imposta il colore scelto
                }

                // Logica per chiamare UNO se necessario
                if (this.getHandSize() == 1) {
                    try {
                        game.callUno(this);
                    } catch (IllegalStateException e) {
                        // L'IA non farà mai una falsa chiamata
                    }
                }
            }
            else{
                // 6. Altrimenti, passa
                System.out.println(this.name + " passa il turno.");
                game.playerPassTurn();
            }
        }
    }

    /**
     * Logica per determinare se una carta è giocabile.
     * @param game
     * @return
     */

    private boolean isMoveValid(Card card, Game game) {
        Card topCard = game.getTopDiscardCard(); //
        
        // Determina il colore attivo. Se currentColor è impostato (da un Jolly),
        // usa quello. Altrimenti, usa il colore della carta in cima.
        CardColor activeColor = (game.getCurrentColor() != null) ? game.getCurrentColor() : topCard.getColor();

        // 1. Regola Jolly Standard (WILD)
        if (card.getValue() == CardValue.WILD) {
            return true;
        }

        // 2. Regola Jolly +4 (WILD_DRAW_FOUR)
        if (card.getValue() == CardValue.WILD_DRAW_FOUR) {
            // Regola ufficiale: puoi giocarla solo se NON hai
            // altre carte che corrispondono al COLORE ATTIVO.
            for (Card cardInHand : game.getCurrentPlayer().getHand()) {
                if (cardInHand.getColor() == activeColor) {
                    return false; // Mossa illegale: hai un'altra carta giocabile
                }
            }
            return true; // Mossa legale
        }

        // 3. Regole Standard (non-Jolly)
        // La carta è valida se corrisponde al colore ATTIVO...
        if (card.getColor() == activeColor) {
            return true;
        }
        
        // ...o se corrisponde al VALORE della carta in cima.
        if (card.getValue() == topCard.getValue()) {
            return true;
        }

        // Se nessuna regola è soddisfatta, la mossa non è valida
        return false;
    }

    /**
     * Logica di ricerca base: trova la prima carta valida.
     * @param game Il modello Game per i controlli.
     * @return Una carta giocabile, o Optional.empty() se non ce ne sono.
     */
    private Optional<Card> findFirstPlayableCard(Game game) {
        Card topCard = game.getTopDiscardCard();
        CardColor currentColor = game.getCurrentColor(); 

        for (Card card : this.hand) {
            if (isMoveValid(card, game)) {
                return Optional.of(card);
            }
        }
        return Optional.empty(); // Nessuna carta trovata
    }

    /**
     * Logica IA base per scegliere un colore (es. il colore più presente nella sua mano).
     */
    private CardColor chooseBestColor() {
        // TODO: Implementare una logica reale (es. conta i colori in mano)
        // Per ora, sceglie ROSSO di default.
        System.out.println(this.name + " sceglie ROSSO.");
        return CardColor.RED;
    }
}