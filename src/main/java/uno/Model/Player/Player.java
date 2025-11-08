// Percorso: src/main/java/uno/Model/Player/Player.java
package uno.Model.Player;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un giocatore. È la classe base per tutti i tipi di giocatori.
 * Il giocatore umano sarà un'istanza diretta di questa classe.
 */
public class Player {

    protected final String name;
    protected List<Card> hand;

    protected boolean hasCalledUno;

    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    /**
     * Restituisce una vista non modificabile della mano.
     */
    public List<Card> getHand() {
        return List.copyOf(hand);
    }

    public int getHandSize() {
        return hand.size();
    }

    public boolean getHasCalledUno() {
        return hasCalledUno;
    }

    /**
     * Aggiunge una carta alla mano del giocatore (es. quando pesca).
     */
    public void addCardToHand(Card card) {
        this.hand.add(card);
    }

    /**
     * Rimuove una carta specifica dalla mano (quando viene giocata).
     * @param card La carta da giocare.
     * @return true se la carta è stata trovata e rimossa, false altrimenti.
     */
    public boolean playCard(Card card) {
        return this.hand.remove(card);
    }
    
    public boolean hasWon() {
        return this.hand.isEmpty();
    }

    /**
     * Verifica se il giocatore ha chiamato UNO.
     * La classe Game userà questo metodo per decidere
     * se penalizzare un giocatore alla fine del suo turno.
     *
     * @return true se il giocatore ha chiamato UNO, false altrimenti.
     */
    public void hasCalledUno() {
        this.hasCalledUno = true;
    }

    /**
     * Resetta lo stato "UNO!" del giocatore.
     * La classe Game chiamerà questo metodo all'inizio
     * del turno di ogni *altro* giocatore, per rendere
     * il giocatore vulnerabile alla penalità.
     */
    public void resetUnoStatus() {
        this.hasCalledUno = false;
    }

    /**
     * Metodo che il Controller chiamerà per il turno del giocatore.
     * Per il giocatore umano (questa classe), il metodo è "passivo":
     * aspetta che il Controller (tramite input della View)
     * chiami un metodo su Game (es. game.playCard(player, card)).
     *
     * @param game L'istanza corrente del gioco.
     */
    public void takeTurn(Game game) {
        // Per il Player umano, la logica è gestita dal Controller
        // che attende un input dall'interfaccia utente.
        System.out.println("Turno di " + this.name + " (Umano). In attesa di input...");
    }
}
