package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import edu.upc.epsevg.prop.amazons.SearchType;
import java.awt.Point;
import java.util.ArrayList;

/**
 *
 * @author Héctor Montesinos, César Médina
 */
public class Paco implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private int cont;

    public Paco() {
        this.name = "Paco";
    }

    //@Override
    public Move move(GameStatus s){
        CellType color = s.getCurrentPlayer();                      // Devuelve jugador actual (P1 o P2)
        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        int i = 0;
        while (i < listAmazonas.size()){
            System.out.println("Amazona: " + i +": " + listAmazonas.get(i));
            i++;
        }
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = new ArrayList<>();
            listMoviments = s.getAmazonMoves(listAmazonas.get(i), true);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
            
            for (int j=0; j<listMoviments.size(); j++){
                System.out.println("Movimientos disponibles para Amazona: " + i + ": " + listMoviments.get(j));
            }
        }

        
        Point queenTo = null;
        Point queenFrom = null;

        // "s" és una còpia del tauler, per es pot manipular sense perill
        s.moveAmazon(queenFrom, queenTo);

        Point arrowTo = listAmazonas.get(0);

        return new Move(queenFrom, queenTo, arrowTo, 0, 0, SearchType.RANDOM);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /*private int max(GameStatus t, int depth, int player, int alpha, int beta){
    // Max
    
        if (isGameOver || depth == 0) return heuristica(t, player);
        for (int i=0;i<t.getSize();i++){    
            if (t.movpossible(i)){
                GameStatus t2 = new GameStatus(t);
                cont++;
                t2.afegeix(i, player);
                if (t2.solucio(i, player)) return Integer.MAX_VALUE;
                //estat2.pintaTaulerALaConsola();
                int valor = min(t2, depth-1, -player, alpha, beta);    
                alpha = Math.max(alpha, valor);
                if (beta <= alpha){
                    return alpha;
                }
            }
        }
        return alpha;
    }*/
    
    
    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }

    @Override
    public String getName() {
        return name;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}