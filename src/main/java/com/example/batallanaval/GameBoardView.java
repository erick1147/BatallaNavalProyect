// GameBoardView.java
package com.example.batallanaval;

import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * La clase `GameBoardView` es una componente de la interfaz de usuario que se encarga
 * de construir y configurar un tablero de juego visual en forma de cuadrícula (`GridPane`).
 * Su propósito principal es encapsular toda la lógica relacionada con la creación
 * de la representación gráfica del tablero, incluyendo sus dimensiones y el estilo de sus celdas.
 * Esto contribuye a la modularidad del código y sigue los principios de la arquitectura
 * Modelo-Vista-Controlador, donde esta clase representa la "Vista" del tablero.
 */
public class GameBoardView {

    // Variable privada y final para el GridPane, que será el tablero de juego real.
    private final GridPane gridPane;

    /**
     * Constructor de GameBoardView.
     * Este constructor es el responsable de inicializar y configurar el GridPane del tablero
     * con las dimensiones y estilos predefinidos en la clase `Constants`.
     */
    public GameBoardView() {
        // Paso 1: Inicialización del GridPane
        // Se crea una nueva instancia de GridPane, que será el contenedor para las celdas del tablero.
        gridPane = new GridPane();

        // Paso 2: Configuración del Tamaño del GridPane
        // Se establece el tamaño preferido del GridPane. Esto se calcula multiplicando
        // el número de columnas/filas por el tamaño de cada celda, asegurando que el tablero
        // tenga las dimensiones correctas según las constantes del juego.
        gridPane.setPrefSize(Constants.GRID_COLS * Constants.CELL_SIZE, Constants.GRID_ROWS * Constants.CELL_SIZE);

        // Paso 3: Visualización de las Líneas de la Cuadrícula
        // Se activa la visibilidad de las líneas de la cuadrícula. Esto ayuda a los jugadores
        // a distinguir claramente las celdas individuales del tablero.
        gridPane.setGridLinesVisible(true);

        // Paso 4: Rellenado del GridPane con Celdas Individuales
        // Se utilizan dos bucles anidados para iterar sobre cada fila y columna de la cuadrícula.
        // Por cada posición (columna, fila), se crea un 'Pane' individual que actúa como una celda.
        // Esto permite aplicar estilos de borde a cada celda y facilita futuras interacciones
        // (como detectar clics para ataques o posicionamiento).
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                Pane cellPane = new Pane();
                // Se establece el tamaño preferido de cada celda individual para que coincida con CELL_SIZE.
                cellPane.setPrefSize(Constants.CELL_SIZE, Constants.CELL_SIZE);
                // Se aplica un estilo CSS a cada celda para darle un borde negro, mejorando su visibilidad.
                cellPane.setStyle("-fx-border-color: black; -fx-border-width: 0.8;");
                // La celda (Pane) se añade al GridPane en su posición correspondiente.
                gridPane.add(cellPane, col, row);
            }
        }
    }

    /**
     * Método para obtener el GridPane configurado.
     * Permite que otras clases (como MainApp) accedan al GridPane que representa el tablero de juego
     * para añadirlo a la escena o interactuar con él.
     * @return El objeto GridPane que ha sido creado y configurado.
     */
    public GridPane getGridPane() {
        return this.gridPane;
    }
}
