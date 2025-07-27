package com.example.batallanaval.interfaces;

import com.example.batallanaval.modelo.GameState;
import com.example.batallanaval.exceptions.GameSaveException;
import com.example.batallanaval.exceptions.GameLoadException;

/**
 * Interface para el guardado automático del juego
 * Define los métodos necesarios para la persistencia del estado del juego
 */
public interface GameSaveInterface {
    
    /**
     * Guarda el estado actual del juego
     * @param gameState Estado del juego a guardar
     * @throws GameSaveException Si ocurre un error durante el guardado
     */
    void saveGame(GameState gameState) throws GameSaveException;
    
    /**
     * Carga el último estado guardado del juego
     * @return Estado del juego cargado
     * @throws GameLoadException Si ocurre un error durante la carga
     */
    GameState loadGame() throws GameLoadException;
    
    /**
     * Verifica si existe una partida guardada válida
     * @return true si existe una partida guardada, false en caso contrario
     */
    boolean hasSavedGame();
    
    /**
     * Elimina la partida guardada
     * @throws GameSaveException Si ocurre un error al eliminar el archivo
     */
    void deleteSavedGame() throws GameSaveException;
}