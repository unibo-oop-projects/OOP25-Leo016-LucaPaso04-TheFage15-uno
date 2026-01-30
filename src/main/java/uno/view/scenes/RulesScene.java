package uno.view.scenes;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import uno.controller.api.MenuObserver;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Schermata per la configurazione delle regole personalizzate (House Rules).
 * Mantiene lo stesso stile grafico di MenuScene.
 */
public class RulesScene extends JPanel {

    private MenuObserver observer;

    // Palette colori (coerente con MenuScene)
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color DESC_COLOR = new Color(180, 180, 180); // Grigio chiaro per descrizioni
    private static final Color BUTTON_COLOR = new Color(211, 47, 47);
    private static final Color BUTTON_TEXT_COLOR = Color.WHITE;

    // Componenti di input per le regole
    private final JCheckBox unoPenaltyCheck;
    private final JCheckBox skipAfterDrawCheck;
    private final JCheckBox mandatoryPassCheck;

    public RulesScene() {
        super(new GridBagLayout());
        setBackground(BACKGROUND_COLOR);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        // 1. Titolo
        JLabel title = new JLabel("Regole Personalizzate");
        title.setFont(new Font("Helvetica Neue", Font.BOLD, 48));
        title.setForeground(TEXT_COLOR);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // 2. Creazione dei pannelli per le regole
        // Regola 1: Penalità UNO (Default: Attiva)
        JPanel rule1 = createRulePanel(
            "Penalità UNO Facoltativa", 
            "Se DISATTIVATO, non è obbligatorio premere 'UNO!' quando si resta con una sola carta.",
            true 
        );
        unoPenaltyCheck = (JCheckBox) rule1.getClientProperty("checkbox");

        // Regola 2: Salto dopo la Pesca (Default: Disattiva)
        JPanel rule2 = createRulePanel(
            "Salto del Turno dopo la Pesca", 
            "Se ATTIVATO, il giocatore non può giocare subito la carta appena pescata, ma deve passare.",
            false
        );
        skipAfterDrawCheck = (JCheckBox) rule2.getClientProperty("checkbox");

        // Regola 3: Passaggio Obbligatorio (Default: Disattiva)
        JPanel rule3 = createRulePanel(
            "Passaggio Obbligatorio (No Reshuffle)", 
            "Se il mazzo di pesca finisce, la partita termina in pareggio (senza rimescolare gli scarti).",
            false
        );
        mandatoryPassCheck = (JCheckBox) rule3.getClientProperty("checkbox");

        // 3. Bottone "Salva e Indietro"
        JButton backButton = createStyledButton("Salva e Torna al Menu");
        backButton.setMnemonic(KeyEvent.VK_B);
        backButton.addActionListener(e -> {
            if (observer != null) {
                // Nota: Qui invocherai un metodo del controller per tornare al menu (es. onBackToMenu)
                // Per ora simuliamo solo l'azione o usiamo un metodo esistente se adattato.
                System.out.println("Salvataggio regole e ritorno al menu...");
                // Esempio: observer.onBackToMenu(); 
            }
        });

        // 4. Assemblaggio
        contentPanel.add(title);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 40))); // Spazio
        contentPanel.add(rule1);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(rule2);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(rule3);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 50))); // Spazio prima del bottone
        contentPanel.add(backButton);

        add(contentPanel);
    }

    public void setObserver(MenuObserver observer) {
        this.observer = observer;
    }

    // --- METODI GETTER PER IL CONTROLLER ---
    
    public boolean isUnoPenaltyEnabled() {
        return unoPenaltyCheck.isSelected(); // Nota: La checkbox qui indica se la regola è attiva (quindi Penalità SI)
    }

    public boolean isSkipAfterDrawEnabled() {
        return skipAfterDrawCheck.isSelected();
    }

    public boolean isMandatoryPassEnabled() {
        return mandatoryPassCheck.isSelected();
    }

    // --- METODI HELPER GRAFICI ---

    /**
     * Crea un pannello orizzontale contenente Titolo+Descrizione a sinistra e Checkbox a destra.
     */
    private JPanel createRulePanel(String titleText, String descText, boolean defaultState) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(600, 80));
        panel.setPreferredSize(new Dimension(600, 80));
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(80, 80, 80))); // Linea separatrice

        // Parte sinistra: Testi
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(titleText);
        lblTitle.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_COLOR);

        JLabel lblDesc = new JLabel("<html><body style='width: 450px'>" + descText + "</body></html>");
        lblDesc.setFont(new Font("Helvetica Neue", Font.PLAIN, 12));
        lblDesc.setForeground(DESC_COLOR);

        textPanel.add(lblTitle);
        textPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        textPanel.add(lblDesc);

        // Parte destra: Checkbox
        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setSelected(defaultState);
        // Salviamo il riferimento alla checkbox come proprietà del pannello per recuperarlo nel costruttore
        panel.putClientProperty("checkbox", checkBox);

        panel.add(textPanel, BorderLayout.CENTER);
        panel.add(checkBox, BorderLayout.EAST);

        return panel;
    }

    /**
     * Stesso stile del bottone presente in MenuScene.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 82, 82));
                } else {
                    g2.setColor(BUTTON_COLOR);
                }
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
                g2.dispose();
            }
        };
        button.setFont(new Font("Helvetica Neue", Font.BOLD, 18));
        button.setForeground(BUTTON_TEXT_COLOR);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(350, 60));
        button.setPreferredSize(new Dimension(350, 60));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}