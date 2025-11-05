// Percorso: src/main/java/uno/Model/Card.java
package uno.Model.Cards;

import uno.Controller.GameController;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

/**
 * Interfaccia che definisce il contratto per qualsiasi carta da gioco Uno.
 * Verrà usata dalla classe Deck (es. Deck<Card>).
 */
public interface Card {

    /**
     * @return Il colore della carta (o WILD).
     */
    CardColor getColor();

    /**
     * @return Il valore/tipo della carta (es. NINE, SKIP, WILD).
     */
    CardValue getValue();

    /**
     * Determina se questa carta può essere legalmente giocata
     * sopra la carta in cima al mazzo degli scarti.
     *
     * @param topCard La carta in cima al mazzo degli scarti.
     * @return true se la mossa è valida, false altrimenti.
     */
    boolean canBePlayedOn(Card topCard);

    /**
     * Esegue l'azione/effetto speciale della carta.
     * Il Controller chiamerà questo metodo dopo che la carta è stata giocata.
     *
     * @param controller Il GameController che gestisce il flusso del gioco.
     */
    void executeEffect(GameController controller);
}
