// Percorso: src/main/java/uno/Model/Cards/Types/DrawTwoCard.java
package uno.Model.Cards.Types;

import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Attributes.CardFace;

/**
 * Rappresenta una carta "Pesca Due" (Draw Two).
 */
public class DrawTwoCard extends AbstractCard {

    public DrawTwoCard(CardColor color) {
        super(color, CardValue.DRAW_TWO);
    }

    public DrawTwoCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }

    /**
     * Esegue l'effetto "pesca due" modificando lo stato del gioco.
     */
    @Override
    public void performEffect(Game game) {
        game.makeNextPlayerDraw(2);
        game.skipNextPlayer(); // In molte regole, il +2 fa anche saltare
    }
}
