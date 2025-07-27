package com.example.batallanaval.interfaces;

import com.example.batallanaval.modelo.GameState;

/**
 * Interface para notificaciones del estado del juego
 * Implementa el patrón Observer para notificar cambios en el estado del juego
 */
public interface GameStateListener {
    
    /**
     * Se llama cuando el estado del juego cambia
     * @param newState Nuevo estado del juego
     */
    void onGameStateChanged(GameState newState);
    
    /**
     * Se llama cuando se guarda exitosamente una partida
     */
    void onGameSaved();
    
    /**
     * Se llama cuando se carga exitosamente una partida
     */
    void onGameLoaded();
    
    /**
     * Se llama cuando ocurre un error durante el guardado o carga
     * @param error Mensaje de error descriptivo
     */
    void onSaveError(String error);
}