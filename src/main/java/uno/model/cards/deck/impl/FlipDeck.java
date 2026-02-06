package uno.model.cards.deck.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

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
import uno.model.game.api.Game;
import uno.model.utils.api.GameLogger;

import java.util.Locale;

import uno.model.cards.dto.CardSide;
import uno.model.cards.dto.DoubleSidedEntryDTO;

/**
 * Represents the UNO Flip deck (112 cards).
 * This class loads the Light-to-Dark side mapping from a JSON resource file.
 * It maps specific behaviors (like {@link FlipBehavior} or
 * {@link WildBehavior})
 * based on the card values defined in the configuration.
 */
public class FlipDeck extends AbstractDeckImpl<Card> {

    private static final String RESOURCE_PATH = "/json/flipmap.json";

    private static final int DRAW_FIVE_COUNT = 5;

    /**
     * Constructs a new FlipDeck by loading the card mappings from JSON.
     * 
     * @param logger logger
     */
    public FlipDeck(final GameLogger logger) {
        super(logger);
        initializeDeck();
        shuffle();
    }

    /**
     * Reads the JSON configuration and populates the deck.
     */
    private void initializeDeck() {
        final Gson gson = new Gson();

        try (InputStream is = FlipDeck.class.getResourceAsStream(RESOURCE_PATH)) {

            try (Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                // Parse JSON array into DoubleSidedEntryDTO objects
                final DoubleSidedEntryDTO[] mappings = gson.fromJson(reader, DoubleSidedEntryDTO[].class);

                for (final DoubleSidedEntryDTO mapping : mappings) {
                    // Generates the card(s) based on the mapping count
                    addCardMappingToDeck(mapping);
                }
            }
        } catch (final IOException e) {
            this.getLogger().logError("Errore durante la lettura del file di configurazione: " + RESOURCE_PATH, e);
        }
    }

    /**
     * Creates Card instances from a mapping and adds them to the deck.
     * Handles multiple copies (count) if defined in JSON.
     * 
     * @param mapping The mapping defining light and dark sides and count.
     */
    private void addCardMappingToDeck(final DoubleSidedEntryDTO mapping) {
        // 1. Create Data Objects (Faces) locally
        final CardSide lightFace = new CardSide(
                CardColor.valueOf(mapping.getLight().getColor().toUpperCase(Locale.ROOT)),
                CardValue.valueOf(mapping.getLight().getValue().toUpperCase(Locale.ROOT)));

        final CardSide darkFace = new CardSide(
                CardColor.valueOf(mapping.getDark().getColor().toUpperCase(Locale.ROOT)),
                CardValue.valueOf(mapping.getDark().getValue().toUpperCase(Locale.ROOT)));

        // 2. Create Behaviors (Logic)
        final CardSideBehavior lightBehavior = createBehavior(lightFace);
        final CardSideBehavior darkBehavior = createBehavior(darkFace);

        // 3. Add copies to the deck based on 'count'
        final int quantity = (mapping.getCount() > 0) ? mapping.getCount() : 1; // Default to 1 if missing

        for (int i = 0; i < quantity; i++) {
            final Card card = new DoubleSidedCard(lightBehavior, darkBehavior);
            this.addCard(card); // Use the public method from DeckImpl
        }
    }

    /**
     * Factory method: Converts raw Card Data (Side) into Executable Logic
     * (Behavior).
     * 
     * @param side The card side data (color and value).
     * @return The corresponding CardSideBehavior instance.
     */
    private CardSideBehavior createBehavior(final CardSide side) {
        final CardColor c = side.color();
        final CardValue v = side.value();

        // --- A. WILD CARDS ---
        if (c == CardColor.WILD) {
            switch (v) {
                // LIGHT SIDE: Wild (Standard), Wild Draw 2
                case WILD:
                case WILD_DRAW_COLOR:
                    // Value, Draw, ColorChoice, DrawUntilColor, Skip
                    return new WildBehavior(v, 0);
                case WILD_DRAW_TWO:
                    // Draw 2, ColorChoice=True
                    return new WildBehavior(v, 2);
                default:
                    return new WildBehavior(v, 0);
            }
        }

        // --- B. ACTION & NUMBER CARDS ---
        switch (v) {
            // Draw Actions
            case DRAW_ONE:
                return new DrawBehavior(c, v, 1);
            case DRAW_TWO:
                return new DrawBehavior(c, v, 2);
            case DRAW_FIVE:
                return new DrawBehavior(c, v, DRAW_FIVE_COUNT); // Dark Side specific

            // Flow Actions
            case SKIP:
                return new ActionBehavior(c, v, g -> g.skipPlayers(1));

            case SKIP_EVERYONE:
                // Skips (PlayerCount - 1), effectively giving the turn back to self
                return new ActionBehavior(c, v, g -> g.skipPlayers(g.getPlayers().size() - 1));

            case REVERSE:
                return new ActionBehavior(c, v, Game::reversePlayOrder);

            // The FLIP Action
            case FLIP:
                return new FlipBehavior(c, v);

            // Numbers (0-9)
            default:
                return new NumericBehavior(c, v);
        }
    }
}
