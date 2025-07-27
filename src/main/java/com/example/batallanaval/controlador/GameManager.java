package com.example.batallanaval.controlador;

import com.example.batallanaval.interfaces.GameStateListener;
import com.example.batallanaval.modelo.GameState;
import com.example.batallanaval.modelo.GameSaveManager;
import com.example.batallanaval.modelo.GameLogic;
import com.example.batallanaval.modelo.Ship;
import com.example.batallanaval.exceptions.GameSaveException;
import com.example.batallanaval.exceptions.GameLoadException;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestor principal del juego que maneja el estado, guardado automático y lógica
 * Adaptado para JavaFX y tu estructura existente
 */
public class GameManager {
    private static GameManager instance;

    // Estado del juego
    private GameState currentGameState;
    private GameLogic gameLogic;
    private GameSaveManager saveManager;
    private List<GameStateListener> listeners;

    // Control de auto-guardado
    private boolean autoSaveEnabled;
    private boolean gameInProgress;

    private GameManager() {
        this.currentGameState = new GameState();
        this.listeners = new ArrayList<>();
        this.saveManager = new GameSaveManager();
        this.autoSaveEnabled = true;
        this.gameInProgress = false;

        // Agregar este manager como listener del saveManager
        this.saveManager.addListener(new GameStateListener() {
            @Override
            public void onGameStateChanged(GameState newState) {
                // No necesario para el saveManager
            }

            @Override
            public void onGameSaved() {
                notifyGameSaved();
            }

            @Override
            public void onGameLoaded() {
                notifyGameLoaded();
            }

            @Override
            public void onSaveError(String error) {
                notifyError(error);
            }
        });
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    /**
     * Inicializa el manager con la lógica de juego existente
     */
    public void initializeWithGameLogic(GameLogic gameLogic) {
        this.gameLogic = gameLogic;
        System.out.println("✓ GameManager inicializado con GameLogic");
    }

    /**
     * Marca el inicio de una nueva partida
     */
    public void startNewGame() {
        this.currentGameState = new GameState();
        this.currentGameState.setGameStarted(true);
        this.currentGameState.setPlayerTurn(true);
        this.currentGameState.setPlayerNickname("Capitán");
        this.gameInProgress = true;

        System.out.println("✓ Nueva partida iniciada en GameManager");
        notifyGameStateChanged();
    }

    /**
     * Captura el estado actual del juego desde GameLogic
     */
    public void captureGameState() {
        if (gameLogic == null || !gameInProgress) {
            return;
        }

        try {
            // Capturar matrices del GameLogic
            currentGameState.setMatrizLimpiezaPlayer(copyMatrix(gameLogic.getMatrizLimpiezaPlayer()));
            currentGameState.setMatrizLimpiezaCpu(copyMatrix(gameLogic.getMatrizLimpiezaCpu()));
            currentGameState.setMatrizDisparosPlayer(copyMatrix(gameLogic.getMatrizDisparosPlayer()));
            currentGameState.setMatrizDisparosCpu(copyMatrix(gameLogic.getMatrizDisparosCpu()));
            currentGameState.setMatrizAtinacionPlayer(copyMatrix(gameLogic.getMatrizAtinacionPlayer()));
            currentGameState.setMatrizAtinacionCpu(copyMatrix(gameLogic.getMatrizAtinacionCpu()));

            // Capturar estado del juego
            currentGameState.setGameEnded(gameLogic.isGameEnded());
            currentGameState.setWinner(gameLogic.getWinner());

            // Capturar barcos de la CPU
            currentGameState.getCpuShips().clear();
            if (gameLogic.getArrayCpu() != null) {
                for (int i = 0; i < gameLogic.getArrayCpu().length; i++) {
                    if (gameLogic.getArrayCpu()[i] != null && gameLogic.getArrayCpu()[i].getSize() > 0) {
                        GameState.ShipState shipState = new GameState.ShipState();
                        shipState.setCoordinates(new ArrayList<>(gameLogic.getArrayCpu()[i].getCoordinates()));
                        shipState.setState(gameLogic.getArrayCpu()[i].getState());
                        currentGameState.getCpuShips().add(shipState);
                    }
                }
            }

            // Actualizar timestamp
            currentGameState.setSaveTimestamp(System.currentTimeMillis());

        } catch (Exception e) {
            System.err.println("Error capturando estado del juego: " + e.getMessage());
        }
    }

    /**
     * Restaura el estado del juego en GameLogic
     */
    public boolean restoreGameState(GameState gameState) {
        if (gameLogic == null || gameState == null) {
            return false;
        }

        try {
            // Restaurar matrices en GameLogic
            if (gameState.getMatrizLimpiezaPlayer() != null) {
                gameLogic.setMatrizLimpiezaPlayer(gameState.getMatrizLimpiezaPlayer());
            }
            if (gameState.getMatrizLimpiezaCpu() != null) {
                gameLogic.setMatrizLimpiezaCpu(gameState.getMatrizLimpiezaCpu());
            }
            if (gameState.getMatrizDisparosPlayer() != null) {
                gameLogic.setMatrizDisparosPlayer(gameState.getMatrizDisparosPlayer());
            }
            if (gameState.getMatrizDisparosCpu() != null) {
                gameLogic.setMatrizDisparosCpu(gameState.getMatrizDisparosCpu());
            }
            if (gameState.getMatrizAtinacionPlayer() != null) {
                gameLogic.setMatrizAtinacionPlayer(gameState.getMatrizAtinacionPlayer());
            }
            if (gameState.getMatrizAtinacionCpu() != null) {
                gameLogic.setMatrizAtinacionCpu(gameState.getMatrizAtinacionCpu());
            }

            // Restaurar barcos de la CPU
            if (gameState.getCpuShips() != null && !gameState.getCpuShips().isEmpty()) {
                for (int i = 0; i < gameState.getCpuShips().size() && i < gameLogic.getArrayCpu().length; i++) {
                    GameState.ShipState shipState = gameState.getCpuShips().get(i);
                    if (gameLogic.getArrayCpu()[i] == null) {
                        gameLogic.getArrayCpu()[i] = new Ship();
                    }
                    gameLogic.getArrayCpu()[i].clearCoordinates();
                    for (int[] coord : shipState.getCoordinates()) {
                        gameLogic.getArrayCpu()[i].addCoordinate(coord[0], coord[1]);
                    }
                    gameLogic.getArrayCpu()[i].setState(shipState.getState());
                }
            }

            // Actualizar estado local
            this.currentGameState = gameState;
            this.gameInProgress = gameState.isGameStarted();

            System.out.println("✓ Estado del juego restaurado exitosamente");
            notifyGameStateChanged();
            return true;

        } catch (Exception e) {
            System.err.println("Error restaurando estado del juego: " + e.getMessage());
            return false;
        }
    }

    /**
     * Guarda automáticamente el juego después de cada jugada
     */
    public void autoSaveAfterMove() {
        if (!autoSaveEnabled || !gameInProgress) {
            return;
        }

        // Capturar estado actual
        captureGameState();

        // Guardar en hilo separado para no bloquear la UI
        new Thread(() -> {
            try {
                Thread.sleep(500); // Pequeño delay para asegurar que el estado esté completo
                saveManager.saveGame(currentGameState);
                System.out.println("✓ Auto-guardado completado");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (GameSaveException e) {
                System.err.println("Error en auto-guardado: " + e.getMessage());
                notifyError("Error en auto-guardado: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Guarda manualmente el juego
     */
    public void saveGame() {
        if (!gameInProgress) {
            System.out.println("No hay partida activa para guardar");
            return;
        }

        captureGameState();

        try {
            saveManager.saveGame(currentGameState);
            System.out.println("✓ Partida guardada manualmente");
        } catch (GameSaveException e) {
            System.err.println("Error guardando partida: " + e.getMessage());
            notifyError("Error guardando partida: " + e.getMessage());
        }
    }

    /**
     * Carga una partida guardada
     */
    public boolean loadGame() {
        try {
            GameState loadedState = saveManager.loadGame();
            if (loadedState != null) {
                boolean success = restoreGameState(loadedState);
                if (success) {
                    System.out.println("✓ Partida cargada exitosamente");
                    return true;
                }
            }
            return false;
        } catch (GameLoadException e) {
            System.err.println("Error cargando partida: " + e.getMessage());
            notifyError("Error cargando partida: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si existe una partida guardada
     */
    public boolean hasSavedGame() {
        return saveManager.hasSavedGame();
    }

    /**
     * Elimina la partida guardada
     */
    public void deleteSavedGame() {
        try {
            saveManager.deleteSavedGame();
            System.out.println("✓ Partida guardada eliminada");
        } catch (GameSaveException e) {
            System.err.println("Error eliminando partida: " + e.getMessage());
            notifyError("Error eliminando partida: " + e.getMessage());
        }
    }

    /**
     * Actualiza el turno del jugador
     */
    public void setPlayerTurn(boolean isPlayerTurn) {
        currentGameState.setPlayerTurn(isPlayerTurn);
    }

    /**
     * Actualiza si es el primer movimiento del jugador
     */
    public void setFirstPlayerMove(boolean firstMove) {
        currentGameState.setFirstPlayerMove(firstMove);
    }

    /**
     * Marca que el juego ha terminado
     */
    public void setGameEnded(boolean ended, String winner) {
        currentGameState.setGameEnded(ended);
        currentGameState.setWinner(winner);
        if (ended) {
            // Guardado final cuando termina el juego
            autoSaveAfterMove();
        }
    }

    /**
     * Copia una matriz booleana
     */
    private boolean[][] copyMatrix(boolean[][] original) {
        if (original == null) return null;

        boolean[][] copy = new boolean[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    // Métodos para listeners
    public void addGameStateListener(GameStateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeGameStateListener(GameStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyGameStateChanged() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onGameStateChanged(currentGameState);
            } catch (Exception e) {
                System.err.println("Error notificando cambio de estado: " + e.getMessage());
            }
        }
    }

    private void notifyGameSaved() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onGameSaved();
            } catch (Exception e) {
                System.err.println("Error notificando guardado: " + e.getMessage());
            }
        }
    }

    private void notifyGameLoaded() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onGameLoaded();
            } catch (Exception e) {
                System.err.println("Error notificando carga: " + e.getMessage());
            }
        }
    }

    private void notifyError(String error) {
        for (GameStateListener listener : listeners) {
            try {
                listener.onSaveError(error);
            } catch (Exception e) {
                System.err.println("Error notificando error: " + e.getMessage());
            }
        }
    }

    // Getters
    public GameState getCurrentGameState() {
        return currentGameState;
    }

    public boolean isAutoSaveEnabled() {
        return autoSaveEnabled;
    }

    public void setAutoSaveEnabled(boolean autoSaveEnabled) {
        this.autoSaveEnabled = autoSaveEnabled;
    }

    public boolean isGameInProgress() {
        return gameInProgress;
    }
}