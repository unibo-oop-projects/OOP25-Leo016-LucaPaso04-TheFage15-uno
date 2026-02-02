package uno.view.utils.api;

import javax.swing.ImageIcon;

/**
 * Interface for the component responsible for retrieving card images.
 * It abstracts the logic of loading, caching, and processing image files.
 */
public interface CardImageLoader {

    /**
     * Retrieves the standard image for a specific card.
     *
     * @param cardName The unique identifier name of the card (e.g., "RED_ZERO").
     * @return The ImageIcon corresponding to the card, or Optional.empty() if not found.
     */
    ImageIcon getImage(String cardName);

    /**
     * Retrieves a semi-transparent version of the card image.
     * Useful for visual cues like disabled cards or "ghost" effects.
     *
     * @param cardName The unique identifier name of the card.
     * @return The transparent ImageIcon, or Optional.empty() if not found.
     */
    ImageIcon getTransparentImage(String cardName);
}
