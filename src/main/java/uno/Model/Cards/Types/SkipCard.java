// Percorso: src/main/java/uno/Model/Cards/Types/SkipCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Attributes.CardFace;

/**
 * Rappresenta una carta "Salta Turno" (Skip).
 */
public class SkipCard extends AbstractCard {

    public SkipCard(CardColor color) {
        super(color, CardValue.SKIP);
    }

    public SkipCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }
}
