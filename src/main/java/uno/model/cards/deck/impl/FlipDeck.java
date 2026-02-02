package uno.model.cards.deck.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.api.CardSideBehavior;
import uno.model.cards.behaviors.impl.ActionBehavior;
import uno.model.cards.behaviors.impl.DrawBehavior;
import uno.model.cards.behaviors.impl.FlipBehavior;
import uno.model.cards.behaviors.impl.NumericBehavior;
import uno.model.cards.behaviors.impl.WildBehavior;
import uno.model.cards.types.api.Card;
import uno.model.cards.types.impl.DoubleSidedCard;

/**
 * Represents the UNO Flip deck (112 cards).
 * This class loads the Light-to-Dark side mapping from a JSON resource file.
 * It maps specific behaviors (like {@link FlipBehavior} or {@link WildBehavior})
 * based on the card values defined in the configuration.
 */
public class FlipDeck extends DeckImpl<Card> {

    private static final String RESOURCE_PATH = "/json/flipmap.json";

    private static final int DRAW_FIVE_COUNT = 5;

    /**
     * Constructs a new FlipDeck by loading the card mappings from JSON.
     */
    public FlipDeck() {
        super();
        initializeDeck();
        shuffle();
    }

    /**
     * Reads the JSON configuration and populates the deck.
     */
    private void initializeDeck() {
        final Gson gson = new Gson();

        try (InputStream is = getClass().getResourceAsStream(RESOURCE_PATH)) {
            if (is == null) {
                throw new IOException("Resource not found: " + RESOURCE_PATH);
            }

            try (Reader reader = new InputStreamReader(is)) {
                // Parse JSON array into Mapping objects
                final Mapping[] mappings = gson.fromJson(reader, Mapping[].class);

                if (mappings == null) {
                    throw new IOException("JSON is empty or invalid.");
                }

                for (final Mapping mapping : mappings) {
                    // Generates the card(s) based on the mapping count
                    addCardMappingToDeck(mapping);
                }
            }
        } catch (final IOException e) {
            System.err.println("CRITICAL ERROR: Failed to load UNO Flip Deck. " + e.getMessage());
            e.printStackTrace();
            // In a real app, you might want to fallback to a hardcoded generation here
        }
    }

    /**
     * Creates Card instances from a mapping and adds them to the deck.
     * Handles multiple copies (count) if defined in JSON.
     * @param mapping The mapping defining light and dark sides and count.
     */
    private void addCardMappingToDeck(final Mapping mapping) {
        // 1. Create Data Objects (Faces)
        final CardFace lightFace = new CardFace(
            CardColor.valueOf(mapping.light.color.toUpperCase()),
            CardValue.valueOf(mapping.light.value.toUpperCase())
        );

        final CardFace darkFace = new CardFace(
            CardColor.valueOf(mapping.dark.color.toUpperCase()),
            CardValue.valueOf(mapping.dark.value.toUpperCase())
        );

        // 2. Create Behaviors (Logic)
        final CardSideBehavior lightBehavior = createBehavior(lightFace);
        final CardSideBehavior darkBehavior = createBehavior(darkFace);

        // 3. Add copies to the deck based on 'count'
        final int quantity = (mapping.count > 0) ? mapping.count : 1; // Default to 1 if missing

        for (int i = 0; i < quantity; i++) {
            final Card card = new DoubleSidedCard(lightBehavior, darkBehavior);
            this.addCard(card); // Use the public method from DeckImpl
        }
    }

    /**
     * Factory method: Converts raw Card Data (Face) into Executable Logic (Behavior).
     * @param face The card face data (color and value).
     * @return The corresponding CardSideBehavior instance.
     */
    private CardSideBehavior createBehavior(final CardFace face) {
        final CardColor c = face.color();
        final CardValue v = face.value();

        // --- A. WILD CARDS ---
        if (c == CardColor.WILD || c == CardColor.WILD) {
            switch (v) {
                // LIGHT SIDE: Wild (Standard), Wild Draw 2
                case WILD:
                    // Value, Draw, ColorChoice, DrawUntilColor, Skip
                    return new WildBehavior(v, 0); 
                case WILD_DRAW_TWO:
                     // Draw 2, ColorChoice=True
                    return new WildBehavior(v, 2);

                // DARK SIDE: Wild (Standard), Wild Draw Color
                case WILD_DRAW_COLOR:
                    // The "Mean" Card: Draw ?, ColorChoice=True, DrawUntilColor=TRUE, Skip=1
                    return new WildBehavior(v, 0);

                default:
                    return new WildBehavior(v, 0);
            }
        }

        // --- B. ACTION & NUMBER CARDS ---
        switch (v) {
            // Draw Actions
            case DRAW_ONE:      return new DrawBehavior(c, v, 1);
            case DRAW_TWO:      return new DrawBehavior(c, v, 2);
            case DRAW_FIVE:     return new DrawBehavior(c, v, DRAW_FIVE_COUNT); // Dark Side specific

            // Flow Actions
            case SKIP:
                return new ActionBehavior(c, v, g -> g.skipPlayers(1));

            case SKIP_EVERYONE: 
                // Skips (PlayerCount - 1), effectively giving the turn back to self
                return new ActionBehavior(c, v, g -> g.skipPlayers(g.getPlayers().size() - 1));

            case REVERSE:
                return new ActionBehavior(c, v, g -> g.reversePlayOrder());

            // The FLIP Action
            case FLIP:
                return new FlipBehavior(c, v);

            // Numbers (0-9)
            default:
                return new NumericBehavior(c, v);
        }
    }

    // =========================================================================
    // DTOs (Data Transfer Objects) for JSON Parsing
    // =========================================================================

    // Simple Record or Class to hold color/value pair temporarily
    private record CardFace(CardColor color, CardValue value) {

    }

    private static final class CardConfig {
        @SerializedName("color") 
        private String color;
        @SerializedName("value") 
        private String value;
    }

    private static final class Mapping {
        @SerializedName("light") 
        private CardConfig light;
        @SerializedName("dark")
        private CardConfig dark;
        @SerializedName("count") 
        private int count;
    }
}
