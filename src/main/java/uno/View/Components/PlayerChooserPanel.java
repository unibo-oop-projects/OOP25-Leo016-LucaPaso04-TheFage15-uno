package uno.View.Components;

import uno.Controller.GameViewObserver;
import uno.Model.Players.Player;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List; // Importa la classe List

/**
 * Un pannello che appare quando un giocatore deve scegliere
 * un altro giocatore (un avversario) come bersaglio di un effetto.
 */
public class PlayerChooserPanel extends JPanel implements ActionListener {

    private final GameViewObserver observer;
    private final List<Player> opponents;

    // Stili copiati da ColorChooserPanel per coerenza
    private static final Color PANEL_COLOR = new Color(50, 50, 50);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 14);

    /**
     * Costruisce il pannello di scelta.
     * @param observer L'observer (GameController) da notificare.
     * @param opponents La lista degli avversari (già filtrata, escludendo il giocatore corrente).
     */
    public PlayerChooserPanel(GameViewObserver observer, List<Player> opponents) {
        this.observer = observer;
        this.opponents = opponents;

        // Layout verticale per i pulsanti
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(PANEL_COLOR);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Scegli Avversario",
            TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
        ));
        
        // Crea un pulsante per ogni avversario
        for (Player opponent : opponents) {
            JButton button = createStyledButton(opponent.getName()); // Usa il nome del giocatore
            button.setActionCommand(opponent.getName()); // Usa il nome come identificatore
            button.addActionListener(this);
            
            add(button);
            add(Box.createRigidArea(new Dimension(0, 5))); // Spaziatore
        }
        
        // Imposta una dimensione preferita
        setPreferredSize(new Dimension(220, 150));
    }

    /**
     * Metodo helper per creare un pulsante stilizzato.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BOLD_FONT);
        button.setBackground(Color.DARK_GRAY); // Un colore neutro
        button.setForeground(Color.WHITE);
        
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        button.setPreferredSize(new Dimension(200, 40));
        
        return button;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String chosenName = e.getActionCommand();
        Player chosenPlayer = null;

        // 1. Trova l'oggetto Player corrispondente al nome
        for (Player p : opponents) {
            if (p.getName().equals(chosenName)) {
                chosenPlayer = p;
                break;
            }
        }
        
        if (chosenPlayer != null) {
            // 2. Notifica il controller (richiede un nuovo metodo nell'observer)
            if (observer != null) {
                observer.onPlayerChosen(chosenPlayer);
            }
            
            // 3. Chiude il popup
            Window parentDialog = SwingUtilities.getWindowAncestor(this);
            if (parentDialog != null) {
                parentDialog.dispose(); 
            }
        }
    }
}