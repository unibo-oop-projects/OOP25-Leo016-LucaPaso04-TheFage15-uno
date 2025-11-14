// Percorso: src/main/java/uno/Model/Cards/Types/NumberedCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Attributes.CardFace;

/**
 * Rappresenta una carta numerata (0-9).
 * Non ha effetti speciali.
 */
public class NumberedCard extends AbstractCard {

    public NumberedCard(CardColor color, CardValue value) {
        super(color, value);
        // Controllo di sicurezza
        if (value.ordinal() > CardValue.NINE.ordinal()) {
            throw new IllegalArgumentException("NumberedCard deve avere un valore tra 0 e 9.");
        }
    }

    public NumberedCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }
}