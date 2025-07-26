// DraggableShape.java
package com.example.batallanaval;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class DraggableShape {

    private Group group;
    private Rectangle backgroundRect;
    private ImageView imageView;

    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    // Estado para la orientación del grupo: true para vertical, false para horizontal
    private boolean isVertical = false; // Empieza en horizontal por defecto (2x1)

    public DraggableShape() {
        // --- Crear el Rectangle que servirá como representación visual del grupo ---
        backgroundRect = new Rectangle(0, 0, Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH, Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT);
        backgroundRect.setFill(Color.LIGHTCORAL.deriveColor(1, 1, 1, 0.7));
        backgroundRect.setStroke(Color.DARKRED);
        backgroundRect.setStrokeWidth(2);
        backgroundRect.setArcWidth(10);
        backgroundRect.setArcHeight(10);

        // --- Reintroducir ImageView ---
        // Asegúrate de que esta URL sea accesible. Puedes usar una ruta local si prefieres.
        Image image = new Image("https://upload.wikimedia.org/wikipedia/commons/4/47/PNG_transparency_demonstration_1.png");
        imageView = new ImageView(image);
        imageView.setPreserveRatio(true);

        // Inicializar el tamaño y posición de la imagen para la orientación horizontal por defecto
        imageView.setFitWidth(Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH - 10);
        imageView.setFitHeight(Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT - 10);
        /*imageView.setLayoutX(100);
        imageView.setLayoutY(200);*/

        // Crear el Group y añadir sus hijos
        group = new Group();
        group.getChildren().addAll(backgroundRect, imageView);

        // Posición inicial del grupo
        group.setTranslateX(0);
        group.setTranslateY(0);

        // Configurar los manejadores de eventos
        setupDragHandlers();
        setupRotationHandler();
    }

    private void setupDragHandlers() {
        group.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                orgSceneX = event.getSceneX();
                orgSceneY = event.getSceneY();
                orgTranslateX = group.getTranslateX();
                orgTranslateY = group.getTranslateY();
                group.toFront();
                event.consume();
            }
        });

        group.setOnMouseDragged(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double offsetX = event.getSceneX() - orgSceneX;
                double offsetY = event.getSceneY() - orgSceneY;
                double newTranslateX = orgTranslateX + offsetX;
                double newTranslateY = orgTranslateY + offsetY;

                // Obtener las dimensiones actuales del grupo para el cálculo de límites
                double currentGroupWidth = isVertical ? Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT : Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH;
                double currentGroupHeight = isVertical ? Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH : Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT;

                double maxTranslateX = (Constants.GRID_COLS * Constants.CELL_SIZE) - currentGroupWidth;
                double maxTranslateY = (Constants.GRID_ROWS * Constants.CELL_SIZE) - currentGroupHeight;

                newTranslateX = Math.max(0, Math.min(newTranslateX, maxTranslateX));
                newTranslateY = Math.max(0, Math.min(newTranslateY, maxTranslateY));

                group.setTranslateX(newTranslateX);
                group.setTranslateY(newTranslateY);
                event.consume();
            }
        });

        group.setOnMouseReleased(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                snapToGrid();
                System.out.println("Grupo soltado y encajado en: (" + (int)(group.getTranslateX() / Constants.CELL_SIZE) + ", " + (int)(group.getTranslateY() / Constants.CELL_SIZE) + ")");
                event.consume();
            }
        });
    }

    private void setupRotationHandler() {
        group.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) { // Si es clic derecho
                isVertical = !isVertical; // Invertir la orientación

                // Intercambiar dimensiones del rectángulo de fondo
                double tempWidth = backgroundRect.getWidth();
                backgroundRect.setWidth(backgroundRect.getHeight());
                backgroundRect.setHeight(tempWidth);

                // Intercambiar dimensiones del ImageView y ajustar su posición dentro del group
                double tempImageWidth = imageView.getFitWidth();
                imageView.setFitWidth(imageView.getFitHeight());
                imageView.setFitHeight(tempImageWidth);

                // Ajustar la posición de la imagen dentro del rectángulo "rotado"
                // Si el rectángulo era 2x1 y se vuelve 1x2, el centro no cambia si la imagen es centrada.
                // Pero como usamos un padding fijo (5), esto ajusta la imagen para que siga centrada en el nuevo tamaño del fondo.
                imageView.setLayoutX((backgroundRect.getWidth() - imageView.getFitWidth()) / 2);
                imageView.setLayoutY((backgroundRect.getHeight() - imageView.getFitHeight()) / 2);


                // IMPORTANTE: NO usamos group.setRotate(90) en el Group para evitar problemas con el pivote
                // La "rotación" visual se logra intercambiando las dimensiones de los hijos.
                // Si la imagen en sí tuviera que rotar para que el dibujo se viera rotado (y no solo la forma),
                // entonces tendrías que aplicar un efecto Rotate a la ImageView o cargar una imagen diferente.
                // Para tu caso de encaje, solo necesitamos que la forma cambie y se encaje.

                snapToGrid(); // Volver a encajar el grupo después del cambio de forma
                System.out.println("Grupo rotado. Nueva orientación: " + (isVertical ? "Vertical" : "Horizontal"));
                event.consume();
            }
        });
    }

    private void snapToGrid() {
        double currentTranslateX = group.getTranslateX();
        double currentTranslateY = group.getTranslateY();

        double snappedX = Math.round(currentTranslateX / Constants.CELL_SIZE) * Constants.CELL_SIZE;
        double snappedY = Math.round(currentTranslateY / Constants.CELL_SIZE) * Constants.CELL_SIZE;

        // Obtener las dimensiones correctas del grupo para el encaje
        double currentGroupWidth = isVertical ? Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT : Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH;
        double currentGroupHeight = isVertical ? Constants.DEFAULT_DRAGGABLE_GROUP_WIDTH : Constants.DEFAULT_DRAGGABLE_GROUP_HEIGHT;

        // Asegurarse de que el encaje no lo mueva fuera de los límites
        double maxTranslateX = (Constants.GRID_COLS * Constants.CELL_SIZE) - currentGroupWidth;
        double maxTranslateY = (Constants.GRID_ROWS * Constants.CELL_SIZE) - currentGroupHeight;

        snappedX = Math.max(0, Math.min(snappedX, maxTranslateX));
        snappedY = Math.max(0, Math.min(snappedY, maxTranslateY));

        group.setTranslateX(snappedX);
        group.setTranslateY(snappedY);
    }

    public Node getNode() {
        return group;
    }
}