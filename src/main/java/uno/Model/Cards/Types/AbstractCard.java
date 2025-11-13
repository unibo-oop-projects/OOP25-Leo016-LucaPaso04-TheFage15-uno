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
        CardColor activeColor = (game.getCurrentColor() != null) ? game.getCurrentColor() : topCard.getColor(game);

        // --- REGOLE DI MATCH ---
        
        // A. Match di COLORE ATTIVO (Regola prioritaria se c'è un Jolly attivo)
        if (myFace.color() == activeColor) {
            return true;
        }
        
        // B. Match di VALORE della carta in cima (usiamo getValue(game) per il lato attivo della topCard)
        if (myFace.value() == topCard.getValue(game)) {
            return true;
        }

        // Se nessuna regola è soddisfatta, la mossa non è valida.
        return false;
    }

    /**
     * Implementazione di default per l'effetto della carta.
     * Le carte numeriche non hanno effetti, quindi questo metodo è vuoto.
     * Le classi figlie (es. SkipCard) faranno l'override di questo metodo.
     */
    @Override
    public void performEffect(Game game) {
        // Nessun effetto di default
        CardValue activeValue = this.getValue(game);
        // Chiama il centro di controllo per eseguire l'azione corretta
        dispatchBasicEffect(game, activeValue);
        //TODO: Tutte le carte usano lo stesso codice, sostituisci i metodi nelle varie classi
    }

    /**
     * NUOVO METODO STATICO (Dispatcher)
     * Esegue un'azione di gioco base in base al CardValue fornito.
     * @param game L'istanza del gioco.
     * @param valueIl valore ATTIVO della carta (dal lato chiaro o scuro).
     */
    protected void dispatchBasicEffect(Game game, CardValue value) {
        switch (value) {
            case SKIP:
                game.skipNextPlayer();
                break;
            case SKIP_EVERYONE:
                game.skipPlayers(4);
                break;
            case REVERSE:
                game.reversePlayOrder();
                break;
            case DRAW_ONE:
                game.makeNextPlayerDraw(1);
                game.skipNextPlayer();
                break;
            case DRAW_TWO:
                game.makeNextPlayerDraw(2);
                game.skipNextPlayer();
                break;
            case DRAW_FIVE:
                game.makeNextPlayerDraw(5);
                game.skipNextPlayer();
                break;
            case FLIP:
                game.flipTheWorld(this);
                break;
            case WILD, WILD_DARK, WILD_DRAW_COLOR:
                game.requestColorChoice();
                break;
            case WILD_DRAW_FOUR:
                game.makeNextPlayerDraw(4);
                game.skipNextPlayer();
                game.requestColorChoice();
                break;
            case WILD_DRAW_TWO:
                game.makeNextPlayerDraw(2);
                game.skipNextPlayer();
                game.requestColorChoice();
                break;
            case WILD_FORCED_SWAP:
                game.requestPlayerChoice();
                break;
            case WILD_REVERSE:
                game.reversePlayOrder();
                break;
            case WILD_SKIP:
                game.skipNextPlayer();
                break;
            case WILD_SKIP_TWO:
                game.skipPlayers(3);
                break;
            case WILD_TARGETED_DRAW_TWO:
                game.requestPlayerChoice();
                break;
            case WILD_DRAW_FOUR_ALLWILD:
                game.makeNextPlayerDraw(4);
                game.skipNextPlayer();
                break;
            case WILD_DRAW_TWO_ALLWILD:
                game.makeNextPlayerDraw(2);
                game.skipNextPlayer();
                break;
            default:
                break;
        }
    }

    @Override
    public String toString() {
        return "C: " + this.lightSide.color() + " " + this.lightSide.value() + " / S: " + this.darkSide.color() + " " + this.darkSide.value();
    }
}
