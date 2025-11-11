package uno.Model.Cards.Deck;

import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.NumberedCard;
import uno.Model.Cards.Types.SkipCard;
import uno.Model.Cards.Types.ReverseCard;
import uno.Model.Cards.Types.DrawTwoCard;
import uno.Model.Cards.Types.FlipCard;
import uno.Model.Cards.Types.WildCard;
import uno.Model.Cards.Types.WildDrawFourCard;

import java.util.Arrays;
import java.util.List;

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
        // Lista dei 4 colori principali (come lato chiaro)
        List<CardColor> lightColors = Arrays.asList(CardColor.RED, CardColor.YELLOW, CardColor.GREEN, CardColor.BLUE);
        // Valori numerici da 1 a 9
        List<CardValue> numberValues = Arrays.asList(
                CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR,
                CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE
        );

        // Mappa per definire la controparte scura (Dark Side) di ogni colore
        // NOTA: In un gioco reale, i valori/effetti cambierebbero, qui solo i colori.
        for (final CardColor lightColor : lightColors) {
            CardColor darkColor;
            
            // Implementazione della logica di swap richiesta dall'utente
            if (lightColor == CardColor.RED) darkColor = CardColor.YELLOW;
            else if (lightColor == CardColor.YELLOW) darkColor = CardColor.RED;
            else if (lightColor == CardColor.BLUE) darkColor = CardColor.GREEN;
            else if (lightColor == CardColor.GREEN) darkColor = CardColor.BLUE;
            else continue; // Salta i Jolly se per errore sono inclusi qui
            
            // --- A. Carte Numeriche ---

            // 1. Aggiunge una carta ZERO (1x)
            CardFace lightZero = new CardFace(lightColor, CardValue.ZERO);
            CardFace darkZero = new CardFace(darkColor, CardValue.ZERO);
            this.cards.add(new NumberedCard(lightZero, darkZero));
            
            // 2. Aggiunge due carte 1-9 (2x)
            for (final CardValue value : numberValues) {
                CardFace lightFace = new CardFace(lightColor, value);
                CardFace darkFace = new CardFace(darkColor, value);
                this.cards.add(new NumberedCard(lightFace, darkFace));
                this.cards.add(new NumberedCard(lightFace, darkFace));
            }
            
            // --- B. Carte Azione Standard (2x per tipo) ---
            
            // SKIP
            CardFace lightSkip = new CardFace(lightColor, CardValue.SKIP);
            CardFace darkSkip = new CardFace(darkColor, CardValue.SKIP); 
            this.cards.add(new SkipCard(lightSkip, darkSkip));
            this.cards.add(new SkipCard(lightSkip, darkSkip));

            // REVERSE
            CardFace lightReverse = new CardFace(lightColor, CardValue.REVERSE);
            CardFace darkReverse = new CardFace(darkColor, CardValue.REVERSE);
            this.cards.add(new ReverseCard(lightReverse, darkReverse));
            this.cards.add(new ReverseCard(lightReverse, darkReverse));

            // DRAW_TWO
            CardFace lightDrawTwo = new CardFace(lightColor, CardValue.DRAW_TWO);
            CardFace darkDrawTwo = new CardFace(darkColor, CardValue.DRAW_TWO);
            this.cards.add(new DrawTwoCard(lightDrawTwo, darkDrawTwo));
            this.cards.add(new DrawTwoCard(lightDrawTwo, darkDrawTwo));
            
            // --- C. Flip Card (2x per colore) ---
            // L'effetto FlipCard fa game.flipTheWorld() su entrambi i lati.
            CardFace lightFlip = new CardFace(lightColor, CardValue.FLIP);
            CardFace darkFlip = new CardFace(darkColor, CardValue.FLIP); // La carta Flip girata ha un colore diverso
            this.cards.add(new FlipCard(lightFlip, darkFlip));
            this.cards.add(new FlipCard(lightFlip, darkFlip));
        }

        // --- 3. Carte Jolly ---
        
        // WILD (4x)
        CardFace lightWild = new CardFace(CardColor.WILD, CardValue.WILD);
        // Lato scuro di Wild: assumiamo l'equivalente di WILD_DRAW_FOUR (come da mappa precedente in Game.java)
        CardFace darkWild = new CardFace(CardColor.WILD, CardValue.WILD_DRAW_FOUR);

        for (int i = 0; i < 4; i++) {
            // Wild (Light) <-> Wild Draw Four (Dark)
            this.cards.add(new WildCard(lightWild, darkWild));
        }

        // WILD_DRAW_FOUR (4x)
        CardFace lightWDF = new CardFace(CardColor.WILD, CardValue.WILD_DRAW_FOUR);
        // Lato scuro di Wild Draw Four: assumiamo l'equivalente di WILD
        CardFace darkWDF = new CardFace(CardColor.WILD, CardValue.WILD);
        
        for (int i = 0; i < 4; i++) {
            // Wild Draw Four (Light) <-> Wild (Dark)
            this.cards.add(new WildDrawFourCard(lightWDF, darkWDF));
        }
    }
}