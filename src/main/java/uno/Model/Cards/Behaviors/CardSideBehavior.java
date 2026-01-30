package uno.model.cards.behaviors;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

public interface CardSideBehavior {
    CardColor getColor();
    CardValue getValue();
    void executeEffect(Game game);
    
    // Facoltativo: toString specifico per il lato
    String toString(); 
}