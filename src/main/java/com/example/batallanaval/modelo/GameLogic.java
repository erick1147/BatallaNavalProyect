package com.example.batallanaval.modelo;
import java.util.Random;
import java.util.List;

public class GameLogic {

    // Matrices para rastrear el estado del juego
    private boolean[][] matrizLimpiezaPlayer = new boolean[10][10];
    private boolean[][] matrizLimpiezaCpu = new boolean[10][10];
    private boolean[][] matrizAtinacionPlayer = new boolean[10][10];
    private boolean[][] matrizAtinacionCpu = new boolean[10][10];
    private boolean[][] matrizDisparosPlayer = new boolean[10][10];
    private boolean[][] matrizDisparosCpu = new boolean[10][10];

    // Arrays para almacenar los barcos
    private Ship[] arrayPlayer = new Ship[10];
    private Ship[] arrayCpu = new Ship[10];

    // Variables para el control del fin del juego
    private boolean gameEnded = false;
    private String winner = "";

    // Random para posicionamiento automático de barcos de la CPU
    private Random random = new Random();

    /**
     * Verifica si un barco puede ser colocado en las coordenadas especificadas
     */
    public boolean canPlaceShip(int startCol, int startRow, int widthCells, int heightCells) {
        if (startCol < 0 || startRow < 0 ||
                startCol + widthCells > Constants.GRID_COLS ||
                startRow + heightCells > Constants.GRID_ROWS) {
            return false;
        }

        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                if (matrizLimpiezaPlayer[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Verifica si un barco de la CPU puede ser colocado en las coordenadas especificadas
     */
    public boolean canPlaceShipCpu(int startCol, int startRow, int widthCells, int heightCells) {
        if (startCol < 0 || startRow < 0 ||
                startCol + widthCells > Constants.GRID_COLS ||
                startRow + heightCells > Constants.GRID_ROWS) {
            return false;
        }

        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                if (matrizLimpiezaCpu[row][col]) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Marca las celdas como ocupadas por un barco en la matriz de limpieza
     */
    public void placeShip(int startCol, int startRow, int widthCells, int heightCells) {
        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                matrizLimpiezaPlayer[row][col] = true;
            }
        }
    }

    /**
     * Marca las celdas como ocupadas por un barco de la CPU
     */
    public void placeShipCpu(int startCol, int startRow, int widthCells, int heightCells) {
        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                matrizLimpiezaCpu[row][col] = true;
            }
        }
    }

    /**
     * Libera las celdas ocupadas por un barco (para cuando se mueve)
     */
    public void removeShip(int startCol, int startRow, int widthCells, int heightCells) {
        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                matrizLimpiezaPlayer[row][col] = false;
            }
        }
    }

    /**
     * Encuentra la posición válida más cercana para colocar un barco
     */
    public int[] findNearestValidPosition(int preferredCol, int preferredRow, int widthCells, int heightCells) {
        if (canPlaceShip(preferredCol, preferredRow, widthCells, heightCells)) {
            return new int[]{preferredCol, preferredRow};
        }

        int maxDistance = Math.max(Constants.GRID_ROWS, Constants.GRID_COLS);

        for (int distance = 1; distance <= maxDistance; distance++) {
            for (int deltaRow = -distance; deltaRow <= distance; deltaRow++) {
                for (int deltaCol = -distance; deltaCol <= distance; deltaCol++) {
                    if (Math.abs(deltaRow) != distance && Math.abs(deltaCol) != distance) {
                        continue;
                    }

                    int newCol = preferredCol + deltaCol;
                    int newRow = preferredRow + deltaRow;

                    if (canPlaceShip(newCol, newRow, widthCells, heightCells)) {
                        return new int[]{newCol, newRow};
                    }
                }
            }
        }

        return null;
    }

    /**
     * Posiciona automáticamente todos los barcos de la CPU
     */
    public void posicionarBarcosCpu() {
        System.out.println("=== POSICIONANDO BARCOS DE LA CPU ===");

        // Limpiar matriz de la CPU
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrizLimpiezaCpu[i][j] = false;
            }
        }

        // Definir los barcos a crear
        int[][] shipSizes = {
                {4, 1}, {3, 1}, {3, 1}, {2, 1}, {2, 1}, 
                {2, 1}, {1, 1}, {1, 1}, {1, 1}, {1, 1}
        };

        String[] shipNames = {"Portaaviones", "Submarino-1", "Submarino-2",
                "Destructor-1", "Destructor-2", "Destructor-3",
                "Fragata-1", "Fragata-2", "Fragata-3", "Fragata-4"};

        // Crear e inicializar los barcos de la CPU
        for (int i = 0; i < 10; i++) {
            arrayCpu[i] = new Ship();
        }

        // Colocar cada barco automáticamente
        for (int i = 0; i < shipSizes.length; i++) {
            int widthCells = shipSizes[i][0];
            int heightCells = shipSizes[i][1];
            boolean placed = false;
            int attempts = 0;
            int maxAttempts = 1000;

            while (!placed && attempts < maxAttempts) {
                int startCol = random.nextInt(Constants.GRID_COLS);
                int startRow = random.nextInt(Constants.GRID_ROWS);

                boolean isVertical = random.nextBoolean();
                int finalWidth = isVertical ? heightCells : widthCells;
                int finalHeight = isVertical ? widthCells : heightCells;

                if (canPlaceShipCpu(startCol, startRow, finalWidth, finalHeight)) {
                    placeShipCpu(startCol, startRow, finalWidth, finalHeight);

                    for (int row = startRow; row < startRow + finalHeight; row++) {
                        for (int col = startCol; col < startCol + finalWidth; col++) {
                            arrayCpu[i].addCoordinate(col, row);
                        }
                    }

                    placed = true;
                    System.out.println("✓ " + shipNames[i] + " colocado en (" + startCol + "," + startRow + ")");
                }
                attempts++;
            }

            if (!placed) {
                System.out.println("✗ ERROR: No se pudo colocar " + shipNames[i]);
            }
        }

        System.out.println("=== BARCOS DE LA CPU POSICIONADOS ===");
    }

    /**
     * Obtiene las coordenadas de todas las celdas que ocupa un barco
     */
    public int[][] getShipCoordinates(int startCol, int startRow, int widthCells, int heightCells) {
        int totalCells = widthCells * heightCells;
        int[][] coordinates = new int[totalCells][2];
        int index = 0;

        for (int row = startRow; row < startRow + heightCells; row++) {
            for (int col = startCol; col < startCol + widthCells; col++) {
                coordinates[index][0] = col;
                coordinates[index][1] = row;
                index++;
            }
        }

        return coordinates;
    }

    /**
     * Método para verificar si ya se disparó a una coordenada
     */
    public boolean yaSeDisparo(int col, int row, int jugador) {
        if (jugador == 0) { // Jugador humano dispara a CPU
            return matrizDisparosPlayer[row][col];
        } else { // CPU dispara a jugador
            return matrizDisparosCpu[row][col];
        }
    }

    /**
     * Método principal para manejar jugadas
     */
    public String jugada(int x, int y, int jugador) {
        if (gameEnded) {
            return "JUEGO_TERMINADO";
        }

        if (jugador == 0) { // Jugador humano dispara a CPU
            if (matrizDisparosPlayer[y][x]) {
                return "YA_DISPARADO";
            }

            matrizDisparosPlayer[y][x] = true;

            if (matrizLimpiezaCpu[y][x]) {
                matrizAtinacionCpu[y][x] = true;

                for (int i = 0; i < 10; i++) {
                    if (arrayCpu[i] != null && arrayCpu[i].containsCoordinate(x, y)) {
                        if (cambiarPng(arrayCpu[i], jugador)) {
                            if (todasFlotasHundidas(1)) {
                                gameEnded = true;
                                winner = "JUGADOR";
                                return "VICTORIA_JUGADOR";
                            }
                            return "HUNDIDO";
                        } else {
                            return "TOCADO";
                        }
                    }
                }
                return "TOCADO";
            } else {
                return "AGUA";
            }

        } else if (jugador == 1) { // CPU dispara a jugador
            if (matrizDisparosCpu[y][x]) {
                return "YA_DISPARADO";
            }

            matrizDisparosCpu[y][x] = true;

            if (matrizLimpiezaPlayer[y][x]) {
                matrizAtinacionPlayer[y][x] = true;

                for (int i = 0; i < 10; i++) {
                    if (arrayPlayer[i] != null && arrayPlayer[i].containsCoordinate(x, y)) {
                        if (cambiarPng(arrayPlayer[i], jugador)) {
                            if (todasFlotasHundidas(0)) {
                                gameEnded = true;
                                winner = "CPU";
                                return "VICTORIA_CPU";
                            }
                            return "HUNDIDO";
                        } else {
                            return "TOCADO";
                        }
                    }
                }
                return "TOCADO";
            } else {
                return "AGUA";
            }
        }

        return "ERROR";
    }

    /**
     * Verifica si todas las flotas de un jugador están hundidas
     */
    public boolean todasFlotasHundidas(int jugador) {
        Ship[] barcosAVerificar;
        boolean[][] matrizAtinacion;

        if (jugador == 0) {
            barcosAVerificar = arrayPlayer;
            matrizAtinacion = matrizAtinacionPlayer;
        } else {
            barcosAVerificar = arrayCpu;
            matrizAtinacion = matrizAtinacionCpu;
        }

        int barcosHundidos = 0;
        int barcosIntactos = 0;

        for (int i = 0; i < 10; i++) {
            if (barcosAVerificar[i] != null && barcosAVerificar[i].getSize() > 0) {
                boolean barcoCompletamenteHundido = true;

                for (int[] coordinate : barcosAVerificar[i].getCoordinates()) {
                    int col = coordinate[0];
                    int row = coordinate[1];

                    if (!matrizAtinacion[row][col]) {
                        barcoCompletamenteHundido = false;
                        break;
                    }
                }

                if (barcoCompletamenteHundido) {
                    barcosHundidos++;
                } else {
                    barcosIntactos++;
                }
            }
        }

        return (barcosIntactos == 0 && barcosHundidos > 0);
    }

    /**
     * Verifica si un barco está completamente hundido
     */
    public boolean cambiarPng(Ship barco, int jugador) {
        boolean allDestroyed = true;

        for (int[] coordinate : barco.getCoordinates()) {
            int col = coordinate[0];
            int row = coordinate[1];

            if (jugador == 0) { // Verificando barcos de CPU
                if (!matrizAtinacionCpu[row][col]) {
                    allDestroyed = false;
                    break;
                }
            } else { // Verificando barcos de jugador
                if (!matrizAtinacionPlayer[row][col]) {
                    allDestroyed = false;
                    break;
                }
            }
        }

        return allDestroyed;
    }

    /**
     * Inicializa los barcos del jugador
     */
    public void inicializarBarcosJugador(Object playerShips) {
        if (playerShips instanceof List<?>) {
            List<?> shipsList = (List<?>) playerShips;

            for (int i = 0; i < shipsList.size() && i < 10; i++) {
                arrayPlayer[i] = new Ship();
                Object ship = shipsList.get(i);

                try {
                    java.lang.reflect.Method getCurrentGridPositionMethod = ship.getClass().getMethod("getCurrentGridPosition");
                    java.lang.reflect.Method getCurrentDimensionsInCellsMethod = ship.getClass().getMethod("getCurrentDimensionsInCells");

                    int[] position = (int[]) getCurrentGridPositionMethod.invoke(ship);
                    int[] dimensions = (int[]) getCurrentDimensionsInCellsMethod.invoke(ship);

                    if (position[0] >= 0 && position[1] >= 0) {
                        for (int row = position[1]; row < position[1] + dimensions[1]; row++) {
                            for (int col = position[0]; col < position[0] + dimensions[0]; col++) {
                                arrayPlayer[i].addCoordinate(col, row);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error inicializando barco " + (i+1) + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Verifica si quedan posiciones válidas para disparar
     */
    public boolean quedanPosicionesParaDisparar(int jugador) {
        boolean[][] matrizDisparos;

        if (jugador == 0) {
            matrizDisparos = matrizDisparosPlayer;
        } else {
            matrizDisparos = matrizDisparosCpu;
        }

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (!matrizDisparos[row][col]) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Encuentra una coordenada aleatoria válida para disparar
     */
    public int[] encontrarCoordenadaValidaParaDisparar(int jugador) {
        if (!quedanPosicionesParaDisparar(jugador)) {
            return null;
        }

        int maxIntentos = 200;
        int intentos = 0;

        while (intentos < maxIntentos) {
            int col = random.nextInt(Constants.GRID_COLS);
            int row = random.nextInt(Constants.GRID_ROWS);

            if (!yaSeDisparo(col, row, jugador)) {
                return new int[]{col, row};
            }
            intentos++;
        }

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (!yaSeDisparo(col, row, jugador)) {
                    return new int[]{col, row};
                }
            }
        }

        return null;
    }

    /**
     * Reinicia el estado del juego
     */
    public void reiniciarJuego() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrizLimpiezaPlayer[i][j] = false;
                matrizLimpiezaCpu[i][j] = false;
                matrizAtinacionPlayer[i][j] = false;
                matrizAtinacionCpu[i][j] = false;
                matrizDisparosPlayer[i][j] = false;
                matrizDisparosCpu[i][j] = false;
            }
        }

        for (int i = 0; i < 10; i++) {
            arrayPlayer[i] = null;
            arrayCpu[i] = null;
        }

        gameEnded = false;
        winner = "";
    }

    /**
     * Imprime el estado actual de la matriz de limpieza
     */
    public void printMatrizLimpieza() {
        System.out.println("Estado actual de la matriz de limpieza JUGADOR:");
        System.out.println("   0 1 2 3 4 5 6 7 8 9  <- COLUMNAS");
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            System.out.print(row + ": ");
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                System.out.print(matrizLimpiezaPlayer[row][col] ? "X " : "- ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // MÉTODOS SETTER PARA RESTAURAR ESTADO DEL JUEGO
    public void setMatrizLimpiezaPlayer(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizLimpiezaPlayer = matriz;
            System.out.println("✓ Matriz limpieza jugador restaurada");
        }
    }

    public void setMatrizLimpiezaCpu(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizLimpiezaCpu = matriz;
            System.out.println("✓ Matriz limpieza CPU restaurada");
        }
    }

    public void setMatrizDisparosPlayer(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizDisparosPlayer = matriz;
            System.out.println("✓ Matriz disparos jugador restaurada");
        }
    }

    public void setMatrizDisparosCpu(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizDisparosCpu = matriz;
            System.out.println("✓ Matriz disparos CPU restaurada");
        }
    }

    public void setMatrizAtinacionCpu(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizAtinacionCpu = matriz;
            System.out.println("✓ Matriz atinación CPU restaurada");
        }
    }

    public void setMatrizAtinacionPlayer(boolean[][] matriz) {
        if (matriz != null && matriz.length == 10 && matriz[0].length == 10) {
            this.matrizAtinacionPlayer = matriz;
            System.out.println("✓ Matriz atinación jugador restaurada");
        }
    }

    // GETTERS PARA EL ESTADO DEL JUEGO
    public boolean isGameEnded() {
        return gameEnded;
    }

    public String getWinner() {
        return winner;
    }

    // GETTERS PARA ACCEDER A LAS MATRICES
    public boolean[][] getMatrizLimpiezaPlayer() {
        return matrizLimpiezaPlayer;
    }

    public boolean[][] getMatrizLimpiezaCpu() {
        return matrizLimpiezaCpu;
    }

    public boolean[][] getMatrizDisparosPlayer() {
        return matrizDisparosPlayer;
    }

    public boolean[][] getMatrizDisparosCpu() {
        return matrizDisparosCpu;
    }

    public boolean[][] getMatrizAtinacionCpu() {
        return matrizAtinacionCpu;
    }

    public boolean[][] getMatrizAtinacionPlayer() {
        return matrizAtinacionPlayer;
    }

    public Ship[] getArrayCpu() {
        return arrayCpu;
    }

    public Ship[] getArrayPlayer() {
        return arrayPlayer;
    }
}