package uno.controller.api;

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

    /**
     * Chiamato quando l'utente clicca "Avvia Modalità Tutti Jolly".
     */
    void onStartAllWildGame();

    /**
     * Chiamato quando l'utente clicca "Regole".
     */
    void onOpenRules();

    /**
     * Chiamato quando l'utente salva le regole personalizzate.
     * 
     * @param rules Le nuove regole impostate.
     */
    void onSaveRules(uno.model.game.api.GameRules rules);

    /**
     * Chiamato quando l'utente vuole tornare al menu principale dalla schermata
     * regole.
     */
    void onBackToMenu();
}
