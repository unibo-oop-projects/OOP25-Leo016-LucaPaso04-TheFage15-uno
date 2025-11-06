package uno.View.Scenes;

import uno.Controller.MenuObserver;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

/**
 * Il pannello (JPanel) che rappresenta la schermata del menu principale.
 */
public class MenuScene extends JPanel {

    private MenuObserver observer;

    public MenuScene() {
        super(new BorderLayout(10, 10)); // Layout principale
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. Titolo
        JLabel title = new JLabel("UNO");
        title.setFont(new Font("Arial", Font.BOLD, 48));
        title.setHorizontalAlignment(JLabel.CENTER);
        add(title, BorderLayout.NORTH);

        // 2. Pannello centrale per i bottoni
        JPanel buttonPanel = new JPanel();
        // BoxLayout per impilare i bottoni verticalmente
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // Aggiungiamo i bottoni con spaziatura
        JButton classicButton = createMenuButton("Modalità Classica");
        JButton flipButton = createMenuButton("Modalità Flip (Non implementata)");
        JButton quitButton = createMenuButton("Esci");

        buttonPanel.add(classicButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spaziatore
        buttonPanel.add(flipButton);
        buttonPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Spaziatore
        buttonPanel.add(quitButton);

        // Aggiungiamo il pannello di bottoni al centro (in un box per centrarlo)
        Box centerBox = Box.createVerticalBox();
        centerBox.add(Box.createVerticalGlue());
        centerBox.add(buttonPanel);
        centerBox.add(Box.createVerticalGlue());
        
        add(centerBox, BorderLayout.CENTER);

        // 3. Colleghiamo gli ActionListeners
        classicButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartClassicGame();
            }
        });

        flipButton.setEnabled(false); // Disabilitato per ora
        flipButton.addActionListener(e -> {
            if (observer != null) {
                observer.onStartFlipGame();
            }
        });

        quitButton.addActionListener(e -> {
            if (observer != null) {
                observer.onQuit();
            }
        });
    }

    /**
     * Imposta il controller che ascolterà gli eventi di questo menu.
     */
    public void setObserver(MenuObserver observer) {
        this.observer = observer;
    }

    /**
     * Helper per creare un bottone di menu standard.
     */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Centra il bottone
        button.setMaximumSize(new Dimension(300, 50)); // Dimensione fissa
        button.setFont(new Font("Arial", Font.PLAIN, 18));
        return button;
    }
}