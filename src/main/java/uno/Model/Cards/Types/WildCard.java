// Percorso: src/main/java/uno/Model/Cards/Types/WildCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

import uno.Model.Cards.Attributes.CardFace;

/**
 * Rappresenta una carta Jolly standard (Cambia Colore).
 */
public class WildCard extends AbstractCard {

    public WildCard() {
        super(CardColor.WILD, CardValue.WILD); // Il colore "base" è WILD
    }

    public WildCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }

    /**
     * Sovrascrive la regola base. Una carta Jolly
     * può sempre essere giocata.
     */
    @Override
    public boolean canBePlayedOn(Card topCard, Game game) {
        CardValue activeValue = this.getValue(game);
        
        // 1. Se il LATO ATTIVO è un Jolly (WILD), la mossa è sempre valida.
        if (activeValue == CardValue.WILD) {
            return true;
        }
        
        // 2. Se il LATO ATTIVO non è un Jolly, si applica la logica di abbinamento 
        //    standard (colore/valore) definita nella classe padre AbstractCard.
        return super.canBePlayedOn(topCard, game);
    }
}
