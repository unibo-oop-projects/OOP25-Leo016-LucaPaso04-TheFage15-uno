package uno.controller.api;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.types.api.Card;
import uno.model.players.api.Player;

import java.util.Optional;

/**
 * Interfaccia (Observer) che definisce le azioni che l'utente
 * può compiere dalla schermata di gioco.
 * La GameScene chiamerà questi metodi, e il GameController
 * li implementerà.
 */
public interface GameViewObserver {

    /**
     * Chiamato quando l'utente clicca su una carta nella sua mano.
     * @param card La carta che l'utente ha tentato di giocare.
     */
    void onPlayCard(Optional<Card> card);

    /**
     * Chiamato quando l'utente clicca sul mazzo di pesca.
     */
    void onDrawCard();

    /**
     * Chiamato quando l'utente clicca il bottone "UNO".
     */
    void onCallUno();

    /**
     * Chiamato quando l'utente vuole tornare al menu.
     */
    void onBackToMenu();

    /**
     * Chiamato quando l'utente clicca il bottone "Passa".
     */
    void onPassTurn();

    /**
     * Chiamato quando l'utente clicca su uno dei bottoni giocatore
     * dopo aver giocato una carta che richiede di scegliere un giocatore
     * (es. Asso).
     * @param player Il giocatore scelto.
     */
    void onPlayerChosen(Player player);

    /**
     * Chiamato quando l'utente clicca su uno dei bottoni colore
     * dopo aver giocato una carta Jolly.
     * @param color Il colore scelto.
     */
    void onColorChosen(CardColor color);
}
