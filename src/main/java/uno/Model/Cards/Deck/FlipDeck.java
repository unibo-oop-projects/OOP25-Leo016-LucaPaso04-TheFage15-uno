package uno.Model.Cards.Deck;

import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.NumberedCard;
import uno.Model.Cards.Types.SkipCard;
import uno.Model.Cards.Types.SkipEveryoneCard;
import uno.Model.Cards.Types.ReverseCard;
import uno.Model.Cards.Types.DrawOneCard;
import uno.Model.Cards.Types.DrawTwoCard;
import uno.Model.Cards.Types.FlipCard;
import uno.Model.Cards.Types.WildCard;
import uno.Model.Cards.Types.WildDrawColorCard;
import uno.Model.Cards.Types.WildDrawFourCard;
import uno.Model.Cards.Types.WildDrawTwoCard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

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
        // 1. Converti i DTO in oggetti CardFace
        CardFace lightFace = createCardFace(mapping.light);
        CardFace darkFace = createCardFace(mapping.dark);

        // 2. Determina la classe corretta da istanziare in base al VALORE del lato Chiaro
        Card card;
        CardValue lightValue = lightFace.value();
        CardValue darkValue = darkFace.value();

        // --- 1. PRIORITÀ ALTA: JOLLY COMPLESSI (+4, +2, DRAW_COLOR) ---
        // Se un lato qualsiasi ha un Jolly complesso, usiamo la classe Jolly corrispondente.
        if (lightValue == CardValue.WILD_DRAW_TWO || darkValue == CardValue.WILD_DRAW_TWO) {
            card = new WildDrawTwoCard(lightFace, darkFace);
        } else if (lightValue == CardValue.WILD_DRAW_COLOR || darkValue == CardValue.WILD_DRAW_COLOR) {
             // Questa classe gestisce la giocabilità restrittiva e il cambio colore.
            card = new WildDrawColorCard(lightFace, darkFace);
        } 
        
        // --- 2. PRIORITÀ MEDIA: JOLLY STANDARD (WILD) ---
        // Se un lato qualsiasi è WILD, usiamo WildCard.
        else if (lightValue == CardValue.WILD || darkValue == CardValue.WILD) {
             // WildCard ha canBePlayedOn che restituisce sempre true, 
             // risolvendo il tuo problema di "Mossa non valida".
            card = new WildCard(lightFace, darkFace);
        }
        
        // --- 3. AZIONI SPECIALI (FLIP) ---
        else if (lightValue == CardValue.FLIP || darkValue == CardValue.FLIP) {
            card = new FlipCard(lightFace, darkFace);
        }
        
        // --- 4. AZIONI SEMPLICI (Devono controllare entrambi i lati) ---
        else if (lightValue == CardValue.SKIP || darkValue == CardValue.SKIP) {
            card = new SkipCard(lightFace, darkFace);
        } else if (lightValue == CardValue.REVERSE || darkValue == CardValue.REVERSE) {
            card = new ReverseCard(lightFace, darkFace);
        } else if (lightValue == CardValue.DRAW_ONE || darkValue == CardValue.DRAW_ONE) {
            card = new DrawOneCard(lightFace, darkFace);
        } else if (lightValue == CardValue.SKIP_EVERYONE || darkValue == CardValue.SKIP_EVERYONE) {
            card = new SkipEveryoneCard(lightFace, darkFace);
        }

        // --- 5. DEFAULT: CARTE NUMERATE ---
        else {
             // Inclusa la CardValue.ZERO che è un caso numerico.
            card = new NumberedCard(lightFace, darkFace);
        }

        cards.add(card);
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