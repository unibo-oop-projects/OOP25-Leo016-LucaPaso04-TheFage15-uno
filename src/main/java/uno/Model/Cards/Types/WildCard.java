// Percorso: src/main/java/uno/Model/WildCard.java
package uno.Model.Cards.Types;

import uno.Controller.GameController;
import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Rappresenta una carta Jolly (Jolly standard o Jolly+4).
 */
public class WildCard extends AbstractCard {

    public WildCard(CardValue value) {
        super(CardColor.WILD, value); // Il colore "base" è WILD
        if (value != CardValue.WILD && value != CardValue.WILD_DRAW_FOUR) {
            throw new IllegalArgumentException("Valore non valido per WildCard.");
        }
    }

    @Override
    public void executeEffect(GameController controller) {
        // 1. L'effetto principale è chiedere di scegliere un colore.
        // Il Controller gestirà la logica per ottenere l'input dalla View.
        controller.promptPlayerForColorChoice();
        
        // 2. Se è un +4, applica l'effetto aggiuntivo
        if (this.value == CardValue.WILD_DRAW_FOUR) {
            controller.makeNextPlayerDraw(4);
        }
    }

    /**
     * Sovrascrive la regola base. Le carte Jolly possono
     * (quasi) sempre essere giocate.
     */
    @Override
    public boolean canBePlayedOn(Card topCard) {
        // La logica complessa del "non puoi giocare un +4 se hai
        // un'altra carta valida" è spesso gestita a livello di
        // Controller/GameModel, non della singola carta.
        return true;
    }
}
