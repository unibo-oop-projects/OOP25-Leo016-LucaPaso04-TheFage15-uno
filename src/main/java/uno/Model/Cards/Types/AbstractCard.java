// Percorso: src/main/java/uno/Model/AbstractCard.java
package uno.Model.Cards.Types;

import uno.Model.Cards.Card;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Game.Game;

/**
 * Classe base astratta che fornisce un'implementazione comune
 * per i campi e metodi base dell'interfaccia Card.
 */
public abstract class AbstractCard implements Card {

    protected CardColor color;
    protected CardValue value;

    protected CardFace lightSide;
    protected CardFace darkSide;

    boolean isDarkSide = false;

    // A. COSTRUTTORE PER MODALITÀ FLIP (DOPPIA FACCIA)
    public AbstractCard(CardFace lightSide, CardFace darkSide) {
        this.lightSide = lightSide;
        this.darkSide = darkSide;
    }

    // B. COSTRUTTORE PER MODALITÀ CLASSICA (SINGOLA FACCIA)
    public AbstractCard(CardColor color, CardValue value) { // <-- Questo è il vecchio costruttore che usi in StandardDeck
        // La logica chiave: In un gioco non-Flip, i due lati sono identici.
        CardFace singleFace = new CardFace(color, value);
        this.lightSide = singleFace;
        this.darkSide = singleFace; // <--- MAI NULL!
    }

    // Metodo helper che decide quale faccia è attiva
    protected CardFace getActiveFace(Game game) {
        return game.isDarkSide() ? darkSide : lightSide;
    }

    // Implementazione dei metodi dell'interfaccia
    @Override
    public CardColor getColor(Game game) {
        return getActiveFace(game).color();
    }

    @Override
    public CardValue getValue(Game game) {
        return getActiveFace(game).value();
    }

    @Override
    public boolean canBePlayedOn(Card topCard, Game game) {
        CardFace myFace = getActiveFace(game);
        
        // La logica ora usa i valori ATTIVI
        return myFace.color() == topCard.getColor(game) || myFace.value() == topCard.getValue(game);
    }

    /**
     * Implementazione di default per l'effetto della carta.
     * Le carte numeriche non hanno effetti, quindi questo metodo è vuoto.
     * Le classi figlie (es. SkipCard) faranno l'override di questo metodo.
     */
    @Override
    public void performEffect(Game game) {
        // Nessun effetto di default
    }

    @Override
    public String toString() {
        return "C: " + this.lightSide.color() + " " + this.lightSide.value() + " / S: " + this.darkSide.color() + " " + this.darkSide.value();
    }
}
