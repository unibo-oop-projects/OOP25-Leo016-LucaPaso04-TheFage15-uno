package uno.Model.Player;

public abstract class AIPlayer extends Player {
    public AIPlayer(String name) {
        super(name);
    }
    //Metodo astratto da implementare nelle classi figlie delle modalità AI
    public abstract void AIPlayCard();

    // Logica di gioco generale valida per tutte le AI
    public void makeMove() {
        if (getHandSize() > 0)
        {
            if(getHandSize() == 1)
            {
                callUno();
                AIPlayCard();
            }
            else
            {
                AIPlayCard();
            }
        }
        else
        {
            //MANCA IMPLEMENTAZIONE NEL GAME PER GESTIRE LA VITTORIA DELL'AI/GIOCATORE
            //win(this.id);
        }
    }
}
