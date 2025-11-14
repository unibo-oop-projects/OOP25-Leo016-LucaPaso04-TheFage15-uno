// Percorso: src/main/java/uno/Model/Cards/Types/SkipCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Rappresenta una carta "Salta Turno" (Skip).
 */
public class SkipEveryoneCard extends AbstractCard {

    public SkipEveryoneCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }
}
