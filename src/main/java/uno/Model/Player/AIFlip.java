package uno.Model.Player;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor; // Importa CardColor
import uno.Model.Cards.Attributes.CardValue;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional; // Importa Optional

/**
 * Implementazione di una IA di livello "classico" o base.
 * Estende AIPlayer e implementa la logica decisionale.
 */
public class AIFlip extends AIPlayer {

    public AIFlip(String name) {
        super(name);
    }

    /**
     * Logica decisionale principale dell'IA.
     * Chiamato dal GameController quando è il turno di questa IA.
     * @param game L'istanza corrente del modello Game.
     */

    @Override
    public void takeTurn(Game game) {
        // Controlla che sia il suo turno per evitare errori
        if (game.getCurrentPlayer() != this) {
            return;
            //throw new IllegalStateException("Non è il turno di " + this.name);
        }

        System.out.println("[" + this.name + "]" + " sta pensando...]");

        // 1. Trova una carta giocabile
        Optional<Card> cardToPlay = findFirstPlayableCard(game);

        if (cardToPlay.isPresent()) {
            // 2. Se trova una carta, la gioca
            Card card = cardToPlay.get();
            
            // Gioca la carta
            game.playCard(card);
            System.out.println("[" + this.name + "]" + " gioca " + card);

            // Se la carta è un Jolly, l'IA deve anche scegliere un colore
            if (card.getColor(game) == CardColor.WILD && card.getValue(game) != CardValue.WILD_DRAW_COLOR) {
                CardColor chosenColor = chooseBestColor(game);
                game.setColor(chosenColor); // Imposta il colore scelto
            }

            if (card.getValue(game) == CardValue.WILD_DRAW_COLOR) {
                CardColor chosenColor = CardColor.PURPLE;
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

            System.out.println("[" + this.name + "]" + " NON ha carte, quindi pesca.");

            // 5. Controlla se la carta pescata è giocabile
            if (isMoveValid(drawnCard, game)) {
                
                game.playCard(drawnCard);
                System.out.println("[" + this.name + "]" + " gioca " + drawnCard);

                // Se è giocabile, la gioca immediatamente
                if (drawnCard.getColor(game) == CardColor.WILD) {
                    CardColor chosenColor = chooseBestColor(game);
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
                System.out.println("[" + this.name + "]" + " passa il turno.");
                game.playerPassTurn();
            }
        }
    }

    private boolean isMoveValid(Card card, Game game) {
        Card topCard = game.getTopDiscardCard();
        return card.canBePlayedOn(topCard, game);
    }

    /**
     * Trova la prima carta giocabile nella mano dell'IA.
     * @param game L'istanza corrente del modello Game.
     * @return Un Optional contenente la carta giocabile, o vuoto se nessuna è trovata.
     */
    private Optional<Card> findFirstPlayableCard(Game game) {
        for (Card card : hand) {
            if (isMoveValid(card, game)) {
                return Optional.of(card);
            }
        }
        return Optional.empty();
    }

    /**
     * Logica per scegliere il miglior colore quando si gioca un Jolly.
     * Sceglie il colore che l'IA ha in maggior quantità.
     * @param game L'istanza corrente del modello Game.
     * @return Il colore scelto.
     */
    private CardColor chooseBestColor(Game game) {
        // Usiamo un EnumMap per contare i colori. È molto efficiente per le chiavi Enum.
        Map<CardColor, Integer> colorCounts = new EnumMap<>(CardColor.class);

        // Inizializza i conteggi a 0 per i colori che si possono scegliere
        colorCounts.put(CardColor.RED, 0);
        colorCounts.put(CardColor.GREEN, 0);
        colorCounts.put(CardColor.BLUE, 0);
        colorCounts.put(CardColor.YELLOW, 0);

        // Itera sulla mano dell'IA ('this.hand' è ereditato da Player)
        for (Card card : this.hand) {
            CardColor color = card.getColor(game);
            
            // Contiamo solo le carte colorate, ignoriamo le WILD
            if (colorCounts.containsKey(color)) {
                colorCounts.put(color, colorCounts.get(color) + 1);
            }
        }

        // Ora trova il colore con il conteggio massimo
        CardColor bestColor = CardColor.RED; // Colore di default se la mano è vuota o ha solo Jolly
        int maxCount = -1;

        for (Map.Entry<CardColor, Integer> entry : colorCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestColor = entry.getKey();
            }
        }
        
        return bestColor;
    }
}