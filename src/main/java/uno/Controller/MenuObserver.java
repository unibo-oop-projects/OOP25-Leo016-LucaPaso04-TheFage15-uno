package uno.Controller;

/**
 * Interfaccia (Observer) che definisce le azioni che l'utente
 * può compiere dalla schermata del menu.
 * La View (MenuScene) chiamerà questi metodi, e il Controller
 * (MenuController) li implementerà.
 */
public interface MenuObserver {

    /**
     * Chiamato quando l'utente clicca "Avvia Partita Classica".
     */
    void onStartClassicGame();

    /**
     * Chiamato quando l'utente clicca "Avvia Modalità Flip" (Esempio).
     */
    void onStartFlipGame();

    /**
     * Chiamato quando l'utente clicca "Esci".
     */
    void onQuit();
}