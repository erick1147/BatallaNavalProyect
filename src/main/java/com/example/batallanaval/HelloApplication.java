// MainApp.java
package com.example.batallanaval; // Mismo paquete que Constants.java y DraggableShape.java

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class HelloApplication extends Application {

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Configuración del GridPane (Tablero) ---
        GridPane gridPane = new GridPane();
        gridPane.setPrefSize(Constants.GRID_COLS * Constants.CELL_SIZE, Constants.GRID_ROWS * Constants.CELL_SIZE);
        gridPane.setGridLinesVisible(true); // Visualiza las líneas de la cuadrícula

        // Opcional: Rellenar el GridPane con Panes para cada celda
        for (int row = 0; row < Constants.GRID_ROWS; row++) {
            for (int col = 0; col < Constants.GRID_COLS; col++) {
                Pane cellPane = new Pane();
                cellPane.setPrefSize(Constants.CELL_SIZE, Constants.CELL_SIZE)  ;
                cellPane.setStyle("-fx-border-color: lightgray; -fx-border-width: 0.5;");
                gridPane.add(cellPane, col, row);
            }
        }

        // --- 2. Crear una instancia de DraggableShape ---
        DraggableShape myDraggableShape = new DraggableShape();

        // --- 3. Añadir el GridPane y el DraggableShape al Pane raíz ---
        // El Pane raíz permite posicionar hijos con setTranslateX/Y
        Pane root = new Pane();
        root.getChildren().addAll(gridPane, myDraggableShape.getNode()); // GridPane primero para que DraggableShape esté encima

        // --- 4. Configuración de la Escena y el Stage ---
        Scene scene = new Scene(root, Constants.GRID_COLS * Constants.CELL_SIZE, Constants.GRID_ROWS * Constants.CELL_SIZE);
        primaryStage.setTitle("Draggable Shape on GridPane");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}