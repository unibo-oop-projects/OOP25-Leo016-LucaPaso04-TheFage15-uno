// Percorso: src/main/java/uno/Model/Cards/Types/ReverseCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

import uno.Model.Cards.Attributes.CardFace;

/**
 * Rappresenta una carta "Inverti Giro" (Reverse).
 */
public class ReverseCard extends AbstractCard {

    public ReverseCard(CardColor color) {
        super(color, CardValue.REVERSE);
    }

    public ReverseCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }

    /**
     * Esegue l'effetto "inverti giro" modificando lo stato del gioco.
     */
    @Override
    public void performEffect(Game game) {
        game.reversePlayOrder();
    }
}