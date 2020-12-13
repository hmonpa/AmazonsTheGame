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
    private double millor_moviment;
    //private Level level;
    private int depth;
    int nodesExp;
    
    public Paco(int depth) {
        this.name = "Paco";
        this.depth = depth;
    }

    //@Override
    public Move move(GameStatus s){
        millor_moviment = Double.NEGATIVE_INFINITY;
        Point arrowTo = null;
        Point amazonTo = null;
        Point amazonFrom = null;
        nodesExp = 0;
        
        if (!s.isGameOver()){
            CellType color = s.getCurrentPlayer();                      // Devuelve jugador actual (P1 o P2)
        System.out.println("-------------------------------------" + color);
        
        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        int i = 0;
        for (i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        i = 0;
        
        // Imprime lista de amazonas propias y sus posiciones
        while (i < listAmazonas.size()){
            System.out.println("Amazona " + i +": " + listAmazonas.get(i));
            i++;
        }
        
        ArrayList<Point> listEnemics = new ArrayList<>();
        
        boolean trobades = false;                           // Es el enemic
        int cont = 0;                                       // Contador que busca los enemigos
        for (i=0; i<s.getSize() && !trobades; i++){         // Filas
            for (int j=0; j<s.getSize() && !trobades; j++){ // Columnas
                Point t = new Point(i,j);                   // t = posición i,j
                if (s.getPos(t) == opposite(color)) {    
                    listEnemics.add(t);
                    cont++;
                    if (cont == 4) trobades = true;
                }
            }
        }
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), false);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                Point arrowToActual = null;
                //System.out.println("Movimiento de : " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j)) ;
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));
                //System.out.println("Jugador: " + color);
                nodesExp++;
                int ii = 0;
                boolean trobat = false;
                // Bucle tiraflechas
                while (ii < listEnemics.size() && !trobat){
                    int x = listEnemics.get(ii).x;       // COLUMNA
                    int y = listEnemics.get(ii).y;       // FILA

                    arrowToActual = buscarMejorTiro(x, y, s2);
                    //System.out.println("Enemigo posición: X: "+ x + ",  Y: " + y);
                    /*System.out.println("Enemigo " + ii);   
                    int varX = x-1, varY = y-1;
                    int conta = 0;
                    
                    while (varX <= x+1 && varY <= y+2 && !trobat){
                        //System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
                        if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                            arrowToActual = new Point(varX,varY);
                            if (s2.getPos(arrowToActual) == EMPTY){
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + arrowToActual);
                                s2.placeArrow(arrowToActual);
                                //System.out.println(s2.isGameOver());
                                trobat = true;
                            }
                        }
                        varY++;
                        conta++;
                        if (conta == 3){
                            varY = y-1;
                            varX++;
                            conta = 0;
                        }
                    }
                    */
                    if (arrowToActual == null ) arrowToActual = new Point(5,5);
                    else trobat = true;
                    ii++;
                }
                
                //System.out.println("Primer print: ");
                //System.out.println(s2.toString());
                        
                // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                double moviment = min_max(s2, depth-1, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, listAmazonas, false);
                if(moviment > millor_moviment){
                    amazonTo = listMoviments.get(j);
                    amazonFrom = listAmazonas.get(i);
                    arrowTo = arrowToActual;
                    millor_moviment = moviment;
                }
            }
        }
        }
        System.out.println("is game over" + s.isGameOver());
        System.out.println("arrow " + arrowTo);
        return new Move(amazonFrom, amazonTo, arrowTo, nodesExp, depth, SearchType.MINIMAX);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private double min_max(GameStatus s, int depth, CellType color, double alpha, double beta, ArrayList<Point> listEnemics, boolean min_or_max){
        System.out.println("Profundidad = " + depth);
        double val_actual;
        nodesExp++;
        if (depth == 0 || s.isGameOver()){
            System.out.println("Prof " + depth + " o Gameover " + s.isGameOver());
            double ret =  funcio_heuristica(s, color, listEnemics);
            System.out.println("Heuristica: "+ret);
            return ret;
        }

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
        
        if(min_or_max) val_actual = Double.NEGATIVE_INFINITY; // true = max
        else val_actual = Double.POSITIVE_INFINITY; // false = min
        
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), true); // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                System.out.println("Movimiento de: " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j));                                
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));
                listAmazonas.set(i, listMoviments.get(j));
                int ii = 0;
                boolean trobat = false;
                // Bucle tiraflechas
                while (ii < listEnemics.size() && !trobat){
                    int x = listEnemics.get(ii).x;       // COLUMNA
                    int y = listEnemics.get(ii).y;       // FILA

                    buscarMejorTiro(x, y, s2);
                    //System.out.println("Enemigo posición: X: "+ x + ",  Y: " + y);
                    //System.out.println("Amazona enemiga " + i +": " + listEnemics.get(i));
                    /*
                    int varX = x-1, varY = y-1;
                    int conta = 0;
                    
                    while (varX <= x+1 && varY <= y+1 && !trobat){
                        System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
                        if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                            Point arrowToActual = new Point(varX,varY);
                            if (s2.getPos(arrowToActual) == EMPTY){
                                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + arrowToActual);
                                s2.placeArrow(arrowToActual);
                                trobat = true;
                            }
                        }
                        varY++;
                        conta++;
                        if (conta == 3){
                            varY = y-1;
                            varX++;
                            conta = 0;
                        }
                    }*/
                    
                    
                    ii++;
                }
                
                System.out.println("Jugador: " + color);
                //System.out.println("Print: " + s2.toString());
                //System.out.println("booleano" + min_or_max);
                double eval = min_max(s2, depth-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                
                if(min_or_max){
                    val_actual = Math.max(eval, val_actual);
                    alpha = Math.max(alpha, eval);
                    if(beta <= alpha) return val_actual;
                    System.out.println("Max ");
                }
                else{
                    val_actual = Math.min(eval, val_actual);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) return val_actual;
                    System.out.println("Min ");
                }
            }
        } 
      
        return val_actual;
    }
    
    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }

    @Override
    public String getName() {
        return name;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * funcio_heuristic
     * @param s tauler
     * @param color jugador
     * @param listEnemics lista de amazonas del jugador enemic
     * @return 
     */
    public double funcio_heuristica(GameStatus s,CellType color, ArrayList<Point> listEnemics){
        
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        ArrayList<Point> listAmazonas = new ArrayList<>();
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        
        int cont = 0;
        for(int i=0; i<listAmazonas.size();i++){
            int x = listAmazonas.get(i).x;       // COLUMNA
            int y = listAmazonas.get(i).y;       // FILA
            //System.out.println("Enemigo posición: X: "+ x + ",  Y: " + y);
            
            int varX = x-1, varY = y-1;
            int conta = 0;
            boolean trobat = false;
            while (varX <= x+1 && varY <= y+2 && !trobat){
                //System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
                if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                    Point t = new Point(varX,varY);
                    if (s.getPos(t) == EMPTY){
                        cont++;
                    }
                }
                varY++;
                conta++;
                if (conta == 3){
                    varY = y-1;
                    varX++;
                    conta = 0;
                }

            }
        }
        
        int cont2 = 0;
        boolean trobat = false;
        for(int i=0; i<listEnemics.size() && !trobat;i++){
            int x = listEnemics.get(i).x;       // COLUMNA
            int y = listEnemics.get(i).y;       // FILA
            //System.out.println("Enemigo posición: X: "+ x + ",  Y: " + y);
            
            int varX = x-1, varY = y-1;
            int conta = 0;
            
            while (varX <= x+1 && varY <= y+2 && !trobat){
                //System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
                if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                    Point t = new Point(varX,varY);
                    if (s.getPos(t) == EMPTY){
                        cont2++;
                    }
                }
                varY++;
                conta++;
                if (conta == 3){
                    varY = y-1;
                    varX++;
                    conta = 0;
                }
            }
        }
        System.out.println("cont: "+cont + "  cont2: "+cont2);
        return cont2-cont;
    }
    
    /**
     * buscarMejorTiro Dada una posicion x e y de una amazona busca.
     * @param x
     * @param y
     * @param s2
     * @return
     */
    public Point buscarMejorTiro(int x, int y, GameStatus s2){
        
        int varX = x-1, varY = y-1;
        int conta = 0;
        boolean trobat = false;
        Point arrowToActual = null;
        while (varX <= x+1 && varY <= y+2 && !trobat){
            System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                arrowToActual = new Point(varX,varY);
                if (s2.getPos(arrowToActual) == EMPTY){
                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + arrowToActual);
                    s2.placeArrow(arrowToActual);
                    trobat = true;
                }
            }
            varY++;
            conta++;
            if (conta == 3){
                varY = y-1;
                varX++;
                conta = 0;
            }
        }
        
        if(trobat == false) arrowToActual = null;
        
        return arrowToActual;
    }

}