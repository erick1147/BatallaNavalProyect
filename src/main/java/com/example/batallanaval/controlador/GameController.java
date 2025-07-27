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

// Importaciones originales
import com.example.batallanaval.modelo.Constants;
import com.example.batallanaval.modelo.GameLogic;
import com.example.batallanaval.modelo.Ship;
import com.example.batallanaval.vista.GameBoardView;
import com.example.batallanaval.vista.DraggableShape;
import com.example.batallanaval.vista.PlayerShipManager;

// Nuevas importaciones
import com.example.batallanaval.modelo.GameState;
import com.example.batallanaval.vista.MainMenuView;
import com.example.batallanaval.interfaces.MenuListener;
import com.example.batallanaval.adapters.GameStateAdapter;
import com.example.batallanaval.exceptions.GameSaveException;
import com.example.batallanaval.exceptions.GameLoadException;
import com.example.batallanaval.modelo.GameSaveManager;

public class GameController extends GameStateAdapter implements MenuListener {

    // Variables originales del juego
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

    // Nuevas variables
    private GameSaveManager saveManager;
    private MainMenuView mainMenu;
    private Stage primaryStage;
    private String playerNickname = "Capitán";

    public GameController() {
        this.saveManager = new GameSaveManager();
        saveManager.addListener(this);
    }

    // Método de entrada principal con menú
    public void initializeApplication(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        mainMenu = new MainMenuView(primaryStage);
        mainMenu.setMenuListener(this);
        mainMenu.showMenu();
        
        System.out.println("=== BATALLA NAVAL CON GUARDADO AUTOMÁTICO ===");
    }

    // Implementación de MenuListener
    @Override
    public void onNewGameRequested() {
        try {
            saveManager.deleteSavedGame();
        } catch (GameSaveException e) {
            System.err.println("Error eliminando partida: " + e.getMessage());
        }
        initializeGame(primaryStage);
    }

    @Override
    public void onLoadGameRequested() {
        try {
            GameState loadedState = saveManager.loadGame();
            restoreGameFromState(loadedState);
        } catch (GameLoadException e) {
            mainMenu.showLoadError("No hay partida guardada");
            mainMenu.showMenu();
        }
    }

    @Override
    public void onMenuClosed() {
        System.out.println("Menú cerrado");
    }

    // Método para capturar estado del juego
    private GameState captureCurrentGameState() {
        GameState state = new GameState();
        
        state.setPlayerNickname(playerNickname);
        state.setGameStarted(gameStarted);
        state.setGameEnded(gameLogic.isGameEnded());
        state.setPlayerTurn(isPlayerTurn);
        state.setFirstPlayerMove(firstPlayerMove);
        state.setWinner(gameLogic.getWinner());
        
        // Copiar matrices
        state.setMatrizLimpiezaPlayer(copyMatrix(gameLogic.getMatrizLimpiezaPlayer()));
        state.setMatrizLimpiezaCpu(copyMatrix(gameLogic.getMatrizLimpiezaCpu()));
        state.setMatrizDisparosPlayer(copyMatrix(gameLogic.getMatrizDisparosPlayer()));
        state.setMatrizAtinacionCpu(copyMatrix(gameLogic.getMatrizAtinacionCpu()));
        state.setMatrizDisparosCpu(copyMatrix(gameLogic.getMatrizDisparosCpu()));
        state.setMatrizAtinacionPlayer(copyMatrix(gameLogic.getMatrizAtinacionPlayer()));
        
        // Guardar barcos CPU
        Ship[] arrayCpu = gameLogic.getArrayCpu();
        for (int i = 0; i < arrayCpu.length; i++) {
            if (arrayCpu[i] != null && arrayCpu[i].getSize() > 0) {
                GameState.ShipState shipState = new GameState.ShipState();
                shipState.setCoordinates(new ArrayList<>(arrayCpu[i].getCoordinates()));
                shipState.setState(arrayCpu[i].getState());
                state.getCpuShips().add(shipState);
            }
        }
        
        return state;
    }

    // Método para restaurar estado del juego
    private void restoreGameFromState(GameState state) {
        initializeGame(primaryStage);
        
        gameStarted = state.isGameStarted();
        isPlayerTurn = state.isPlayerTurn();
        firstPlayerMove = state.isFirstPlayerMove();
        playerNickname = state.getPlayerNickname();
        
        // Restaurar matrices
        if (state.getMatrizLimpiezaPlayer() != null) {
            gameLogic.setMatrizLimpiezaPlayer(state.getMatrizLimpiezaPlayer());
        }
        if (state.getMatrizLimpiezaCpu() != null) {
            gameLogic.setMatrizLimpiezaCpu(state.getMatrizLimpiezaCpu());
        }
        if (state.getMatrizDisparosPlayer() != null) {
            gameLogic.setMatrizDisparosPlayer(state.getMatrizDisparosPlayer());
        }
        if (state.getMatrizAtinacionCpu() != null) {
            gameLogic.setMatrizAtinacionCpu(state.getMatrizAtinacionCpu());
        }
        if (state.getMatrizDisparosCpu() != null) {
            gameLogic.setMatrizDisparosCpu(state.getMatrizDisparosCpu());
        }
        if (state.getMatrizAtinacionPlayer() != null) {
            gameLogic.setMatrizAtinacionPlayer(state.getMatrizAtinacionPlayer());
        }
        
        // Restaurar barcos CPU
        if (state.getCpuShips() != null && !state.getCpuShips().isEmpty()) {
            Ship[] arrayCpu = gameLogic.getArrayCpu();
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
            gameLogic.posicionarBarcosCpu();
        }
        
        // Actualizar UI si el juego ya empezó
        if (gameStarted) {
            startGameButton.setText("PARTIDA CARGADA");
            startGameButton.setDisable(true);
            startGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
            
            statusLabel.setText("Partida cargada - Jugador: " + playerNickname);
            statusLabel.setTextFill(Color.DARKGREEN);
            
            for (DraggableShape ship : playerShips) {
                ship.disableDragging();
            }
            
            showCpuShipsButton.setVisible(true);
            
            if (isPlayerTurn) {
                updateGameStatus("Es tu turno", Color.DARKGREEN);
                updateTurnIndicator("Tu turno", Color.DARKGREEN);
            } else {
                updateGameStatus("Turno de la CPU", Color.DARKRED);
                updateTurnIndicator("Turno de la CPU", Color.DARKRED);
            }
            
            // Restaurar visual del tablero
            restaurarVisualTablero();
        }
    }

    // Método para restaurar apariencia visual
    private void restaurarVisualTablero() {
        boolean[][] disparosPlayer = gameLogic.getMatrizDisparosPlayer();
        boolean[][] atinacionCpu = gameLogic.getMatrizAtinacionCpu();
        boolean[][] disparosCpu = gameLogic.getMatrizDisparosCpu();
        
        // Restaurar tablero CPU
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (disparosPlayer[row][col]) {
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);
                            
                            if (nodeCol != null && nodeRow != null && nodeCol.equals(col) && nodeRow.equals(row)) {
                                if (atinacionCpu[row][col]) {
                                    node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                                } else {
                                    node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        
        // Restaurar tablero jugador
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (disparosCpu[row][col]) {
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
            }
        }
    }

    // Método para copiar matrices
    private boolean[][] copyMatrix(boolean[][] original) {
        if (original == null) return null;
        
        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    // Guardado automático simple
    private void autoSave() {
        if (!gameStarted) return;
        
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                GameState state = captureCurrentGameState();
                saveManager.saveGame(state);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (GameSaveException e) {
                System.err.println("Error guardando: " + e.getMessage());
            }
        }).start();
    }

    // AQUÍ EMPIEZA TODO EL CÓDIGO ORIGINAL SIN CAMBIOS
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

        Label instructionsLabel = new Label("Arrastra los barcos solo al tablero AZUL. Click derecho para rotar. Los barcos se pueden mover dentro del tablero.");
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
        primaryStage.setTitle("Batalla Naval - Sistema de Fin de Juego Completo");
        primaryStage.setScene(scene);
        primaryStage.show();

        checkAllShipsPlaced();

        System.out.println("=== BATALLA NAVAL - SISTEMA COMPLETO CON FIN DE JUEGO ===");
        System.out.println("Tablero del jugador - Inicio: (" + Constants.BOARD_START_X + ", " + Constants.BOARD_START_Y + ")");
        System.out.println("Tablero de la CPU - Inicio: (" + cpuBoardX + ", " + Constants.BOARD_START_Y + ")");
        System.out.println();
        System.out.println("NUEVAS CARACTERÍSTICAS:");
        System.out.println("• Sistema de turnos automático");
        System.out.println("• Detección automática de fin de juego");
        System.out.println("• Pantalla de victoria/derrota");
        System.out.println("• Botón para nueva partida");
        System.out.println("• Verificación completa de flotas hundidas");
        System.out.println();
        gameLogic.printMatrizLimpieza();
    }

    private void setupEventHandlers() {
        cpuGridPane.setOnMouseClicked(event -> {
            if (!gameStarted) {
                System.out.println("¡Debes iniciar el juego primero!");
                updateGameStatus("¡Debes iniciar el juego primero!", Color.DARKORANGE);
                return;
            }

            if (gameLogic.isGameEnded()) {
                System.out.println("¡El juego ya ha terminado! Presiona 'NUEVA PARTIDA' para jugar de nuevo.");
                updateGameStatus("¡El juego ya ha terminado! Presiona 'NUEVA PARTIDA' para jugar de nuevo.", Color.GRAY);
                return;
            }

            if (!isPlayerTurn) {
                System.out.println("¡No es tu turno! Espera a que la CPU termine.");
                updateGameStatus("¡No es tu turno! Espera a que la CPU termine.", Color.DARKORANGE);
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

        playerGridPane.setOnMouseClicked(event -> {
            double clickX = event.getX();
            double clickY = event.getY();

            int col = (int) (clickX / Constants.CELL_SIZE);
            int row = (int) (clickY / Constants.CELL_SIZE);

            if (col >= 0 && col < Constants.GRID_COLS && row >= 0 && row < Constants.GRID_ROWS) {
                boolean isOccupied = gameLogic.getMatrizLimpiezaPlayer()[row][col];
                System.out.println("Click en tu tablero - Celda: (" + col + ", " + row + ") - " +
                        (isOccupied ? "Ocupada" : "Libre"));

                double cellPixelX = Constants.BOARD_START_X + (col * Constants.CELL_SIZE);
                double cellPixelY = Constants.BOARD_START_Y + (row * Constants.CELL_SIZE);
                System.out.println("Coordenadas de píxel de la celda: (" + cellPixelX + ", " + cellPixelY + ")");
            }
        });
    }

    private void realizarDisparoJugador(int col, int row, Pane clickedPane) {
        System.out.println("=== DISPARO DEL JUGADOR ===");
        System.out.println("Disparando a coordenadas: (" + col + ", " + row + ")");

        String resultado = gameLogic.jugada(col, row, 0);

        // GUARDADO AUTOMÁTICO
        autoSave();

        switch (resultado) {
            case "AGUA":
                clickedPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                updateGameStatus("¡Agua! Fin de tu turno. La CPU jugará en breve...", Color.BLUE);
                updateTurnIndicator("Fin de tu turno - Preparando turno de la CPU", Color.DARKORANGE);
                System.out.println("¡AGUA! El turno pasa a la CPU");

                isPlayerTurn = false;

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);

                        Platform.runLater(() -> {
                            updateGameStatus("La CPU está pensando...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Analizando", Color.DARKRED);
                        });

                        Thread.sleep(1500);

                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Platform.runLater(() -> {
                            isPlayerTurn = true;
                            updateGameStatus("Error en el turno de la CPU. Es tu turno.", Color.RED);
                            updateTurnIndicator("Tu turno", Color.DARKGREEN);
                        });
                    }
                }).start();
                break;

            case "TOCADO":
                clickedPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                updateGameStatus("¡Tocado! Puedes disparar de nuevo.", Color.DARKORANGE);
                updateTurnIndicator("Tu turno - ¡Sigue disparando!", Color.DARKGREEN);
                System.out.println("¡TOCADO! El jugador puede disparar de nuevo");
                break;

            case "HUNDIDO":
                marcarBarcoHundidoCpu(col, row);
                updateGameStatus("¡Barco hundido! Puedes disparar de nuevo.", Color.DARKRED);
                updateTurnIndicator("Tu turno - ¡Barco hundido!", Color.DARKGREEN);
                System.out.println("¡HUNDIDO! El jugador puede disparar de nuevo");
                break;

            case "VICTORIA_JUGADOR":
                marcarBarcoHundidoCpu(col, row);
                manejarFinDelJuego("VICTORIA_JUGADOR");
                break;

            case "YA_DISPARADO":
                updateGameStatus("Ya disparaste a esa coordenada. Elige otra.", Color.GRAY);
                System.out.println("Ya se había disparado a esa coordenada");
                break;

            default:
                updateGameStatus("Error en el disparo.", Color.RED);
                System.out.println("Error: Resultado desconocido - " + resultado);
                break;
        }
    }

    private void turnoDelaCpu() {
        System.out.println("=== TURNO DE LA CPU ===");

        updateGameStatus("La CPU está apuntando...", Color.DARKRED);
        updateTurnIndicator("Turno de la CPU - Apuntando", Color.DARKRED);

        new Thread(() -> {
            try {
                Thread.sleep(1500);

                boolean disparoExitoso = false;
                int intentos = 0;
                int maxIntentos = 100;

                while (!disparoExitoso && intentos < maxIntentos) {
                    int col = cpuRandom.nextInt(Constants.GRID_COLS);
                    int row = cpuRandom.nextInt(Constants.GRID_ROWS);

                    if (!gameLogic.yaSeDisparo(col, row, 1)) {

                        Platform.runLater(() -> {
                            updateGameStatus("¡La CPU dispara a (" + col + ", " + row + ")!", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - ¡Disparando!", Color.DARKRED);
                        });

                        System.out.println("CPU dispara a coordenadas: (" + col + ", " + row + ")");

                        Thread.sleep(1000);

                        String resultado = gameLogic.jugada(col, row, 1);

                        // GUARDADO AUTOMÁTICO
                        autoSave();

                        Platform.runLater(() -> {
                            actualizarTableroJugador(col, row, resultado);
                        });

                        Thread.sleep(800);

                        Platform.runLater(() -> {
                            manejarResultadoCpu(resultado, col, row);
                        });

                        disparoExitoso = true;
                    }
                    intentos++;
                }

                if (intentos >= maxIntentos) {
                    Platform.runLater(() -> {
                        System.out.println("ERROR: CPU no pudo encontrar una coordenada válida para disparar");
                        updateGameStatus("Error en el turno de la CPU", Color.RED);
                        isPlayerTurn = true;
                        updateTurnIndicator("Tu turno (Error de CPU)", Color.DARKGREEN);
                    });
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Platform.runLater(() -> {
                    isPlayerTurn = true;
                    updateGameStatus("Error en el turno de la CPU. Es tu turno.", Color.RED);
                    updateTurnIndicator("Tu turno", Color.DARKGREEN);
                });
            }
        }).start();
    }

    private void manejarResultadoCpu(String resultado, int col, int row) {
        System.out.println("Resultado del disparo de la CPU: " + resultado);

        switch (resultado) {
            case "AGUA":
                updateGameStatus("¡La CPU falló en (" + col + ", " + row + ")! Preparando tu turno...", Color.BLUE);
                updateTurnIndicator("La CPU falló - Tu turno en breve", Color.DARKORANGE);
                System.out.println("¡CPU falló! El turno vuelve al jugador");

                new Thread(() -> {
                    try {
                        Thread.sleep(2000);

                        Platform.runLater(() -> {
                            isPlayerTurn = true;
                            updateGameStatus("¡Es tu turno! Haz clic en el tablero enemigo para disparar.", Color.DARKGREEN);
                            updateTurnIndicator("Tu turno", Color.DARKGREEN);
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).start();
                break;

            case "TOCADO":
                aplicarOverlayEnBarco(col, row, "TOCADO");
                updateGameStatus("¡La CPU te tocó un barco en (" + col + ", " + row + ")! CPU sigue jugando...", Color.DARKORANGE);
                updateTurnIndicator("Turno de la CPU - Te tocó", Color.DARKRED);
                System.out.println("¡CPU tocó un barco! CPU puede disparar de nuevo");

                new Thread(() -> {
                    try {
                        Thread.sleep(2500);

                        Platform.runLater(() -> {
                            updateGameStatus("La CPU prepara otro disparo...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Preparando siguiente disparo", Color.DARKRED);
                        });

                        Thread.sleep(1500);

                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Platform.runLater(() -> {
                            isPlayerTurn = true;
                            updateGameStatus("Error en el turno de la CPU. Es tu turno.", Color.RED);
                            updateTurnIndicator("Tu turno", Color.DARKGREEN);
                        });
                    }
                }).start();
                break;

            case "HUNDIDO":
                marcarBarcoHundidoJugadorConOverlays(col, row);
                updateGameStatus("¡La CPU hundió tu barco en (" + col + ", " + row + ")! CPU sigue jugando...", Color.DARKRED);
                updateTurnIndicator("Turno de la CPU - ¡Barco hundido!", Color.DARKRED);
                System.out.println("¡CPU hundió un barco! CPU puede disparar de nuevo");

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);

                        Platform.runLater(() -> {
                            updateGameStatus("La CPU celebra y prepara otro disparo...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Celebrando victoria", Color.DARKRED);
                        });

                        Thread.sleep(2000);

                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        Platform.runLater(() -> {
                            isPlayerTurn = true;
                            updateGameStatus("Error en el turno de la CPU. Es tu turno.", Color.RED);
                            updateTurnIndicator("Tu turno", Color.DARKGREEN);
                        });
                    }
                }).start();
                break;

            case "VICTORIA_CPU":
                manejarFinDelJuego("VICTORIA_CPU");
                break;

            default:
                System.out.println("Error: Resultado desconocido de la CPU - " + resultado);
                updateGameStatus("Error en el turno de la CPU", Color.RED);
                isPlayerTurn = true;
                updateTurnIndicator("Tu turno (Error de CPU)", Color.DARKGREEN);
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

        System.out.println("Barcos en el tablero: " + shipsOnBoard + " de " + playerShips.size());

        if (shipsOnBoard == playerShips.size()) {
            startGameButton.setDisable(false);
            startGameButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
            statusLabel.setText("¡Todos los barcos están colocados! Presiona 'INICIAR JUEGO' para comenzar");
            statusLabel.setTextFill(Color.DARKGREEN);
            System.out.println("✓ ¡TODOS LOS BARCOS ESTÁN COLOCADOS! El botón 'INICIAR JUEGO' está ahora habilitado.");
        } else {
            startGameButton.setDisable(true);
            startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");
            statusLabel.setText("Coloca todos los barcos en el tablero para poder iniciar el juego (" +
                    shipsOnBoard + "/" + playerShips.size() + " colocados)");
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

        statusLabel.setText("¡Juego iniciado! Sistema de turnos activado. Puedes ver los barcos de la CPU antes de tu primer disparo.");
        statusLabel.setTextFill(Color.DARKBLUE);

        updateGameStatus("¡Juego iniciado! Haz clic en el tablero enemigo para disparar.", Color.DARKGREEN);
        updateTurnIndicator("Tu turno", Color.DARKGREEN);

        isPlayerTurn = true;
        firstPlayerMove = true;

        // GUARDADO AUTOMÁTICO
        autoSave();

        System.out.println("=== JUEGO INICIADO CON SISTEMA COMPLETO ===");
        System.out.println("✓ Los barcos han sido bloqueados");
        System.out.println("✓ Los barcos de la CPU han sido posicionados");
        System.out.println("✓ Sistema de turnos activado");
        System.out.println("✓ Sistema de fin de juego activado");
        System.out.println("✓ Botón de visualización de barcos CPU disponible");
        System.out.println("✓ Es el turno del jugador");
        System.out.println("✓ GUARDADO AUTOMÁTICO ACTIVADO");
        System.out.println();

        System.out.println("POSICIONES FINALES DE LOS BARCOS DEL JUGADOR:");
        for (int i = 0; i < playerShips.size(); i++) {
            DraggableShape ship = playerShips.get(i);
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();
            String orientation = ship.isVertical() ? "Vertical" : "Horizontal";

            System.out.println("Barco " + (i + 1) + ": Posición (" + position[0] + "," + position[1] +
                    ") - Tamaño: " + dimensions[0] + "x" + dimensions[1] + " - " + orientation);
        }

        System.out.println();
        gameLogic.printMatrizLimpieza();
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
        System.out.println("✓ Barcos de la CPU mostrados en color verde claro");
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
        System.out.println("✓ Barcos de la CPU ocultados");
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
        System.out.println("=== INICIANDO NUEVA PARTIDA ===");

        gameLogic.reiniciarJuego();

        gameStarted = false;
        firstPlayerMove = true;
        cpuShipsVisible = false;
        isPlayerTurn = true;

        limpiarTablerosVisuales();

        for (DraggableShape ship : playerShips) {
            ship.enableDragging();
            if (ship.getNode() instanceof javafx.scene.Group) {
                javafx.scene.Group shipGroup = (javafx.scene.Group) ship.getNode();
                for (javafx.scene.Node child : shipGroup.getChildren()) {
                    if (child instanceof javafx.scene.shape.Rectangle &&
                            child != ship.backgroundRect) {
                        ((javafx.scene.shape.Rectangle) child).setFill(Color.TRANSPARENT);
                    }
                }
            }
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

        statusLabel.setText("Coloca todos los barcos en el tablero para poder iniciar el juego");
        statusLabel.setTextFill(Color.DARKORANGE);
        statusLabel.setVisible(true);

        gameStatusLabel.setVisible(false);
        turnIndicatorLabel.setVisible(false);
        victoryLabel.setVisible(false);

        checkAllShipsPlaced();

        System.out.println("✓ Nueva partida iniciada - Coloca tus barcos y presiona 'INICIAR JUEGO'");
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

        System.out.println("✓ Tableros limpiados visualmente");
    }

    private void manejarFinDelJuego(String tipoVictoria) {
        System.out.println("=== FIN DEL JUEGO ===");

        isPlayerTurn = false;

        String mensajeVictoria;
        Color colorVictoria;

        switch (tipoVictoria) {
            case "VICTORIA_JUGADOR":
                mensajeVictoria = "🎉 ¡VICTORIA! 🎉\n¡Has hundido toda la flota enemiga!";
                colorVictoria = Color.GOLD;
                updateGameStatus("¡FELICIDADES! Has ganado la batalla naval.", Color.DARKGREEN);
                updateTurnIndicator("¡VICTORIA DEL JUGADOR!", Color.GOLD);
                System.out.println("🎉 ¡EL JUGADOR HA GANADO!");
                break;

            case "VICTORIA_CPU":
                mensajeVictoria = "💀 DERROTA 💀\nLa CPU ha hundido toda tu flota";
                colorVictoria = Color.DARKRED;
                updateGameStatus("Has sido derrotado. La CPU ha ganado.", Color.DARKRED);
                updateTurnIndicator("¡VICTORIA DE LA CPU!", Color.DARKRED);
                System.out.println("💀 LA CPU HA GANADO");
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

        System.out.println("✓ Pantalla de fin de juego mostrada");
        System.out.println("✓ Botón 'NUEVA PARTIDA' disponible");
        System.out.println("✓ Todos los barcos de la CPU revelados");
    }

    private void aplicarOverlayEnBarco(int col, int row, String impactType) {
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    ship.markCellImpact(col, row, impactType);
                    System.out.println("✓ Overlay aplicado en barco del jugador - Coordenadas: (" + col + "," + row + ") - Tipo: " + impactType);
                    return;
                }
            }
        }
    }

    private void marcarBarcoHundidoJugadorConOverlays(int col, int row) {
        System.out.println("=== MARCANDO BARCO HUNDIDO DEL JUGADOR CON OVERLAYS ===");

        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    ship.markShipAsDestroyed();

                    System.out.println("✗ Tu barco ha sido hundido por la CPU usando overlays");
                    System.out.print("Posición del barco: (" + position[0] + "," + position[1] + ") ");
                    System.out.println("Tamaño: " + dimensions[0] + "x" + dimensions[1]);
                    return;
                }
            }
        }
    }

    private void marcarBarcoHundidoCpu(int col, int row) {
        System.out.println("=== MARCANDO BARCO HUNDIDO DE LA CPU ===");

        Ship[] barcosCpu = gameLogic.getArrayCpu();

        for (int i = 0; i < barcosCpu.length; i++) {
            if (barcosCpu[i] != null && barcosCpu[i].containsCoordinate(col, row)) {
                System.out.println("✓ Barco " + (i+1) + " de la CPU ha sido hundido");

                List<int[]> coordinates = barcosCpu[i].getCoordinates();

                System.out.print("Marcando en rojo las coordenadas: ");

                for (int[] coordinate : coordinates) {
                    int shipCol = coordinate[0];
                    int shipRow = coordinate[1];

                    System.out.print("(" + shipCol + "," + shipRow + ") ");

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

                System.out.println();
                System.out.println("✓ Barco completamente marcado en rojo");
                break;
            }
        }
    }

    private void actualizarTableroJugador(int col, int row, String resultado) {
        switch (resultado) {
            case "AGUA":
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
                break;
        }
    }

    // SOBREESCRIBIR MÉTODOS DEL ADAPTER
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
}