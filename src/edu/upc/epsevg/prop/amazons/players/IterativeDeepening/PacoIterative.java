package edu.upc.epsevg.prop.amazons.players.IterativeDeepening;

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
import java.util.Random;

/**
 * 
 * @author Héctor Montesinos, César Médina
 */
public class PacoIterative implements IPlayer, IAuto {

    private final String name;
    private double millor_moviment;
    private int depth;
    int nodesExp;
    int nodesQuarterBoard;
    boolean hihaTemps;
    Move bestMove;
    
    public PacoIterative() {
        this.name = "Paco iter";
        this.hihaTemps = true;
        this.nodesQuarterBoard = 23;
    }

    @Override
    public Move move(GameStatus s){
        
        millor_moviment = Double.NEGATIVE_INFINITY;
       
        Point arrowTo = null;
        Point amazonTo = null;
        Point amazonFrom = null;
        nodesExp = 0;
        depth = 1;
        hihaTemps = true;
        
        while (hihaTemps && depth <= s.getEmptyCellsCount()){
            if (!s.isGameOver()){
                
                CellType color = s.getCurrentPlayer();                      // Devuelve jugador actual (P1 o P2)
                //System.out.println("Move- Jugador:" + color);
                ArrayList<Point> listAmazonas = new ArrayList<>();
                int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)

                for (int i=0; i<numAmazonas; i++){
                    listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
                }

                ArrayList<Point> listEnemics = new ArrayList<>();

                boolean trobades = false;                                   // Es el enemic
                int cont = 0;                                               // Contador que busca los enemigos
                for (int i=0; i<s.getSize() && !trobades; i++){             // Filas
                    for (int j=0; j<s.getSize() && !trobades; j++){         // Columnas
                        Point t = new Point(i,j);                           // t = posición i,j
                        if (s.getPos(t) == opposite(color)) {    
                            listEnemics.add(t);
                            cont++;
                            if (cont == 4) trobades = true;
                        }
                    }
                }

                for (int i=0; i<listAmazonas.size(); i++){
                    ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), false);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
                    for (int j=0; j<listMoviments.size(); j++){
                        GameStatus s2 = new GameStatus(s);
                        Point arrowToActual = null;
                        //System.out.println("Movimiento de : " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j)) ;
                        Point amazonTemp = s2.getAmazon(color, i);
                        s2.moveAmazon(amazonTemp, listMoviments.get(j));
                        listAmazonas.set(i, listMoviments.get(j));
                        nodesExp++;
                        
                        if (s2.getEmptyCellsCount() > nodesQuarterBoard){
                            //System.out.println("tengo más de 23");
                            int ii = 0;
                            boolean trobat = false;

                            // Bucle tiraflechas
                            while (ii < listEnemics.size() && !trobat){
                                int x = listEnemics.get(ii).x;       // COLUMNA
                                int y = listEnemics.get(ii).y;       // FILA

                                arrowToActual = buscarMejorTiro(x, y, s2, nodesExp);
                                if (arrowToActual == null) arrowToActual = primer_lliure(s2, nodesExp);           // Si un jugador se suicida y no tiene ningun hueco a su alrededor
                                else trobat = true;                                            // el otro gana la partida colocando una flecha en el primer hueco libre
                                ii++;
                            }
                            
                            s2.placeArrow(arrowToActual);

                            // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                            double moviment = min_max(s2, depth-1, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, listAmazonas, false);
                            listAmazonas.set(i,amazonTemp);
                            if(moviment > millor_moviment){
                                amazonTo = listMoviments.get(j);
                                amazonFrom = listAmazonas.get(i);
                                arrowTo = arrowToActual;
                                millor_moviment = moviment;
                            }
                        }
                        else {
                            //System.out.println("tengo menos de 23");
                            double millor_moviment_fletxa = Double.NEGATIVE_INFINITY;
                            Point millor_fletxa = null;

                            //arrowToActual = null;
                            for(int ii=0;ii<s2.getSize();ii++){
                                for(int jj=0; jj<s2.getSize();jj++){
                                    nodesExp++;
                                    arrowToActual = new Point(jj, ii);
                                    if (s2.getPos(arrowToActual) == EMPTY){
                                         GameStatus s3 = new GameStatus(s2);
                                         s3.placeArrow(arrowToActual);
                                         //System.out.println(s3.toString());
                                         // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                                        double moviment_fletxa = Double.NEGATIVE_INFINITY;
                                        if (hihaTemps) moviment_fletxa = min_max(s3, depth-1, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, listAmazonas, false);
                                        if (moviment_fletxa >= millor_moviment_fletxa){
                                            millor_moviment_fletxa = moviment_fletxa;
                                            millor_fletxa = arrowToActual;
                                        }
                                        if (!hihaTemps) millor_moviment = Double.NEGATIVE_INFINITY;

                                    }
                                }
                            }
                            listAmazonas.set(i,amazonTemp);
                            if (millor_moviment_fletxa >= millor_moviment ){
                                amazonTo = listMoviments.get(j);
                                amazonFrom = listAmazonas.get(i);
                                arrowTo = millor_fletxa;
                                millor_moviment = millor_moviment_fletxa;
                            }
                        }
                    }
                }
            }
            if (millor_moviment != Double.NEGATIVE_INFINITY) bestMove = new Move(amazonFrom, amazonTo, arrowTo, nodesExp, depth, SearchType.MINIMAX);
        
            //System.out.println("amazona de: " + amazonFrom + " , amazona a:  " + amazonTo + " , flecha a:  " + arrowTo + " , profundidad: " + depth);
            depth++;
        }
        //System.out.println("Acaba");
        //System.out.println("is game over" + s.isGameOver());
        //System.out.println("arrow " + arrowTo);
        //System.out.println("best move: " + bestMove);
        return bestMove;
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    /**
     * 
     * @param s
     * @param depth
     * @param color
     * @param alpha
     * @param beta
     * @param listEnemics
     * @param min_or_max
     * @return 
     */
    private double min_max(GameStatus s, int depth, CellType color, double alpha, double beta, ArrayList<Point> listEnemics, boolean min_or_max){
        
        //System.out.println("MinMax- Profundidad = " + depth);
        double val_actual;
        nodesExp++;
        if (depth == 0 || s.isGameOver()){
            //System.out.println("Prof " + depth + " o Gameover " + s.isGameOver());
            //if(s.isGameOver()) return 0;
            //System.out.println("enemics abans: "+listEnemics);
            
            //System.out.println(s.toString());
            double heu = funcio_heuristica(s, color, listEnemics);
            //System.out.println("Heuristica: " + heu);
            return heu;
        }

        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        
        /*while (i < listAmazonas.size()){
            //System.out.println("MinMax- Amazona: " + i +": " + listAmazonas.get(i));
            i++;
        }*/
        
        if(min_or_max) val_actual = Double.NEGATIVE_INFINITY; // true = max
        else val_actual = Double.POSITIVE_INFINITY; // false = min
        
        for (int i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), false); // Boolean=True: Muestra sólo jugadas finales, no intermedias
            for (int j=0; j<listMoviments.size(); j++){
                GameStatus s2 = new GameStatus(s);
                //System.out.println("MinMax- Movimiento de: " + s2.getAmazon(color, i) + " hacia " + listMoviments.get(j));        
                Point arrowToActual = null;
                Point amazonTemp = s2.getAmazon(color, i);
                s2.moveAmazon(amazonTemp, listMoviments.get(j));
                //System.out.println("antes de la actualizacion:"+listAmazonas);
                
                listAmazonas.set(i, listMoviments.get(j)); // actualizamos las posiciones de las amazonas
                //System.out.println("enemics abans: "+listAmazonas);
                if (s2.getEmptyCellsCount() > nodesQuarterBoard){
                    int ii = 0;
                    boolean trobat = false;
                    // Bucle tiraflechas
                    while (ii < listEnemics.size() && !trobat){
                        int x = listEnemics.get(ii).x;       // COLUMNA
                        int y = listEnemics.get(ii).y;       // FILA

                        arrowToActual = buscarMejorTiro(x, y, s2, nodesExp);
                        if (arrowToActual == null) arrowToActual = primer_lliure(s2, nodesExp);          
                        else trobat = true;
                        ii++;
                    }

                    s2.placeArrow(arrowToActual);
                    //System.out.println("MinMax- Jugador: " + color);
                    //System.out.println(s2.toString());

                    //System.out.println("Print: " + s2.toString());
                    //System.out.println("booleano" + min_or_max);
                    double eval = Double.NEGATIVE_INFINITY;
                    if (hihaTemps) eval = min_max(s2, depth-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                    listAmazonas.set(i,amazonTemp);
                    if(min_or_max){ //MAX
                        val_actual = Math.max(eval, val_actual);
                        //alpha = Math.max(alpha, eval);
                        alpha = Math.max(alpha, val_actual);
                        if(beta <= alpha) return val_actual;
                        //System.out.println("Estoy en MAX");
                    }
                    else{ //MIN
                        val_actual = Math.min(eval, val_actual);
                        //beta = Math.min(beta, eval);
                        beta = Math.min(beta, val_actual);
                        if (beta <= alpha) return val_actual;
                        //System.out.println("Estoy en MIN");
                    }
                    if (!hihaTemps) return Double.NEGATIVE_INFINITY;
                    //System.out.println("despues de la llamada min_max: "+listAmazonas);
                }
                
                else {
                //System.out.println("Minimax: tengo menos de 23");
                //double millor_moviment_fletxa = Double.NEGATIVE_INFINITY;
                //Point millor_fletxa = null;
                    //arrowToActual = null;
                    for(int ii=0;ii<s2.getSize();ii++){
                        for(int jj=0; jj<s2.getSize();jj++){
                            nodesExp++;
                            arrowToActual = new Point(jj, ii);
                            if (s2.getPos(arrowToActual) == EMPTY){
                                 GameStatus s3 = new GameStatus(s2);
                                 s3.placeArrow(arrowToActual);
                                 //System.out.println(s3.toString());
                                 // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                                //double moviment_fletxa = Double.NEGATIVE_INFINITY;
                                double eval = Double.NEGATIVE_INFINITY;
                                if (hihaTemps) eval = min_max(s3, depth-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                                if(min_or_max){ // MAX
                                    val_actual = Math.max(eval, val_actual);
                                    //alpha = Math.max(alpha, eval);
                                    alpha = Math.max(alpha, val_actual);
                                    if(beta <= alpha) return val_actual;
                                    //System.out.println("Estoy en MAX");
                                }
                                else{ // Min
                                    val_actual = Math.min(eval, val_actual);
                                    //beta = Math.min(beta, eval);
                                    beta = Math.min(beta, val_actual);
                                    if (beta <= alpha) return val_actual;
                                    //System.out.println("Estoy en MIN");
                                }
                                if (!hihaTemps) return Double.NEGATIVE_INFINITY;
                                /*if(moviment_fletxa >= millor_moviment_fletxa){
                                    millor_moviment_fletxa = moviment_fletxa;
                                    millor_fletxa = arrowToActual;
                                }*/
                                //if (!hihaTemps) millor_moviment = Double.NEGATIVE_INFINITY;


                            }
                        }
                    }
                    listAmazonas.set(i,amazonTemp);
                }
            }
        }
      
        return val_actual;
    }
    
    
    public void zobrist(GameStatus s){
        int mida = s.getSize();
        int peces = s.getNumberOfAmazonsForEachColor();
        long matrix[][] = new long[mida][mida];
        
        Random random = new Random();
        
        for(int i=0; i<mida; i++){
            for (int j=0; j<peces; j++){
                matrix[i][j] = random.nextLong();
            }
        }
    }
    
    
    /**
     * funcio_heuristic
     * @param s tauler
     * @param color jugador
     * @param listEnemics lista de amazonas del jugador enemic
     * @return 
     */
    public double funcio_heuristica(GameStatus s, CellType color, ArrayList<Point> listEnemics){
        
        //System.out.println("Heuristica ");
        //System.out.println(s.toString());
        
        int mida = s.getSize();
        int contAllied=0, contEnemy=0;
        Casella[][] matrix = new Casella[mida][mida];
        //System.out.println("matriu " + matrix[0][0]);
        
        for (int i=0; i<mida; i++){
            for (int j=0; j<mida; j++){
                matrix[i][j] = new Casella();
            }
        }
        
        //System.out.println("matriu " + matrix[0][0]);
        
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
        ArrayList<Point> listAmazonas = new ArrayList<>();
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
        }
        
        // Busquem moviments aliats
        boolean jugador = true;
        for(int i=0; i< listAmazonas.size();i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listAmazonas.get(i), false);
            int midaMoviments = listMoviments.size();
            for (int j=0; j< midaMoviments; j++){
                int x = listMoviments.get(j).x;
                int y = listMoviments.get(j).y;
                //System.out.println("chivato aliado ");
                //System.out.println("x: " + x + "  y : " + y);
                matrix[x][y].setAllied();                // es blanca 
                //System.out.println(matrix[x][y].getOwner());
                buscarJugadas(s, matrix, jugador, x, y, nodesExp);
                //System.out.println(matrix[x][y].getOwner());
            }
            contAllied = contAllied + midaMoviments;
        }
        
        // Busquem moviments enemics
        for(int i=0; i< listEnemics.size();i++){
            ArrayList<Point> listMoviments = s.getAmazonMoves(listEnemics.get(i), false);
            int midaMoviments = listMoviments.size();
            for (int j=0; j< midaMoviments; j++){
                int x = listMoviments.get(j).x;
                int y = listMoviments.get(j).y;
                //System.out.println("chivato rival");
                matrix[x][y].setEnemy();                // es negra
                buscarJugadas(s, matrix, !jugador, x, y, nodesExp);
            }      
            //System.out.println("Reina: "+ listEnemics.get(i));
            //System.out.println(s.toString());
            contEnemy = contEnemy + midaMoviments;
        }
        
        // Recorrem tota la matriu, per comprovar propietaris
        int contWhites = 0, contBlacks = 0;
        for (int i=0; i<mida; i++){
            for (int j=0; j<mida; j++){
                String dada = matrix[i][j].getOwner();
                if ("B".equals(dada)) contBlacks+=4;
                else if ("W".equals(dada)) contWhites+=4;
            }
        }
        return (contBlacks+contEnemy) - (contWhites+contAllied);
        //return contEnemy - contAllied;
    }
    
    /**
     * 
     * @param s
     * @param matrix
     * @param jugador   (TRUE = Blanc / FALSE = Negre)
     * @param x
     * @param y
     * @param nodesExp 
     */
    public void buscarJugadas(GameStatus s, Casella matrix[][], boolean jugador, int x, int y, int nodesExp){
        int varX = x-1, varY = y-1;
        int conta = 0;
         
        while (varX <= x+1 && varY <= y+2){
            nodesExp++; 
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                Point nouMoviment = new Point(varX,varY);
     
                // Sólo revisamos si la casilla está libre
                if (s.getPos(nouMoviment) == EMPTY){
                    int copiaX, copiaY;
                    
                    if (nouMoviment.x == x-1 && nouMoviment.y == y-1){          // Diagonal superior izq
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        //int maxbuides = 0;                                      // En cada movimiento, revisa el máximo de vacías que hay a su alrededor
                        copiaX = varX-1;
                        copiaY = varY-1;
                        
                        //copiaX = varX;
                        //copiaY = varY;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            //int buides = 0;
                            // Por cada movimiento en una dirección, revisa las casillas de su alrededor...
                           /* if (s.getPos(new Point(copiaX-1,copiaY-1)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX,copiaY-1)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX+1,copiaY-1)) == EMPTY) buides++;    
                            if (s.getPos(new Point(copiaX-1,copiaY)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX+1,copiaY)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX-1,copiaY+1)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX,copiaY+1)) == EMPTY) buides++;
                            if (s.getPos(new Point(copiaX+1,copiaY+1)) == EMPTY) buides++;
                            
                            if (maxbuides < buides){
                                // POR TERMINAR...
                                maxbuides = buides;
                                //s.getPos(copiaX,copiaY);    // Esta es la posición más segura a la que moverse
                                
                            }*/
                            // Sigue modificando copiaX y copiaY en la dirección indicada (diagonal superior izq en este caso) hasta que no encuentre un Empty
                            copiaX = copiaX-1;
                            copiaY = copiaY-1;
                            // Y sigue marcando territorio
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }         
                    else if (nouMoviment.x == x-1 && nouMoviment.y == y){       // Vertical superior         
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX-1;
                        copiaY = varY;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }      
                    else if (nouMoviment.x == x-1 && nouMoviment.y == y+1){     // Diagonal superior der
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX-1;
                        copiaY = varY+1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            copiaY = copiaY+1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }    
                    else if (nouMoviment.x == x && nouMoviment.y == y-1){       // Horizontal izq
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX;
                        copiaY = varY-1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaY = copiaY-1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }      
                    else if (nouMoviment.x == x && nouMoviment.y == y+1){       // Horizontal der
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX;
                        copiaY = varY+1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaY = copiaY+1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }      
                    else if (nouMoviment.x == x+1 && nouMoviment.y == y-1){     // Diagonal inferior izq
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX+1;
                        copiaY = varY-1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY-1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }    
                    else if (nouMoviment.x == x+1 && nouMoviment.y == y){       // Vertical inferior
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX+1;
                        copiaY = varY;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }      
                    else if (nouMoviment.x == x+1 && nouMoviment.y == y+1){     // Diagonal inferior der
                        if (jugador) matrix[x][y].setNumAllied(5);
                        else matrix[x][y].setNumEnemy(5);
                        copiaX = varX+1;
                        copiaY = varY+1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY+1;
                            if (jugador) matrix[x][y].setNumAllied(5);
                            else matrix[x][y].setNumEnemy(5);
                        }
                    }
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
        /*
        ANTIGUA HEURISTICA BÁSICA:
        
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
        */

    
    

    
    /**
     * primer_lliure
     * @param s2
     * @return 
     */
    public Point primer_lliure(GameStatus s2, int nodesExp){
        //System.out.println(":)");
        int varX = 5;
        int varY = 5;
        boolean trobat = false;
        Point arrowToActual = null;
        int cont = 0;
        while (varX <= 9 && varY <= 9 && !trobat){
            nodesExp++;
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
    public Point buscarMejorTiro(int x, int y, GameStatus s2, int nodesExp){
        
        int varX = x-1, varY = y-1;
        int conta = 0;
        //boolean trobat = false;
        Point arrowToActual = null;
        Point bestArrow = null;
        int contadorMax = 0;
        
        
        
        while (varX <= x+1 && varY <= y+2){
            nodesExp++; 
            //System.out.println("VarX es: " + varX + " y VarY es: " +varY + "cont es " + conta);
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                arrowToActual = new Point(varX,varY);
                
                //System.out.println("entramos en mejor tiro");
                if (s2.getPos(arrowToActual) == EMPTY){
                    int i = 1;
                    int copiaX, copiaY;
                    //System.out.println("entramos en el bucle:x "+varX + " y:"+varY);
                    //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" + arrowToActual);
                    //int i;
                    
                    // Revisión de las 8 posibles posiciones alrededor del contrincante
                    if (arrowToActual.x == x-1 && arrowToActual.y == y-1){          // Diagonal superior izq
                        copiaX = varX-1;
                        copiaY = varY-1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            copiaY = copiaY-1;
                            i++;
                        }
                    }         
                    else if (arrowToActual.x == x-1 && arrowToActual.y == y){       // Vertical superior         
                        //System.out.println("Punto actual: " + arrowToActual.x + ", " + arrowToActual.y);
                        copiaX = varX-1;
                        copiaY = varY;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            //System.out.println("Punto actual: " + copiaX + ", " + copiaY);
                            copiaX = copiaX-1;
                            
                            //copiaY = copiaY;
                            i++;
                            //System.out.println("izquierda");
                        }
                    }      
                    else if (arrowToActual.x == x-1 && arrowToActual.y == y+1){     // Diagonal superior der
                        copiaX = varX-1;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            //System.out.println("Punto actual pre incremento: " + copiaX + ", " + copiaY);
                            copiaX = copiaX-1;
                            copiaY = copiaY+1;
                            //System.out.println("Punto actual post incremento: " + copiaX + ", " + copiaY);
                            i++;
                        }
                    }    
                    else if (arrowToActual.x == x && arrowToActual.y == y-1){       // Horizontal izq
                        copiaX = varX;
                        copiaY = varY-1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            //copiaX = copiaX;
                            copiaY = copiaY-1;
                            i++;
                        }
                    }      
                    else if (arrowToActual.x == x && arrowToActual.y == y+1){       // Horizontal der
                        copiaX = varX;
                        copiaY = varY+1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            //copiaX = copiaX;
                            copiaY = copiaY+1;
                            i++;
                        }
                    }      
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y-1){     // Diagonal inferior izq
                        copiaX = varX+1;
                        copiaY = varY-1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY-1;
                            i++;
                        }
                    }    
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y){       // Vertical inferior
                        copiaX = varX+1;
                        copiaY = varY;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            //copiaY = copiaY;
                            i++;
                        }
                    }      
                    else if (arrowToActual.x == x+1 && arrowToActual.y == y+1){     // Diagonal inferior der
                        copiaX = varX+1;
                        copiaY = varY+1;
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY+1;
                            i++;
                        }
                    }
                    //System.out.println("Antes del Chivo:"+i);

                    if(i > contadorMax){
                        //System.out.println("Chivo");
                        contadorMax = i;
                        bestArrow = new Point(arrowToActual);
                        //System.out.println("BEST ARROW: " + bestArrow);
                    }
                    //s2.placeArrow(arrowToActual);
                    //trobat = true;
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
        
        //if(trobat == false) arrowToActual = null;
        //s2.placeArrow(bestArrow);
        return bestArrow;
    }

    @Override
    public void timeout() {
        // Accede tras exceder el tiempo indicado como Timeout
        hihaTemps = false;
    }

    @Override
    public String getName() {
        return name;
    }
}


// Millora de la heurística, elecció d'un propietari per a cada casella del tauler
class Casella {
   String owner;        // Propietari (B / N / None)
   boolean allied;      // El jugador blanc arriba a la casella?
   boolean enemy;       // El jugador negre arriba a la casella? 
   int numMovesAllied;    // Número de moviments del blanc fins a arribar a la casella
   int numMovesEnemy;    // Número de moviments del negre fins a arribar a la casella
   
   // Constructor
   Casella() {
    //this.owner = "None";
    this.allied = false;
    this.enemy = false;
    this.numMovesAllied = 10;
    this.numMovesEnemy = 10;
   }
   
    // Getter
    String getOwner(){
       if (enemy && !allied){
           return "B";
       }
       else if (!enemy && allied){
           return "W";
       }
       else if (!enemy && !allied){
           if (numMovesEnemy > numMovesAllied) return "B";
           else if (numMovesAllied > numMovesEnemy) return "W";
           else return "None";
       }
       else {                       // Els dos arriben en un moviment
           return "None";
       }
    }
    
    // Setters
    void setNumEnemy(int numMovesEnemy){
        this.numMovesEnemy = numMovesEnemy;
    }
    
    void setNumAllied(int numMovesAllied){
        this.numMovesAllied = numMovesAllied;
    }
    
    void setAllied(){
        this.allied = true;
    }
    
    void setEnemy(){
        this.enemy = true;
    }
}

