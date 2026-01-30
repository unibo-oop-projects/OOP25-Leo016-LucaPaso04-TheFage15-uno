package uno.model.players;

import uno.model.cards.Card;


public abstract class AIPlayer extends Player {
    public AIPlayer(String name) {
        super(name);
    }
    //Metodo astratto da implementare nelle classi figlie delle modalità AI
   // public abstract void AIPlayCard();

    public boolean isPlayable(Card card) {
        // Implementa la logica per verificare se la carta è giocabile
        return true;
    }

    // Logica di gioco generale valida per tutte le AI
    /*public void makeMove() {
        if (getHandSize() > 0)
        {
            //Controllo se devo pescare o giocare una carta
            if(hasPlayableCard() && ! blocked())
            {
                //Esecuzione mossa implementata nella modalità specifica
                AIPlayCard();
                //Controllo se bisogna dichiarare UNO
                if(getHandSize() == 1)
                {
                    //Dichiarazione Uno!
                    callUno();
                }
            }
            else
            {
                //Pesca una carta e controlla se può giocare altrimenti il turno finisce
                drawCard();
                    if(hasPlayableCard())
                    {
                        //Esecuzione mossa implementata nella modalità specifica
                        AIPlayCard();
                        //Controllo se bisogna dichiarare UNO
                        if(getHandSize() == 1)
                        {
                            //Dichiarazione Uno!
                            callUno();
                        }
                    }
                    else
                    {
                        //Fine turno
                    }
                }       
        }
        else
        {
            //MANCA IMPLEMENTAZIONE NEL GAME PER GESTIRE LA VITTORIA DELL'AI/GIOCATORE
            //win(this.id);
        }
    }*/
}
