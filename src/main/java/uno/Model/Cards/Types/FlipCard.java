package uno.Model.Cards.Types;

import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardFace;

/**
 * La carta speciale FLIP (esiste solo sul Lato Chiaro).
 * Il suo unico scopo è chiamare game.flipTheWorld().
 */
public class FlipCard extends AbstractCard {

    public FlipCard(CardFace lightSide, CardFace darkSide) {
        super(lightSide, darkSide);
    }
}