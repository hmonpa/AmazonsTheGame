package edu.upc.epsevg.prop.amazons.players;

import edu.upc.epsevg.prop.amazons.CellType;
import static edu.upc.epsevg.prop.amazons.CellType.*;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Level;
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
    //private Level level;
    private int depth;
    
    public Paco(int depth) {
        this.name = "Paco";
        this.depth = depth;
    }

    //@Override
    public Move move(GameStatus s){
        CellType color = s.getCurrentPlayer();                      // Devuelve jugador actual (P1 o P2)
        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        for (int i=0; i<numAmazonas ; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        int i = 0;
        while (i < listAmazonas.size()){
            System.out.println("Amazona " + i +": " + listAmazonas.get(i));
            i++;
        }
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = new ArrayList<>();
            listMoviments = s.getAmazonMoves(listAmazonas.get(i), true);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                System.out.println("Movimiento de : " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j)) ;
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));
                System.out.println("Color: " + color);
                
                System.out.println("Flecha a: " + listMoviments.get(j+1));
                s2.placeArrow(listMoviments.get(j+1));
                
                System.out.println("Primer print: ");
                System.out.println(s2.toString());
                        
                
                // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                double valMax = min_max(s2, --depth, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, false);
                //. ...
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
    
    private double min_max(GameStatus s, int depth, CellType color, double alpha, double beta, boolean min_or_max){
        
        //color = CellType.opposite(color);
        double val_actual;
        
        if (depth == 0) return 0;     // aquí va la función heurística

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
        
        
        if(min_or_max) val_actual = Double.NEGATIVE_INFINITY;
        else val_actual = Double.POSITIVE_INFINITY;
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = new ArrayList<>();
            listMoviments = s.getAmazonMoves(listAmazonas.get(i), true);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                System.out.println("Movimiento de: " + s2.getAmazon(color, i) + "hacia " + listMoviments.get(j));                                
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));

                if (j+1 < listMoviments.size()) s2.placeArrow(listMoviments.get(j+1));
                else s2.placeArrow(listMoviments.get(j-2));
                System.out.println("Flecha a: " + listMoviments.get(j+1));
                System.out.println("Color: " + color);
                System.out.println("Print: " + s2.toString());

                double eval = min_max(s2, --depth, opposite(color), alpha, beta, !min_or_max);
                if(min_or_max){
                    val_actual = Math.max(eval, val_actual);
                    alpha = Math.max(alpha, eval);
                    if(beta <= alpha) return val_actual;
                    System.out.println("max ");
                }
                else{
                    val_actual = Math.min(eval, val_actual);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) return val_actual;
                    System.out.println("min ");
                }
            }
        } 
      
        return val_actual;
    }
    
    /*
    private double max(GameStatus s, int depth, CellType color, double alpha, double beta){
        // Min
        if (depth == 0) return 0;   // aquí va la función heurística
      
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
        
        double valMax = Double.NEGATIVE_INFINITY;
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = new ArrayList<>();
            listMoviments = s.getAmazonMoves(listAmazonas.get(i), true);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));
                valMax = Math.max(valMax, min(s2, --depth, opposite(color), alpha, beta));
                    
                alpha = Math.max(alpha, valMax);
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