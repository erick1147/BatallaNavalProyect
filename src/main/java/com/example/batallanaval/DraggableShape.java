// DraggableShape.java - Versión con Callback, Control de Arrastre y Sistema de Overlays
package com.example.batallanaval;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.HashMap;
import java.util.Map;

public class DraggableShape {

    private Group group;
    public Rectangle backgroundRect;
    private ImageView imageView;

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    private boolean isVertical = false;
    private boolean draggingEnabled = true; // Control para habilitar/deshabilitar arrastre

    // Almacenar las dimensiones originales para cálculos
    private final double originalWidth;
    private final double originalHeight;

    // Dimensiones en celdas para diferentes tipos de barcos
    private final int widthCells;
    private final int heightCells;

    // Referencia a la lógica del juego para manejar colisiones
    private Logic gameLogic;

    // Callback para notificar cambios de posición
    private Runnable positionChangeCallback;

    // Coordenadas actuales en la cuadrícula
    private int currentGridCol = -1;
    private int currentGridRow = -1;

    // Posición inicial para poder regresar si es necesario
    private double initialX;
    private double initialY;

    // NUEVO: Sistema de overlays para mostrar impactos
    private Map<String, Rectangle> cellOverlays = new HashMap<>();

    public DraggableShape(double widthPx, double heightPx, String imagePath, Logic logic, Runnable callback) {
        // Guardar dimensiones originales
        this.originalWidth = widthPx;
        this.originalHeight = heightPx;
        this.gameLogic = logic;
        this.positionChangeCallback = callback;

        // Calcular dimensiones en celdas basándose en las dimensiones en píxeles
        this.widthCells = (int) (widthPx / Constants.CELL_SIZE);
        this.heightCells = (int) (heightPx / Constants.CELL_SIZE);

        backgroundRect = new Rectangle(0, 0, widthPx, heightPx);
        backgroundRect.setFill(Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.8));
        backgroundRect.setStroke(Color.DARKGRAY);
        backgroundRect.setStrokeWidth(2);
        backgroundRect.setArcWidth(10);
        backgroundRect.setArcHeight(10);

        Image image = new Image(imagePath);
        imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(widthPx - 5);
        imageView.setFitHeight(heightPx - 5);
        imageView.setLayoutX(5);
        imageView.setLayoutY(5);

        group = new Group();
        group.getChildren().addAll(backgroundRect, imageView);

        // NUEVO: Crear overlays para cada celda del barco
        createCellOverlays();

        group.setTranslateX(0);
        group.setTranslateY(0);

        setupDragHandlers();
        setupRotationHandler();
    }

    /**
     * NUEVO: Crea los overlays transparentes para cada celda del barco
     */
    private void createCellOverlays() {
        int currentWidthCells = isVertical ? heightCells : widthCells;
        int currentHeightCells = isVertical ? widthCells : heightCells;

        for (int cellRow = 0; cellRow < currentHeightCells; cellRow++) {
            for (int cellCol = 0; cellCol < currentWidthCells; cellCol++) {
                String cellKey = cellCol + "," + cellRow;

                Rectangle overlay = new Rectangle(
                        cellCol * Constants.CELL_SIZE,
                        cellRow * Constants.CELL_SIZE,
                        Constants.CELL_SIZE,
                        Constants.CELL_SIZE
                );

                overlay.setFill(Color.TRANSPARENT);
                overlay.setStroke(Color.TRANSPARENT);
                overlay.setMouseTransparent(true); // No interfiere con los eventos del barco

                cellOverlays.put(cellKey, overlay);
                group.getChildren().add(overlay);
            }
        }
    }

    /**
     * NUEVO: Actualiza los overlays cuando el barco cambia de orientación
     */
    private void updateCellOverlays() {
        // Remover overlays existentes
        for (Rectangle overlay : cellOverlays.values()) {
            group.getChildren().remove(overlay);
        }
        cellOverlays.clear();

        // Crear nuevos overlays con la orientación actualizada
        createCellOverlays();
    }

    /**
     * NUEVO: Marca una celda específica del barco como impactada
     * @param globalCol Columna global en el tablero
     * @param globalRow Fila global en el tablero
     * @param impactType "TOCADO" = amarillo, "HUNDIDO" = rojo
     */
    public void markCellImpact(int globalCol, int globalRow, String impactType) {
        if (currentGridCol < 0 || currentGridRow < 0) {
            return; // El barco no está colocado en el tablero
        }

        // Calcular la posición relativa de la celda dentro del barco
        int relativeCellCol = globalCol - currentGridCol;
        int relativeCellRow = globalRow - currentGridRow;

        // Verificar que la celda esté dentro del rango del barco
        int currentWidthCells = isVertical ? heightCells : widthCells;
        int currentHeightCells = isVertical ? widthCells : heightCells;

        if (relativeCellCol >= 0 && relativeCellCol < currentWidthCells &&
                relativeCellRow >= 0 && relativeCellRow < currentHeightCells) {

            String cellKey = relativeCellCol + "," + relativeCellRow;
            Rectangle overlay = cellOverlays.get(cellKey);

            if (overlay != null) {
                Color impactColor;
                switch (impactType) {
                    case "TOCADO":
                        impactColor = Color.YELLOW.deriveColor(1, 1, 1, 0.8);
                        break;
                    case "HUNDIDO":
                        impactColor = Color.RED.deriveColor(1, 1, 1, 0.8);
                        break;
                    default:
                        impactColor = Color.TRANSPARENT;
                        break;
                }

                overlay.setFill(impactColor);
                System.out.println("✓ Overlay aplicado en celda relativa (" + relativeCellCol + "," + relativeCellRow +
                        ") del barco - Color: " + impactType);
            }
        }
    }

    /**
     * NUEVO: Marca todo el barco como hundido (todas las celdas en rojo)
     */
    public void markShipAsDestroyed() {
        for (Rectangle overlay : cellOverlays.values()) {
            overlay.setFill(Color.RED.deriveColor(1, 1, 1, 0.8));
        }
        System.out.println("✗ Barco completamente marcado como destruido con overlays rojos");
    }

    private void setupDragHandlers() {
        group.setOnMousePressed(event -> {
            if (!draggingEnabled) {
                System.out.println("El juego ya ha comenzado. Los barcos no se pueden mover.");
                return;
            }

            if (event.getButton() == MouseButton.PRIMARY) {
                orgSceneX = event.getSceneX();
                orgSceneY = event.getSceneY();
                orgTranslateX = group.getTranslateX();
                orgTranslateY = group.getTranslateY();
                group.toFront();

                // Liberar las celdas ocupadas cuando se comienza a arrastrar
                if (currentGridCol >= 0 && currentGridRow >= 0) {
                    int currentWidthCells = isVertical ? heightCells : widthCells;
                    int currentHeightCells = isVertical ? widthCells : heightCells;
                    gameLogic.removeShip(currentGridCol, currentGridRow, currentWidthCells, currentHeightCells);
                    System.out.println("Liberando celdas del barco en posición: (" + currentGridCol + ", " + currentGridRow + ")");

                    // Actualizar coordenadas actuales
                    currentGridCol = -1;
                    currentGridRow = -1;

                    // Notificar el cambio de posición
                    if (positionChangeCallback != null) {
                        positionChangeCallback.run();
                    }
                }

                event.consume();
            }
        });

        group.setOnMouseDragged(event -> {
            if (!draggingEnabled) {
                return;
            }

            if (event.getButton() == MouseButton.PRIMARY) {
                double offsetX = event.getSceneX() - orgSceneX;
                double offsetY = event.getSceneY() - orgSceneY;
                double newTranslateX = orgTranslateX + offsetX;
                double newTranslateY = orgTranslateY + offsetY;

                group.setTranslateX(newTranslateX);
                group.setTranslateY(newTranslateY);
                event.consume();
            }
        });

        group.setOnMouseReleased(event -> {
            if (!draggingEnabled) {
                return;
            }

            if (event.getButton() == MouseButton.PRIMARY) {
                snapToPlayerBoardOnly();
                event.consume();
            }
        });
    }

    private void setupRotationHandler() {
        group.setOnMouseClicked(event -> {
            if (!draggingEnabled) {
                System.out.println("El juego ya ha comenzado. Los barcos no se pueden rotar.");
                return;
            }

            if (event.getButton() == MouseButton.SECONDARY) {
                // Liberar celdas actuales antes de rotar
                if (currentGridCol >= 0 && currentGridRow >= 0) {
                    int currentWidthCells = isVertical ? heightCells : widthCells;
                    int currentHeightCells = isVertical ? widthCells : heightCells;
                    gameLogic.removeShip(currentGridCol, currentGridRow, currentWidthCells, currentHeightCells);
                }

                // Guardar posición actual para mantener el barco en la misma zona
                double currentX = group.getTranslateX();
                double currentY = group.getTranslateY();

                isVertical = !isVertical;

                // Intercambiar dimensiones del rectángulo
                double tempWidth = backgroundRect.getWidth();
                backgroundRect.setWidth(backgroundRect.getHeight());
                backgroundRect.setHeight(tempWidth);

                // Intercambiar dimensiones de la imagen
                double tempImageWidth = imageView.getFitWidth();
                imageView.setFitWidth(imageView.getFitHeight());
                imageView.setFitHeight(tempImageWidth);

                // Centrar la imagen en el rectángulo rotado
                imageView.setLayoutX((backgroundRect.getWidth() - imageView.getFitWidth()) / 2);
                imageView.setLayoutY((backgroundRect.getHeight() - imageView.getFitHeight()) / 2);

                // NUEVO: Actualizar overlays después de la rotación
                updateCellOverlays();

                // Mantener la posición y reacomodar
                group.setTranslateX(currentX);
                group.setTranslateY(currentY);

                snapToPlayerBoardOnly();
                System.out.println("Barco rotado. Nueva orientación: " + (isVertical ? "Vertical" : "Horizontal"));
                event.consume();
            }
        });
    }

    private void snapToPlayerBoardOnly() {
        double currentTranslateX = group.getTranslateX();
        double currentTranslateY = group.getTranslateY();

        // USAR LAS COORDENADAS EXACTAS DEL MAINAPP
        double playerBoardStartX = MainApp.getPlayerBoardStartX();
        double playerBoardStartY = MainApp.getPlayerBoardStartY();
        double playerBoardEndX = playerBoardStartX + (Constants.GRID_COLS * Constants.CELL_SIZE);
        double playerBoardEndY = playerBoardStartY + (Constants.GRID_ROWS * Constants.CELL_SIZE);

        System.out.println("=== DEBUGGING SNAP ===");
        System.out.println("Posición actual del barco: (" + currentTranslateX + ", " + currentTranslateY + ")");
        System.out.println("Área del tablero: X(" + playerBoardStartX + " - " + playerBoardEndX + ") Y(" + playerBoardStartY + " - " + playerBoardEndY + ")");

        // Verificar si el barco está sobre el tablero del jugador
        if (currentTranslateX < playerBoardStartX ||
                currentTranslateX + getCurrentWidth() > playerBoardEndX ||
                currentTranslateY < playerBoardStartY ||
                currentTranslateY + getCurrentHeight() > playerBoardEndY) {
            System.out.println("El barco está fuera del área del tablero del jugador");
            returnToValidPosition();
            return;
        }

        // Calcular posición relativa al tablero usando las coordenadas exactas
        double boardRelativeX = currentTranslateX - playerBoardStartX;
        double boardRelativeY = currentTranslateY - playerBoardStartY;

        System.out.println("Posición relativa al tablero: (" + boardRelativeX + ", " + boardRelativeY + ")");

        // Calcular la celda más cercana para snap
        int snapCol = (int) Math.round(boardRelativeX / Constants.CELL_SIZE);
        int snapRow = (int) Math.round(boardRelativeY / Constants.CELL_SIZE);

        System.out.println("Celda calculada para snap: (" + snapCol + ", " + snapRow + ")");

        // Obtener dimensiones actuales en celdas considerando orientación
        int currentWidthCells = isVertical ? heightCells : widthCells;
        int currentHeightCells = isVertical ? widthCells : heightCells;

        // Asegurar que el barco no se salga del tablero después del snap
        if (snapCol + currentWidthCells > Constants.GRID_COLS) {
            snapCol = Constants.GRID_COLS - currentWidthCells;
        }
        if (snapRow + currentHeightCells > Constants.GRID_ROWS) {
            snapRow = Constants.GRID_ROWS - currentHeightCells;
        }
        if (snapCol < 0) snapCol = 0;
        if (snapRow < 0) snapRow = 0;

        System.out.println("Celda ajustada para snap: (" + snapCol + ", " + snapRow + ")");

        // Buscar la posición válida más cercana (verificar colisiones)
        int[] validPosition = gameLogic.findNearestValidPosition(snapCol, snapRow, currentWidthCells, currentHeightCells);

        if (validPosition != null) {
            int finalCol = validPosition[0];
            int finalRow = validPosition[1];

            // CÁLCULO EXACTO DE COORDENADAS FINALES
            double snappedX = playerBoardStartX + (finalCol * Constants.CELL_SIZE);
            double snappedY = playerBoardStartY + (finalRow * Constants.CELL_SIZE);

            System.out.println("Posición final calculada: (" + snappedX + ", " + snappedY + ")");

            // Posicionar el barco perfectamente alineado con las celdas
            group.setTranslateX(snappedX);
            group.setTranslateY(snappedY);

            // Marcar las celdas como ocupadas
            gameLogic.placeShip(finalCol, finalRow, currentWidthCells, currentHeightCells);

            // Actualizar coordenadas actuales
            currentGridCol = finalCol;
            currentGridRow = finalRow;

            // Obtener y mostrar todas las coordenadas ocupadas
            int[][] occupiedCoordinates = gameLogic.getShipCoordinates(finalCol, finalRow, currentWidthCells, currentHeightCells);
            System.out.println("✓ Barco colocado exitosamente en posición: (" + finalCol + ", " + finalRow + ")");
            System.out.print("Coordenadas ocupadas: ");
            for (int i = 0; i < occupiedCoordinates.length; i++) {
                System.out.print("(" + occupiedCoordinates[i][0] + "," + occupiedCoordinates[i][1] + ")");
                if (i < occupiedCoordinates.length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
            System.out.println("Posición final en píxeles: (" + snappedX + ", " + snappedY + ")");
            System.out.println("=== FIN DEBUGGING SNAP ===");

            // Notificar el cambio de posición
            if (positionChangeCallback != null) {
                positionChangeCallback.run();
            }
        } else {
            System.out.println("¡No hay espacio disponible para colocar el barco en esa posición!");
            returnToValidPosition();
        }
    }

    /**
     * Devuelve el barco a una posición válida dentro del tablero del jugador
     */
    private void returnToValidPosition() {
        // Obtener dimensiones actuales en celdas considerando orientación
        int currentWidthCells = isVertical ? heightCells : widthCells;
        int currentHeightCells = isVertical ? widthCells : heightCells;

        // Buscar cualquier posición válida en el tablero
        int[] validPosition = gameLogic.findNearestValidPosition(0, 0, currentWidthCells, currentHeightCells);

        if (validPosition != null) {
            int finalCol = validPosition[0];
            int finalRow = validPosition[1];

            // Usar las mismas coordenadas exactas que en snapToPlayerBoardOnly
            double snappedX = MainApp.getPlayerBoardStartX() + (finalCol * Constants.CELL_SIZE);
            double snappedY = MainApp.getPlayerBoardStartY() + (finalRow * Constants.CELL_SIZE);

            group.setTranslateX(snappedX);
            group.setTranslateY(snappedY);

            gameLogic.placeShip(finalCol, finalRow, currentWidthCells, currentHeightCells);
            currentGridCol = finalCol;
            currentGridRow = finalRow;

            System.out.println("Barco reubicado automáticamente en posición válida: (" + finalCol + ", " + finalRow + ")");
            System.out.println("Posición en píxeles: (" + snappedX + ", " + snappedY + ")");

            // Notificar el cambio de posición
            if (positionChangeCallback != null) {
                positionChangeCallback.run();
            }
        } else {
            // Si no hay posición válida, mover el barco fuera del tablero (debajo)
            double fallbackX = 50;
            double fallbackY = MainApp.getPlayerBoardStartY() + (Constants.GRID_ROWS * Constants.CELL_SIZE) + 40;
            group.setTranslateX(fallbackX);
            group.setTranslateY(fallbackY);
            currentGridCol = -1;
            currentGridRow = -1;
            System.out.println("No hay espacio en el tablero. Barco movido fuera del tablero.");

            // Notificar el cambio de posición
            if (positionChangeCallback != null) {
                positionChangeCallback.run();
            }
        }
    }

    /**
     * Establece la posición inicial del barco (para poder regresar si es necesario)
     */
    public void setInitialPosition(double x, double y) {
        this.initialX = x;
        this.initialY = y;
        group.setTranslateX(x);
        group.setTranslateY(y);
    }

    /**
     * Deshabilita el arrastre y rotación del barco (para cuando el juego comience)
     */
    public void disableDragging() {
        draggingEnabled = false;

        // Cambiar el estilo visual para indicar que el barco está bloqueado
        backgroundRect.setStroke(Color.DARKBLUE);
        backgroundRect.setStrokeWidth(3);
        backgroundRect.setFill(Color.LIGHTBLUE.deriveColor(1, 1, 1, 0.9));

        // Cambiar el cursor para indicar que no se puede arrastrar
        group.setStyle("-fx-cursor: default;");

        System.out.println("Barco bloqueado en posición: (" + currentGridCol + ", " + currentGridRow + ")");
    }

    /**
     * Habilita el arrastre y rotación del barco (para modo de configuración)
     */
    public void enableDragging() {
        draggingEnabled = true;

        // Restaurar el estilo visual original
        backgroundRect.setStroke(Color.DARKGRAY);
        backgroundRect.setStrokeWidth(2);
        backgroundRect.setFill(Color.LIGHTGRAY.deriveColor(1, 1, 1, 0.8));

        // Cambiar el cursor para indicar que se puede arrastrar
        group.setStyle("-fx-cursor: hand;");

        System.out.println("Barco desbloqueado - se puede mover libremente");
    }

    /**
     * Obtiene el ancho actual del barco considerando su orientación
     */
    private double getCurrentWidth() {
        return isVertical ? originalHeight : originalWidth;
    }

    /**
     * Obtiene la altura actual del barco considerando su orientación
     */
    private double getCurrentHeight() {
        return isVertical ? originalWidth : originalHeight;
    }

    /**
     * Método público para acceder al estado de orientación
     */
    public boolean isVertical() {
        return isVertical;
    }

    /**
     * Obtiene las dimensiones del barco en celdas considerando su orientación actual
     */
    public int[] getCurrentDimensionsInCells() {
        if (isVertical) {
            return new int[]{heightCells, widthCells}; // [ancho, alto] cuando está vertical
        } else {
            return new int[]{widthCells, heightCells}; // [ancho, alto] cuando está horizontal
        }
    }

    /**
     * Obtiene las coordenadas actuales del barco en la cuadrícula
     */
    public int[] getCurrentGridPosition() {
        return new int[]{currentGridCol, currentGridRow};
    }

    /**
     * Verifica si el barco está actualmente colocado en el tablero
     */
    public boolean isPlacedOnBoard() {
        return currentGridCol >= 0 && currentGridRow >= 0;
    }

    /**
     * Obtiene el estado del arrastre
     */
    public boolean isDraggingEnabled() {
        return draggingEnabled;
    }

    public Node getNode() {
        return group;
    }
}
