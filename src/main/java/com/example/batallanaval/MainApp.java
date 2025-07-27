package com.example.batallanaval;

import javafx.application.Application;
import javafx.stage.Stage;
import com.example.batallanaval.controlador.GameController;

/**
 * Clase principal de la aplicación modificada para incluir menú
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Crear el controlador principal
            GameController gameController = new GameController();

            // Inicializar la aplicación con menú
            gameController.initializeApplication(primaryStage);

        } catch (Exception e) {
            System.err.println("Error iniciando la aplicación: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("=== INICIANDO BATALLA NAVAL ===");
        System.out.println("Sistema de guardado automático activado");
        System.out.println("Interfaces y excepciones implementadas");

        launch(args);
    }
}