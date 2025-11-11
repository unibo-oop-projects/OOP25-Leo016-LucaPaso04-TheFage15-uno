package uno.Model.Cards.Deck;

import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.NumberedCard;
import uno.Model.Cards.Types.SkipCard;
import uno.Model.Cards.Types.ReverseCard;
import uno.Model.Cards.Types.DrawTwoCard;
import uno.Model.Cards.Types.WildCard;
import uno.Model.Cards.Types.WildDrawFourCard;


import java.util.Arrays;
import java.util.List;

/**
 * Rappresenta un mazzo di UNO classico da 108 carte.
 * Estende la classe Deck astratta e implementa il metodo
 * di creazione specifico per questa modalità di gioco.
 */
public class StandardDeck extends Deck<Card> {

    /**
     * Costruisce un nuovo mazzo standard.
     * Il costruttore della classe padre (Deck) chiamerà automaticamente
     * createDeck() e poi shuffle().
     */
    public StandardDeck() {
        super();
    }

    /**
     * Implementazione del metodo astratto per popolare la lista 'cards' 
     * (protetta nella classe padre) con le 108 carte del gioco classico,
     * utilizzando classi specifiche per ogni effetto.
     */
    @Override
    protected void createDeck() {
        // Lista dei 4 colori principali
        final List<CardColor> colors = Arrays.asList(
                CardColor.RED, 
                CardColor.BLUE, 
                CardColor.GREEN, 
                CardColor.YELLOW
        );

        // Lista dei valori numerici da 1 a 9
        final List<CardValue> numberValues = Arrays.asList(
                CardValue.ONE, CardValue.TWO, CardValue.THREE, CardValue.FOUR,
                CardValue.FIVE, CardValue.SIX, CardValue.SEVEN, CardValue.EIGHT, CardValue.NINE
        );

        // Itera sui 4 colori
        for (final CardColor color : colors) {
            
            // 1. Aggiunge una carta ZERO per ogni colore (4 carte totali)
            cards.add(new NumberedCard(color, CardValue.ZERO));

            // 2. Aggiunge due carte 1-9 per ogni colore (72 carte totali)
            for (final CardValue value : numberValues) {
                cards.add(new NumberedCard(color, value));
                cards.add(new NumberedCard(color, value));
            }

            // 3. Aggiunge due carte Azione (Salta, Inverti, PescaDue) per ogni colore (24 carte totali)
            //    usando le nuove classi specifiche.
            cards.add(new SkipCard(color));
            cards.add(new SkipCard(color));

            cards.add(new ReverseCard(color));
            cards.add(new ReverseCard(color));

            cards.add(new DrawTwoCard(color));
            cards.add(new DrawTwoCard(color));
        }

        // 4. Aggiunge 4 carte Jolly (Cambia Colore) e 4 Jolly Pesca Quattro (8 carte totali)
        //    usando le nuove classi specifiche.
        for (int i = 0; i < 4; i++) {
            cards.add(new WildCard());
            cards.add(new WildDrawFourCard());
        }
        
        // Totale: 4 (Zero) + 72 (1-9) + 24 (Azione) + 8 (Jolly) = 108 carte
    }
}