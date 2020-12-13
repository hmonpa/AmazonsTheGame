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
        System.out.println("Move- Jugador:" + color);
        
        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        int i = 0;
        for (i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        i = 0;
        
        // Imprime lista de amazonas propias y sus posiciones
        while (i < listAmazonas.size()){
            System.out.println("Move- Amazona " + i +": " + listAmazonas.get(i));
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
                nodesExp++;
                int ii = 0;
                boolean trobat = false;
                
                // Bucle tiraflechas
                while (ii < listEnemics.size() && !trobat){
                    int x = listEnemics.get(ii).x;       // COLUMNA
                    int y = listEnemics.get(ii).y;       // FILA
                            
                    arrowToActual = buscarMejorTiro(x, y, s2);
                    //if (arrowToActual == null) arrowToActual = new Point(5,5);
                    if (arrowToActual == null) arrowToActual = primer_lliure(s2);           // Si un jugador se suicida y no tiene ningun hueco a su alrededor
                    else trobat = true;                                                     // el otro gana la partida colocando una flecha en el primer hueco libre
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
        //System.out.println("is game over" + s.isGameOver());
        //System.out.println("arrow " + arrowTo);
        return new Move(amazonFrom, amazonTo, arrowTo, nodesExp, depth, SearchType.MINIMAX);
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private double min_max(GameStatus s, int depth, CellType color, double alpha, double beta, ArrayList<Point> listEnemics, boolean min_or_max){
        System.out.println("MinMax- Profundidad = " + depth);
        double val_actual;
        nodesExp++;
        if (depth == 0 || s.isGameOver()){
            //System.out.println("Prof " + depth + " o Gameover " + s.isGameOver());
            double heu = funcio_heuristica(s, color, listEnemics);
            System.out.println("Heuristica: " + heu);
            return heu;
        }

        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        int i = 0;
        while (i < listAmazonas.size()){
            System.out.println("MinMax- Amazona: " + i +": " + listAmazonas.get(i));
            i++;
        }
        
        if(min_or_max) val_actual = Double.NEGATIVE_INFINITY; // true = max
        else val_actual = Double.POSITIVE_INFINITY; // false = min
        
        for (i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), true); // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                System.out.println("MinMax- Movimiento de: " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j));        
                
                s2.moveAmazon(s2.getAmazon(color, i), listMoviments.get(j));

                listAmazonas.set(i, listMoviments.get(j));
                int ii = 0;
                boolean trobat = false;
                // Bucle tiraflechas
                while (ii < listEnemics.size() && !trobat){
                    int x = listEnemics.get(ii).x;       // COLUMNA
                    int y = listEnemics.get(ii).y;       // FILA

                    buscarMejorTiro(x, y, s2);
                    ii++;
                }
                
                System.out.println("MinMax- Jugador: " + color);
                //System.out.println("Print: " + s2.toString());
                //System.out.println("booleano" + min_or_max);
                double eval = min_max(s2, depth-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                
                if(min_or_max){
                    val_actual = Math.max(eval, val_actual);
                    alpha = Math.max(alpha, eval);
                    if(beta <= alpha) return val_actual;
                    System.out.println("Estoy en MAX");
                }
                else{
                    val_actual = Math.min(eval, val_actual);
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) return val_actual;
                    System.out.println("Estoy en MIN");
                }
            }
        } 
      
        return val_actual;
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
     * primer_lliure
     * @param s2
     * @return 
     */
    public Point primer_lliure(GameStatus s2){
        int varX = 5;
        int varY = 5;
        boolean trobat = false;
        Point arrowToActual = null;
        int cont = 0;
        while (varX <= 9 && varY <= 9 && !trobat){
            arrowToActual = new Point(varX, varY);
            if (s2.getPos(arrowToActual) == EMPTY){
                trobat = true;
            }
            cont++;
            varY++;
            if (cont == 4){
                varY=5;
                varX++;
                cont=0;
            }
        }
        return arrowToActual;
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
            //System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                arrowToActual = new Point(varX,varY);
                if (s2.getPos(arrowToActual) == EMPTY){
                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + arrowToActual);
                    int i;
                    Point arrowTo1 = null;
                    Point arrowTo2 = null;
                    Point arrowTo3 = null;
                    Point arrowTo4 = null;
                    Point arrowTo5 = null;
                    Point arrowTo6 = null;
                    Point arrowTo7 = null;
                    Point arrowTo8 = null;
                    int contDiagSupIzq;
                    int contVerticalSup;
                    int contDiagSupDer;
                    int contHorizIzq;
                    int contHorizDer;
                    int contDiagInfIzq;
                    int contVerticalInf;
                    int contDiagInfDer;
                     
                    Integer vec[] = new Integer[8];
                    // Revisión de las 8 posibles posiciones alrededor del contrincante
                    if (arrowToActual.x == x-1 && arrowToActual.y == y-1){          // Diagonal superior izq
                        i = 1;
                        arrowTo1 = arrowToActual;
                        arrowToActual.x = varX-i;
                        arrowToActual.y = varY-i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x-i;
                            arrowToActual.y = y-i;
                            i++;
                        }
                        contDiagSupIzq = i;
                        vec[0] = contDiagSupIzq;
                    }         
                    else if (arrowToActual.x == x-1 && arrowToActual.y == y){       // Vertical superior
                        i = 1;
                        arrowTo2 = arrowToActual;
                        arrowToActual.x = varX-i;
                        arrowToActual.y = varY;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x-i;
                            arrowToActual.y = y;
                            i++;
                        }
                        contVerticalSup = i;
                        vec[1] = contVerticalSup;
                    }      
                    else if (arrowToActual.x == x-1 && arrowToActual.y == y+1){     // Diagonal superior der
                        i = 1;
                        arrowTo3 = arrowToActual;
                        arrowToActual.x = varX-i;
                        arrowToActual.y = varY+i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x-i;
                            arrowToActual.y = y+i;
                            i++;
                        }
                        contDiagSupDer = i;
                        vec[2] = contDiagSupDer;
                    }    
                    else if (arrowToActual.x == x && arrowToActual.y == y-1){       // Horizontal izq
                        i = 1;
                        arrowTo4 = arrowToActual;
                        arrowToActual.x = varX;
                        arrowToActual.y = varY-i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x;
                            arrowToActual.y = y-i;
                            i++;
                        }
                        contHorizIzq = i;
                        vec[3] = contHorizIzq;
                    }      
                    else if (arrowToActual.x == x && arrowToActual.y == y+1){       // Horizontal der
                        i = 1;
                        arrowTo5 = arrowToActual;
                        arrowToActual.x = varX;
                        arrowToActual.y = varY+i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x;
                            arrowToActual.y = y+i;
                            i++;
                        }
                        contHorizDer = i;
                        vec[4] = contHorizDer;
                    }      
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y-1){     // Diagonal inferior izq
                        i = 1;
                        arrowTo6 = arrowToActual;
                        arrowToActual.x = varX+i;
                        arrowToActual.y = varY-i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x+i;
                            arrowToActual.y = y-i;
                            i++;
                        }
                        contDiagInfIzq = i;
                        vec[5] = contDiagInfIzq;
                    }    
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y){       // Vertical inferior
                        i = 1;
                        arrowTo7 = arrowToActual;
                        arrowToActual.x = varX+i;
                        arrowToActual.y = varY;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x+i;
                            arrowToActual.y = y;
                            i++;
                        }
                        contVerticalInf = i;
                        vec[6] = contVerticalInf;
                    }      
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y+1){     // Diagonal inferior der
                        i = 1;
                        arrowTo8 = arrowToActual;
                        arrowToActual.x = varX+i;
                        arrowToActual.y = varY+i;
                        
                        while (s2.getPos(arrowToActual) != EMPTY){
                            arrowToActual.x = x+i;
                            arrowToActual.y = y+i;
                            i++;
                        }
                        contDiagInfDer = i;
                        vec[7] = contDiagInfDer;
                    }    
                    
                    int max = 0;
                    for (i=0; i<vec.length;i++)
                    {
                        if (max < vec[i]) max = vec[i];
                    }
                    
                    if (max == vec[0]) s2.placeArrow(arrowTo1);
                    else if (max == vec[1]) s2.placeArrow(arrowTo2);
                    else if (max == vec[2]) s2.placeArrow(arrowTo3);
                    else if (max == vec[3]) s2.placeArrow(arrowTo4);
                    else if (max == vec[4]) s2.placeArrow(arrowTo5);
                    else if (max == vec[5]) s2.placeArrow(arrowTo6);
                    else if (max == vec[6]) s2.placeArrow(arrowTo7);
                    else if (max == vec[7]) s2.placeArrow(arrowTo8);
                    //s2.placeArrow(arrowToActual);
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