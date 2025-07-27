package com.example.batallanaval.modelo;

/**
 * La clase `Constants` actúa como un repositorio centralizado para todos los valores fijos y predefinidos
 * (constantes) que se utilizan a lo largo del programa de Batalla Naval. Su propósito principal es
 * mejorar la legibilidad del código, facilitar su mantenimiento y permitir modificaciones globales de manera eficiente.
 * Al definir estos valores en un solo lugar, cualquier cambio en una dimensión o característica del juego
 * se propaga automáticamente a todas las clases que los utilizan, evitando errores y duplicidad de código.
 */
public class Constants {

    // Paso 1: Definición del Tamaño Base de la Cuadrícula
    // CELL_SIZE: Esta constante define el tamaño en píxeles de cada celda individual en el tablero de juego.
    // Es fundamental porque todas las demás dimensiones (barcos, tableros) se calculan en base a este valor.
    // Un cambio aquí ajustará proporcionalmente el tamaño de todo el juego.
    public static final double CELL_SIZE = 70.0;

    // GRID_ROWS y GRID_COLS: Estas constantes determinan las dimensiones de la cuadrícula del tablero.
    // Definen el número de filas y columnas que tendrá cada tablero de Batalla Naval.
    public static final int GRID_ROWS = 10;
    public static final int GRID_COLS = 10;

    // Paso 2: Dimensiones de los Barcos en Unidades de Celda
    // Estas constantes especifican el tamaño que ocupa cada tipo de barco en términos de celdas de la cuadrícula,
    // asumiendo su orientación horizontal por defecto (ancho x alto en celdas).
    // Esto es útil para la lógica del juego, como la verificación de espacio en el tablero.
    public static final double DESTROYER_WIDTH_CELLS = 2.0;
    public static final double DESTROYER_HEIGHT_CELLS = 1.0;

    public static final double CARRIER_WIDTH_CELLS = 4.0;
    public static final double CARRIER_HEIGHT_CELLS = 1.0;

    public static final double SUBMARINE_WIDTH_CELLS = 3.0;
    public static final double SUBMARINE_HEIGHT_CELLS = 1.0;

    public static final double FRIGATE_WIDTH_CELLS = 1.0;
    public static final double FRIGATE_HEIGHT_CELLS = 1.0;

    // Paso 3: Dimensiones de los Barcos en Píxeles
    // Estas constantes calculan el tamaño real en píxeles de cada tipo de barco.
    // Se derivan de las dimensiones en celdas multiplicadas por el CELL_SIZE.
    // Son utilizadas por las clases que dibujan o manipulan visualmente los barcos (ej., DraggableShape).
    public static final double DESTROYER_GROUP_WIDTH = CELL_SIZE * DESTROYER_WIDTH_CELLS;
    public static final double DESTROYER_GROUP_HEIGHT = CELL_SIZE * DESTROYER_HEIGHT_CELLS;

    public static final double CARRIER_GROUP_WIDTH = CELL_SIZE * CARRIER_WIDTH_CELLS;
    public static final double CARRIER_GROUP_HEIGHT = CELL_SIZE * CARRIER_HEIGHT_CELLS;

    public static final double SUBMARINE_GROUP_WIDTH = CELL_SIZE * SUBMARINE_WIDTH_CELLS;
    public static final double SUBMARINE_GROUP_HEIGHT = CELL_SIZE * SUBMARINE_HEIGHT_CELLS;

    public static final double FRIGATE_GROUP_WIDTH = CELL_SIZE * FRIGATE_WIDTH_CELLS;
    public static final double FRIGATE_GROUP_HEIGHT = CELL_SIZE * FRIGATE_HEIGHT_CELLS;

    // COORDENADAS EXACTAS DEL TABLERO - CRÍTICAS PARA ALINEACIÓN
    public static final double BOARD_START_X = 10.0;
    public static final double BOARD_START_Y = 30.0;
    public static final double BOARD_SPACING = 20.0;
}