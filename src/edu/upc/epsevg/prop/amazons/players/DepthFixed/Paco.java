package edu.upc.epsevg.prop.amazons.players.DepthFixed;

import edu.upc.epsevg.prop.amazons.CellType;
import static edu.upc.epsevg.prop.amazons.CellType.*;
import edu.upc.epsevg.prop.amazons.GameStatus;
import edu.upc.epsevg.prop.amazons.IAuto;
import edu.upc.epsevg.prop.amazons.IPlayer;
import edu.upc.epsevg.prop.amazons.Move;
import edu.upc.epsevg.prop.amazons.SearchType;
import java.awt.Point;
import java.util.ArrayList;

/**
 * DepthFixed version
 * @authors Héctor Montesinos, César Médina
 * Inteligencia artificial de Paco
 */
public class Paco implements IPlayer, IAuto {

    private String name;
    private GameStatus s;
    private double mejor_movimiento;
    private int profundidad;
    int nodosQuarterBoard;
    int nodosExp;
    
    /**
     * Contructor por defecto de Paco DepthFixed
     *
     * @param profundidad   - Profundidad pasada por parámetro
     */
    public Paco(int profundidad) {
        this.name = "Paco fijo";
        this.profundidad = profundidad;
        this.nodosQuarterBoard = 23;
    }

    
    /**
     * Método que evalua y devuelve el mejor movimiento posible dado un tablero
     * @param s         - Tablero actual
     * @return bestMove - Mejor combinación de movimiento y tirada de flecha
     */
    @Override
    public Move move(GameStatus s){
        mejor_movimiento = Double.NEGATIVE_INFINITY;
        Point arrowTo = null;
        Point amazonTo = null;
        Point amazonFrom = null;
        nodosExp = 0;
        
        if (!s.isGameOver()){
            CellType color = s.getCurrentPlayer();                      // Color = Jugador actual (P1 o P2)
            ArrayList<Point> listAmazonas = new ArrayList<>();
            int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador (4)
            for (int i=0; i<numAmazonas; i++){
                listAmazonas.add(s.getAmazon(color, i));                // Posiciones de las amazonas
            }

            ArrayList<Point> listEnemigos = new ArrayList<>();           
            boolean encontrados = false;                                    // ¿Es el enemigo?
            int cont = 0;                                                   // Contador que busca los enemigos
            // Búsqueda de las amazonas enemigas
            for (int i=0; i<s.getSize() && !encontrados; i++){              // Filas
                for (int j=0; j<s.getSize() && !encontrados; j++){          // Columnas
                    Point t = new Point(i,j);                               // t = posición i,j
                    if (s.getPos(t) == opposite(color)) {    
                        listEnemigos.add(t);
                        cont++;
                        if (cont == 4) encontrados = true;
                    }
                }
            }

            for (int i=0; i<listAmazonas.size(); i++){
                ArrayList<Point> listMovimientos = s.getAmazonMoves(listAmazonas.get(i), false);    // Boolean=True: Muestra sólo jugadas finales, no intermedias
                for (int j=0; j<listMovimientos.size(); j++){
                    GameStatus s2 = new GameStatus(s);
                    Point arrowToActual = null;
                    Point amazonTemp = s2.getAmazon(color, i);
                    s2.moveAmazon(amazonTemp, listMovimientos.get(j));
                    listAmazonas.set(i, listMovimientos.get(j));
                    nodosExp++;

                    // ----------------------- Estrategia 1 -----------------------
                    if (s2.getEmptyCellsCount() > nodosQuarterBoard){
                        int ii = 0;
                        boolean encontrado = false;

                        // Bucle tiraflechas
                        while (ii < listEnemigos.size() && !encontrado){
                            int x = listEnemigos.get(ii).x;       // COLUMNA
                            int y = listEnemigos.get(ii).y;       // FILA

                            arrowToActual = buscarMejorTiro(x, y, s2, nodosExp);
                            if (arrowToActual == null) arrowToActual = primeroLibre(s2, nodosExp);          
                            else encontrado = true;                                            
                            ii++;
                        }
                        s2.placeArrow(arrowToActual);
                                                                                        // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                        double movimiento = min_max(s2, profundidad-1, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, listAmazonas, false);
                        listAmazonas.set(i,amazonTemp);
                        if(movimiento > mejor_movimiento){
                            amazonTo = listMovimientos.get(j);
                            amazonFrom = listAmazonas.get(i);
                            arrowTo = arrowToActual;
                            mejor_movimiento = movimiento;
                        }
                    }
                    // ----------------------- Estrategia 2 -----------------------
                    else {
                        double mejor_movimiento_flecha = Double.NEGATIVE_INFINITY;
                        Point mejor_flecha = null;

                        arrowToActual = null;
                        for(int ii=0;ii<s2.getSize();ii++){
                            for(int jj=0; jj<s2.getSize();jj++){
                                nodosExp++;
                                arrowToActual = new Point(jj, ii);
                                if (s2.getPos(arrowToActual) == EMPTY){
                                    GameStatus s3 = new GameStatus(s2);
                                    s3.placeArrow(arrowToActual);
                                                                                                           // NEGATIVE_INFINITY = Alpha, POSITIVE_INFINITY = Beta
                                    double movimiento_flecha = min_max(s3, profundidad-1, opposite(color), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, listAmazonas, false);
                                    if (movimiento_flecha >= mejor_movimiento_flecha){
                                        mejor_movimiento_flecha = movimiento_flecha;
                                        mejor_flecha = arrowToActual;
                                    }
                                }
                            }
                        }
                        listAmazonas.set(i,amazonTemp);
                        
                        if (mejor_movimiento_flecha >= mejor_movimiento){
                            amazonTo = listMovimientos.get(j);
                            amazonFrom = listAmazonas.get(i);
                            arrowTo = mejor_flecha;
                            mejor_movimiento = mejor_movimiento_flecha;
                        }
                    }
                }
            }
        }
        return new Move(amazonFrom, amazonTo, arrowTo, nodosExp, profundidad, SearchType.MINIMAX);
    }
    
    /**
     * Realiza el algoritmo Min_Max con la poda Alpha-Beta
     * 
     * @param s                 - Tablero
     * @param profundidad       - Profundidad
     * @param color             - Jugador (P1 o P2)
     * @param alpha             - α
     * @param beta              - β
     * @param listEnemigos      - Lista de enemigos
     * @param min_or_max        - TRUE = MAX / FALSE = MIN
     * @return val_actual       - Valor del mejor movimiento
     */
    private double min_max(GameStatus s, int profundidad, CellType color, double alpha, double beta, ArrayList<Point> listEnemigos, boolean min_or_max){
        double val_actual;
        nodosExp++;
        if (profundidad == 0 || s.isGameOver()){                                // Llamada a la función heurística
            double heu = funcionHeuristica(s, color, listEnemigos);             
            return heu;
        }

        ArrayList<Point> listAmazonas = new ArrayList<>();
        int numAmazonas = s.getNumberOfAmazonsForEachColor();                   // Número de amazonas para cada jugador (4)
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                            // Posiciones de las amazonas
        }

        
        if(min_or_max) val_actual = Double.NEGATIVE_INFINITY;                   // TRUE = MAX
        else val_actual = Double.POSITIVE_INFINITY;                             // FALSE = MIN
        
        for (int i=0; i<listAmazonas.size(); i++){
            ArrayList<Point> listMovimientos = s.getAmazonMoves(listAmazonas.get(i), false); // Restricted=False: Muestra todas las jugadas
            for (int j=0; j<listMovimientos.size(); j++){
                GameStatus s2 = new GameStatus(s);       
                Point arrowToActual = null;
                Point amazonTemp = s2.getAmazon(color, i);
                s2.moveAmazon(amazonTemp, listMovimientos.get(j));
                
                listAmazonas.set(i, listMovimientos.get(j));        // Actualización de las posiciones de las amazonas
                
                // ----------------------- Estrategia 1 -----------------------
                if (s2.getEmptyCellsCount() > nodosQuarterBoard){
                    int ii = 0;
                    boolean encontrado = false;
                    
                    // Bucle tiraflechas
                    while (ii < listEnemigos.size() && !encontrado){
                        int x = listEnemigos.get(ii).x;       // COLUMNA
                        int y = listEnemigos.get(ii).y;       // FILA

                        arrowToActual = buscarMejorTiro(x, y, s2, nodosExp);
                        if (arrowToActual == null) arrowToActual = primeroLibre(s2, nodosExp);          
                        else encontrado = true;
                        ii++;
                    }
                    s2.placeArrow(arrowToActual);

                    double eval = min_max(s2, profundidad-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                    listAmazonas.set(i,amazonTemp);

                    if(min_or_max){  // MAX
                        val_actual = Math.max(eval, val_actual);
                        alpha = Math.max(alpha, eval);
                        if(beta <= alpha) return val_actual;
                    }
                    else {           // MIN 
                        val_actual = Math.min(eval, val_actual);
                        beta = Math.min(beta, eval);
                        if (beta <= alpha) return val_actual;
                    }
                }
                // ----------------------- Estrategia 2 -----------------------
                else { 
                    arrowToActual = null;
                    for (int ii=0;ii<s2.getSize();ii++){
                        for(int jj=0; jj<s2.getSize();jj++){
                            nodosExp++;
                            arrowToActual = new Point(jj, ii);
                            if (s2.getPos(arrowToActual) == EMPTY){
                                GameStatus s3 = new GameStatus(s2);
                                s3.placeArrow(arrowToActual);
                                
                                double eval = min_max(s3, profundidad-1, opposite(color), alpha, beta, listAmazonas, !min_or_max);
                                if(min_or_max){ // MAX 
                                    val_actual = Math.max(eval, val_actual);
                                    alpha = Math.max(alpha, eval);
                                    if(beta <= alpha) return val_actual;
                                }
                                else {           // MIN 
                                    val_actual = Math.min(eval, val_actual);
                                    beta = Math.min(beta, eval);
                                    if (beta <= alpha) return val_actual;
                                }
                            }
                        }
                    }
                    listAmazonas.set(i,amazonTemp);
                }
            }
        } 
      
        return val_actual;
    }
    
    
    /**
     * Recorre todo el tablero e inicializa una matriz, 
     * posteriormente contabiliza movimientos de cada jugador y número de casillas de las cuales es propietario cada jugador.
     * Devuelve la diferencia de la suma de movimientos y propiedad de casillas (multiplicado por 4) de los dos jugadores.
     * 
     * @param s                 - Tablero
     * @param color             - Jugador (P1 o P2)
     * @param listEnemigos      - Lista de amazonas del enemigo
     * @return                  (numCasillasPropiedadP2*4+numMovimientosP2) - (numCasillasPropiedadP1*4+numMovimientosP1)
    */
    public double funcionHeuristica(GameStatus s, CellType color, ArrayList<Point> listEnemigos){

        int tam = s.getSize();
        int contAliado=0, contEnemigo=0;
        Casilla[][] matriz = new Casilla[tam][tam];
        
        // Inicialización de la matriz
        for (int i=0; i<tam; i++){
            for (int j=0; j<tam; j++){
                matriz[i][j] = new Casilla();
            }
        }
        
        // Posiciones de las amazonas aliadas
        int numAmazonas = s.getNumberOfAmazonsForEachColor();       // Número de amazonas para cada jugador = 4
        ArrayList<Point> listAmazonas = new ArrayList<>();
        for (int i=0; i<numAmazonas; i++){
            listAmazonas.add(s.getAmazon(color, i));                
        }
        
        // Buscamos movimientos aliados
        boolean jugador = true;
        for(int i=0; i< listAmazonas.size();i++){
            ArrayList<Point> listMovimientos = s.getAmazonMoves(listAmazonas.get(i), false);
            int tamMovimientos = listMovimientos.size();
            for (int j=0; j< tamMovimientos; j++){
                int x = listMovimientos.get(j).x;
                int y = listMovimientos.get(j).y;

                matriz[x][y].modAliado();                           // Es aliado 
                buscarJugadas(s, matriz, jugador, x, y, nodosExp);
            }
            contAliado = contAliado + tamMovimientos;
        }
        
        // Buscamos movimientos enemigos
        for(int i=0; i< listEnemigos.size();i++){
            ArrayList<Point> listMovimientos = s.getAmazonMoves(listEnemigos.get(i), false);
            int tamMovimientos = listMovimientos.size();
            for (int j=0; j< tamMovimientos; j++){
                int x = listMovimientos.get(j).x;
                int y = listMovimientos.get(j).y;

                matriz[x][y].modEnemigo();                            // Es enemigo
                buscarJugadas(s, matriz, !jugador, x, y, nodosExp);
            }      
            contEnemigo = contEnemigo + tamMovimientos;
        }
        
        // Se recorre toda la matriz para comprobar y contabilizar propietarios de cada casilla
        int contBlancos = 0, contNegros = 0;
        for (int i=0; i<tam; i++){
            for (int j=0; j<tam; j++){
                String dato = matriz[i][j].obtenerPropietario();
                if ("B".equals(dato)) contNegros+=4;                // W = WHITES
                else if ("W".equals(dato)) contBlancos+=4;          // B = BLACKS
            }
        }
        // El return se realiza en este orden, y no en el contrario, ya que se llama a la función heurística 
        // con el jugador actual en ese momento, y no con el jugador contrario (opposite)
        return (contNegros+contEnemigo) - (contBlancos+contAliado);
    }
    
    /** Este método recorre el tablero dado un segundo movimiento de un jugador en busca de territorializar casillas.
     * Si dado un movimiento (será el segundo, dada una posición) de un jugador, pasa por una casilla y modificará su contador en dicha casilla
     * a 5, para indicar que ha pasado por ahí.
     *
     * @param s                 - Tablero
     * @param matriz            - Matriz de 10x10 que simula el tablero
     * @param jugador           - TRUE = Aliado / FALSE = Enemigo
     * @param x                 - Parámetro x dado un punto
     * @param y                 - Parámetro y dado un punto
     * @param nodosExp          - Número de nodos explorados
     */
    public void buscarJugadas(GameStatus s, Casilla matriz[][], boolean jugador, int x, int y, int nodosExp){
        int varX = x-1, varY = y-1;
        int conta = 0;
         
        while (varX <= x+1 && varY <= y+2){
            nodosExp++; 
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                Point nuevoMovimiento = new Point(varX,varY);
     
                // Sólo revisamos si la casilla está libre
                if (s.getPos(nuevoMovimiento) == EMPTY){
                    int copiaX, copiaY;
                    
                    if (nuevoMovimiento.x == x-1 && nuevoMovimiento.y == y-1){          // Diagonal superior izq
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        
                        copiaX = varX-1;
                        copiaY = varY-1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            copiaY = copiaY-1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }         
                    else if (nuevoMovimiento.x == x-1 && nuevoMovimiento.y == y){       // Vertical superior         
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX-1;
                        copiaY = varY;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }      
                    else if (nuevoMovimiento.x == x-1 && nuevoMovimiento.y == y+1){     // Diagonal superior der
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX-1;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            copiaY = copiaY+1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }    
                    else if (nuevoMovimiento.x == x && nuevoMovimiento.y == y-1){       // Horizontal izq
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX;
                        copiaY = varY-1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaY = copiaY-1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }      
                    else if (nuevoMovimiento.x == x && nuevoMovimiento.y == y+1){       // Horizontal der
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaY = copiaY+1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }      
                    else if (nuevoMovimiento.x == x+1 && nuevoMovimiento.y == y-1){     // Diagonal inferior izq
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX+1;
                        copiaY = varY-1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY-1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }    
                    else if (nuevoMovimiento.x == x+1 && nuevoMovimiento.y == y){       // Vertical inferior
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX+1;
                        copiaY = varY;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
                        }
                    }      
                    else if (nuevoMovimiento.x == x+1 && nuevoMovimiento.y == y+1){     // Diagonal inferior der
                        if (jugador) matriz[x][y].modNumAliados(5);
                        else matriz[x][y].modNumEnemigos(5);
                        copiaX = varX+1;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX+1;
                            copiaY = copiaY+1;
                            if (jugador) matriz[x][y].modNumAliados(5);
                            else matriz[x][y].modNumEnemigos(5);
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

    /**
     * Este método recorre las casillas adyacentes libres del enemigo, sin salirse del tablero, y coloca una flecha en su costado más prometedor.
     * Es decir, en caso de qué el enemigo tenga 8 casillas libres a su alrededor, realizará un conteo en cada una de las 8 direcciones y,
     * en función de la dirección en la cuál se encuentre más casillas libres, tapará esa dirección con una flecha en la casilla adyacente del enemigo.
     * 
     * @param x                     - Parámetro x dado un punto         -
     * @param y                     - Parámetro y dado un punto
     * @param s2                    - Tablero
     * @param nodosExp              - Nodos explorados
     * @return bestArrow            - Punto en el tablero dónde colocar la flecha
     */
    public Point buscarMejorTiro(int x, int y, GameStatus s2, int nodosExp){
        
        int varX = x-1, varY = y-1;
        int conta = 0;
        Point arrowToActual = null;
        Point bestArrow = null;
        int contadorMax = 0;

        while (varX <= x+1 && varY <= y+2){
            nodosExp++; 
            if ((varX >= 0 && varX <= 9) && (varY >=0 && varY <= 9)){
                arrowToActual = new Point(varX,varY);
                
                if (s2.getPos(arrowToActual) == EMPTY){
                    int i = 1;
                    int copiaX, copiaY;
                    
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
                        copiaX = varX-1;
                        copiaY = varY;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            i++;
                        }
                    }      
                    else if (arrowToActual.x == x-1 && arrowToActual.y == y+1){     // Diagonal superior der
                        copiaX = varX-1;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaX = copiaX-1;
                            copiaY = copiaY+1;
                            i++;
                        }
                    }    
                    else if (arrowToActual.x == x && arrowToActual.y == y-1){       // Horizontal izq
                        copiaX = varX;
                        copiaY = varY-1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
                            copiaY = copiaY-1;
                            i++;
                        }
                    }      
                    else if (arrowToActual.x == x && arrowToActual.y == y+1){       // Horizontal der
                        copiaX = varX;
                        copiaY = varY+1;
                        
                        while ((copiaX >= 0 && copiaX <= 9) && (copiaY >=0 && copiaY <= 9) && s2.getPos(new Point(copiaX,copiaY)) == EMPTY){
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

                    if(i > contadorMax){
                        contadorMax = i;
                        bestArrow = new Point(arrowToActual);
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
        return bestArrow;
    }

    
    /** Este método complementa a buscarMejorTiro(). 
    * Ante una situación remota dónde uno de los dos jugadores se acorrala a si mismo y no hay casillas libres a su alrededor,
    * esta función recorre el último cuarto de tablero en busca de una casilla libre dónde poner la última flecha y finalizar la partida.
    * 
    * @param s2                 - Tablero
    * @param nodosExp           - Nodos explorados
    * @return arrowToActual     - Punto en el tablero dónde colocar la flecha
    */
    public Point primeroLibre(GameStatus s2, int nodosExp){
        int varX = 5;
        int varY = 5;
        boolean encontrado = false;
        Point arrowToActual = null;
        int cont = 0;
        while (varX <= 9 && varY <= 9 && !encontrado){
            nodosExp++;
            arrowToActual = new Point(varX, varY);
            if (s2.getPos(arrowToActual) == EMPTY){
                encontrado = true;
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
        
        
    @Override
    public void timeout() {
        // Nothing to do! I'm so fast, I never timeout 8-)
    }

    
    /** Devuelve el nombre del jugador.
     */
    @Override
    public String getName() {
        return name;
    }
}


/**
 * @authors Héctor Montesinos, César Médina
 * Clase Casilla: Mejora la heurística para territorializar el tablero.
 */
class Casilla {
   String owner;            // Propietario (B / N / None)
   boolean aliado;          // El jugador blanco llega a la casilla?
   boolean enemigo;         // El jugador negro llega a la casilla? 
   int numMovsAliados;      // Número de movimientos del blanco hasta llegar a la casilla
   int numMovsEnemigos;     // Número de movimientos del negro hasta llegar a la casilla
   
   /** Casilla
    *  Constructor de la clase
    */
   Casilla() {
    this.aliado = false;
    this.enemigo = false;
    this.numMovsAliados = 10;
    this.numMovsEnemigos = 10;
   }
   
    // Getter - Consultor
   
   /** obtenerPropietario
    *  Devuelve el propietario de cada casilla.
    * @return Propietario de cada casilla
    */
    String obtenerPropietario(){
       if (enemigo && !aliado){         // Negro llega en un movimiento (Black)
           return "B";
       }
       else if (!enemigo && aliado){    // Blanco llega en un movimiento (White)
           return "W";
       }
       else if (!enemigo && !aliado){   // Los dos llegan en dos o más movimientos
           if (numMovsEnemigos > numMovsAliados) return "B";
           else if (numMovsAliados > numMovsEnemigos) return "W";
           else return "None";
       }
       else {                           // Los dos llegan en un movimiento
           return "None";
       }
    }
    
    // Setters - Modificadores
    /** modNumEnemigos
     * Modifica el número de movimientos enemigos.
     * @param numMovsEnemigos 
     */
    void modNumEnemigos(int numMovsEnemigos){
        this.numMovsEnemigos = numMovsEnemigos;
    }
    
    /** numMovsAliados
     * Modifica el número de movimientos aliados.
     * @param numMovsAliados 
     */
    void modNumAliados(int numMovsAliados){
        this.numMovsAliados = numMovsAliados;
    }
    
    /** modAliado
     * Registra una casilla de propiedad del aliado.
     */
    void modAliado(){
        this.aliado = true;
    }
    
    /** modEnemigo
     * Registra una casilla de propiedad del enemigo.
     */
    void modEnemigo(){
        this.enemigo = true;
    }
}