package uno.view.components;

import uno.controller.api.GameViewObserver;
import uno.model.cards.attributes.CardColor;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List; // Import necessario

/**
 * Un pannello che appare quando un giocatore deve scegliere un colore.
 */
public class ColorChooserPanel extends JPanel implements ActionListener {

    private final GameViewObserver observer;

    private static final Color PANEL_COLOR = new Color(50, 50, 50);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Font BOLD_FONT = new Font("Arial", Font.BOLD, 14);

    // MODIFICA: Il costruttore ora accetta un flag per determinare i colori.
    public ColorChooserPanel(GameViewObserver observer, boolean isDarkSide) { // <-- NUOVO PARAMETRO
        this.observer = observer;

        // CAMBIA LAYOUT: 2 righe, 2 colonne, con 5 pixel di spazio (verticale e orizzontale)
        setLayout(new GridLayout(2, 2, 10, 10)); // Ho messo 10px per maggiore visibilità

        setBackground(PANEL_COLOR);
        setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Scegli Colore",
            TitledBorder.LEFT, TitledBorder.TOP, BOLD_FONT, TEXT_COLOR
        ));

        // 1. Determina i colori da offrire in base al lato del gioco.
        List<CardColor> colorsToOffer = isDarkSide ? 
            List.of(CardColor.PINK, CardColor.TEAL, CardColor.ORANGE, CardColor.PURPLE) :
            List.of(CardColor.RED, CardColor.GREEN, CardColor.BLUE, CardColor.YELLOW);
            
        // 2. Aggiunge i pulsanti dinamicamente (GridLayout li posiziona in griglia)
        for (CardColor colorEnum : colorsToOffer) {
            Color awtColor;
            Color textColor;
            String text;

            // Mappa l'enum CardColor a un colore AWT e testo
            if (isDarkSide) {
                switch (colorEnum) {
                    case PINK: awtColor = new Color(255, 105, 180); textColor = Color.BLACK; text = "ROSA"; break;
                    case TEAL: awtColor = new Color(0, 128, 128); textColor = Color.WHITE; text = "VERDE ACQUA"; break;
                    case ORANGE: awtColor = new Color(255, 140, 0); textColor = Color.BLACK; text = "ARANCIO"; break;
                    case PURPLE: awtColor = new Color(153, 50, 204); textColor = Color.WHITE; text = "VIOLA"; break;
                    default: continue;
                }
            } else {
                switch (colorEnum) {
                    case RED: awtColor = new Color(211, 47, 47); textColor = Color.WHITE; text = "ROSSO"; break;
                    case GREEN: awtColor = new Color(76, 175, 80); textColor = Color.WHITE; text = "VERDE"; break;
                    case BLUE: awtColor = new Color(33, 150, 243); textColor = Color.WHITE; text = "BLU"; break;
                    case YELLOW: awtColor = new Color(255, 235, 59); textColor = Color.BLACK; text = "GIALLO"; break;
                    default: continue;
                }
            }
            
            JButton button = createColorButton(text, awtColor, textColor, colorEnum);
            add(button); // Aggiunge direttamente il bottone
            // Rimosso: add(Box.createRigidArea(new Dimension(0, 5)));
        }
        
        // Imposta una dimensione preferita che funziona bene con 2x2
        setPreferredSize(new Dimension(220, 150));
    }
    
    /**
     * Metodo helper per creare e configurare i bottoni colorati, associando il CardColor.
     */
    private JButton createColorButton(String text, Color bgColor, Color fgColor, CardColor colorEnum) {
        JButton button = new JButton(text);
        button.setFont(BOLD_FONT);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        
        // Imposta le dimensioni in modo che i pulsanti siano grossomodo quadrati
        Dimension buttonSize = new Dimension(100, 50); 
        button.setMaximumSize(buttonSize);
        button.setPreferredSize(buttonSize);
        
        button.setActionCommand(colorEnum.name());
        button.addActionListener(this); 
        
        return button;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // Usa getActionCommand per recuperare il nome dell'enum
        String command = e.getActionCommand();
        CardColor chosenColor = CardColor.valueOf(command);
        
        if (chosenColor != null) {
            // 1. Notifica il controller
            if (observer != null) {
                observer.onColorChosen(chosenColor);
            }
            
            // 2. Chiude il popup
            Window parentDialog = SwingUtilities.getWindowAncestor(this);
            if (parentDialog != null) {
                parentDialog.dispose(); 
            }
        }
    }
}