package com.example.batallanaval;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

public class Ship implements Serializable {
    // AÑADIDO: serialVersionUID para una serialización segura.
    private static final long serialVersionUID = 1L;

    // CORREGIDO: Usar List en lugar de Map para almacenar múltiples coordenadas
    List<int[]> coordinates = new ArrayList<>();
    private int state = 0; // 0 = intacto, 1 = dañado, 2 = hundido

    /**
     * Constructor por defecto
     */
    public Ship() {
        // La ArrayList ya está inicializada arriba
    }

    /**
     * Obtiene el estado actual del barco
     * @return 0 = intacto, 1 = dañado, 2 = hundido
     */
    public int getState() {
        return state;
    }

    /**
     * Establece el estado del barco
     * @param state 0 = intacto, 1 = dañado, 2 = hundido
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * CORREGIDO: Obtiene las coordenadas del barco como List
     * @return List con arrays [col, row] de las coordenadas del barco
     */
    public List<int[]> getCoordinates() {
        return coordinates;
    }

    /**
     * Obtiene el número de segmentos del barco
     * @return Número total de coordenadas que ocupa el barco
     */
    public int getSize() {
        return coordinates.size();
    }

    /**
     * CORREGIDO: Verifica si el barco contiene una coordenada específica
     * @param col Columna a verificar
     * @param row Fila a verificar
     * @return true si el barco ocupa esa coordenada
     */
    public boolean containsCoordinate(int col, int row) {
        for (int[] coordinate : coordinates) {
            int shipCol = coordinate[0]; // columna del barco
            int shipRow = coordinate[1]; // fila del barco

            if (shipCol == col && shipRow == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * CORREGIDO: Añade una coordenada al barco
     * @param col Columna
     * @param row Fila
     */
    public void addCoordinate(int col, int row) {
        coordinates.add(new int[]{col, row});
    }

    /**
     * Limpia todas las coordenadas del barco
     */
    public void clearCoordinates() {
        coordinates.clear();
    }

    /**
     * NUEVO: Método de compatibilidad para obtener coordenadas como Map (para el código existente)
     * @return Map simulado con las coordenadas del barco
     */
    public java.util.Map<Integer, Integer> getCoordinatesAsMap() {
        java.util.Map<Integer, Integer> map = new java.util.HashMap<>();
        for (int i = 0; i < coordinates.size(); i++) {
            int[] coord = coordinates.get(i);
            // Usar el índice como clave para evitar sobrescribir
            map.put(i, coord[0] * 100 + coord[1]); // Codificar col y row en un entero
        }
        return map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ship{state=").append(state);
        sb.append(", coordinates=[");

        boolean first = true;
        for (int[] coordinate : coordinates) {
            if (!first) {
                sb.append(", ");
            }
            sb.append("(").append(coordinate[0]).append(",").append(coordinate[1]).append(")");
            first = false;
        }

        sb.append("]}");
        return sb.toString();
    }
}

/* public class RecorrerMapEntrySet {
    public static void main(String[] args) {
        Map<String, Integer> edades = new HashMap<>();
        edades.put("Ana", 25);
        edades.put("Juan", 30);
        edades.put("María", 28);

        // Usando un for-each para recorrer el conjunto de entradas
        for (Map.Entry<String, Integer> entrada : edades.entrySet()) {
            String nombre = entrada.getKey();
            Integer edad = entrada.getValue();
            System.out.println("Nombre: " + nombre + ", Edad: " + edad);
        }
    }
}*/
