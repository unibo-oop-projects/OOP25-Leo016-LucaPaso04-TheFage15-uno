package uno.Model.Player;

public abstract class AIPlayer {
    public abstract int cardsLeft();
    public void makeMove() {
        // Logica di gioco generale
        if (hasCardsLeft())
        {
            if(cardsLeft() == 1)
            {
                callUno();
                playCard();
            }
            else
            {
                playCard();
            }
        }
        else
        {
            win(this.id);
        }
    }
}
