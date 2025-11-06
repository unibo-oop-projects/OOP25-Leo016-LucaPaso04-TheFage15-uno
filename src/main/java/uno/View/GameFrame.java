package uno.View;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.Dimension;

/**
 * La finestra principale (JFrame) dell'applicazione.
 * Funziona come un contenitore per le varie "scene" (pannelli) 
 * come il Menu, la schermata di Gioco, ecc.
 */
public class GameFrame extends JFrame {

    public GameFrame(String title) {
        super(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null); // Centra la finestra
    }

    /**
     * Sostituisce la scena corrente con una nuova.
     * @param scene Il JPanel da mostrare (es. MenuScene, GameScene).
     */
    public void showScene(JPanel scene) {
        setContentPane(scene);
        // Richiama validate/repaint per assicurarsi che il nuovo pannello appaia
        validate();
        repaint();
    }
}