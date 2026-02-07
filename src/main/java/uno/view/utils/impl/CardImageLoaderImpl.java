package uno.view.utils.impl;

import uno.view.utils.api.CardImageLoader;

import javax.swing.ImageIcon;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Concrete implementation of CardImageLoader.
 * Handles loading images from resources, scaling them to specific dimensions,
 * and caching the results to improve performance.
 */
public class CardImageLoaderImpl implements CardImageLoader {

    /**
     * Static list of all expected image filenames in the resource folder.
     */
    private static final List<String> CARD_NAMES = Arrays.asList(

            "RED_ZERO", "RED_ONE", "RED_TWO", "RED_THREE", "RED_FOUR", "RED_FIVE", "RED_SIX",
            "RED_SEVEN", "RED_EIGHT", "RED_NINE",
            "RED_SKIP", "RED_REVERSE", "RED_DRAW_TWO", "RED_FLIP", "RED_DRAW_ONE",
            "GREEN_ZERO", "GREEN_ONE", "GREEN_TWO", "GREEN_THREE", "GREEN_FOUR", "GREEN_FIVE", "GREEN_SIX",
            "GREEN_SEVEN", "GREEN_EIGHT", "GREEN_NINE",
            "GREEN_SKIP", "GREEN_REVERSE", "GREEN_DRAW_TWO", "GREEN_FLIP", "GREEN_DRAW_ONE",
            "BLUE_ZERO", "BLUE_ONE", "BLUE_TWO", "BLUE_THREE", "BLUE_FOUR", "BLUE_FIVE", "BLUE_SIX",
            "BLUE_SEVEN", "BLUE_EIGHT", "BLUE_NINE",
            "BLUE_SKIP", "BLUE_REVERSE", "BLUE_DRAW_TWO", "BLUE_FLIP", "BLUE_DRAW_ONE",
            "YELLOW_ZERO", "YELLOW_ONE", "YELLOW_TWO", "YELLOW_THREE", "YELLOW_FOUR", "YELLOW_FIVE",
            "YELLOW_SIX", "YELLOW_SEVEN", "YELLOW_EIGHT", "YELLOW_NINE",
            "YELLOW_SKIP", "YELLOW_REVERSE", "YELLOW_DRAW_TWO", "YELLOW_FLIP", "YELLOW_DRAW_ONE",

            "PINK_ONE", "PINK_TWO", "PINK_THREE", "PINK_FOUR", "PINK_FIVE", "PINK_SIX",
            "PINK_SEVEN", "PINK_EIGHT", "PINK_NINE",
            "PINK_REVERSE", "PINK_FLIP", "PINK_DRAW_FIVE", "PINK_SKIP_EVERYONE",
            "TEAL_ONE", "TEAL_TWO", "TEAL_THREE", "TEAL_FOUR", "TEAL_FIVE", "TEAL_SIX",
            "TEAL_SEVEN", "TEAL_EIGHT", "TEAL_NINE",
            "TEAL_REVERSE", "TEAL_FLIP", "TEAL_DRAW_FIVE", "TEAL_SKIP_EVERYONE",
            "ORANGE_ONE", "ORANGE_TWO", "ORANGE_THREE", "ORANGE_FOUR", "ORANGE_FIVE", "ORANGE_SIX",
            "ORANGE_SEVEN", "ORANGE_EIGHT", "ORANGE_NINE",
            "ORANGE_REVERSE", "ORANGE_FLIP", "ORANGE_DRAW_FIVE", "ORANGE_SKIP_EVERYONE",
            "PURPLE_ONE", "PURPLE_TWO", "PURPLE_THREE", "PURPLE_FOUR", "PURPLE_FIVE", "PURPLE_SIX",
            "PURPLE_SEVEN", "PURPLE_EIGHT", "PURPLE_NINE",
            "PURPLE_REVERSE", "PURPLE_FLIP", "PURPLE_DRAW_FIVE", "PURPLE_SKIP_EVERYONE",

            "WILD_WILD", "WILD_WILD_DARK", "WILD_WILD_DRAW_FOUR", "WILD_WILD_DRAW_TWO", "WILD_WILD_DRAW_COLOR",
            "WILD_WILD_FORCED_SWAP", "WILD_WILD_ALLWILD", "WILD_WILD_DRAW_FOUR_ALLWILD",
            "WILD_WILD_DRAW_TWO_ALLWILD", "WILD_WILD_TARGETED_DRAW_TWO", "WILD_WILD_REVERSE",
            "WILD_WILD_SKIP", "WILD_WILD_SKIP_TWO",

            "CARD_BACK");

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger
            .getLogger(CardImageLoaderImpl.class.getName());

    private final Map<String, ImageIcon> cardImageCache;
    private final Map<String, ImageIcon> transparentImageCache;
    private final int cardWidth;
    private final int cardHeight;

    /**
     * Constructs and initializes the loader.
     * All images are pre-loaded into memory upon creation.
     *
     * @param cardWidth  Desired width for the card icons.
     * @param cardHeight Desired height for the card icons.
     */
    public CardImageLoaderImpl(final int cardWidth, final int cardHeight) {
        this.cardWidth = cardWidth;
        this.cardHeight = cardHeight;
        this.cardImageCache = new HashMap<>();
        this.transparentImageCache = new HashMap<>();
        loadAllImages();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageIcon getImage(final String cardName) {
        return cardImageCache.get(cardName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageIcon getTransparentImage(final String cardName) {
        return transparentImageCache.get(cardName);
    }

    /**
     * Iterates through the static list and loads each image file.
     */
    private void loadAllImages() {
        for (final String cardName : CARD_NAMES) {
            loadImage(cardName);
        }
    }

    /**
     * Helper method to load, scale, and store a single image.
     * 
     * @param cardName The unique identifier name of the card.
     */
    private void loadImage(final String cardName) {
        final String path = "/images/cards/" + cardName + ".png";

        Optional.ofNullable(CardImageLoaderImpl.class.getResource(path))
                .ifPresentOrElse(
                        resource -> {
                            final ImageIcon icon = new ImageIcon(resource);
                            final Image scaledImg = icon.getImage().getScaledInstance(cardWidth, cardHeight,
                                    Image.SCALE_SMOOTH);
                            final ImageIcon scaledIcon = new ImageIcon(scaledImg);

                            cardImageCache.put(cardName, scaledIcon);
                            transparentImageCache.put(cardName, createTransparentIcon(scaledIcon, 0.5f));
                        },
                        () -> LOGGER.warning("Risorsa non trovata: " + path));
    }

    /**
     * Creates a semi-transparent copy of an existing ImageIcon.
     * 
     * @param original The original ImageIcon.
     * @param alpha    The desired transparency level (0.0f to 1.0f).
     * @return A new ImageIcon with the specified transparency.
     */
    private ImageIcon createTransparentIcon(final ImageIcon original, final float alpha) {
        final Image originalImage = original.getImage();
        final int width = original.getIconWidth();
        final int height = original.getIconHeight();

        final BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g2d = newImage.createGraphics();

        // Set opacity
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();

        return new ImageIcon(newImage);
    }
}
