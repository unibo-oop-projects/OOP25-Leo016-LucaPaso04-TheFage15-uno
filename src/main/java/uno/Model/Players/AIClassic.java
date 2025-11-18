package uno.Model.Players;

import uno.Model.Cards.Card;
import uno.Model.Game.Game;
import uno.Model.Cards.Attributes.CardColor;
import uno.Model.Cards.Attributes.CardValue;

import java.util.*;

/**
 * Implementazione migliorata di una IA di livello "classico".
 * Utilizza strategie avanzate per scegliere la carta migliore.
 */
public class AIClassic extends AIPlayer {

    public AIClassic(String name) {
        super(name);
    }

    /**
     * Logica decisionale principale dell'IA.
     */
    @Override
    public void takeTurn(Game game) {
        if (game.getCurrentPlayer() != this) {
            return;
        }

        System.out.println(this.name + " sta pensando...");

        // 1. Trova la MIGLIORE carta giocabile (non più la prima)
        Optional<Card> cardToPlay = chooseBestCard(game);

        if (cardToPlay.isPresent()) {
            Card card = cardToPlay.get();
            
            // Gioca la carta
            game.playCard(card);
            System.out.println(this.name + " gioca " + card);

            // Gestione Jolly
            handleWildCard(card, game);
            
            // Chiamata UNO
            handleUnoCall(game);
            
        } else {
            // Nessuna carta giocabile: pesca
            game.playerInitiatesDraw();
            Card drawnCard = hand.get(hand.size() - 1);
            System.out.println(this.name + " pesca una carta.");

            // Controlla se la carta pescata è giocabile
            if (isMoveValid(drawnCard, game)) {
                game.playCard(drawnCard);
                System.out.println(this.name + " gioca " + drawnCard + " dopo averla pescata.");

                handleWildCard(drawnCard, game);
                handleUnoCall(game);
            } else {
                System.out.println(this.name + " passa il turno.");
                game.playerPassTurn();
            }
        }
    }

    /**
     * Gestisce le carte Jolly (scelta colore).
     */
    private void handleWildCard(Card card, Game game) {
        if (card.getColor(game) == CardColor.WILD) {
            CardColor chosenColor = chooseBestColor(game);
            game.setColor(chosenColor);
            // RIMOSSO: game.AIAdvanceTurn() <- BUG! Il turno è già gestito da playCard()
        }
    }

    /**
     * Gestisce la chiamata UNO.
     */
    private void handleUnoCall(Game game) {
        if (this.getHandSize() == 1) {
            try {
                game.callUno(this);
            } catch (IllegalStateException e) {
                // L'IA non farà mai una falsa chiamata
            }
        }
    }

    /**
     * STRATEGIA MIGLIORATA: Sceglie la carta migliore da giocare.
     * 
     * Priorità:
     * 1. Se un avversario ha 1 carta (UNO), gioca carte offensive
     * 2. Se ho poche carte, conserva i Jolly
     * 3. Altrimenti, gioca carte azione prima dei numeri
     * 4. Tra i numeri, gioca quelli più alti
     */
    private Optional<Card> chooseBestCard(Game game) {
        List<Card> playableCards = getPlayableCards(game);
        
        if (playableCards.isEmpty()) {
            return Optional.empty();
        }

        // Analizza la situazione
        boolean opponentHasUno = hasOpponentWithUno(game);
        boolean iAmClose = this.getHandSize() <= 3;

        // Separa le carte per categoria
        List<Card> actionCards = new ArrayList<>();
        List<Card> numberCards = new ArrayList<>();
        List<Card> wildCards = new ArrayList<>();

        for (Card card : playableCards) {
            CardValue value = card.getValue(game);
            if (value == CardValue.WILD || value == CardValue.WILD_DRAW_FOUR ||
                value == CardValue.WILD_DRAW_COLOR || value == CardValue.WILD_FORCED_SWAP ||
                value == CardValue.WILD_TARGETED_DRAW_TWO) {
                wildCards.add(card);
            } else if (isActionCard(value)) {
                actionCards.add(card);
            } else {
                numberCards.add(card);
            }
        }

        // STRATEGIA 1: Avversario in UNO -> Gioca carte offensive
        if (opponentHasUno) {
            // Priorità: +4, +2, Skip, Reverse, poi altri
            Card offensiveCard = findBestOffensiveCard(actionCards, wildCards, game);
            if (offensiveCard != null) {
                System.out.println(this.name + " usa strategia difensiva contro giocatore in UNO!");
                return Optional.of(offensiveCard);
            }
        }

        // STRATEGIA 2: Sono vicino alla vittoria -> Conserva i Jolly
        if (iAmClose) {
            // Gioca prima azioni, poi numeri, lascia i Jolly per dopo
            if (!actionCards.isEmpty()) {
                return Optional.of(actionCards.get(0));
            }
            if (!numberCards.isEmpty()) {
                // Gioca il numero più alto
                numberCards.sort((c1, c2) -> getNumericValue(c2, game) - getNumericValue(c1, game));
                return Optional.of(numberCards.get(0));
            }
        }

        // STRATEGIA 3: Situazione normale
        // Gioca prima le azioni, poi i numeri alti, infine i Jolly
        if (!actionCards.isEmpty()) {
            // Tra le azioni, preferisci quelle più forti
            actionCards.sort((c1, c2) -> getActionCardPriority(c2.getValue(game)) - 
                                          getActionCardPriority(c1.getValue(game)));
            return Optional.of(actionCards.get(0));
        }

        if (!numberCards.isEmpty()) {
            // Gioca il numero più alto per liberarsi delle carte pesanti
            numberCards.sort((c1, c2) -> getNumericValue(c2, game) - getNumericValue(c1, game));
            return Optional.of(numberCards.get(0));
        }

        // Ultima risorsa: usa un Jolly
        if (!wildCards.isEmpty()) {
            // Preferisci i Jolly semplici al +4 (più versatile)
            wildCards.sort((c1, c2) -> {
                boolean is1DrawFour = c1.getValue(game) == CardValue.WILD_DRAW_FOUR;
                boolean is2DrawFour = c2.getValue(game) == CardValue.WILD_DRAW_FOUR;
                if (is1DrawFour && !is2DrawFour) return 1; // c2 prima
                if (!is1DrawFour && is2DrawFour) return -1; // c1 prima
                return 0;
            });
            return Optional.of(wildCards.get(0));
        }

        return Optional.empty();
    }

    /**
     * Trova la migliore carta offensiva contro un avversario in UNO.
     */
    private Card findBestOffensiveCard(List<Card> actionCards, List<Card> wildCards, Game game) {
        // Priorità: +4 > +2 > Skip > Reverse
        
        // Cerca +4
        for (Card card : wildCards) {
            if (card.getValue(game) == CardValue.WILD_DRAW_FOUR) {
                return card;
            }
        }
        
        // Cerca +2
        for (Card card : actionCards) {
            if (card.getValue(game) == CardValue.DRAW_TWO) {
                return card;
            }
        }
        
        // Cerca Skip
        for (Card card : actionCards) {
            if (card.getValue(game) == CardValue.SKIP) {
                return card;
            }
        }
        
        // Altrimenti qualsiasi azione
        if (!actionCards.isEmpty()) {
            return actionCards.get(0);
        }
        
        return null;
    }

    /**
     * Controlla se qualche avversario ha una sola carta (UNO).
     */
    private boolean hasOpponentWithUno(Game game) {
        for (Player player : game.getPlayers()) {
            if (player != this && player.getHandSize() == 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ottiene tutte le carte giocabili.
     */
    private List<Card> getPlayableCards(Game game) {
        List<Card> playable = new ArrayList<>();
        for (Card card : this.hand) {
            if (isMoveValid(card, game)) {
                playable.add(card);
            }
        }
        return playable;
    }

    /**
     * Controlla se una carta è un'azione.
     */
    private boolean isActionCard(CardValue value) {
        return value == CardValue.SKIP || 
               value == CardValue.REVERSE || 
               value == CardValue.DRAW_TWO ||
               value == CardValue.FLIP ||
               value == CardValue.SKIP_EVERYONE;
    }

    /**
     * Assegna una priorità numerica alle carte azione.
     * Valori più alti = più importanti.
     */
    private int getActionCardPriority(CardValue value) {
        switch (value) {
            case DRAW_TWO: return 5;
            case SKIP: return 4;
            case SKIP_EVERYONE: return 4;
            case REVERSE: return 3;
            case FLIP: return 2;
            default: return 0;
        }
    }

    /**
     * Ottiene il valore numerico di una carta (per il sorting).
     */
    private int getNumericValue(Card card, Game game) {
        CardValue value = card.getValue(game);
        switch (value) {
            case ZERO: return 0;
            case ONE: return 1;
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            default: return 0;
        }
    }

    /**
     * Logica per determinare se una carta è giocabile.
     */
    private boolean isMoveValid(Card card, Game game) {
        Card topCard = game.getTopDiscardCard();
        CardColor activeColor = (game.getCurrentColor() != null) ? 
                                 game.getCurrentColor() : 
                                 topCard.getColor(game);

        // Jolly standard
        if (card.getValue(game) == CardValue.WILD) {
            return true;
        }

        // Jolly +4 (solo se non ho altre carte del colore attivo)
        if (card.getValue(game) == CardValue.WILD_DRAW_FOUR) {
            for (Card cardInHand : game.getCurrentPlayer().getHand()) {
                if (cardInHand.getColor(game) == activeColor) {
                    return false;
                }
            }
            return true;
        }

        // Altri jolly
        if (card.getColor(game) == CardColor.WILD) {
            return true;
        }

        // Corrispondenza colore o valore
        if (card.getColor(game) == activeColor) {
            return true;
        }
        
        if (card.getValue(game) == topCard.getValue(game)) {
            return true;
        }

        return false;
    }

    /**
     * STRATEGIA MIGLIORATA: Scelta del colore basata su analisi avanzata.
     * 
     * Non solo conta i colori nella mano, ma considera anche:
     * 1. Carte azione di ogni colore (valgono di più)
     * 2. Numero totale di carte per colore
     */
    private CardColor chooseBestColor(Game game) {
        Map<CardColor, Integer> colorScores = new EnumMap<>(CardColor.class);

        // Inizializza i punteggi
        colorScores.put(CardColor.RED, 0);
        colorScores.put(CardColor.GREEN, 0);
        colorScores.put(CardColor.BLUE, 0);
        colorScores.put(CardColor.YELLOW, 0);

        // Calcola i punteggi
        for (Card card : this.hand) {
            CardColor color = card.getColor(game);
            
            if (colorScores.containsKey(color)) {
                int score = 1; // Punteggio base
                
                // Le carte azione valgono di più
                if (isActionCard(card.getValue(game))) {
                    score = 3;
                }
                
                colorScores.put(color, colorScores.get(color) + score);
            }
        }

        // Trova il colore con il punteggio massimo
        CardColor bestColor = CardColor.RED;
        int maxScore = -1;

        for (Map.Entry<CardColor, Integer> entry : colorScores.entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                bestColor = entry.getKey();
            }
        }
        
        System.out.println(this.name + " sceglie " + bestColor + " (punteggio: " + maxScore + ")");
        return bestColor;
    }
}