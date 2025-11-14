// Percorso: src/main/java/uno/Model/Cards/Types/WildCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Players.Player;
import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Rappresenta una carta Jolly standard (Cambia Colore).
 */
public class WildDrawColorCard extends AbstractCard {

    public WildDrawColorCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }

    /**
     * Sovrascrive la regola base.
     */
    @Override
    public boolean canBePlayedOn(Card topCard, Game game) {
        // La regola ufficiale (Wild Draw Color) stabilisce che la carta può essere 
        // giocata *soltanto* se il giocatore NON ha una carta abbinabile per COLORE 
        // con il COLORE ATTIVO del gioco.

        CardValue activeValue = this.getValue(game);
        
        // 1. Se il LATO ATTIVO è un Jolly (WILD), la mossa è sempre valida.
        if (activeValue != CardValue.WILD_DRAW_COLOR) {
            return super.canBePlayedOn(topCard, game);
        }

        // 1. Determina il colore attivo nel gioco.
        CardColor activeColor = game.getCurrentColor();
        if (activeColor == null) {
            // Se nessun Jolly ha impostato il colore, il colore attivo è quello della carta in cima.
            activeColor = topCard.getColor(game); // Usa il metodo a doppia faccia (flip-aware)
        }
        
        // 2. Ottieni il giocatore che sta provando a giocare la carta.
        Player currentPlayer = game.getCurrentPlayer();

        // 3. Itera sulla mano del giocatore per trovare una corrispondenza di COLORE.
        for (Card cardInHand : currentPlayer.getHand()) {
            
            // I Jolly non sono considerati "carte abbinabili per colore" ai fini di questa regola.
            // (Li si può giocare, ma non invalidano l'uso del Wild Draw Color).
            if (cardInHand.getValue(game) == CardValue.WILD_DRAW_COLOR) {
                continue;
            }

            // Se la carta in mano ha lo stesso colore ATTIVO, la mossa Wild Draw Color è illegale.
            // La restrizione è solo sul match di COLORE, non di numero/simbolo.
            if (cardInHand.getColor(game) == activeColor) {
                // Trovata una carta dello stesso COLORE ATTIVO.
                return false;
            }
        }
        
        // Se il loop termina senza trovare carte che corrispondono al COLORE ATTIVO, 
        // la mossa è considerata valida e la carta può essere giocata.
        return true;
    }
}
