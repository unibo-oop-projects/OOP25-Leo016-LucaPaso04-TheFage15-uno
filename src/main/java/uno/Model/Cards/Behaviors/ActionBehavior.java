package uno.model.cards.behaviors;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

public class ActionBehavior implements CardSideBehavior {
    private final CardColor color;
    private final CardValue value;
    // Usiamo una interfaccia funzionale o un enum interno per definire l'azione
    private final RunnableAction action;

    //TODO: CONTROLLA
    public interface RunnableAction {
        void run(Game game);
    }

    public ActionBehavior(CardColor color, CardValue value, RunnableAction action) {
        this.color = color;
        this.value = value;
        this.action = action;
    }

    @Override
    public void executeEffect(Game game) {
        if (action != null) action.run(game);
    }

    @Override public CardColor getColor() { return color; }
    @Override public CardValue getValue() { return value; }
    @Override public String toString() { return color + " " + value; }
}