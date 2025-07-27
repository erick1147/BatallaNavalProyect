package com.example.batallanaval.vista;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

import com.example.batallanaval.interfaces.MenuInterface;
import com.example.batallanaval.interfaces.MenuListener;

import java.util.Optional;

/**
 * Menú principal del juego - Vista bonita y simple
 */
public class MainMenuView implements MenuInterface {
    
    private Stage stage;
    private Scene menuScene;
    private MenuListener menuListener;
    
    public MainMenuView(Stage stage) {
        this.stage = stage;
        createMenuScene();
    }
    
    private void createMenuScene() {
        // Título principal
        Label titleLabel = new Label("⚓ BATALLA NAVAL ⚓");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 42));
        titleLabel.setTextFill(Color.DARKBLUE);
        
        // Subtítulo
        Label subtitleLabel = new Label("¡Prepárate para la batalla en alta mar!");
        subtitleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleLabel.setTextFill(Color.DARKRED);
        
        // Botón Nueva Partida
        Button newGameButton = createButton("🚢 NUEVA PARTIDA", "#4CAF50");
        newGameButton.setOnAction(e -> {
            String nickname = requestPlayerNickname();
            if (nickname != null && !nickname.trim().isEmpty()) {
                hideMenu();
                if (menuListener != null) {
                    menuListener.onNewGameRequested();
                }
            }
        });
        
        // Botón Cargar Partida
        Button loadGameButton = createButton("📁 CARGAR PARTIDA", "#2196F3");
        loadGameButton.setOnAction(e -> {
            hideMenu();
            if (menuListener != null) {
                menuListener.onLoadGameRequested();
            }
        });
        
        // Botón Salir
        Button exitButton = createButton("🚪 SALIR", "#f44336");
        exitButton.setOnAction(e -> stage.close());
        
        // Contenedor principal
        VBox mainContainer = new VBox(30);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(50));
        mainContainer.setStyle("-fx-background-color: linear-gradient(to bottom, #e3f2fd, #bbdefb);");
        mainContainer.getChildren().addAll(titleLabel, subtitleLabel, newGameButton, loadGameButton, exitButton);
        
        menuScene = new Scene(mainContainer, 600, 500);
    }
    
    private Button createButton(String text, String color) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        button.setPrefSize(300, 60);
        button.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-text-fill: white; " +
            "-fx-border-radius: 10; " +
            "-fx-background-radius: 10;",
            color
        ));
        return button;
    }
    
    private String requestPlayerNickname() {
        TextInputDialog dialog = new TextInputDialog("Capitán");
        dialog.setTitle("Batalla Naval");
        dialog.setHeaderText("Ingresa tu nombre de capitán");
        dialog.setContentText("Nombre:");
        
        Optional<String> result = dialog.showAndWait();
        return result.orElse("Capitán");
    }
    
    public void showLoadError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No se pudo cargar la partida");
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    @Override
    public void showMenu() {
        stage.setScene(menuScene);
        stage.setTitle("Batalla Naval - Menú Principal");
        stage.show();
    }
    
    @Override
    public void hideMenu() {
        // Simplemente no hacer nada, el controlador cambiará la escena
    }
    
    @Override
    public void setMenuListener(MenuListener listener) {
        this.menuListener = listener;
    }
}