package uno.Model.Cards.Deck;

import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardFace;
import uno.Model.Cards.Attributes.CardValue;
import uno.Model.Cards.Card;
import uno.Model.Cards.Types.*;


import java.util.Arrays;
import java.util.List;

/**
 * Rappresenta un mazzo di UNO classico da 108 carte.
 * Estende la classe Deck astratta e implementa il metodo
 * di creazione specifico per questa modalità di gioco.
 */
public class AllWildDeck extends Deck<Card> {

    /**
     * Costruisce un nuovo mazzo All Wild.
     * Il costruttore della classe padre (Deck) chiamerà automaticamente
     * createDeck() e poi shuffle().
     */
    public AllWildDeck() {
        super();
    }

    /**
     * Implementazione del metodo astratto per popolare la lista 'cards' 
     * (protetta nella classe padre) con le 112 carte del gioco classico,
     * utilizzando classi specifiche per ogni effetto.
     */
    @Override
    protected void createDeck() {
        CardFace wildCardFace = new CardFace(CardColor.WILD, CardValue.WILD_ALLWILD);
        CardFace wildDrawFourCardFace = new CardFace(CardColor.WILD, CardValue.WILD_DRAW_FOUR_ALLWILD);
        CardFace wildDrawTwoCardFace = new CardFace(CardColor.WILD, CardValue.WILD_DRAW_TWO_ALLWILD);
        // aggiungi 14 carte per Wild
        for(int i = 0; i < 14; i++) {
            this.cards.add(new WildCard(wildCardFace, wildCardFace));
            this.cards.add(new WildDrawFourCard(wildDrawFourCardFace, wildDrawFourCardFace));
            this.cards.add(new WildForcedSwapCard());
            this.cards.add(new WildReverseCard());
            this.cards.add(new WildSkipCard());
            this.cards.add(new WildTargetedDrawTwoCard());
            this.cards.add(new WildDrawTwoCard(wildDrawTwoCardFace, wildDrawTwoCardFace));
            this.cards.add(new WildSkipTwoCard());
        }

        // Totale: 14 carte per Wild card = 112 carte
    }
}