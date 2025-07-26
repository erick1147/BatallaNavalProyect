// Constants.java
package com.example.batallanaval;

public class Constants {
    public static final double CELL_SIZE = 100.0;
    public static final int GRID_ROWS = 10;
    public static final int GRID_COLS = 10;

    // Dimensiones DEL GRUPO EN SU ORIENTACIÓN HORIZONTAL POR DEFECTO (2x1 celdas)
    public static final double DEFAULT_DRAGGABLE_WIDTH_CELLS = 2.0; // Ancho por defecto en celdas
    public static final double DEFAULT_DRAGGABLE_HEIGHT_CELLS = 1.0; // Alto por defecto en celdas

    public static final double DEFAULT_DRAGGABLE_GROUP_WIDTH = CELL_SIZE * DEFAULT_DRAGGABLE_WIDTH_CELLS;
    public static final double DEFAULT_DRAGGABLE_GROUP_HEIGHT = CELL_SIZE * DEFAULT_DRAGGABLE_HEIGHT_CELLS;
}