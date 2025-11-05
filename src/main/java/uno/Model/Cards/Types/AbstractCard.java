// Percorso: src/main/java/uno/Model/AbstractCard.java
package uno.Model.Cards.Types;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Classe base astratta che fornisce un'implementazione comune
 * per i campi e metodi base dell'interfaccia Card.
 */
public abstract class AbstractCard implements Card {

    protected final CardColor color;
    protected final CardValue value;

    public AbstractCard(CardColor color, CardValue value) {
        this.color = color;
        this.value = value;
    }

    @Override
    public CardColor getColor() {
        return color;
    }

    @Override
    public CardValue getValue() {
        return value;
    }

    /**
     * Fornisce la regola di gioco standard.
     * Le WildCard sovrascriveranno questo metodo.
     */
    @Override
    public boolean canBePlayedOn(Card topCard) {
        // Regola standard: stesso colore o stesso valore
        return this.color == topCard.getColor() || this.value == topCard.getValue();
    }

    @Override
    public String toString() {
        // Utile per il debug
        return color + " " + value;
    }
}
