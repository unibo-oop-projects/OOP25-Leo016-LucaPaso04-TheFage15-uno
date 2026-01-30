package uno.view.scenes;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import uno.controller.api.MenuObserver;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;

/**
 * Il pannello (JPanel) che rappresenta la schermata del menu principale.
 * Questa versione utilizza una grafica più moderna e accessibile.
 */
public class MenuScene extends JPanel {

    private MenuObserver observer;

    // Palette di colori moderna
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color TITLE_COLOR = Color.WHITE;
    private static final Color BUTTON_COLOR = new Color(211, 47, 47); // Rosso UNO
    private static final Color BUTTON_HOVER_COLOR = new Color(255, 82, 82);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;

    public MenuScene() {
        super(new GridBagLayout()); // Usiamo GridBagLayout per centrare tutto
        setBackground(BACKGROUND_COLOR);

        // Pannello interno che conterrà i componenti
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false); // Rende il pannello trasparente

        // 1. Titolo
        JLabel title = new JLabel("UNO");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, 82));
        title.setForeground(TITLE_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setBorder(new EmptyBorder(0, 0, 40, 0)); // Spazio sotto il titolo

        // 2. Creazione Bottoni Stilizzati
        JButton classicButton = createStyledButton("Modalità Classica");
        classicButton.setMnemonic(KeyEvent.VK_C); 
        
        JButton flipButton = createStyledButton("Modalità Flip");
        flipButton.setMnemonic(KeyEvent.VK_F); 

        JButton allWildButton = createStyledButton("Modalità All Wild");
        allWildButton.setMnemonic(KeyEvent.VK_W);
        
        // --- NUOVO BOTTONE AGGIUNTO ---
        JButton rulesButton = createStyledButton("Impostazioni Regole");
        rulesButton.setMnemonic(KeyEvent.VK_R); // Accessibilità (Alt+R)
        // ------------------------------
        
        JButton quitButton = createStyledButton("Esci dal Gioco");
        quitButton.setMnemonic(KeyEvent.VK_E); 

        // 3. Aggiunta componenti al pannello interno
        contentPanel.add(title);
        contentPanel.add(classicButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        contentPanel.add(flipButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(allWildButton);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Aggiunta del bottone regole con spaziatura
        contentPanel.add(rulesButton); 
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20))); 
        
        contentPanel.add(quitButton);
        
        // 4. Aggiungiamo il pannello centrale alla scena
        add(contentPanel, new GridBagConstraints());

        // 5. Colleghiamo gli ActionListeners
        classicButton.addActionListener(e -> {
            if (observer != null) observer.onStartClassicGame();
        });

        flipButton.addActionListener(e -> {
            if (observer != null) observer.onStartFlipGame();
        });

        allWildButton.addActionListener(e -> {
            if (observer != null) observer.onStartAllWildGame();
        });

        // Listener per il nuovo bottone
        rulesButton.addActionListener(e -> {
            if (observer != null) {
                observer.onOpenRules();
            }
        });

        quitButton.addActionListener(e -> {
            if (observer != null) observer.onQuit();
        });
    }

    public void setObserver(MenuObserver observer) {
        this.observer = observer;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isRollover()) {
                    g2.setColor(BUTTON_HOVER_COLOR);
                } else {
                    g2.setColor(getBackground());
                }
                
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20); 
                super.paintComponent(g);
                g2.dispose();
            }
        };

        button.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(350, 60));
        button.setPreferredSize(new Dimension(350, 60));
        button.setMinimumSize(new Dimension(350, 60));

        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false); 
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }
}