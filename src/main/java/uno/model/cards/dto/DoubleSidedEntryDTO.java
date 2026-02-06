package uno.model.cards.dto;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for a deck entry containing double-sided card definitions.
 * Maps a Light side to a Dark side and specifies the number of copies.
 */
public class DoubleSidedEntryDTO {
    @SerializedName("light")
    private CardDTO light;

    @SerializedName("dark")
    private CardDTO dark;

    @SerializedName("count")
    private int count;

    /**
     * @return the DTO for the light side
     */
    public CardDTO getLight() {
        return light;
    }

    /**
     * @return the DTO for the dark side
     */
    public CardDTO getDark() {
        return dark;
    }

    /**
     * @return the number of copies of this card pair to add to the deck
     */
    public int getCount() {
        return count;
    }
}
