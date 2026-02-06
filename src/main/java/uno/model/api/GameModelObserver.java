package uno.model.api;

/**
 * Interfaccia (Observer) che permette al Modello (Game)
 * di notificare alla Vista (GameScene) che è avvenuto un cambiamento.
 */
@FunctionalInterface
public interface GameModelObserver {

    /**
     * Metodo generico di aggiornamento.
     * La View, ricevuta questa notifica, andrà a rileggere i dati
     * dal modello e si ridisegnerà.
     */
    void onGameUpdate();
}
