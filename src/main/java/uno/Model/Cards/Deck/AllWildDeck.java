package uno.model.cards.deck;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.*;
import uno.model.cards.types.DoubleSidedCard;

/**
 * Rappresenta il mazzo per la modalità "All Wild" (Tutto Jolly).
 * In questa modalità tutte le carte sono Jolly e hanno effetti vari.
 */
public class AllWildDeck extends Deck<Card> {

    // Istanza unica del dorso per risparmiare memoria (comune a tutte le carte)
    private final CardSideBehavior STANDARD_BACK = new BackSideBehavior();

    public AllWildDeck() {
        super();
    }

    @Override
    protected void createDeck() {
        // Nella modalità All Wild, le carte vengono ripetute molte volte.
        // Basandoci sul tuo codice originale: 14 copie per ogni tipo (14 * 8 = 112 carte).
        
        for (int i = 0; i < 14; i++) {
            
            // 1. Jolly Classico (WILD)
            // Usa WildBehavior: chiede il colore (opzionale in AllWild, ma standard per coerenza)
            addCard(new WildBehavior(CardValue.WILD_ALLWILD, 0));

            // 2. Jolly Pesca 4 (+4)
            addCard(new WildBehavior(CardValue.WILD_DRAW_FOUR_ALLWILD, 4));

            // 3. Jolly Pesca 2 (+2)
            addCard(new WildBehavior(CardValue.WILD_DRAW_TWO_ALLWILD, 2));

            // 6. Inverti (Wild Reverse)
            addCard(new WildBehavior(CardValue.WILD_REVERSE, 0));

            // --- AZIONI SPECIALI (Usiamo ActionBehavior con lambda) ---
            
            // 4. Scambio Forzato (Forced Swap)
            // L'effetto è chiedere di scegliere un giocatore.
            addCard(new WildBehavior(CardValue.WILD_FORCED_SWAP, 0));

            // 5. Pesca 2 Mirata (Targeted Draw 2)
            // Anche questo richiede di scegliere un giocatore target.
            addCard(new WildBehavior(CardValue.WILD_TARGETED_DRAW_TWO, 0));

            // 7. Salta (Skip)
            addCard(new WildBehavior(CardValue.WILD_SKIP, 
                0));

            // 8. Salta Due (Skip Two)
            addCard(new WildBehavior(CardValue.WILD_SKIP_TWO, 0));
        }
    }

    /**
     * Metodo helper per creare la DoubleSidedCard.
     * Associa il comportamento specifico al lato Front e il dorso standard al lato Back.
     */
    private void addCard(CardSideBehavior frontBehavior) {
        Card card = new DoubleSidedCard(frontBehavior, STANDARD_BACK);
        this.cards.add(card);
    }
}