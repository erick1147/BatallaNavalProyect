package com.example.batallanaval.vista;

import java.util.ArrayList;
import java.util.List;
import com.example.batallanaval.modelo.Constants;
import com.example.batallanaval.modelo.GameLogic;

public class PlayerShipManager {

    private final String shipImagePath = "/ship_part.png";

    /**
     * Crea todas las instancias de los barcos del jugador y los posiciona inicialmente
     * debajo de los tableros de juego usando las coordenadas exactas.
     *
     * @param gameLogic Instancia de la lógica del juego para manejar colisiones
     * @param positionChangeCallback Callback que se ejecuta cuando un barco cambia de posición
     * @return Una lista de objetos DraggableShape que representan los barcos del jugador
     */
    public List<DraggableShape> createAndPositionShips(GameLogic gameLogic, Runnable positionChangeCallback) {
        List<DraggableShape> playerShips = new ArrayList<>();

        System.out.println("=== CREANDO BARCOS DEL JUGADOR ===");

        // Crear cada tipo de barco pasando la referencia a gameLogic y el callback

        // 1 Portaaviones (ocupa 4 casillas)
        DraggableShape carrier = new DraggableShape(
                Constants.CARRIER_GROUP_WIDTH,
                Constants.CARRIER_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        playerShips.add(carrier);
        System.out.println("✓ Portaaviones creado (4 casillas)");

        // 2 Submarinos (ocupan 3 casillas cada uno)
        DraggableShape submarine1 = new DraggableShape(
                Constants.SUBMARINE_GROUP_WIDTH,
                Constants.SUBMARINE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape submarine2 = new DraggableShape(
                Constants.SUBMARINE_GROUP_WIDTH,
                Constants.SUBMARINE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        playerShips.add(submarine1);
        playerShips.add(submarine2);
        System.out.println("✓ 2 Submarinos creados (3 casillas cada uno)");

        // 3 Destructores (ocupan 2 casillas cada uno)
        DraggableShape destroyer1 = new DraggableShape(
                Constants.DESTROYER_GROUP_WIDTH,
                Constants.DESTROYER_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape destroyer2 = new DraggableShape(
                Constants.DESTROYER_GROUP_WIDTH,
                Constants.DESTROYER_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape destroyer3 = new DraggableShape(
                Constants.DESTROYER_GROUP_WIDTH,
                Constants.DESTROYER_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        playerShips.add(destroyer1);
        playerShips.add(destroyer2);
        playerShips.add(destroyer3);
        System.out.println("✓ 3 Destructores creados (2 casillas cada uno)");

        // 4 Fragatas (ocupan 1 casilla cada una)
        DraggableShape frigate1 = new DraggableShape(
                Constants.FRIGATE_GROUP_WIDTH,
                Constants.FRIGATE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape frigate2 = new DraggableShape(
                Constants.FRIGATE_GROUP_WIDTH,
                Constants.FRIGATE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape frigate3 = new DraggableShape(
                Constants.FRIGATE_GROUP_WIDTH,
                Constants.FRIGATE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        DraggableShape frigate4 = new DraggableShape(
                Constants.FRIGATE_GROUP_WIDTH,
                Constants.FRIGATE_GROUP_HEIGHT,
                shipImagePath,
                gameLogic,
                positionChangeCallback
        );
        playerShips.add(frigate1);
        playerShips.add(frigate2);
        playerShips.add(frigate3);
        playerShips.add(frigate4);
        System.out.println("✓ 4 Fragatas creadas (1 casilla cada una)");

        // Posicionamiento inicial de los barcos DEBAJO de los tableros usando coordenadas exactas
        positionShipsBelowBoards(playerShips);

        System.out.println("✓ Total de barcos creados: " + playerShips.size());
        System.out.println("✓ Total de celdas que deben ocupar: 4+3+3+2+2+2+1+1+1+1 = 20 celdas");
        System.out.println("=== BARCOS LISTOS PARA COLOCAR ===");

        return playerShips;
    }

    /**
     * Posiciona los barcos debajo de los tableros de juego en filas organizadas
     * usando las coordenadas exactas de Constants
     * @param playerShips Lista de barcos a posicionar
     */
    private void positionShipsBelowBoards(List<DraggableShape> playerShips) {
        // Usar las coordenadas exactas de Constants
        double boardStartX = Constants.BOARD_START_X;
        double boardStartY = Constants.BOARD_START_Y;

        // Calcular la posición Y donde empezarán los barcos (debajo de los tableros)
        double boardHeight = Constants.GRID_ROWS * Constants.CELL_SIZE;
        double startY = boardStartY + boardHeight + 140; // 140 píxeles de margen para botón y etiquetas

        // Posición X inicial (alineada con el área de los tableros)
        double startX = boardStartX;

        // Variables para controlar el posicionamiento en filas
        double currentX = startX;
        double currentY = startY;
        double rowHeight = 0;
        double maxRowWidth = (Constants.GRID_COLS * Constants.CELL_SIZE * 2) + 20; // Ancho disponible para ambos tableros

        System.out.println("=== POSICIONAMIENTO INICIAL DE BARCOS ===");
        System.out.println("Área de tableros: Inicio X=" + boardStartX + ", Y=" + boardStartY);
        System.out.println("Área de barcos: Inicio X=" + startX + ", Y=" + startY);
        System.out.println("Ancho máximo disponible: " + maxRowWidth);

        for (int i = 0; i < playerShips.size(); i++) {
            DraggableShape ship = playerShips.get(i);
            double shipWidth = ship.backgroundRect.getWidth();
            double shipHeight = ship.backgroundRect.getHeight();

            // Verificar si el barco cabe en la fila actual
            if (currentX + shipWidth > startX + maxRowWidth && currentX > startX) {
                // Pasar a la siguiente fila
                currentX = startX;
                currentY += rowHeight + 15; // 15 píxeles de separación entre filas
                rowHeight = 0;
            }

            // Posicionar el barco
            ship.setInitialPosition(currentX, currentY);

            // Actualizar posición para el siguiente barco
            currentX += shipWidth + 20; // 20 píxeles de separación horizontal
            rowHeight = Math.max(rowHeight, shipHeight);

            // Determinar tipo de barco para logging
            String shipType = "";
            if (shipWidth == Constants.CARRIER_GROUP_WIDTH) {
                shipType = "Portaaviones";
            } else if (shipWidth == Constants.SUBMARINE_GROUP_WIDTH) {
                shipType = "Submarino";
            } else if (shipWidth == Constants.DESTROYER_GROUP_WIDTH) {
                shipType = "Destructor";
            } else if (shipWidth == Constants.FRIGATE_GROUP_WIDTH) {
                shipType = "Fragata";
            }

            System.out.println("Barco " + (i+1) + " (" + shipType + ") posicionado en: (" +
                    currentX + ", " + currentY + ") - Tamaño: " + shipWidth + "x" + shipHeight);
        }


    }
}