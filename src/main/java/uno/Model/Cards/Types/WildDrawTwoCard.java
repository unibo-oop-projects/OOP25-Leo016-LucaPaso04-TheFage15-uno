// Percorso: src/main/java/uno/Model/Cards/Types/WildDrawFourCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Rappresenta una carta Jolly Pesca Due (+2).
 */
public class WildDrawTwoCard extends AbstractCard {

    public WildDrawTwoCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }

    public WildDrawTwoCard() {
        super(CardColor.WILD, CardValue.WILD_DRAW_TWO); // Il colore "base" è WILD
    }

    /**
     * Esegue l'effetto "pesca quattro" e "cambia colore".
     */
    @Override
    public void performEffect(Game game) {
        CardValue activeValue = this.getValue(game);
        dispatchBasicEffect(game, activeValue);
    }

    /**
     * Sovrascrive la regola base.
     * La logica complessa del "non puoi giocarla se hai altre carte"
     * viene gestita dal Controller/Game prima di chiamare questo metodo.
     * A livello di carta, è sempre "giocabile".
     */
    @Override
    public boolean canBePlayedOn(Card topCard, Game game) {
        CardValue activeValue = this.getValue(game);
        
        // 1. Se il LATO ATTIVO è un Jolly (WILD), la mossa è sempre valida.
        if (activeValue == CardValue.WILD_DRAW_TWO) {
            return true;
        }
        
        // 2. Se il LATO ATTIVO non è un Jolly, si applica la logica di abbinamento 
        //    standard (colore/valore) definita nella classe padre AbstractCard.
        return super.canBePlayedOn(topCard, game);
    }
}