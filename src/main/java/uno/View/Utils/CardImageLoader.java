package uno.View.Utils;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Gestisce il caricamento, lo scaling e la cache di tutte le immagini
 * delle carte utilizzate nel gioco.
 */
public class CardImageLoader {

    private final Map<String, ImageIcon> cardImageCache;
    private final Map<String, ImageIcon> transparentImageCache;
    private final int cardWidth;
    private final int cardHeight;

    /**
     * Lista statica di tutti i nomi di file delle immagini delle carte da caricare.
     */
    private static final List<String> CARD_NAMES = Arrays.asList(
            // Lato Chiaro (Standard)
            "RED_ZERO", "RED_ONE", "RED_TWO", "RED_THREE", "RED_FOUR", "RED_FIVE", "RED_SIX", "RED_SEVEN", "RED_EIGHT", "RED_NINE",
            "RED_SKIP", "RED_REVERSE", "RED_DRAW_TWO", "RED_FLIP", "RED_DRAW_ONE",
            "GREEN_ZERO", "GREEN_ONE", "GREEN_TWO", "GREEN_THREE", "GREEN_FOUR", "GREEN_FIVE", "GREEN_SIX", "GREEN_SEVEN", "GREEN_EIGHT", "GREEN_NINE",
            "GREEN_SKIP", "GREEN_REVERSE", "GREEN_DRAW_TWO", "GREEN_FLIP", "GREEN_DRAW_ONE",
            "BLUE_ZERO", "BLUE_ONE", "BLUE_TWO", "BLUE_THREE", "BLUE_FOUR", "BLUE_FIVE", "BLUE_SIX", "BLUE_SEVEN", "BLUE_EIGHT", "BLUE_NINE",
            "BLUE_SKIP", "BLUE_REVERSE", "BLUE_DRAW_TWO", "BLUE_FLIP", "BLUE_DRAW_ONE",
            "YELLOW_ZERO", "YELLOW_ONE", "YELLOW_TWO", "YELLOW_THREE", "YELLOW_FOUR", "YELLOW_FIVE", "YELLOW_SIX", "YELLOW_SEVEN", "YELLOW_EIGHT", "YELLOW_NINE",
            "YELLOW_SKIP", "YELLOW_REVERSE", "YELLOW_DRAW_TWO", "YELLOW_FLIP", "YELLOW_DRAW_ONE",
            
            // Lato Scuro (Flip)
            "PINK_ONE", "PINK_TWO", "PINK_THREE", "PINK_FOUR", "PINK_FIVE", "PINK_SIX", "PINK_SEVEN", "PINK_EIGHT", "PINK_NINE",
            "PINK_REVERSE", "PINK_FLIP", "PINK_DRAW_FIVE", "PINK_SKIP_EVERYONE",
            "TEAL_ONE", "TEAL_TWO", "TEAL_THREE", "TEAL_FOUR", "TEAL_FIVE", "TEAL_SIX", "TEAL_SEVEN", "TEAL_EIGHT", "TEAL_NINE",
            "TEAL_REVERSE", "TEAL_FLIP", "TEAL_DRAW_FIVE", "TEAL_SKIP_EVERYONE",
            "ORANGE_ONE", "ORANGE_TWO", "ORANGE_THREE", "ORANGE_FOUR", "ORANGE_FIVE", "ORANGE_SIX", "ORANGE_SEVEN", "ORANGE_EIGHT", "ORANGE_NINE",
            "ORANGE_REVERSE", "ORANGE_FLIP", "ORANGE_DRAW_FIVE", "ORANGE_SKIP_EVERYONE",
            "PURPLE_ONE", "PURPLE_TWO", "PURPLE_THREE", "PURPLE_FOUR", "PURPLE_FIVE", "PURPLE_SIX", "PURPLE_SEVEN", "PURPLE_EIGHT", "PURPLE_NINE",
            "PURPLE_REVERSE", "PURPLE_FLIP", "PURPLE_DRAW_FIVE", "PURPLE_SKIP_EVERYONE",
            
            // Carte Jolly
            "WILD_WILD", "WILD_WILD_DARK", "WILD_WILD_DRAW_FOUR", "WILD_WILD_DRAW_TWO", "WILD_WILD_DRAW_COLOR", 
            "WILD_WILD_FORCED_SWAP", "WILD_WILD_ALLWILD", "WILD_WILD_DRAW_FOUR_ALLWILD", "WILD_WILD_DRAW_TWO_ALLWILD", "WILD_WILD_TARGETED_DRAW_TWO", "WILD_WILD_REVERSE",
            "WILD_WILD_SKIP", "WILD_WILD_SKIP_TWO",
            
            // Dorso
            "CARD_BACK"
    );

    /**
     * Costruisce e inizializza il loader caricando tutte le immagini.
     * @param cardWidth Larghezza desiderata per le icone.
     * @param cardHeight Altezza desiderata per le icone.
     */
    public CardImageLoader(int cardWidth, int cardHeight) {
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.cardImageCache = new HashMap<>();
        this.transparentImageCache = new HashMap<>();
        loadAllImages(); // Carica tutto al momento della creazione
    }

    /**
     * Restituisce l'icona della carta richiesta.
     */
    public ImageIcon getImage(String cardName) {
        return cardImageCache.get(cardName);
    }

    /**
     * Restituisce l'icona trasparente (disabilitata) della carta richiesta.
     */
    public ImageIcon getTransparentImage(String cardName) {
        return transparentImageCache.get(cardName);
    }

    /**
     * Itera sulla lista statica e carica ogni immagine.
     */
    private void loadAllImages() {
        for (String cardName : CARD_NAMES) {
            loadImage(cardName);
        }
    }

    /**
     * Metodo helper per caricare, scalare e memorizzare una singola immagine.
     */
    private void loadImage(String cardName) {
        try {
            String path = "/images/cards/" + cardName + ".png";
            URL imgURL = getClass().getResource(path);

            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image scaledImg = icon.getImage().getScaledInstance(cardWidth, cardHeight, Image.SCALE_SMOOTH);
                ImageIcon scaledIcon = new ImageIcon(scaledImg);
                
                // Salva nella cache normale
                cardImageCache.put(cardName, scaledIcon);
                // Salva nella cache trasparente
                transparentImageCache.put(cardName, createTransparentIcon(scaledIcon, 0.5f));
            } else {
                System.err.println("Immagine non trovata: " + path);
            }
        } catch (Exception e) {
            System.err.println("Errore durante il caricamento di: " + cardName);
            e.printStackTrace();
        }
    }

    /**
     * Crea una versione semitrasparente di una ImageIcon.
     */
    private ImageIcon createTransparentIcon(ImageIcon original, float alpha) {
        Image originalImage = original.getImage();
        int width = original.getIconWidth();
        int height = original.getIconHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return new ImageIcon(newImage);
    }
}