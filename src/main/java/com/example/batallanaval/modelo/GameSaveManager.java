package com.example.batallanaval.modelo;

import com.example.batallanaval.interfaces.GameSaveInterface;
import com.example.batallanaval.interfaces.GameStateListener;
import com.example.batallanaval.exceptions.GameSaveException;
import com.example.batallanaval.exceptions.GameLoadException;
import com.example.batallanaval.exceptions.GameCriticalException;
import com.example.batallanaval.modelo.GameState;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

/**
 * Manager para el guardado automático del juego
 * Implementa el patrón Singleton y maneja la persistencia del estado del juego
 */
public class GameSaveManager implements GameSaveInterface {
    
    private static final String SAVE_DIRECTORY = "battleship_saves";
    private static final String SAVE_FILE = "last_game.dat";
    private static final String BACKUP_FILE = "last_game_backup.dat";
    
    private List<GameStateListener> listeners;
    
    /**
     * Clase interna para manejar las operaciones de archivo
     * Encapsula la lógica de manejo de archivos
     */
    private class FileOperationHandler {
        
        /**
         * Asegura que el directorio de guardado existe
         * @throws GameSaveException Si no se puede crear el directorio
         */
        public void ensureSaveDirectoryExists() throws GameSaveException {
            try {
                Path saveDir = Paths.get(SAVE_DIRECTORY);
                if (!Files.exists(saveDir)) {
                    Files.createDirectories(saveDir);
                    System.out.println("✓ Directorio de guardado creado: " + saveDir.toAbsolutePath());
                }
            } catch (IOException e) {
                throw new GameSaveException("No se pudo crear el directorio de guardado", e);
            }
        }
        
        /**
         * Obtiene la ruta del archivo principal de guardado
         * @return Path del archivo de guardado
         */
        public Path getSaveFilePath() {
            return Paths.get(SAVE_DIRECTORY, SAVE_FILE);
        }
        
        /**
         * Obtiene la ruta del archivo de backup
         * @return Path del archivo de backup
         */
        public Path getBackupFilePath() {
            return Paths.get(SAVE_DIRECTORY, BACKUP_FILE);
        }
    }
    
    private FileOperationHandler fileHandler;
    
    /**
     * Constructor del GameSaveManager
     */
    public GameSaveManager() {
        this.listeners = new ArrayList<>();
        this.fileHandler = new FileOperationHandler();
    }
    
    /**
     * Añade un listener para eventos de guardado/carga
     * @param listener Listener a añadir
     */
    public void addListener(GameStateListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    /**
     * Remueve un listener
     * @param listener Listener a remover
     */
    public void removeListener(GameStateListener listener) {
        listeners.remove(listener);
    }
    
    @Override
    public void saveGame(GameState gameState) throws GameSaveException {
        if (gameState == null) {
            throw new IllegalArgumentException("El estado del juego no puede ser null");
        }
        
        try {
            fileHandler.ensureSaveDirectoryExists();
            
            Path saveFile = fileHandler.getSaveFilePath();
            Path backupFile = fileHandler.getBackupFilePath();
            
            // Crear backup del archivo anterior si existe
            if (Files.exists(saveFile)) {
                try {
                    Files.copy(saveFile, backupFile, 
                              java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("✓ Backup creado exitosamente");
                } catch (IOException e) {
                    System.err.println("Advertencia: No se pudo crear backup: " + e.getMessage());
                }
            }
            
            // Guardar el nuevo estado
            gameState.setSaveTimestamp(System.currentTimeMillis());
            
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(saveFile.toFile()))) {
                oos.writeObject(gameState);
                oos.flush();
            }
            
            // Verificar que se guardó correctamente
            if (!Files.exists(saveFile) || Files.size(saveFile) == 0) {
                throw new GameSaveException("El archivo de guardado está vacío o no se creó");
            }
            
            // Notificar a los listeners
            notifyGameSaved();
            
            System.out.println("✓ Partida guardada automáticamente en: " + saveFile.toAbsolutePath());
            
        } catch (IOException e) {
            notifyError("Error al guardar la partida: " + e.getMessage());
            throw new GameSaveException("Error al guardar el estado del juego", e);
        } catch (Exception e) {
            notifyError("Error crítico al guardar: " + e.getMessage());
            throw new GameCriticalException("Error crítico durante el guardado", e);
        }
    }
    
    @Override
    public GameState loadGame() throws GameLoadException {
        Path saveFile = fileHandler.getSaveFilePath();
        Path backupFile = fileHandler.getBackupFilePath();
        
        if (!Files.exists(saveFile)) {
            throw new GameLoadException("No existe una partida guardada");
        }
        
        GameState gameState = null;
        
        // Intentar cargar el archivo principal
        try {
            gameState = loadFromFile(saveFile);
            System.out.println("✓ Archivo principal cargado exitosamente");
        } catch (Exception e) {
            System.err.println("Error cargando archivo principal: " + e.getMessage());
            
            // Intentar cargar desde backup
            if (Files.exists(backupFile)) {
                try {
                    System.out.println("Intentando cargar desde backup...");
                    gameState = loadFromFile(backupFile);
                    System.out.println("✓ Cargado desde archivo de backup");
                } catch (Exception backupError) {
                    throw new GameLoadException("Error al cargar tanto el archivo principal como el backup", 
                                               backupError);
                }
            } else {
                throw new GameLoadException("Archivo corrupto y no existe backup", e);
            }
        }
        
        if (gameState == null) {
            throw new GameLoadException("No se pudo cargar el estado del juego");
        }
        
        // Notificar a los listeners
        notifyGameLoaded();
        
        System.out.println("✓ Partida cargada exitosamente - Jugador: " + 
                          gameState.getPlayerNickname());
        return gameState;
    }
    
    /**
     * Carga el estado del juego desde un archivo específico
     * @param filePath Ruta del archivo a cargar
     * @return Estado del juego cargado
     * @throws GameLoadException Si ocurre un error durante la carga
     */
    private GameState loadFromFile(Path filePath) throws GameLoadException {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            
            Object obj = ois.readObject();
            if (!(obj instanceof GameState)) {
                throw new GameLoadException("El archivo no contiene un estado de juego válido");
            }
            
            return (GameState) obj;
            
        } catch (IOException e) {
            throw new GameLoadException("Error de E/O al cargar el archivo", e);
        } catch (ClassNotFoundException e) {
            throw new GameLoadException("Error de versión: Clase no encontrada", e);
        }
    }
    
    @Override
    public boolean hasSavedGame() {
        Path saveFile = fileHandler.getSaveFilePath();
        try {
            return Files.exists(saveFile) && Files.size(saveFile) > 0;
        } catch (IOException e) {
            System.err.println("Error verificando archivo guardado: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public void deleteSavedGame() throws GameSaveException {
        Path saveFile = fileHandler.getSaveFilePath();
        Path backupFile = fileHandler.getBackupFilePath();
        
        try {
            boolean deletedMain = false;
            boolean deletedBackup = false;
            
            if (Files.exists(saveFile)) {
                Files.delete(saveFile);
                deletedMain = true;
            }
            if (Files.exists(backupFile)) {
                Files.delete(backupFile);
                deletedBackup = true;
            }
            
            if (deletedMain || deletedBackup) {
                System.out.println("✓ Archivos de guardado eliminados");
            } else {
                System.out.println("No había archivos de guardado para eliminar");
            }
        } catch (IOException e) {
            throw new GameSaveException("Error al eliminar los archivos de guardado", e);
        }
    }
    
    /**
     * Notifica a todos los listeners que se ha guardado el juego
     */
    private void notifyGameSaved() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onGameSaved();
            } catch (Exception e) {
                System.err.println("Error notificando guardado: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notifica a todos los listeners que se ha cargado el juego
     */
    private void notifyGameLoaded() {
        for (GameStateListener listener : listeners) {
            try {
                listener.onGameLoaded();
            } catch (Exception e) {
                System.err.println("Error notificando carga: " + e.getMessage());
            }
        }
    }
    
    /**
     * Notifica a todos los listeners de un error
     * @param error Mensaje de error
     */
    private void notifyError(String error) {
        for (GameStateListener listener : listeners) {
            try {
                listener.onSaveError(error);
            } catch (Exception e) {
                System.err.println("Error notificando error: " + e.getMessage());
            }
        }
    }
}