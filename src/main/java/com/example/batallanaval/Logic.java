package com.example.batallanaval;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Random;

public class Logic {

    // Matriz para rastrear qué celdas están ocupadas por barcos del jugador
    private boolean[][] matrizLimpiezaPlayer = new boolean[10][10];
    private boolean[][] matrizLimpiezaCpu = new boolean[10][10];

    // Arrays para almacenar los barcos
    private Ship[] arrayPlayer = new Ship[10];
    private Ship[] arrayCpu = new Ship[10];
    private boolean[][] matrizAtinacionPlayer = new boolean[10][10];
    private boolean[][] matrizAtinacionCpu = new boolean[10][10];

    // Matriz para rastrear disparos realizados (para evitar disparar dos veces al mismo lugar)
    private boolean[][] matrizDisparosPlayer = new boolean[10][10]; // Disparos del jugador al CPU
    private boolean[][] matrizDisparosCpu = new boolean[10][10];    // Disparos del CPU al jugador

    // Random para posicionamiento automático de barcos de la CPU
    private Random random = new Random();

    // NUEVO: Variables para el control del fin del juego
    private boolean gameEnded = false;
    private String winner = "";

    /**
     * Verifica si un barco puede ser colocado en las coordenadas especificadas
     * @param startCol Columna inicial (esquina superior izquierda)
     * @param startRow Fila inicial (esquina superior izquierda)
     * @param widthCells Ancho en celdas
     * @param heightCells Alto en celdas
     * @return true si la posición está libre, false si hay colisión
     */
    public boolean canPlaceShip(int startCol, int startRow, int widthCells, int heightCells) {
        // Verificar que el barco no se salga del tablero
        if (startCol < 0 || startRow < 0 ||
                startCol + widthCells > Constants.GRID_COLS ||
                startRow + heightCells > Constants.GRID_ROWS) {
            return false;
        }

        // Verificar que todas las celdas estén libres en la matriz de limpieza
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
        // Verificar que el barco no se salga del tablero
        if (startCol < 0 || startRow < 0 ||
                startCol + widthCells > Constants.GRID_COLS ||
                startRow + heightCells > Constants.GRID_ROWS) {
            return false;
        }

        // Verificar que todas las celdas estén libres en la matriz de limpieza de la CPU
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
     * @param preferredCol Columna preferida
     * @param preferredRow Fila preferida
     * @param widthCells Ancho del barco en celdas
     * @param heightCells Alto del barco en celdas
     * @return Array con [col, row] de la posición válida, o null si no hay espacio
     */
    public int[] findNearestValidPosition(int preferredCol, int preferredRow, int widthCells, int heightCells) {
        // Primero verificar si la posición preferida es válida
        if (canPlaceShip(preferredCol, preferredRow, widthCells, heightCells)) {
            return new int[]{preferredCol, preferredRow};
        }

        // Buscar en círculos concéntricos alrededor de la posición preferida
        int maxDistance = Math.max(Constants.GRID_ROWS, Constants.GRID_COLS);

        for (int distance = 1; distance <= maxDistance; distance++) {
            // Verificar todas las posiciones a la distancia actual
            for (int deltaRow = -distance; deltaRow <= distance; deltaRow++) {
                for (int deltaCol = -distance; deltaCol <= distance; deltaCol++) {
                    // Solo verificar posiciones en el borde del cuadrado actual
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

        // Si no se encuentra ninguna posición válida
        return null;
    }

    /**
     * CORREGIDO: Posiciona automáticamente todos los barcos de la CPU usando List
     */
    public void posicionarBarcosCpu() {
        System.out.println("=== POSICIONANDO BARCOS DE LA CPU ===");

        // Limpiar matriz de la CPU por si acaso
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                matrizLimpiezaCpu[i][j] = false;
            }
        }

        // Definir los barcos a crear (mismo orden que el jugador)
        int[][] shipSizes = {
                {4, 1}, // 1 Portaaviones (4x1)
                {3, 1}, // 1 Submarino (3x1)
                {3, 1}, // 1 Submarino (3x1)
                {2, 1}, // 1 Destructor (2x1)
                {2, 1}, // 1 Destructor (2x1)
                {2, 1}, // 1 Destructor (2x1)
                {1, 1}, // 1 Fragata (1x1)
                {1, 1}, // 1 Fragata (1x1)
                {1, 1}, // 1 Fragata (1x1)
                {1, 1}  // 1 Fragata (1x1)
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
            int maxAttempts = 1000; // Evitar bucle infinito

            while (!placed && attempts < maxAttempts) {
                // Generar posición aleatoria
                int startCol = random.nextInt(Constants.GRID_COLS);
                int startRow = random.nextInt(Constants.GRID_ROWS);

                // Decidir orientación aleatoria (50% horizontal, 50% vertical)
                boolean isVertical = random.nextBoolean();
                int finalWidth = isVertical ? heightCells : widthCells;
                int finalHeight = isVertical ? widthCells : heightCells;

                // Verificar si se puede colocar
                if (canPlaceShipCpu(startCol, startRow, finalWidth, finalHeight)) {
                    // Colocar el barco
                    placeShipCpu(startCol, startRow, finalWidth, finalHeight);

                    // CORREGIDO: Guardar las coordenadas en el objeto Ship usando List
                    for (int row = startRow; row < startRow + finalHeight; row++) {
                        for (int col = startCol; col < startCol + finalWidth; col++) {
                            arrayCpu[i].addCoordinate(col, row);
                        }
                    }

                    placed = true;
                    System.out.println("✓ " + shipNames[i] + " colocado en (" + startCol + "," + startRow +
                            ") - Tamaño: " + finalWidth + "x" + finalHeight +
                            " - " + (isVertical ? "Vertical" : "Horizontal"));

                    // NUEVO: Mostrar las coordenadas exactas ocupadas
                    System.out.print("  Coordenadas ocupadas: ");
                    for (int row = startRow; row < startRow + finalHeight; row++) {
                        for (int col = startCol; col < startCol + finalWidth; col++) {
                            System.out.print("(" + col + "," + row + ") ");
                        }
                    }
                    System.out.println();
                }
                attempts++;
            }

            if (!placed) {
                System.out.println("✗ ERROR: No se pudo colocar " + shipNames[i] + " después de " + maxAttempts + " intentos");
            }
        }

        System.out.println("=== BARCOS DE LA CPU POSICIONADOS ===");
        printMatrizLimpiezaCpu();
    }

    /**
     * Obtiene las coordenadas de todas las celdas que ocupa un barco
     * @param startCol Columna inicial
     * @param startRow Fila inicial
     * @param widthCells Ancho en celdas
     * @param heightCells Alto en celdas
     * @return Array de coordenadas [col, row] de todas las celdas ocupadas
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
     * CORREGIDO: Método para verificar si ya se disparó a una coordenada - INDEXACIÓN CORREGIDA
     * @param col coordenada X (columna)
     * @param row coordenada Y (fila)
     * @param jugador 0 = jugador humano, 1 = CPU
     * @return true si ya se disparó a esa coordenada
     */
    public boolean yaSeDisparo(int col, int row, int jugador) {
        if (jugador == 0) { // Jugador humano dispara a CPU
            return matrizDisparosPlayer[row][col]; // CORREGIDO: [fila][columna]
        } else { // CPU dispara a jugador
            return matrizDisparosCpu[row][col]; // CORREGIDO: [fila][columna]
        }
    }

    /**
     * CORREGIDO: Método principal para manejar jugadas usando List con verificación de fin de juego
     * @param x coordenada X (columna)
     * @param y coordenada Y (fila)
     * @param jugador 0 = jugador humano, 1 = CPU
     * @return String con el resultado: "AGUA", "TOCADO", "HUNDIDO", "YA_DISPARADO", "VICTORIA_JUGADOR", "VICTORIA_CPU"
     */
    public String jugada(int x, int y, int jugador) {
        System.out.println("=== PROCESANDO JUGADA ===");
        System.out.println("Coordenadas: (" + x + ", " + y + ") - Jugador: " + (jugador == 0 ? "Humano" : "CPU"));

        // NUEVO: Verificar si el juego ya terminó
        if (gameEnded) {
            System.out.println("El juego ya ha terminado. Ganador: " + winner);
            return "JUEGO_TERMINADO";
        }

        if (jugador == 0) { // Jugador humano dispara a CPU
            // CORREGIDO: Usar [y][x] para acceder a la matriz (fila, columna)
            if (matrizDisparosPlayer[y][x]) {
                System.out.println("Resultado: YA_DISPARADO");
                return "YA_DISPARADO";
            }

            // Marcar como disparado
            matrizDisparosPlayer[y][x] = true;

            // CORREGIDO: Verificar si hay barco en esa posición usando [y][x]
            if (matrizLimpiezaCpu[y][x]) {
                // ¡Tocado! Marcar como atinado
                matrizAtinacionCpu[y][x] = true;

                // CORREGIDO: Buscar qué barco fue tocado usando List
                for (int i = 0; i < 10; i++) {
                    if (arrayCpu[i].containsCoordinate(x, y)) {
                        // Verificar si el barco está completamente hundido
                        if (cambiarPng(arrayCpu[i], jugador)) {
                            System.out.println("Resultado: HUNDIDO - Barco " + (i+1) + " completamente destruido");

                            // NUEVO: Verificar si todos los barcos de la CPU están hundidos
                            if (todasFlotasHundidas(1)) { // 1 = verificar barcos de la CPU
                                gameEnded = true;
                                winner = "JUGADOR";
                                System.out.println("🎉 ¡VICTORIA DEL JUGADOR! Todos los barcos de la CPU han sido hundidos");
                                return "VICTORIA_JUGADOR";
                            }

                            return "HUNDIDO";
                        } else {
                            System.out.println("Resultado: TOCADO - Barco " + (i+1) + " herido");
                            return "TOCADO";
                        }
                    }
                }

                // Si llegamos aquí, hay un problema con la lógica
                System.out.println("ERROR: Barco tocado pero no encontrado en arrays");
                return "TOCADO";

            } else {
                System.out.println("Resultado: AGUA");
                return "AGUA";
            }

        } else if (jugador == 1) { // CPU dispara a jugador
            // CORREGIDO: Usar [y][x] para acceder a la matriz
            if (matrizDisparosCpu[y][x]) {
                return "YA_DISPARADO";
            }

            // Marcar como disparado
            matrizDisparosCpu[y][x] = true;

            if (matrizLimpiezaPlayer[y][x]) {
                matrizAtinacionPlayer[y][x] = true;

                // CORREGIDO: Buscar qué barco fue tocado usando List
                for (int i = 0; i < 10; i++) {
                    if (arrayPlayer[i] != null && arrayPlayer[i].containsCoordinate(x, y)) {
                        if (cambiarPng(arrayPlayer[i], jugador)) {
                            System.out.println("CPU hundió un barco del jugador");

                            // NUEVO: Verificar si todos los barcos del jugador están hundidos
                            if (todasFlotasHundidas(0)) { // 0 = verificar barcos del jugador
                                gameEnded = true;
                                winner = "CPU";
                                System.out.println("💀 ¡VICTORIA DE LA CPU! Todos los barcos del jugador han sido hundidos");
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
     * NUEVO: Verifica si todas las flotas de un jugador están completamente hundidas
     * @param jugador 0 = verificar barcos del jugador, 1 = verificar barcos de la CPU
     * @return true si todos los barcos están hundidos, false si queda algún barco intacto
     */
    public boolean todasFlotasHundidas(int jugador) {
        Ship[] barcosAVerificar;
        boolean[][] matrizAtinacion;

        if (jugador == 0) { // Verificar barcos del jugador
            barcosAVerificar = arrayPlayer;
            matrizAtinacion = matrizAtinacionPlayer;
            System.out.println("=== VERIFICANDO SI TODOS LOS BARCOS DEL JUGADOR ESTÁN HUNDIDOS ===");
        } else { // Verificar barcos de la CPU
            barcosAVerificar = arrayCpu;
            matrizAtinacion = matrizAtinacionCpu;
            System.out.println("=== VERIFICANDO SI TODOS LOS BARCOS DE LA CPU ESTÁN HUNDIDOS ===");
        }

        int barcosHundidos = 0;
        int barcosIntactos = 0;

        for (int i = 0; i < 10; i++) {
            if (barcosAVerificar[i] != null && barcosAVerificar[i].getSize() > 0) {
                boolean barcoCompletamenteHundido = true;

                // Verificar cada coordenada del barco
                for (int[] coordinate : barcosAVerificar[i].getCoordinates()) {
                    int col = coordinate[0];
                    int row = coordinate[1];

                    // Si alguna coordenada del barco no ha sido tocada, el barco no está hundido
                    if (!matrizAtinacion[row][col]) {
                        barcoCompletamenteHundido = false;
                        break;
                    }
                }

                if (barcoCompletamenteHundido) {
                    barcosHundidos++;
                    System.out.println("✗ Barco " + (i+1) + " - HUNDIDO");
                } else {
                    barcosIntactos++;
                    System.out.println("✓ Barco " + (i+1) + " - AÚN FLOTANDO");
                }
            }
        }

        System.out.println("RESUMEN: " + barcosHundidos + " barcos hundidos, " + barcosIntactos + " barcos flotando");

        if (barcosIntactos == 0 && barcosHundidos > 0) {
            System.out.println("🚨 ¡TODAS LAS FLOTAS HUNDIDAS! - " + (jugador == 0 ? "JUGADOR DERROTADO" : "CPU DERROTADA"));
            return true;
        } else {
            System.out.println("⚓ Aún quedan barcos flotando - El juego continúa");
            return false;
        }
    }

    /**
     * CORREGIDO: Verifica si un barco está completamente hundido usando List
     * @param barco El barco a verificar
     * @param jugador 0 = verificar barcos de CPU, 1 = verificar barcos de jugador
     * @return true si está completamente hundido, false si aún tiene partes intactas
     */
    public boolean cambiarPng(Ship barco, int jugador) {
        boolean allDestroyed = true;

        // CORREGIDO: Usar List en lugar de Map
        for (int[] coordinate : barco.getCoordinates()) {
            int col = coordinate[0];
            int row = coordinate[1];

            // CORREGIDO: Usar [row][col] = [fila][columna] para acceder a la matriz
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
     * CORREGIDO: Inicializa los barcos del jugador usando List
     */
    public void inicializarBarcosJugador(java.util.List<DraggableShape> playerShips) {
        System.out.println("=== INICIALIZANDO BARCOS DEL JUGADOR ===");

        for (int i = 0; i < playerShips.size() && i < 10; i++) {
            arrayPlayer[i] = new Ship();
            DraggableShape ship = playerShips.get(i);

            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                // CORREGIDO: Guardar las coordenadas usando addCoordinate
                for (int row = position[1]; row < position[1] + dimensions[1]; row++) {
                    for (int col = position[0]; col < position[0] + dimensions[0]; col++) {
                        arrayPlayer[i].addCoordinate(col, row);
                    }
                }

                System.out.println("✓ Barco " + (i+1) + " inicializado en (" + position[0] + "," + position[1] +
                        ") - Tamaño: " + dimensions[0] + "x" + dimensions[1]);
            }
        }

        System.out.println("=== BARCOS DEL JUGADOR INICIALIZADOS ===");
    }

    /**
     * NUEVO: Verifica si quedan posiciones válidas para disparar (para evitar bucles infinitos)
     * @param jugador 0 = jugador humano verifica disparos a CPU, 1 = CPU verifica disparos a jugador
     * @return true si quedan posiciones por disparar
     */
    public boolean quedanPosicionesParaDisparar(int jugador) {
        boolean[][] matrizDisparos;

        if (jugador == 0) { // Jugador humano verifica disparos a CPU
            matrizDisparos = matrizDisparosPlayer;
        } else { // CPU verifica disparos a jugador
            matrizDisparos = matrizDisparosCpu;
        }

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (!matrizDisparos[row][col]) {
                    return true; // Encontramos al menos una posición no disparada
                }
            }
        }
        return false; // Todas las posiciones han sido disparadas
    }

    /**
     * NUEVO: Encuentra una coordenada aleatoria válida para disparar (que no haya sido atacada antes)
     * @param jugador 0 = jugador humano, 1 = CPU
     * @return array [col, row] con coordenadas válidas, o null si no quedan posiciones
     */
    public int[] encontrarCoordenadaValidaParaDisparar(int jugador) {
        if (!quedanPosicionesParaDisparar(jugador)) {
            return null; // No quedan posiciones válidas
        }

        int maxIntentos = 200; // Evitar bucle infinito
        int intentos = 0;

        while (intentos < maxIntentos) {
            int col = random.nextInt(Constants.GRID_COLS);
            int row = random.nextInt(Constants.GRID_ROWS);

            if (!yaSeDisparo(col, row, jugador)) {
                return new int[]{col, row};
            }
            intentos++;
        }

        // Si llegamos aquí, buscar sistemáticamente
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (!yaSeDisparo(col, row, jugador)) {
                    return new int[]{col, row};
                }
            }
        }

        return null; // No se encontró ninguna posición válida
    }

    /**
     * NUEVO: Reinicia el estado del juego para poder jugar una nueva partida
     */
    public void reiniciarJuego() {
        // Limpiar matrices
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

        // Limpiar arrays de barcos
        for (int i = 0; i < 10; i++) {
            arrayPlayer[i] = null;
            arrayCpu[i] = null;
        }

        // Reiniciar estado del juego
        gameEnded = false;
        winner = "";

        System.out.println("=== JUEGO REINICIADO ===");
        System.out.println("✓ Todas las matrices limpiadas");
        System.out.println("✓ Arrays de barcos reiniciados");
        System.out.println("✓ Estado del juego reiniciado");
    }

    /**
     * Imprime el estado actual de la matriz de limpieza (para debugging)
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
        System.out.println("^");
        System.out.println("FILAS");
        System.out.println();
    }

    /**
     * CORREGIDO: Imprime el estado de la matriz de limpieza de la CPU con índices claros
     */
    public void printMatrizLimpiezaCpu() {
        System.out.println("Estado actual de la matriz de limpieza CPU:");
        System.out.println("   0 1 2 3 4 5 6 7 8 9  <- COLUMNAS");
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            System.out.print(row + ": ");
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                System.out.print(matrizLimpiezaCpu[row][col] ? "X " : "- ");
            }
            System.out.println();
        }
        System.out.println("^");
        System.out.println("FILAS");
        System.out.println();
        System.out.println("NOTA: En el tablero visual, hacer clic en la celda (columna, fila)");
        System.out.println("      corresponde a la posición matriz[fila][columna]");
        System.out.println();
    }

    // NUEVOS: Getters para el estado del juego
    public boolean isGameEnded() {
        return gameEnded;
    }

    public String getWinner() {
        return winner;
    }

    // Getters para acceder a las matrices desde otras clases
    public boolean[][] getMatrizLimpiezaPlayer() {
        return matrizLimpiezaPlayer;
    }

    public boolean[][] getMatrizLimpiezaCpu() {
        return matrizLimpiezaCpu;
    }

    public boolean[][] getMatrizDisparosPlayer() {
        return matrizDisparosPlayer;
    }

    public boolean[][] getMatrizAtinacionCpu() {
        return matrizAtinacionCpu;
    }

    public Ship[] getArrayCpu() {
        return arrayCpu;
    }

    public Ship[] getArrayPlayer() {
        return arrayPlayer;
    }
}