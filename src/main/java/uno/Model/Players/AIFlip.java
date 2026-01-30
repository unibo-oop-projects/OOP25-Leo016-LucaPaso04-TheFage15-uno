package uno.model.players;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional; // Importa Optional

import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

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
                game.AIAdvanceTurn();;
            }

            if (card.getValue(game) == CardValue.WILD_DRAW_COLOR) {
                CardColor chosenColor = CardColor.PURPLE;
                game.setColor(chosenColor); // Imposta il colore scelto
                game.AIAdvanceTurn();;
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
                    game.AIAdvanceTurn();;
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
     * Logica per scegliere il miglior colore quando si gioca un Jolly in modalità Flip.
     * Rileva automaticamente se siamo sul lato Chiaro o Scuro e conta i colori corretti.
     * * @param game L'istanza corrente del modello Game.
     * @return Il colore scelto (compatibile con il lato attuale).
     */
    private CardColor chooseBestColor(Game game) {
        // Usiamo un EnumMap per contare i colori.
        Map<CardColor, Integer> colorCounts = new EnumMap<>(CardColor.class);

        // 1. DETERMINA I COLORI VALIDI
        // In base al lato attivo, prepariamo i "secchielli" per il conteggio.
        CardColor[] validColors;
        
        if (game.isDarkSide()) {
            // Colori del Lato Oscuro
            validColors = new CardColor[] { 
                CardColor.TEAL, CardColor.PINK, CardColor.PURPLE, CardColor.ORANGE 
            };
        } else {
            // Colori del Lato Chiaro (Classici)
            validColors = new CardColor[] { 
                CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW 
            };
        }

        // Inizializza i conteggi a 0 solo per i colori validi in questo momento
        for (CardColor c : validColors) {
            colorCounts.put(c, 0);
        }

        // 2. CONTA LE CARTE IN MANO
        for (Card card : this.hand) {
            // Grazie alla tua refattorizzazione in DoubleSidedCard, card.getColor(game)
            // restituisce automaticamente il colore del lato attivo (es. TEAL se è scuro).
            CardColor color = card.getColor(game);
            
            // Incrementa solo se il colore è tra quelli validi (ignora WILD o colori dell'altro lato se presenti per errore)
            if (colorCounts.containsKey(color)) {
                colorCounts.put(color, colorCounts.get(color) + 1);
            }
        }

        // 3. TROVA IL MAX
        // Impostiamo un default sicuro: il primo colore della lista valida.
        CardColor bestColor = validColors[0]; 
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