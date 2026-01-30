package uno.model.cards.behaviors;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.game.Game;

public class WildBehavior implements CardSideBehavior {
    private final CardValue value;
    private final int drawAmount; // 0 se semplice Wild

    public WildBehavior(CardValue value, int drawAmount) {
        this.value = value;
        this.drawAmount = drawAmount;
    }
    
    // Costruttore speciale per Wild Draw Color se necessario
    public WildBehavior(CardValue value, boolean targetColor) {
        this.value = value;
        this.drawAmount = 0;
    }

    @Override
    public void executeEffect(Game game) {
        // 1. Gestione Pesca (funziona per tutti i Jolly che fanno pescare)
        if (drawAmount > 0) {
            game.makeNextPlayerDraw(drawAmount);
        }

        // 2. Richiesta Colore (SOLO se NON è una carta All Wild)
        if (!isAllWildCard(value)) {
            game.requestColorChoice();
        }

        // 3. Effetti Specifici
        // Nota: Ho raggruppato i casi simili per pulizia
        switch (value) {
            case WILD_DRAW_TWO:
            case WILD_DRAW_FOUR:
            case WILD_SKIP:
                // In All Wild, il Draw Four di solito salta il turno del prossimo? 
                // Se sì, lascia qui. Se no, togli WILD_DRAW_FOUR_ALLWILD da qui sotto o gestiscilo a parte.
            case WILD_DRAW_FOUR_ALLWILD: 
            case WILD_DRAW_TWO_ALLWILD:
                game.skipPlayers(1);
                break;
                
            case WILD_SKIP_TWO:
                game.skipPlayers(2);
                break;
                
            case WILD_REVERSE:
                game.reversePlayOrder();
                break;
                
            case WILD_FORCED_SWAP:
            case WILD_TARGETED_DRAW_TWO:
                game.requestPlayerChoice();
                break;
                
            default:
                break;
        }
    }

    /**
     * Metodo helper per verificare se la carta appartiene alla modalità All Wild.
     * Restituisce true se NON bisogna chiedere il colore.
     */
    private boolean isAllWildCard(CardValue value) {
        return value == CardValue.WILD_ALLWILD ||
            value == CardValue.WILD_DRAW_FOUR_ALLWILD ||
            value == CardValue.WILD_DRAW_TWO_ALLWILD ||
            value == CardValue.WILD_TARGETED_DRAW_TWO ||
            value == CardValue.WILD_FORCED_SWAP ||
            value == CardValue.WILD_REVERSE ||
            value == CardValue.WILD_SKIP ||
            value == CardValue.WILD_SKIP_TWO;
    }

    @Override public CardColor getColor() { return CardColor.WILD; }
    @Override public CardValue getValue() { return value; }
    @Override public String toString() { return "WILD " + value; }
}