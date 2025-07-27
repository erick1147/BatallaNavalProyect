package com.example.batallanaval.adapters;

import com.example.batallanaval.interfaces.GameStateListener;
import com.example.batallanaval.modelo.GameState;

/**
 * Clase adaptadora que implementa GameStateListener con métodos vacíos
 * Permite que las clases que la extiendan solo implementen los métodos necesarios
 * Implementa el patrón Adapter para simplificar la implementación de listeners
 */
public abstract class GameStateAdapter implements GameStateListener {
    
    /**
     * Implementación por defecto vacía para cambios de estado
     * Las subclases pueden sobreescribir este método si necesitan manejar cambios de estado
     */
    @Override
    public void onGameStateChanged(GameState newState) {
        // Implementación por defecto vacía
    }
    
    /**
     * Implementación por defecto vacía para eventos de guardado exitoso
     * Las subclases pueden sobreescribir este método si necesitan manejar guardados
     */
    @Override
    public void onGameSaved() {
        // Implementación por defecto vacía
        System.out.println("Partida guardada automáticamente");
    }
    
    /**
     * Implementación por defecto vacía para eventos de carga exitosa
     * Las subclases pueden sobreescribir este método si necesitan manejar cargas
     */
    @Override
    public void onGameLoaded() {
        // Implementación por defecto vacía
        System.out.println("Partida cargada exitosamente");
    }
    
    /**
     * Implementación por defecto que muestra errores en consola
     * Las subclases pueden sobreescribir este método para manejo personalizado de errores
     */
    @Override
    public void onSaveError(String error) {
        // Implementación por defecto que muestra el error
        System.err.println("Error de guardado/carga: " + error);
    }
}