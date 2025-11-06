package uno;

import uno.Controller.MenuController;
import uno.View.GameFrame;
import uno.View.Scenes.MenuScene;

import javax.swing.SwingUtilities;

/**
 * Classe principale che avvia l'applicazione UNO.
 */
public final class Main {

    private Main() {
    }

    /**
     * Entry point dell'applicazione.
     * @param args argomenti da riga di comando (non usati).
     */
    public static void main(final String[] args) {
        
        // Avvia l'interfaccia grafica sul thread corretto (EDT)
        SwingUtilities.invokeLater(() -> {
            
            // 1. Crea la finestra principale (View)
            GameFrame frame = new GameFrame("UNO");

            // 2. Crea il Controller del Menu e passagli la finestra
            MenuController menuController = new MenuController(frame);

            // 3. Crea la Scena del Menu (View)
            MenuScene menuScene = new MenuScene();

            // 4. Collega la Scena al suo Controller (Observer)
            menuScene.setObserver(menuController);

            // 5. Mostra la scena del menu all'avvio
            frame.showScene(menuScene);
            
            // 6. Rendi visibile la finestra
            frame.setVisible(true);
        });
    }
}