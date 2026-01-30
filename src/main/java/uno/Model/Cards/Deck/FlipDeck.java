package uno.model.cards.deck;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import uno.model.cards.Card;
import uno.model.cards.attributes.CardColor;
import uno.model.cards.attributes.CardFace;
import uno.model.cards.attributes.CardValue;
import uno.model.cards.behaviors.*;
import uno.model.cards.types.DoubleSidedCard;

/**
 * Rappresenta un mazzo di UNO classico da 108 carte.
 * Estende la classe Deck astratta e implementa il metodo
 * di creazione specifico per questa modalità di gioco.
 */
public class FlipDeck extends Deck<Card> {

    /**
     * Costruisce un nuovo mazzo standard.
     * Il costruttore della classe padre (Deck) chiamerà automaticamente
     * createDeck() e poi shuffle().
     */
    public FlipDeck() {
        super();
    }

    /**
     * Implementazione del metodo astratto per popolare la lista 'cards' 
     * (protetta nella classe padre) con le 108 carte del gioco classico,
     * utilizzando classi specifiche per ogni effetto.
     */
    @Override
    protected void createDeck() {
        Gson gson = new Gson();
        // Cerca il file JSON nella cartella delle risorse
        String resourcePath = "/json/flipmap.json";

        try (InputStream is = getClass().getResourceAsStream(resourcePath);
            Reader reader = new InputStreamReader(is)) {

            if (is == null) {
                throw new IOException("File non trovato nelle risorse: " + resourcePath);
            }
            
            // Legge il file JSON in un array di oggetti Mapping
            Mapping[] mappings = gson.fromJson(reader, Mapping[].class);

            // Aggiunge le carte al mazzo
            for (Mapping mapping : mappings) {
                // Instanzia una o più copie della carta basandosi sulla singola mappatura
                addCardMappingToDeck(mapping);
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("FATALE: Errore di caricamento o parsing di flipmap.json. Impossibile creare il mazzo Flip.");
            e.printStackTrace();
        }
    }

    /**
     * Crea un'istanza Card dal Mapping letto e la aggiunge al mazzo il numero corretto di volte.
     */
    private void addCardMappingToDeck(Mapping mapping) {
        // 1. Converti il JSON config in oggetti CardFace
        CardFace lightFace = createCardFace(mapping.light);
        CardFace darkFace = createCardFace(mapping.dark);

        // 2. Crea i comportamenti (Behaviors) per ogni lato
        CardSideBehavior lightBehavior = createBehavior(lightFace);
        CardSideBehavior darkBehavior = createBehavior(darkFace);

        // 3. Crea la carta composta
        Card card = new DoubleSidedCard(lightBehavior, darkBehavior);

        // 4. Aggiungi al mazzo (loop se ci sono copie multiple, se gestito)
        cards.add(card);
    }

    /**
     * Questo metodo sostituisce lo SWITCH gigante di AbstractCard e FlipDeck.
     * Converte Dati -> Comportamento.
     */
    private CardSideBehavior createBehavior(CardFace face) {
        CardColor c = face.color();
        CardValue v = face.value();

        // --- A. CARTE JOLLY (WILD) ---
        if (c == CardColor.WILD) {
            switch (v) {
                case WILD_DRAW_TWO:      return new WildBehavior(v, 2);
                case WILD_DRAW_FOUR:     return new WildBehavior(v, 4);
                case WILD_DRAW_COLOR:    return new WildBehavior(v, true); // Logica specifica
                case WILD:               return new WildBehavior(v, 0);
                // Aggiungi qui altri Jolly AllWild se servono
                default:                 return new WildBehavior(v, 0);
            }
        }

        // --- B. CARTE AZIONE E NUMERICHE ---
        switch (v) {
            // 1. Pesca
            case DRAW_ONE:      return new DrawBehavior(c, v, 1);
            case DRAW_TWO:      return new DrawBehavior(c, v, 2);
            case DRAW_FIVE:     return new DrawBehavior(c, v, 5);
            
            // 2. Azioni semplici
            case SKIP:          return new ActionBehavior(c, v, (g) -> g.skipPlayers(1));
            case SKIP_EVERYONE: return new ActionBehavior(c, v, (g) -> g.skipPlayers(g.getPlayers().size() - 1)); // O metodo specifico
            case REVERSE:       return new ActionBehavior(c, v, (g) -> g.reversePlayOrder());
            
            // 3. Flip
            case FLIP: 
                // Passiamo 'null' o gestiamo il riferimento alla carta? 
                // FlipAction ha bisogno della carta stessa per passarla a game.flipTheWorld(card).
                // Visto che qui stiamo creando il comportamento PRIMA della carta, usiamo un trucco
                // oppure cambiamo Game.flipTheWorld per non richiedere la carta, ma solo il colore.
                // Game.flipTheWorld usa la carta solo per: this.currentColor = card.getColor(this);
                // Possiamo definire una classe specifica per il Flip che si inietta dopo o che passa "this" dinamicamente.
                return new FlipBehavior(c, v);

            // 4. Numeri
            default:            return new NumericBehavior(c, v);
        }
    }

    /**
     * Metodo helper per convertire CardConfig (dal JSON) in CardFace.
     */
    private CardFace createCardFace(CardConfig config) {
        // Usa valueOf per convertire le stringhe del JSON negli Enum
        CardColor color = CardColor.valueOf(config.color.toUpperCase());
        CardValue value = CardValue.valueOf(config.value.toUpperCase());
        return new CardFace(color, value);
    }

    // =========================================================================
    // CLASSI DTO PER IL PARSING JSON
    // TODO: SPOSTALE IN UN FILE SEPARATO SE NECESSARIO
    // =========================================================================

    /**
     * Rappresenta la struttura dati di un singolo lato (Chiaro o Scuro) nel JSON.
     */
    private static class CardConfig {
        @SerializedName("color")
        String color;
        @SerializedName("value")
        String value;
    }

    /**
     * Rappresenta l'intera mappatura (Light <-> Dark) per un tipo di carta.
     */
    private static class Mapping {
        @SerializedName("light")
        CardConfig light;
        @SerializedName("dark")
        CardConfig dark;
        
        // Opzionale: puoi aggiungere qui il numero di copie (es. "count": 2) se il JSON lo includesse.
    }
}