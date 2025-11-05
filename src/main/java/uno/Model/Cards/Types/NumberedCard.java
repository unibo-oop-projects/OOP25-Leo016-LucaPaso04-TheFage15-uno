// Percorso: src/main/java/uno/Model/NumberedCard.java
package uno.Model.Cards.Types;

import uno.Controller.GameController;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

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

    @Override
    public void executeEffect(GameController controller) {
        // Le carte numerate non hanno effetto.
        // Il controller sa già che deve solo passare il turno.
    }
}