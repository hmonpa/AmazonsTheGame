package edu.upc.epsevg.prop.amazons;

import edu.upc.epsevg.prop.amazons.players.HumanPlayer;
import edu.upc.epsevg.prop.amazons.players.CarlinhosPlayer;
import edu.upc.epsevg.prop.amazons.players.Paco;
import edu.upc.epsevg.prop.amazons.players.RandomPlayer;
import javax.swing.SwingUtilities;

/**
 *
 * @author bernat
 */
public class Amazons {
        /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run(){
                
                IPlayer player1 = new Paco(3);
                //IPlayer player2 = new RandomPlayer("Falso paco");
               // IPlayer player2 = new HumanPlayer("Humano paco");
                IPlayer player2 = new CarlinhosPlayer();
                
                new AmazonsBoard(player2 , player1, 7, Level.HALF_BOARD);
                
            }
        });
        
    }
}
