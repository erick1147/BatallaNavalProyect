package com.example.batallanaval;

import javafx.application.Application;
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
import java.util.Map;

public class MainApp extends Application {

    // Instancia global de la lógica del juego
    private Logic gameLogic;

    // Referencias a los barcos y botones
    private List<DraggableShape> playerShips;
    private Button startGameButton;
    private Button showCpuShipsButton;
    private Button newGameButton; // NUEVO: Botón para nueva partida
    private boolean gameStarted = false;
    private boolean firstPlayerMove = true;
    private boolean cpuShipsVisible = false;
    private Label statusLabel;
    private Label gameStatusLabel;
    private Label turnIndicatorLabel;
    private Label victoryLabel; // NUEVO: Etiqueta para mostrar victoria

    // Referencias a los tableros para manejar los disparos
    private GridPane playerGridPane;
    private GridPane cpuGridPane;

    // Variables para el sistema de turnos
    private boolean isPlayerTurn = true;
    private Random cpuRandom = new Random();

    // COORDENADAS EXACTAS DEL TABLERO - CRÍTICAS PARA ALINEACIÓN
    public static final double BOARD_START_X = 10.0;
    public static final double BOARD_START_Y = 30.0;
    public static final double BOARD_SPACING = 20.0;

    @Override
    public void start(Stage primaryStage) {
        // Inicializar la lógica del juego
        gameLogic = new Logic();

        // Configuración de los tableros de juego
        GameBoardView playerBoardView = new GameBoardView();
        playerGridPane = playerBoardView.getGridPane();

        GameBoardView cpuBoardView = new GameBoardView();
        cpuGridPane = cpuBoardView.getGridPane();

        // Crear etiquetas para identificar los tableros
        Label playerLabel = new Label("Tu Tablero");
        playerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playerLabel.setTextFill(Color.DARKBLUE);

        Label cpuLabel = new Label("Tablero Enemigo");
        cpuLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        cpuLabel.setTextFill(Color.DARKRED);

        // POSICIONAMIENTO MANUAL Y EXACTO DE LOS TABLEROS
        playerLabel.setTranslateX(BOARD_START_X);
        playerLabel.setTranslateY(BOARD_START_Y - 20);

        playerGridPane.setTranslateX(BOARD_START_X);
        playerGridPane.setTranslateY(BOARD_START_Y);

        double cpuBoardX = BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + BOARD_SPACING;
        cpuLabel.setTranslateX(cpuBoardX);
        cpuLabel.setTranslateY(BOARD_START_Y - 20);

        cpuGridPane.setTranslateX(cpuBoardX);
        cpuGridPane.setTranslateY(BOARD_START_Y);

        // Panel raíz
        Pane root = new Pane();

        // Añadir elementos al panel raíz en orden correcto
        root.getChildren().addAll(playerLabel, playerGridPane, cpuLabel, cpuGridPane);

        // Creación y gestión inicial de los barcos del jugador
        PlayerShipManager shipManager = new PlayerShipManager();
        playerShips = shipManager.createAndPositionShips(gameLogic, this::checkAllShipsPlaced);

        // Crear etiqueta de instrucciones
        Label instructionsLabel = new Label("Arrastra los barcos solo al tablero AZUL. Click derecho para rotar. Los barcos se pueden mover dentro del tablero.");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        instructionsLabel.setTextFill(Color.DARKGREEN);
        instructionsLabel.setTranslateX(BOARD_START_X);
        instructionsLabel.setTranslateY(BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 30);
        root.getChildren().add(instructionsLabel);

        // Crear etiqueta de estado
        statusLabel = new Label("Coloca todos los barcos en el tablero para poder iniciar el juego");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        statusLabel.setTextFill(Color.DARKORANGE);
        statusLabel.setTranslateX(BOARD_START_X);
        statusLabel.setTranslateY(BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 55);
        root.getChildren().add(statusLabel);

        // Crear etiqueta de estado del juego
        gameStatusLabel = new Label("");
        gameStatusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gameStatusLabel.setTextFill(Color.DARKBLUE);
        gameStatusLabel.setTranslateX(BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + BOARD_SPACING);
        gameStatusLabel.setTranslateY(BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 30);
        gameStatusLabel.setVisible(false);
        root.getChildren().add(gameStatusLabel);

        // Crear indicador de turno
        turnIndicatorLabel = new Label("");
        turnIndicatorLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        turnIndicatorLabel.setTextFill(Color.DARKBLUE);
        turnIndicatorLabel.setTranslateX(BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + BOARD_SPACING);
        turnIndicatorLabel.setTranslateY(BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 60);
        turnIndicatorLabel.setVisible(false);
        root.getChildren().add(turnIndicatorLabel);

        // NUEVO: Crear etiqueta de victoria
        victoryLabel = new Label("");
        victoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        victoryLabel.setTextFill(Color.GOLD);
        victoryLabel.setTranslateX(BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE / 2) - 100);
        victoryLabel.setTranslateY(BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE / 2) - 12);
        victoryLabel.setVisible(false);
        victoryLabel.setStyle("-fx-background-color: rgba(0,0,0,0.8); -fx-padding: 20; -fx-border-color: gold; -fx-border-width: 3;");
        root.getChildren().add(victoryLabel);

        // Crear botones
        createStartGameButton();
        createShowCpuShipsButton();
        createNewGameButton(); // NUEVO: Crear botón de nueva partida
        root.getChildren().add(startGameButton);
        root.getChildren().add(showCpuShipsButton);
        root.getChildren().add(newGameButton);

        // Añadir los barcos al panel raíz
        for (DraggableShape ship : playerShips) {
            root.getChildren().add(ship.getNode());
        }

        // Event handler para clics en el tablero de la CPU (DISPARO)
        cpuGridPane.setOnMouseClicked(event -> {
            if (!gameStarted) {
                System.out.println("¡Debes iniciar el juego primero!");
                updateGameStatus("¡Debes iniciar el juego primero!", Color.DARKORANGE);
                return;
            }

            // NUEVO: Verificar si el juego ya terminó
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
                    // Ocultar botón de mostrar barcos CPU en el primer disparo
                    if (firstPlayerMove) {
                        firstPlayerMove = false;
                        showCpuShipsButton.setVisible(false);
                        if (cpuShipsVisible) {
                            hideCpuShips(); // Ocultar barcos si estaban visibles
                        }
                    }

                    realizarDisparoJugador(col, row, clickedPane);
                }
            }
        });

        // Event handler para el tablero del jugador (solo para información y debugging)
        playerGridPane.setOnMouseClicked(event -> {
            double clickX = event.getX();
            double clickY = event.getY();

            int col = (int) (clickX / Constants.CELL_SIZE);
            int row = (int) (clickY / Constants.CELL_SIZE);

            if (col >= 0 && col < Constants.GRID_COLS && row >= 0 && row < Constants.GRID_ROWS) {
                boolean isOccupied = gameLogic.getMatrizLimpiezaPlayer()[row][col];
                System.out.println("Click en tu tablero - Celda: (" + col + ", " + row + ") - " +
                        (isOccupied ? "Ocupada" : "Libre"));

                double cellPixelX = BOARD_START_X + (col * Constants.CELL_SIZE);
                double cellPixelY = BOARD_START_Y + (row * Constants.CELL_SIZE);
                System.out.println("Coordenadas de píxel de la celda: (" + cellPixelX + ", " + cellPixelY + ")");
            }
        });

        // Configuración de la escena y ventana
        double sceneWidth = BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE * 2) + BOARD_SPACING + 20;
        double sceneHeight = BOARD_START_Y + Constants.GRID_ROWS * Constants.CELL_SIZE + 300; // Espacio extra para botones

        Scene scene = new Scene(root, sceneWidth, sceneHeight);
        primaryStage.setTitle("Batalla Naval - Sistema de Fin de Juego Completo");
        primaryStage.setScene(scene);
        primaryStage.show();

        checkAllShipsPlaced();

        // Información de debugging
        System.out.println("=== BATALLA NAVAL - SISTEMA COMPLETO CON FIN DE JUEGO ===");
        System.out.println("Tablero del jugador - Inicio: (" + BOARD_START_X + ", " + BOARD_START_Y + ")");
        System.out.println("Tablero de la CPU - Inicio: (" + cpuBoardX + ", " + BOARD_START_Y + ")");
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

    /**
     * NUEVO: Crea el botón para nueva partida
     */
    private void createNewGameButton() {
        newGameButton = new Button("NUEVA PARTIDA");
        newGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        newGameButton.setPrefSize(150, 35);

        // Posicionar al lado del botón de start
        double buttonX = BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) - 100 + 250;
        double buttonY = BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 85;

        newGameButton.setTranslateX(buttonX);
        newGameButton.setTranslateY(buttonY);

        newGameButton.setVisible(false); // Inicialmente oculto
        newGameButton.setStyle("-fx-background-color: #FF5722; -fx-text-fill: white; -fx-font-weight: bold;");

        newGameButton.setOnAction(event -> {
            iniciarNuevaPartida();
        });
    }

    /**
     * NUEVO: Inicia una nueva partida reiniciando todo el estado del juego
     */
    private void iniciarNuevaPartida() {
        System.out.println("=== INICIANDO NUEVA PARTIDA ===");

        // Reiniciar la lógica del juego
        gameLogic.reiniciarJuego();

        // Reiniciar variables de estado
        gameStarted = false;
        firstPlayerMove = true;
        cpuShipsVisible = false;
        isPlayerTurn = true;

        // Limpiar tableros visualmente
        limpiarTablerosVisuales();

        // Reiniciar barcos del jugador
        for (DraggableShape ship : playerShips) {
            ship.enableDragging(); // Habilitar movimiento
            // Limpiar overlays de impacto - necesitamos acceder al Group interno
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

        // Reposicionar barcos debajo de los tableros
        PlayerShipManager shipManager = new PlayerShipManager();
        // Crear nuevos barcos y reemplazar los anteriores
        List<DraggableShape> newPlayerShips = shipManager.createAndPositionShips(gameLogic, this::checkAllShipsPlaced);

        // Remover barcos antiguos de la escena
        Pane root = (Pane) playerGridPane.getParent();
        for (DraggableShape oldShip : playerShips) {
            root.getChildren().remove(oldShip.getNode());
        }

        // Añadir nuevos barcos
        playerShips = newPlayerShips;
        for (DraggableShape ship : playerShips) {
            root.getChildren().add(ship.getNode());
        }

        // Reiniciar botones
        startGameButton.setText("INICIAR JUEGO");
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");

        showCpuShipsButton.setVisible(false);
        newGameButton.setVisible(false);

        // Reiniciar etiquetas
        statusLabel.setText("Coloca todos los barcos en el tablero para poder iniciar el juego");
        statusLabel.setTextFill(Color.DARKORANGE);
        statusLabel.setVisible(true);

        gameStatusLabel.setVisible(false);
        turnIndicatorLabel.setVisible(false);
        victoryLabel.setVisible(false);

        checkAllShipsPlaced();

        System.out.println("✓ Nueva partida iniciada - Coloca tus barcos y presiona 'INICIAR JUEGO'");
    }

    /**
     * NUEVO: Limpia visualmente ambos tableros
     */
    private void limpiarTablerosVisuales() {
        // Limpiar tablero del jugador
        for (javafx.scene.Node node : playerGridPane.getChildren()) {
            if (node instanceof Pane) {
                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
            }
        }

        // Limpiar tablero de la CPU
        for (javafx.scene.Node node : cpuGridPane.getChildren()) {
            if (node instanceof Pane) {
                node.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
            }
        }

        System.out.println("✓ Tableros limpiados visualmente");
    }

    /**
     * NUEVO: Maneja el fin del juego cuando hay una victoria
     */
    private void manejarFinDelJuego(String tipoVictoria) {
        System.out.println("=== FIN DEL JUEGO ===");

        // Detener todos los turnos
        isPlayerTurn = false;

        // Mostrar mensaje de victoria
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

        // Mostrar etiqueta de victoria
        victoryLabel.setText(mensajeVictoria);
        victoryLabel.setTextFill(colorVictoria);
        victoryLabel.setVisible(true);

        // Mostrar todos los barcos de la CPU al final del juego
        if (cpuShipsVisible) {
            hideCpuShips(); // Primero ocultar si estaban visibles
        }
        showCpuShips(); // Luego mostrar con estado final

        // Mostrar botón de nueva partida
        newGameButton.setVisible(true);

        // Ocultar otros botones
        showCpuShipsButton.setVisible(false);

        System.out.println("✓ Pantalla de fin de juego mostrada");
        System.out.println("✓ Botón 'NUEVA PARTIDA' disponible");
        System.out.println("✓ Todos los barcos de la CPU revelados");
    }

    /**
     * Crea el botón para mostrar/ocultar barcos de la CPU
     */
    private void createShowCpuShipsButton() {
        showCpuShipsButton = new Button("MOSTRAR BARCOS CPU");
        showCpuShipsButton.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        showCpuShipsButton.setPrefSize(180, 35);

        // Posicionar debajo del tablero de la CPU
        double cpuBoardX = BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + BOARD_SPACING;
        double buttonX = cpuBoardX + (Constants.GRID_COLS * Constants.CELL_SIZE / 2) - 90; // Centrado
        double buttonY = BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 10;

        showCpuShipsButton.setTranslateX(buttonX);
        showCpuShipsButton.setTranslateY(buttonY);

        // Inicialmente oculto hasta que el juego comience
        showCpuShipsButton.setVisible(false);
        showCpuShipsButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");

        // Event handler para el botón
        showCpuShipsButton.setOnAction(event -> {
            if (cpuShipsVisible) {
                hideCpuShips();
            } else {
                showCpuShips();
            }
        });
    }

    /**
     * Muestra los barcos de la CPU en el tablero
     */
    private void showCpuShips() {
        boolean[][] cpuMatrix = gameLogic.getMatrizLimpiezaCpu();

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (cpuMatrix[row][col]) {
                    // Encontrar el panel correspondiente en el GridPane de la CPU
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                                // Marcar como barco (color verde claro)
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

    /**
     * Oculta los barcos de la CPU del tablero
     */
    private void hideCpuShips() {
        boolean[][] cpuMatrix = gameLogic.getMatrizLimpiezaCpu();
        boolean[][] disparosMatrix = gameLogic.getMatrizDisparosPlayer();
        boolean[][] atinacionMatrix = gameLogic.getMatrizAtinacionCpu();

        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                if (cpuMatrix[row][col]) {
                    // Encontrar el panel correspondiente
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null && nodeCol == col && nodeRow == row) {
                                // Restaurar el color según el estado del disparo
                                if (disparosMatrix[row][col]) {
                                    if (atinacionMatrix[row][col]) {
                                        // Era un acierto, mantener el color de acierto
                                        node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: yellow;");
                                    } else {
                                        // Era agua, mantener azul claro
                                        node.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                                    }
                                } else {
                                    // No se ha disparado, volver al color original
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

    /**
     * MODIFICADO: Realiza un disparo del jugador y maneja el resultado con verificación de fin de juego
     */
    private void realizarDisparoJugador(int col, int row, Pane clickedPane) {
        System.out.println("=== DISPARO DEL JUGADOR ===");
        System.out.println("Disparando a coordenadas: (" + col + ", " + row + ")");

        String resultado = gameLogic.jugada(col, row, 0); // 0 = jugador humano

        switch (resultado) {
            case "AGUA":
                clickedPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8; -fx-background-color: lightblue;");
                updateGameStatus("¡Agua! Fin de tu turno. La CPU jugará en breve...", Color.BLUE);
                updateTurnIndicator("Fin de tu turno - Preparando turno de la CPU", Color.DARKORANGE);
                System.out.println("¡AGUA! El turno pasa a la CPU");

                // Cambiar turno a la CPU con un delay más pausado
                isPlayerTurn = false;

                // Usar un nuevo thread para manejar la transición de manera más suave
                new Thread(() -> {
                    try {
                        // Pausa inicial para mostrar el resultado del jugador
                        Thread.sleep(2000); // 2 segundos para procesar el resultado

                        // Actualizar UI en el hilo principal
                        Platform.runLater(() -> {
                            updateGameStatus("La CPU está pensando...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Analizando", Color.DARKRED);
                        });

                        // Pausa adicional para simular "pensamiento" de la CPU
                        Thread.sleep(1500); // 1.5 segundos más de "análisis"

                        // Ejecutar turno de la CPU en el hilo principal
                        Platform.runLater(() -> {
                            turnoDelaCpu();
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        // En caso de interrupción, devolver el turno al jugador
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
                // Marcar todo el barco hundido en rojo
                marcarBarcoHundidoCpu(col, row);
                updateGameStatus("¡Barco hundido! Puedes disparar de nuevo.", Color.DARKRED);
                updateTurnIndicator("Tu turno - ¡Barco hundido!", Color.DARKGREEN);
                System.out.println("¡HUNDIDO! El jugador puede disparar de nuevo");
                break;

            case "VICTORIA_JUGADOR": // NUEVO: Manejar victoria del jugador
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

    /**
     * CORREGIDO: Marca todas las casillas de un barco hundido de la CPU en color rojo usando List
     */
    private void marcarBarcoHundidoCpu(int col, int row) {
        System.out.println("=== MARCANDO BARCO HUNDIDO DE LA CPU ===");

        // Buscar qué barco fue hundido
        Ship[] barcosCpu = gameLogic.getArrayCpu();

        for (int i = 0; i < barcosCpu.length; i++) {
            if (barcosCpu[i] != null && barcosCpu[i].containsCoordinate(col, row)) {
                System.out.println("✓ Barco " + (i+1) + " de la CPU ha sido hundido");

                // CORREGIDO: Obtener todas las coordenadas del barco hundido usando List
                List<int[]> coordinates = barcosCpu[i].getCoordinates();

                System.out.print("Marcando en rojo las coordenadas: ");

                // CORREGIDO: Marcar cada coordenada del barco en rojo usando List
                for (int[] coordinate : coordinates) {
                    int shipCol = coordinate[0];
                    int shipRow = coordinate[1];

                    System.out.print("(" + shipCol + "," + shipRow + ") ");

                    // Encontrar el panel correspondiente en el GridPane de la CPU
                    for (javafx.scene.Node node : cpuGridPane.getChildren()) {
                        if (node instanceof Pane) {
                            Integer nodeCol = GridPane.getColumnIndex(node);
                            Integer nodeRow = GridPane.getRowIndex(node);

                            if (nodeCol != null && nodeRow != null &&
                                    nodeCol == shipCol && nodeRow == shipRow) {
                                // Marcar como hundido (color rojo)
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

    /**
     * MODIFICADO: Actualiza visualmente el tablero del jugador después de un disparo de la CPU usando overlays en los barcos
     */
    private void actualizarTableroJugador(int col, int row, String resultado) {
        switch (resultado) {
            case "AGUA":
                // Para agua, sí cambiar el color de fondo del tablero
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

            case "TOCADO":
                // Para impacto en barco, usar overlay en el barco en lugar del tablero
                aplicarOverlayEnBarco(col, row, "TOCADO");
                break;

            case "HUNDIDO":
            case "VICTORIA_CPU": // NUEVO: También manejar victoria de la CPU
                // Para barco hundido, marcar todo el barco usando overlays
                marcarBarcoHundidoJugadorConOverlays(col, row);
                break;
        }
    }

    /**
     * Aplica overlay de impacto en el barco específico del jugador
     */
    private void aplicarOverlayEnBarco(int col, int row, String impactType) {
        // Buscar qué barco del jugador fue impactado
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                // Verificar si la coordenada está dentro de este barco
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    // Aplicar overlay en la celda específica del barco
                    ship.markCellImpact(col, row, impactType);
                    System.out.println("✓ Overlay aplicado en barco del jugador - Coordenadas: (" + col + "," + row + ") - Tipo: " + impactType);
                    return;
                }
            }
        }
    }

    /**
     * MODIFICADO: Marca todas las casillas de un barco hundido del jugador usando overlays en lugar de color de fondo
     */
    private void marcarBarcoHundidoJugadorConOverlays(int col, int row) {
        System.out.println("=== MARCANDO BARCO HUNDIDO DEL JUGADOR CON OVERLAYS ===");

        // Buscar qué barco del jugador fue hundido
        for (DraggableShape ship : playerShips) {
            int[] position = ship.getCurrentGridPosition();
            int[] dimensions = ship.getCurrentDimensionsInCells();

            if (position[0] >= 0 && position[1] >= 0) {
                // Verificar si la coordenada está dentro de este barco
                if (col >= position[0] && col < position[0] + dimensions[0] &&
                        row >= position[1] && row < position[1] + dimensions[1]) {

                    // Marcar todo el barco como hundido usando overlays
                    ship.markShipAsDestroyed();

                    System.out.println("✗ Tu barco ha sido hundido por la CPU usando overlays");
                    System.out.print("Posición del barco: (" + position[0] + "," + position[1] + ") ");
                    System.out.println("Tamaño: " + dimensions[0] + "x" + dimensions[1]);
                    return;
                }
            }
        }
    }

    /**
     * Ejecuta el turno de la CPU con pausas realistas
     */
    private void turnoDelaCpu() {
        System.out.println("=== TURNO DE LA CPU ===");

        // Actualizar UI para mostrar que la CPU está "apuntando"
        updateGameStatus("La CPU está apuntando...", Color.DARKRED);
        updateTurnIndicator("Turno de la CPU - Apuntando", Color.DARKRED);

        // Usar un thread separado para el proceso completo de la CPU
        new Thread(() -> {
            try {
                // Pausa inicial para simular "apuntado"
                Thread.sleep(1500); // 1.5 segundos de apuntado

                boolean disparoExitoso = false;
                int intentos = 0;
                int maxIntentos = 100;

                while (!disparoExitoso && intentos < maxIntentos) {
                    // Generar coordenadas aleatorias
                    int col = cpuRandom.nextInt(Constants.GRID_COLS);
                    int row = cpuRandom.nextInt(Constants.GRID_ROWS);

                    // Verificar si ya se disparó a esa coordenada
                    if (!gameLogic.yaSeDisparo(col, row, 1)) { // 1 = CPU

                        // Actualizar UI para mostrar que la CPU está disparando
                        Platform.runLater(() -> {
                            updateGameStatus("¡La CPU dispara a (" + col + ", " + row + ")!", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - ¡Disparando!", Color.DARKRED);
                        });

                        System.out.println("CPU dispara a coordenadas: (" + col + ", " + row + ")");

                        // Pausa breve para mostrar las coordenadas del disparo
                        Thread.sleep(1000); // 1 segundo para mostrar el disparo

                        // Realizar el disparo
                        String resultado = gameLogic.jugada(col, row, 1); // 1 = CPU

                        // Actualizar visualmente el tablero del jugador en el hilo principal
                        Platform.runLater(() -> {
                            actualizarTableroJugador(col, row, resultado);
                        });

                        // Pausa para mostrar el resultado del impacto
                        Thread.sleep(800); // 0.8 segundos para procesar el impacto

                        // Manejar el resultado en el hilo principal
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
                        isPlayerTurn = true; // Devolver turno al jugador por seguridad
                        updateTurnIndicator("Tu turno (Error de CPU)", Color.DARKGREEN);
                    });
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // En caso de interrupción, devolver el turno al jugador
                Platform.runLater(() -> {
                    isPlayerTurn = true;
                    updateGameStatus("Error en el turno de la CPU. Es tu turno.", Color.RED);
                    updateTurnIndicator("Tu turno", Color.DARKGREEN);
                });
            }
        }).start();
    }

    /**
     * MODIFICADO: Maneja el resultado del disparo de la CPU con verificación de fin de juego
     */
    private void manejarResultadoCpu(String resultado, int col, int row) {
        System.out.println("Resultado del disparo de la CPU: " + resultado);

        switch (resultado) {
            case "AGUA":
                updateGameStatus("¡La CPU falló en (" + col + ", " + row + ")! Preparando tu turno...", Color.BLUE);
                updateTurnIndicator("La CPU falló - Tu turno en breve", Color.DARKORANGE);
                System.out.println("¡CPU falló! El turno vuelve al jugador");

                // Pausa antes de devolver el turno al jugador
                new Thread(() -> {
                    try {
                        Thread.sleep(2000); // 2 segundos para procesar que la CPU falló

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
                updateGameStatus("¡La CPU te tocó un barco en (" + col + ", " + row + ")! CPU sigue jugando...", Color.DARKORANGE);
                updateTurnIndicator("Turno de la CPU - Te tocó", Color.DARKRED);
                System.out.println("¡CPU tocó un barco! CPU puede disparar de nuevo");

                // La CPU dispara de nuevo después de un delay más largo
                new Thread(() -> {
                    try {
                        Thread.sleep(2500); // 2.5 segundos para procesar el acierto

                        Platform.runLater(() -> {
                            updateGameStatus("La CPU prepara otro disparo...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Preparando siguiente disparo", Color.DARKRED);
                        });

                        Thread.sleep(1500); // 1.5 segundos adicionales de preparación

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
                updateGameStatus("¡La CPU hundió tu barco en (" + col + ", " + row + ")! CPU sigue jugando...", Color.DARKRED);
                updateTurnIndicator("Turno de la CPU - ¡Barco hundido!", Color.DARKRED);
                System.out.println("¡CPU hundió un barco! CPU puede disparar de nuevo");

                // La CPU dispara de nuevo después de un delay aún más largo para celebrar
                new Thread(() -> {
                    try {
                        Thread.sleep(3000); // 3 segundos para procesar el hundimiento

                        Platform.runLater(() -> {
                            updateGameStatus("La CPU celebra y prepara otro disparo...", Color.DARKRED);
                            updateTurnIndicator("Turno de la CPU - Celebrando victoria", Color.DARKRED);
                        });

                        Thread.sleep(2000); // 2 segundos adicionales de "celebración"

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

            case "VICTORIA_CPU": // NUEVO: Manejar victoria de la CPU
                manejarFinDelJuego("VICTORIA_CPU");
                break;

            default:
                System.out.println("Error: Resultado desconocido de la CPU - " + resultado);
                updateGameStatus("Error en el turno de la CPU", Color.RED);
                isPlayerTurn = true; // Devolver turno al jugador por seguridad
                updateTurnIndicator("Tu turno (Error de CPU)", Color.DARKGREEN);
                break;
        }
    }

    /**
     * Actualiza el indicador de turno
     */
    private void updateTurnIndicator(String message, Color color) {
        turnIndicatorLabel.setText(message);
        turnIndicatorLabel.setTextFill(color);
        turnIndicatorLabel.setVisible(true);
    }

    /**
     * Actualiza el mensaje de estado del juego
     */
    private void updateGameStatus(String message, Color color) {
        gameStatusLabel.setText(message);
        gameStatusLabel.setTextFill(color);
        gameStatusLabel.setVisible(true);
    }

    /**
     * Crea y configura el botón de iniciar juego
     */
    private void createStartGameButton() {
        startGameButton = new Button("INICIAR JUEGO");
        startGameButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        startGameButton.setPrefSize(200, 40);

        double buttonX = BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) - 100;
        double buttonY = BOARD_START_Y + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 85;

        startGameButton.setTranslateX(buttonX);
        startGameButton.setTranslateY(buttonY);

        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #cccccc; -fx-text-fill: #666666;");

        startGameButton.setOnAction(event -> {
            startGame();
        });
    }

    /**
     * Verifica si todos los barcos están colocados en el tablero del jugador
     */
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

    /**
     * Inicia el juego y configura el sistema de turnos
     */
    private void startGame() {
        gameStarted = true;

        // Inicializar los barcos del jugador en la lógica
        gameLogic.inicializarBarcosJugador(playerShips);

        // Posicionar automáticamente los barcos de la CPU
        gameLogic.posicionarBarcosCpu();

        // Deshabilitar el movimiento de todos los barcos
        for (DraggableShape ship : playerShips) {
            ship.disableDragging();
        }

        // Cambiar el texto y deshabilitar el botón de inicio
        startGameButton.setText("JUEGO INICIADO");
        startGameButton.setDisable(true);
        startGameButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");

        // Mostrar el botón de visualización de barcos CPU
        showCpuShipsButton.setVisible(true);

        // Actualizar mensajes
        statusLabel.setText("¡Juego iniciado! Sistema de turnos activado. Puedes ver los barcos de la CPU antes de tu primer disparo.");
        statusLabel.setTextFill(Color.DARKBLUE);

        updateGameStatus("¡Juego iniciado! Haz clic en el tablero enemigo para disparar.", Color.DARKGREEN);
        updateTurnIndicator("Tu turno", Color.DARKGREEN);

        // Configurar estado inicial de turnos
        isPlayerTurn = true;
        firstPlayerMove = true;

        System.out.println("=== JUEGO INICIADO CON SISTEMA COMPLETO ===");
        System.out.println("✓ Los barcos han sido bloqueados");
        System.out.println("✓ Los barcos de la CPU han sido posicionados");
        System.out.println("✓ Sistema de turnos activado");
        System.out.println("✓ Sistema de fin de juego activado");
        System.out.println("✓ Botón de visualización de barcos CPU disponible");
        System.out.println("✓ Es el turno del jugador");
        System.out.println();
        System.out.println("CONDICIONES DE VICTORIA:");
        System.out.println("• El jugador gana si hunde todos los barcos de la CPU");
        System.out.println("• La CPU gana si hunde todos los barcos del jugador");
        System.out.println("• Al finalizar el juego, se muestra una pantalla de victoria/derrota");
        System.out.println("• Se revela la posición de todos los barcos de la CPU");
        System.out.println("• El botón 'NUEVA PARTIDA' permite reiniciar el juego completamente");
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

    // Métodos estáticos para que otras clases accedan a las coordenadas exactas
    public static double getPlayerBoardStartX() {
        return BOARD_START_X;
    }

    public static double getPlayerBoardStartY() {
        return BOARD_START_Y;
    }

    public static double getCpuBoardStartX() {
        return BOARD_START_X + (Constants.GRID_COLS * Constants.CELL_SIZE) + BOARD_SPACING;
    }

    public static double getCpuBoardStartY() {
        return BOARD_START_Y;
    }

    public static void main(String[] args) {
        launch(args);
    }
}