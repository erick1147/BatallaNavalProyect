package com.example.batallanaval.controlador;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.application.Platform;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import com.example.batallanaval.modelo.Constants;
import com.example.batallanaval.modelo.GameLogic;
import com.example.batallanaval.modelo.Ship;
import com.example.batallanaval.vista.GameBoardView;
import com.example.batallanaval.vista.DraggableShape;
import com.example.batallanaval.vista.PlayerShipManager;
import com.example.batallanaval.modelo.GameState;
import com.example.batallanaval.vista.MainMenuView;
import com.example.batallanaval.interfaces.MenuListener;
import com.example.batallanaval.adapters.GameStateAdapter;
import com.example.batallanaval.exceptions.GameSaveException;
import com.example.batallanaval.exceptions.GameLoadException;
import com.example.batallanaval.modelo.GameSaveManager;

public class GameController extends GameStateAdapter implements MenuListener {

    private GameLogic gameLogic;
    private List<DraggableShape> playerShips;
    private Button startGameButton;
    private Button showCpuShipsButton;
    private Button newGameButton;
    private boolean gameStarted = false;
    private boolean firstPlayerMove = true;
    private boolean cpuShipsVisible = false;
    private Label statusLabel;
    private Label gameStatusLabel;
    private Label turnIndicatorLabel;
    private Label victoryLabel;
    private GridPane playerGridPane;
    private GridPane cpuGridPane;
    private boolean isPlayerTurn = true;
    private Random cpuRandom = new Random();

    // SIMPLIFICADO: Solo usar GameSaveManager directamente
    private GameSaveManager saveManager;
    private MainMenuView mainMenu;
    private Stage primaryStage;
    private String playerNickname = "Capitán";

    public GameController() {
        this.saveManager = new GameSaveManager();
        saveManager.addListener(this);
        System.out.println("✓ GameController inicializado con sistema de guardado");
    }

    public void initializeApplication(Stage primaryStage) {
        this.primaryStage = primaryStage;
        mainMenu = new MainMenuView(primaryStage);
        mainMenu.setMenuListener(this);
        mainMenu.showMenu();
        System.out.println("=== BATALLA NAVAL CON GUARDADO AUTOMÁTICO ===");
    }

    @Override
    public void onNewGameRequested() {
        try {
            // Eliminar partida guardada anterior
            if (saveManager.hasSavedGame()) {
                saveManager.deleteSavedGame();
                System.out.println("✓ Partida anterior eliminada");
            }
        } catch (Exception e) {
            System.err.println("Error eliminando partida: " + e.getMessage());
        }
        initializeGame(primaryStage);
    }

    @Override
    public void onLoadGameRequested() {
        if (saveManager.hasSavedGame()) {
            try {
                // CORREGIDO: Cargar primero, después inicializar
                System.out.println("📁 Intentando cargar partida guardada...");
                
                GameState loadedState = saveManager.loadGame();
                if (loadedState != null && isValidGameState(loadedState)) {
                    System.out.println("✓ Estado cargado exitosamente");
                    
                    // Ahora sí inicializar el juego con el estado cargado
                    initializeGameWithLoadedState(primaryStage, loadedState);
                } else {
                    System.err.println("✗ Estado del juego inválido");
                    mainMenu.showLoadError("El archivo de guardado está corrupto");
                    mainMenu.showMenu();
                }
            } catch (GameLoadException e) {
                System.err.println("✗ Error cargando partida: " + e.getMessage());
                mainMenu.showLoadError("No se pudo cargar la partida: " + e.getMessage());
                mainMenu.showMenu();
            }
        } else {
            System.err.println("✗ No hay partida guardada");
            mainMenu.showLoadError("No hay partida guardada");
            mainMenu.showMenu();
        }
    }

    @Override
    public void onMenuClosed() {
        System.out.println("Menú cerrado");
    }

    /**
     * NUEVO: Método para validar el estado cargado
     */
    private boolean isValidGameState(GameState state) {
        if (state == null) {
            System.err.println("Estado nulo");
            return false;
        }
        
        // Verificar que tenga datos básicos
        if (state.getPlayerNickname() == null) {
            state.setPlayerNickname("Capitán");
        }
        
        System.out.println("Estado válido - Jugador: " + state.getPlayerNickname() + 
                          ", Juego iniciado: " + state.isGameStarted());
        return true;
    }

    /**
     * NUEVO: Inicializa el juego con un estado cargado
     */
    private void initializeGameWithLoadedState(Stage primaryStage, GameState loadedState) {
        // Primero inicializar la UI normal
        initializeGame(primaryStage);
        
        // Después restaurar el estado cargado
        restoreGameFromState(loadedState);
        
        System.out.println("✓ Juego inicializado con estado cargado");
    }

    /**
     * CORREGIDO: Captura el estado actual con validaciones mejoradas
     */
    private GameState captureCurrentGameState() {
        if (gameLogic == null) {
            System.err.println("❌ GameLogic es null en captureCurrentGameState");
            return null;
        }
        
        try {
            GameState state = new GameState();
            state.setPlayerNickname(playerNickname);
            state.setGameStarted(gameStarted);
            state.setGameEnded(gameLogic.isGameEnded());
            state.setPlayerTurn(isPlayerTurn);
            state.setFirstPlayerMove(firstPlayerMove);
            state.setWinner(gameLogic.getWinner());
            
            // Copiar matrices con validaciones
            state.setMatrizLimpiezaPlayer(copyMatrix(gameLogic.getMatrizLimpiezaPlayer()));
            state.setMatrizLimpiezaCpu(copyMatrix(gameLogic.getMatrizLimpiezaCpu()));
            state.setMatrizDisparosPlayer(copyMatrix(gameLogic.getMatrizDisparosPlayer()));
            state.setMatrizAtinacionCpu(copyMatrix(gameLogic.getMatrizAtinacionCpu()));
            state.setMatrizDisparosCpu(copyMatrix(gameLogic.getMatrizDisparosCpu()));
            state.setMatrizAtinacionPlayer(copyMatrix(gameLogic.getMatrizAtinacionPlayer()));
            
            // CORREGIDO: Guardar barcos del JUGADOR con validaciones
            state.getPlayerShips().clear();
            
            if (playerShips != null && !playerShips.isEmpty()) {
                for (int i = 0; i < playerShips.size(); i++) {
                    DraggableShape ship = playerShips.get(i);
                    if (ship == null) continue;
                    
                    int[] position = ship.getCurrentGridPosition();
                    int[] dimensions = ship.getCurrentDimensionsInCells();
                    boolean isVertical = ship.isVertical();
                    
                    // Solo guardar barcos que estén colocados en el tablero
                    if (position[0] >= 0 && position[1] >= 0) {
                        GameState.ShipState shipState = new GameState.ShipState();
                        shipState.setGridCol(position[0]);
                        shipState.setGridRow(position[1]);
                        shipState.setWidth(dimensions[0] * Constants.CELL_SIZE);
                        shipState.setHeight(dimensions[1] * Constants.CELL_SIZE);
                        shipState.setVertical(isVertical);
                        
                        // Generar coordenadas del barco y determinar su estado
                        List<int[]> coordinates = new ArrayList<>();
                        boolean shipDestroyed = true;
                        int hitCells = 0;
                        
                        boolean[][] atinacionMatrix = gameLogic.getMatrizAtinacionPlayer();
                        
                        for (int row = position[1]; row < position[1] + dimensions[1]; row++) {
                            for (int col = position[0]; col < position[0] + dimensions[0]; col++) {
                                coordinates.add(new int[]{col, row});
                                
                                // Verificar si esta celda fue golpeada
                                if (atinacionMatrix != null && atinacionMatrix[row][col]) {
                                    hitCells++;
                                } else {
                                    shipDestroyed = false;
                                }
                            }
                        }
                        
                        shipState.setCoordinates(coordinates);
                        
                        // Determinar estado: 0=intacto, 1=dañado, 2=hundido
                        if (hitCells == 0) {
                            shipState.setState(0); // Intacto
                        } else if (shipDestroyed) {
                            shipState.setState(2); // Hundido
                        } else {
                            shipState.setState(1); // Dañado
                        }
                        
                        state.getPlayerShips().add(shipState);
                    }
                }
            }
            
            // Guardar barcos de la CPU con estado correcto
            Ship[] arrayCpu = gameLogic.getArrayCpu();
            state.getCpuShips().clear();
            
            if (arrayCpu != null) {
                for (int i = 0; i < arrayCpu.length; i++) {
                    if (arrayCpu[i] != null && arrayCpu[i].getSize() > 0) {
                        GameState.ShipState shipState = new GameState.ShipState();
                        shipState.setCoordinates(new ArrayList<>(arrayCpu[i].getCoordinates()));
                        
                        // Determinar estado del barco de la CPU
                        List<int[]> coords = arrayCpu[i].getCoordinates();
                        boolean shipDestroyed = true;
                        int hitCells = 0;
                        
                        boolean[][] atinacionCpu = gameLogic.getMatrizAtinacionCpu();
                        
                        if (atinacionCpu != null) {
                            for (int[] coord : coords) {
                                if (coord[1] < atinacionCpu.length && coord[0] < atinacionCpu[0].length) {
                                    if (atinacionCpu[coord[1]][coord[0]]) {
                                        hitCells++;
                                    } else {
                                        shipDestroyed = false;
                                    }
                                }
                            }
                        } else {
                            shipDestroyed = false;
                            hitCells = 0;
                        }
                        
                        if (hitCells == 0) {
                            shipState.setState(0); // Intacto
                        } else if (shipDestroyed) {
                            shipState.setState(2); // Hundido
                        } else {
                            shipState.setState(1); // Dañado
                        }
                        
                        state.getCpuShips().add(shipState);
                    }
                }
            }
            
            System.out.println("📊 Estado capturado - Barcos Jugador: " + state.getPlayerShips().size() + 
                              ", Barcos CPU: " + state.getCpuShips().size());
            return state;
            
        } catch (Exception e) {
            System.err.println("❌ Error capturando estado del juego: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * CORREGIDO: Restaura el estado del juego desde un GameState
     */
    private void restoreGameFromState(GameState state) {
        if (state == null) {
            System.err.println("✗ Estado nulo, no se puede restaurar");
            return;
        }
        
        System.out.println("🔄 Restaurando estado del juego...");
        
        // Restaurar variables de estado
        gameStarted = state.isGameStarted();
        isPlayerTurn = state.isPlayerTurn();
        firstPlayerMove = state.isFirstPlayerMove();
        playerNickname = state.getPlayerNickname() != null ? state.getPlayerNickname() : "Capitán";
        
        // CORREGIDO: Limpiar matrices primero para evitar inconsistencias
        gameLogic.reiniciarJuego();
        
        // Restaurar matrices en GameLogic
        if (state.getMatrizLimpiezaPlayer() != null) {
            gameLogic.setMatrizLimpiezaPlayer(copyMatrix(state.getMatrizLimpiezaPlayer()));
        }
        if (state.getMatrizLimpiezaCpu() != null) {
            gameLogic.setMatrizLimpiezaCpu(copyMatrix(state.getMatrizLimpiezaCpu()));
        }
        if (state.getMatrizDisparosPlayer() != null) {
            gameLogic.setMatrizDisparosPlayer(copyMatrix(state.getMatrizDisparosPlayer()));
        }
        if (state.getMatrizAtinacionCpu() != null) {
            gameLogic.setMatrizAtinacionCpu(copyMatrix(state.getMatrizAtinacionCpu()));
        }
        if (state.getMatrizDisparosCpu() != null) {
            gameLogic.setMatrizDisparosCpu(copyMatrix(state.getMatrizDisparosCpu()));
        }
        if (state.getMatrizAtinacionPlayer() != null) {
            gameLogic.setMatrizAtinacionPlayer(copyMatrix(state.getMatrizAtinacionPlayer()));
        }
        
        // CORREGIDO: Restaurar posiciones EXACTAS de los barcos del JUGADOR
        if (state.getPlayerShips() != null && !state.getPlayerShips().isEmpty() && playerShips != null) {
            System.out.println("🚢 Restaurando " + state.getPlayerShips().size() + " barcos del jugador");
            
            // NUEVO: Limpiar posiciones actuales primero
            for (DraggableShape ship : playerShips) {
                int[] currentPos = ship.getCurrentGridPosition();
                if (currentPos[0] >= 0 && currentPos[1] >= 0) {
                    int[] dimensions = ship.getCurrentDimensionsInCells();
                    gameLogic.removeShip(currentPos[0], currentPos[1], dimensions[0], dimensions[1]);
                }
            }
            
            int restoredShips = 0;
            for (int i = 0; i < state.getPlayerShips().size() && i < playerShips.size(); i++) {
                GameState.ShipState shipState = state.getPlayerShips().get(i);
                DraggableShape ship = playerShips.get(i);
                
                // Restaurar posición EXACTA en el tablero
                double posX = Constants.BOARD_START_X + (shipState.getGridCol() * Constants.CELL_SIZE);
                double posY = Constants.BOARD_START_Y + (shipState.getGridRow() * Constants.CELL_SIZE);
                
                // CORREGIDO: Usar el método correcto para posicionar
                ship.getNode().setTranslateX(posX);
                ship.getNode().setTranslateY(posY);
                
                // FORZAR actualización de posición interna del barco
                try {
                    java.lang.reflect.Field colField = ship.getClass().getDeclaredField("currentGridCol");
                    java.lang.reflect.Field rowField = ship.getClass().getDeclaredField("currentGridRow");
                    colField.setAccessible(true);
                    rowField.setAccessible(true);
                    colField.setInt(ship, shipState.getGridCol());
                    rowField.setInt(ship, shipState.getGridRow());
                } catch (Exception e) {
                    System.err.println("⚠️ No se pudo actualizar posición interna del barco: " + e.getMessage());
                }
                
                // Marcar las celdas como ocupadas en GameLogic
                int[] dimensions = ship.getCurrentDimensionsInCells();
                gameLogic.placeShip(shipState.getGridCol(), shipState.getGridRow(), 
                                   dimensions[0], dimensions[1]);
                
                // Restaurar estado visual de impactos en el barco
                restoreShipVisualState(ship, shipState, state.getMatrizAtinacionPlayer());
                
                System.out.println("✓ Barco " + (i+1) + " restaurado en posición (" + 
                                  shipState.getGridCol() + "," + shipState.getGridRow() + 
                                  ") - Estado: " + shipState.getState());
                restoredShips++;
            }
            
            System.out.println("✅ " + restoredShips + " barcos del jugador restaurados");
        } else {
            System.out.println("⚠️ No hay barcos del jugador guardados");
        }
        
        // Restaurar barcos de la CPU
        if (state.getCpuShips() != null && !state.getCpuShips().isEmpty()) {
            Ship[] arrayCpu = gameLogic.getArrayCpu();
            System.out.println("🚢 Restaurando " + state.getCpuShips().size() + " barcos de la CPU");
            
            for (int i = 0; i < state.getCpuShips().size() && i < arrayCpu.length; i++) {
                GameState.ShipState shipState = state.getCpuShips().get(i);
                if (arrayCpu[i] == null) {
                    arrayCpu[i] = new Ship();
                }
                arrayCpu[i].clearCoordinates();
                for (int[] coord : shipState.getCoordinates()) {
                    arrayCpu[i].addCoordinate(coord[0], coord[1]);
                }
                arrayCpu[i].setState(shipState.getState());
            }
        } else {
            System.out.println("⚠️ No hay barcos de CPU guardados, generando nuevos...");
            gameLogic.posicionarBarcosCpu();
        }
        
        // Actualizar UI si el juego ya estaba iniciado
        if (gameStarted) {
            updateUIForLoadedGame();
            restaurarVisualTablero();
        }
        
        System.out.println("✅ Estado restaurado exitosamente");
    }
    
    /**
     * NUEVO: Restaura el estado visual de un barco (impactos, hundimiento)
     */
    private void restoreShipVisualState(DraggableShape ship, GameState.ShipState shipState, boolean[][] atinacionMatrix) {
        if (shipState.getState() == 2) {
            // Barco hundido - marcar todas las celdas como destruidas
            ship.markShipAsDestroyed();
            System.out.println("🔴 Barco marcado como hundido");
        } else if (shipState.getState() == 1) {
            // Barco dañado - marcar celdas específicas como tocadas
            for (int[] coord : shipState.getCoordinates()) {
                int col = coord[0];
                int row = coord[1];
                
                if (atinacionMatrix != null && atinacionMatrix[row][col]) {
                    ship.markCellImpact(col, row, "TOCADO");
                    System.out.println("🟡 Celda (" + col + "," + row + ") marcada como tocada");
                }
            }
        }
        // Estado 0 (intacto) no requiere marcas visuales
    }

    /**
     * NUEVO: Actualiza la UI cuando se carga una partida
     */
    private void updateUIForLoadedGame() {
        startGameButton.setText("PARTIDA CARGADA");
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        
        statusLabel.setText("Partida cargada - Jugador: " + playerNickname);
        statusLabel.setTextFill(Color.DARKGREEN);
        
        // Deshabilitar arrastre de barcos
        for (DraggableShape ship : playerShips) {
            ship.disableDragging();
        }
        
        showCpuShipsButton.setVisible(true);
        
        // CORREGIDO: Manejar el turno correctamente al cargar
        if (isPlayerTurn) {
            updateGameStatus("Es tu turno", Color.DARKGREEN);
            updateTurnIndicator("Tu turno", Color.DARKGREEN);
        } else {
            // Si no es turno del jugador, es turno de la CPU
            updateGameStatus("Turno de la CPU", Color.DARKRED);
            updateTurnIndicator("Turno de la CPU", Color.DARKRED);
            
            // NUEVO: Reanudar turno de la CPU después de cargar
            Platform.runLater(() -> {
                System.out.println("🤖 Reanudando turno de la CPU tras cargar partida...");
                // Pequeño delay para que la UI se estabilice
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 2 segundos para que el jugador vea que se cargó
                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
            });
        }
        
        System.out.println("🎮 UI actualizada para partida cargada - Turno: " + (isPlayerTurn ? "Jugador" : "CPU"));
    }

    /**
     * CORREGIDO: Restaura el estado visual del tablero
     */
    private void restaurarVisualTablero() {
        System.out.println("🎨 Restaurando estado visual del tablero...");
        
        boolean[][] disparosPlayer = gameLogic.getMatrizDisparosPlayer();
        boolean[][] atinacionCpu = gameLogic.getMatrizAtinacionCpu();
        boolean[][] disparosCpu = gameLogic.getMatrizDisparosCpu();
        boolean[][] atinacionPlayer = gameLogic.getMatrizAtinacionPlayer();
        
        // Restaurar disparos del jugador en el tablero de la CPU
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (disparosPlayer[row][col]) {
                    // Verificar si es barco hundido para mostrar rojo
                    boolean isShipSunk = isShipSunkAtPosition(col, row, true); // true = CPU
                    
                    if (isShipSunk) {
                        updateCpuBoardCell(col, row, "HUNDIDO");
                    } else if (atinacionCpu[row][col]) {
                        updateCpuBoardCell(col, row, "TOCADO");
                    } else {
                        updateCpuBoardCell(col, row, "AGUA");
                    }
                }
            }
        }
        
        // Restaurar disparos de la CPU en el tablero del jugador
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (disparosCpu[row][col]) {
                    updatePlayerBoardCell(col, row);
                }
            }
        }
        
        System.out.println("✅ Estado visual restaurado");
    }
    
    /**
     * NUEVO: Verifica si un barco está hundido en una posición específica
     */
    private boolean isShipSunkAtPosition(int col, int row, boolean isCpuBoard) {
        Ship[] ships = isCpuBoard ? gameLogic.getArrayCpu() : gameLogic.getArrayPlayer();
        boolean[][] atinacionMatrix = isCpuBoard ? gameLogic.getMatrizAtinacionCpu() : gameLogic.getMatrizAtinacionPlayer();
        
        // Buscar el barco que contiene esta coordenada
        for (Ship ship : ships) {
            if (ship != null && ship.containsCoordinate(col, row)) {
                // Verificar si todas las celdas del barco están golpeadas
                for (int[] coord : ship.getCoordinates()) {
                    if (!atinacionMatrix[coord[1]][coord[0]]) {
                        return false; // Aún hay celdas sin golpear
                    }
                }
                return true; // Todas las celdas están golpeadas = barco hundido
            }
        }
        return false; // No se encontró barco en esa posición
    }

    /**
     * CORREGIDO: Actualiza una celda del tablero de la CPU
     */
    private void updateCpuBoardCell(int col, int row, String result) {
        for (javafx.scene.Node node : cpuGridPane.getChildren()) {
            if (node instanceof Pane) {
                Integer nodeCol = GridPane.getColumnIndex(node);
                Integer nodeRow = GridPane.getRowIndex(node);
                
                if (nodeCol != null && nodeRow != null && nodeCol.equals(col) && nodeRow.equals(row)) {
                    switch (result) {
                        case "HUNDIDO":
                            node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: red;");
                            break;
                        case "TOCADO":
                            node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                            break;
                        case "AGUA":
                        default:
                            node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                            break;
                    }
                    break;
                }
            }
        }
    }
    
    /**
     * SOBRECARGA: Método de compatibilidad con el código anterior
     */
    private void updateCpuBoardCell(int col, int row, boolean hit) {
        updateCpuBoardCell(col, row, hit ? "TOCADO" : "AGUA");
    }

    /**
     * NUEVO: Actualiza una celda del tablero del jugador
     */
    private void updatePlayerBoardCell(int col, int row) {
        for (javafx.scene.Node node : playerGridPane.getChildren()) {
            if (node instanceof Pane) {
                Integer nodeCol = GridPane.getColumnIndex(node);
                Integer nodeRow = GridPane.getRowIndex(node);
                
                if (nodeCol != null && nodeRow != null && nodeCol.equals(col) && nodeRow.equals(row)) {
                    node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                    break;
                }
            }
        }
    }

    /**
     * Copia una matriz booleana
     */
    private boolean[][] copyMatrix(boolean[][] original) {
        if (original == null) return null;
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    /**
     * CORREGIDO: Auto-guardado con validación mejorada
     */
    private void autoSave() {
        if (!gameStarted) {
            System.out.println("⚠️ Juego no iniciado, omitiendo auto-guardado");
            return;
        }
        
        // NUEVO: Validar que tenemos datos válidos antes de guardar
        if (gameLogic == null) {
            System.err.println("❌ GameLogic es null, no se puede auto-guardar");
            return;
        }
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Reducir delay para guardado más rápido
                
                // NUEVO: Verificar nuevamente que el juego sigue activo
                if (!gameStarted) {
                    System.out.println("⚠️ Juego terminó durante delay, cancelando auto-guardado");
                    return;
                }
                
                GameState state = captureCurrentGameState();
                
                // NUEVO: Validar que el estado capturado es válido
                if (state == null) {
                    System.err.println("❌ Estado capturado es null, cancelando auto-guardado");
                    return;
                }
                
                // NUEVO: Validar que tenemos datos mínimos
                if (state.getPlayerShips() == null || state.getCpuShips() == null) {
                    System.err.println("❌ Datos de barcos incompletos, cancelando auto-guardado");
                    return;
                }
                
                saveManager.saveGame(state);
                System.out.println("💾 Auto-guardado completado exitosamente");
                
            } catch (InterruptedException e) {
                System.out.println("⚠️ Auto-guardado interrumpido");
                Thread.currentThread().interrupt();
            } catch (GameSaveException e) {
                System.err.println("❌ Error en auto-guardado: " + e.getMessage());
                // NUEVO: No mostrar error en UI para auto-guardado fallido
                // Platform.runLater(() -> updateGameStatus("Error guardando", Color.ORANGE));
            } catch (Exception e) {
                System.err.println("❌ Error crítico en auto-guardado: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // ===== MÉTODOS ORIGINALES - SIN CAMBIOS =====
    
    public void initializeGame(Stage primaryStage) {
        gameLogic = new GameLogic();

        GameBoardView playerBoardView = new GameBoardView();
        playerGridPane = playerBoardView.getGridPane();

        GameBoardView cpuBoardView = new GameBoardView();
        cpuGridPane = cpuBoardView.getGridPane();

        Label playerLabel = new Label("Tu Tablero");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerLabel.setTextFill(Color.DARKBLUE);

        Label cpuLabel = new Label("Tablero Enemigo");
        cpuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        cpuLabel.setTextFill(Color.DARKRED);

        playerLabel.setTranslateX(Constants.BOARD_START_X);
        playerLabel.setTranslateY(Constants.BOARD_START_Y - 20);

        playerGridPane.setTranslateX(Constants.BOARD_START_X);
        playerGridPane.setTranslateY(Constants.BOARD_START_Y);

        double cpuBoardX = Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + Constants.BOARD_SPACING;
        cpuLabel.setTranslateX(cpuBoardX);
        cpuLabel.setTranslateY(Constants.BOARD_START_Y - 20);

        cpuGridPane.setTranslateX(cpuBoardX);
        cpuGridPane.setTranslateY(Constants.BOARD_START_Y);

        Pane root = new Pane();
        root.getChildren().addAll(playerLabel, playerGridPane, cpuLabel, cpuGridPane);

        PlayerShipManager shipManager = new PlayerShipManager();
        playerShips = shipManager.createAndPositionShips(gameLogic, this::checkAllShipsPlaced);

        Label instructionsLabel = new Label("Arrastra los barcos solo al tablero AZUL. Click derecho para rotar.");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        instructionsLabel.setTextFill(Color.DARKGREEN);
        instructionsLabel.setTranslateX(Constants.BOARD_START_X);
        instructionsLabel.setTranslateY(Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 30);
        root.getChildren().add(instructionsLabel);

        statusLabel = new Label("Coloca todos los barcos en el tablero para poder iniciar el juego");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.DARKORANGE);
        statusLabel.setTranslateX(Constants.BOARD_START_X);
        statusLabel.setTranslateY(Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 55);
        root.getChildren().add(statusLabel);

        gameStatusLabel = new Label("");
        gameStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameStatusLabel.setTextFill(Color.DARKBLUE);
        gameStatusLabel.setTranslateX(Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + Constants.BOARD_SPACING);
        gameStatusLabel.setTranslateY(Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 30);
        gameStatusLabel.setVisible(false);
        root.getChildren().add(gameStatusLabel);

        turnIndicatorLabel = new Label("");
        turnIndicatorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        turnIndicatorLabel.setTextFill(Color.DARKBLUE);
        turnIndicatorLabel.setTranslateX(Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + Constants.BOARD_SPACING);
        turnIndicatorLabel.setTranslateY(Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 60);
        turnIndicatorLabel.setVisible(false);
        root.getChildren().add(turnIndicatorLabel);

        victoryLabel = new Label("");
        victoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        victoryLabel.setTextFill(Color.GOLD);
        victoryLabel.setTranslateX(Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE / 2) - 100);
        victoryLabel.setTranslateY(Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE / 2) - 12);
        victoryLabel.setVisible(false);
        victoryLabel.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20; -fx-border-color: gold; -fx-border-width: 3;");
        root.getChildren().add(victoryLabel);

        createStartGameButton();
        createShowCpuShipsButton();
        createNewGameButton();
        root.getChildren().add(startGameButton);
        root.getChildren().add(showCpuShipsButton);
        root.getChildren().add(newGameButton);

        for (DraggableShape ship : playerShips) {
            root.getChildren().add(ship.getNode());
        }

        setupEventHandlers();

        double sceneWidth = Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE * 2) + Constants.BOARD_SPACING + 20;
        double sceneHeight = Constants.BOARD_START_Y + Constants.GRID_ROWS * Constants.CELL_SIZE + 300;

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        primaryStage.setTitle("Batalla Naval - Guardado Automático");
        primaryStage.setScene(scene);
        primaryStage.show();

        checkAllShipsPlaced();
        gameLogic.printMatrizLimpieza();
    }

    private void setupEventHandlers() {
        cpuGridPane.setOnMouseClicked(event -> {
            if (!gameStarted) {
                updateGameStatus("¡Debes iniciar el juego primero!", Color.DARKORANGE);
                return;
            }

            if (gameLogic.isGameEnded()) {
                updateGameStatus("¡El juego ya ha terminado!", Color.GRAY);
                return;
            }

            if (!isPlayerTurn) {
                updateGameStatus("¡No es tu turno!", Color.DARKORANGE);
                return;
            }

            javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();

            if (clickedNode instanceof Pane) {
                Pane clickedPane = (Pane) clickedNode;
                Integer col = GridPane.getColumnIndex(clickedPane);
                Integer row = GridPane.getRowIndex(clickedPane);

                if (col != null && row != null) {
                    if (firstPlayerMove) {
                        firstPlayerMove = false;
                        showCpuShipsButton.setVisible(false);
                        if (cpuShipsVisible) {
                            hideCpuShips();
                        }
                    }
                    realizarDisparoJugador(col, row, clickedPane);
                }
            }
        });
    }

    private void realizarDisparoJugador(int col, int row, Pane clickedPane) {
        String resultado = gameLogic.jugada(col, row, 0);
        
        switch (resultado) {
            case "AGUA":
                clickedPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                updateGameStatus("¡Agua! Turno de la CPU", Color.BLUE);
                updateTurnIndicator("Turno de la CPU", Color.DARKRED);
                
                // CORREGIDO: Cambiar turno inmediatamente tras fallo del jugador
                isPlayerTurn = false;
                autoSave(); // Guardar inmediatamente el cambio de turno
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;

            case "TOCADO":
                clickedPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                updateGameStatus("¡Tocado! Puedes disparar de nuevo", Color.DARKORANGE);
                updateTurnIndicator("Tu turno - ¡Sigue!", Color.DARKGREEN);
                autoSave(); // Guardar el progreso
                break;

            case "HUNDIDO":
                marcarBarcoHundidoCpu(col, row);
                updateGameStatus("¡Barco hundido! Sigue disparando", Color.DARKRED);
                updateTurnIndicator("Tu turno - ¡Hundido!", Color.DARKGREEN);
                autoSave(); // Guardar el progreso
                break;

            case "VICTORIA_JUGADOR":
                marcarBarcoHundidoCpu(col, row);
                autoSave(); // Guardar la victoria
                manejarFinDelJuego("VICTORIA_JUGADOR");
                break;

            case "YA_DISPARADO":
                updateGameStatus("Ya disparaste ahí", Color.GRAY);
                break;

            default:
                updateGameStatus("Error en el disparo", Color.RED);
                break;
        }
    }

    private void turnoDelaCpu() {
        updateGameStatus("La CPU está pensando...", Color.DARKRED);
        updateTurnIndicator("Turno de la CPU", Color.DARKRED);

        new Thread(() -> {
            try {
                Thread.sleep(1500);

                boolean disparoExitoso = false;
                int intentos = 0;

                while (!disparoExitoso && intentos < 100) {
                    int col = cpuRandom.nextInt(Constants.GRID_COLS);
                    int row = cpuRandom.nextInt(Constants.GRID_ROWS);

                    if (!gameLogic.yaSeDisparo(col, row, 1)) {
                        String resultado = gameLogic.jugada(col, row, 1);
                        autoSave(); // Auto-guardar después de jugada de CPU

                        Platform.runLater(() -> {
                            actualizarTableroJugador(col, row, resultado);
                            manejarResultadoCpu(resultado, col, row);
                        });

                        disparoExitoso = true;
                    }
                    intentos++;
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void manejarResultadoCpu(String resultado, int col, int row) {
        switch (resultado) {
            case "AGUA":
                updateGameStatus("La CPU falló. ¡Tu turno!", Color.BLUE);
                updateTurnIndicator("Tu turno", Color.DARKGREEN);
                
                // CORREGIDO: Cambiar turno inmediatamente tras fallo de CPU
                isPlayerTurn = true;
                autoSave(); // Guardar el cambio de turno
                
                new Thread(() -> {
                    try {
                        Thread.sleep(2000);
                        Platform.runLater(() -> {
                            // Ya no necesitamos cambiar isPlayerTurn aquí porque ya se cambió arriba
                            updateGameStatus("¡Tu turno! Haz clic para disparar", Color.DARKGREEN);
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;

            case "TOCADO":
                aplicarOverlayEnBarco(col, row, "TOCADO");
                updateGameStatus("¡La CPU te tocó! Sigue jugando", Color.DARKORANGE);
                updateTurnIndicator("Turno de la CPU", Color.DARKRED);
                
                // CPU sigue jugando porque acertó
                new Thread(() -> {
                    try {
                        Thread.sleep(2500);
                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;

            case "HUNDIDO":
                marcarBarcoHundidoJugadorConOverlays(col, row);
                updateGameStatus("¡CPU hundió tu barco!", Color.DARKRED);
                updateTurnIndicator("Turno de la CPU", Color.DARKRED);
                
                // CPU sigue jugando porque hundió un barco
                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;

            case "VICTORIA_CPU":
                manejarFinDelJuego("VICTORIA_CPU");
                break;

            default:
                // En caso de error, pasar turno al jugador
                isPlayerTurn = true;
                updateTurnIndicator("Tu turno", Color.DARKGREEN);
                autoSave(); // Guardar el cambio de turno
                break;
        }
    }

    private void updateTurnIndicator(String message, Color color) {
        turnIndicatorLabel.setText(message);
        turnIndicatorLabel.setTextFill(color);
        turnIndicatorLabel.setVisible(true);
    }

    private void updateGameStatus(String message, Color color) {
        gameStatusLabel.setText(message);
        gameStatusLabel.setTextFill(color);
        gameStatusLabel.setVisible(true);
    }

    private void createStartGameButton() {
        startGameButton = new Button("INICIAR JUEGO");
        startGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        startGameButton.setPrefSize(200, 40);

        double buttonX = Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) - 100;
        double buttonY = Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 85;

        startGameButton.setTranslateX(buttonX);
        startGameButton.setTranslateY(buttonY);
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");

        startGameButton.setOnAction(event -> {
            startGame();
        });
    }

    public void checkAllShipsPlaced() {
        if (gameStarted) return;

        int shipsOnBoard = 0;
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            if (position[0] >= 0 && position[1] >= 0) {
                shipsOnBoard++;
            }
        }

        if (shipsOnBoard == playerShips.size()) {
            startGameButton.setDisable(false);
            startGameButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            statusLabel.setText("¡Todos los barcos colocados! Presiona INICIAR JUEGO");
            statusLabel.setTextFill(Color.DARKGREEN);
        } else {
            startGameButton.setDisable(true);
            startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
            statusLabel.setText("Coloca todos los barcos (" + shipsOnBoard + "/" + playerShips.size() + ")");
            statusLabel.setTextFill(Color.DARKORANGE);
        }
    }

    private void startGame() {
        gameStarted = true;
        gameLogic.inicializarBarcosJugador(playerShips);
        gameLogic.posicionarBarcosCpu();

        for (DraggableShape ship : playerShips) {
            ship.disableDragging();
        }

        startGameButton.setText("JUEGO INICIADO");
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        showCpuShipsButton.setVisible(true);
        statusLabel.setText("¡Juego iniciado! Haz clic en el tablero enemigo");
        statusLabel.setTextFill(Color.DARKBLUE);

        updateGameStatus("¡Tu turno! Dispara", Color.DARKGREEN);
        updateTurnIndicator("Tu turno", Color.DARKGREEN);

        isPlayerTurn = true;
        firstPlayerMove = true;
        autoSave();
    }

    private void createShowCpuShipsButton() {
        showCpuShipsButton = new Button("MOSTRAR BARCOS CPU");
        showCpuShipsButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        showCpuShipsButton.setPrefSize(180, 35);

        double cpuBoardX = Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + Constants.BOARD_SPACING;
        double buttonX = cpuBoardX + (Constants.GRID_COLS * Constants.CELL_SIZE / 2) - 90;
        double buttonY = Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 10;

        showCpuShipsButton.setTranslateX(buttonX);
        showCpuShipsButton.setTranslateY(buttonY);
        showCpuShipsButton.setVisible(false);
        showCpuShipsButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

        showCpuShipsButton.setOnAction(event -> {
            if (cpuShipsVisible) {
                hideCpuShips();
            } else {
                showCpuShips();
            }
        });
    }

    private void showCpuShips() {
        boolean[][] cpuMatrix = gameLogic.getMatrizLimpiezaCpu();

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (cpuMatrix[row][col]) {
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightgreen;");
                                break;
                            }
                        }
                    }
                }
            }
        }

        cpuShipsVisible = true;
        showCpuShipsButton.setText("OCULTAR BARCOS CPU");
        showCpuShipsButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void hideCpuShips() {
        boolean[][] cpuMatrix = gameLogic.getMatrizLimpiezaCpu();
        boolean[][] disparosMatrix = gameLogic.getMatrizDisparosPlayer();
        boolean[][] atinacionMatrix = gameLogic.getMatrizAtinacionCpu();

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (cpuMatrix[row][col]) {
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                                if (disparosMatrix[row][col]) {
                                    if (atinacionMatrix[row][col]) {
                                        node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                                    } else {
                                        node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                                    }
                                } else {
                                    node.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

        cpuShipsVisible = false;
        showCpuShipsButton.setText("MOSTRAR BARCOS CPU");
        showCpuShipsButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
    }

    private void createNewGameButton() {
        newGameButton = new Button("NUEVA PARTIDA");
        newGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        newGameButton.setPrefSize(150, 35);

        double buttonX = Constants.BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) - 100 + 250;
        double buttonY = Constants.BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 85;

        newGameButton.setTranslateX(buttonX);
        newGameButton.setTranslateY(buttonY);
        newGameButton.setVisible(false);
        newGameButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");

        newGameButton.setOnAction(event -> {
            iniciarNuevaPartida();
        });
    }

    private void iniciarNuevaPartida() {
        gameLogic.reiniciarJuego();
        gameStarted = false;
        firstPlayerMove = true;
        cpuShipsVisible = false;
        isPlayerTurn = true;

        limpiarTablerosVisuales();

        for (DraggableShape ship : playerShips) {
            ship.enableDragging();
        }

        PlayerShipManager shipManager = new PlayerShipManager();
        List<DraggableShape> newPlayerShips = shipManager.createAndPositionShips(gameLogic, this::checkAllShipsPlaced);

        Pane root = (Pane) playerGridPane.getParent();
        for (DraggableShape oldShip : playerShips) {
            root.getChildren().remove(oldShip.getNode());
        }

        playerShips = newPlayerShips;
        for (DraggableShape ship : playerShips) {
            root.getChildren().add(ship.getNode());
        }

        startGameButton.setText("INICIAR JUEGO");
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");

        showCpuShipsButton.setVisible(false);
        newGameButton.setVisible(false);

        statusLabel.setText("Coloca todos los barcos en el tablero");
        statusLabel.setTextFill(Color.DARKORANGE);
        statusLabel.setVisible(true);

        gameStatusLabel.setVisible(false);
        turnIndicatorLabel.setVisible(false);
        victoryLabel.setVisible(false);

        checkAllShipsPlaced();
    }

    private void limpiarTablerosVisuales() {
        for (javafx.scene.Node node : playerGridPane.getChildren()) {
            if (node instanceof Pane) {
                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
            }
        }

        for (javafx.scene.Node node : cpuGridPane.getChildren()) {
            if (node instanceof Pane) {
                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
            }
        }
    }

    private void manejarFinDelJuego(String tipoVictoria) {
        isPlayerTurn = false;

        String mensajeVictoria;
        Color colorVictoria;

        switch (tipoVictoria) {
            case "VICTORIA_JUGADOR":
                mensajeVictoria = "¡VICTORIA!\n¡Ganaste!";
                colorVictoria = Color.GOLD;
                updateGameStatus("¡Felicidades! Ganaste", Color.DARKGREEN);
                updateTurnIndicator("¡VICTORIA!", Color.GOLD);
                break;

            case "VICTORIA_CPU":
                mensajeVictoria = "DERROTA\nLa CPU ganó";
                colorVictoria = Color.DARKRED;
                updateGameStatus("La CPU ganó", Color.DARKRED);
                updateTurnIndicator("¡DERROTA!", Color.DARKRED);
                break;

            default:
                mensajeVictoria = "Fin del juego";
                colorVictoria = Color.GRAY;
                break;
        }

        victoryLabel.setText(mensajeVictoria);
        victoryLabel.setTextFill(colorVictoria);
        victoryLabel.setVisible(true);

        if (cpuShipsVisible) {
            hideCpuShips();
        }
        showCpuShips();

        newGameButton.setVisible(true);
        showCpuShipsButton.setVisible(false);
    }

    private void aplicarOverlayEnBarco(int col, int row, String impactType) {
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    ship.markCellImpact(col, row, impactType);
                    return;
                }
            }
        }
    }

    private void marcarBarcoHundidoJugadorConOverlays(int col, int row) {
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    ship.markShipAsDestroyed();
                    return;
                }
            }
        }
    }

    private void marcarBarcoHundidoCpu(int col, int row) {
        Ship[] barcosCpu = gameLogic.getArrayCpu();

        for (int i = 0; i < barcosCpu.length; i++) {
            if (barcosCpu[i] != null && barcosCpu[i].containsCoordinate(col, row)) {
                List<int[]> coordinates = barcosCpu[i].getCoordinates();

                for (int[] coordinate : coordinates) {
                    int shipCol = coordinate[0];
                    int shipRow = coordinate[1];

                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null &&
                                    nodeCol == shipCol && nodeRow == shipRow) {
                                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: red;");
                                break;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    private void actualizarTableroJugador(int col, int row, String resultado) {
        if (resultado.equals("AGUA")) {
            for (javafx.scene.Node node : playerGridPane.getChildren()) {
                if (node instanceof Pane) {
                    Integer nodeCol = GridPane.getColumnIndex(node);
                    Integer nodeRow = GridPane.getRowIndex(node);

                    if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                        node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onGameSaved() {
        System.out.println("✓ Partida guardada automáticamente");
    }

    @Override
    public void onGameLoaded() {
        System.out.println("✓ Partida cargada exitosamente");
    }

    @Override
    public void onSaveError(String error) {
        System.err.println("Error de guardado: " + error);
        Platform.runLater(() -> {
            updateGameStatus("Error guardando partida", Color.ORANGE);
        });
    }

    @Override
    public void onGameStateChanged(GameState newState) {
        if (newState != null) {
            System.out.println("Estado del juego actualizado: " + newState.getPlayerNickname());
        }
    }
}