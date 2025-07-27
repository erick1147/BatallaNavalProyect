package com.example.batallanaval.interfaces;

/**
 * Interface para eventos del menú
 * Define los callbacks para las acciones del menú principal
 */
public interface MenuListener {
    
    /**
     * Se llama cuando el usuario solicita una nueva partida
     */
    void onNewGameRequested();
    
    /**
     * Se llama cuando el usuario solicita cargar una partida
     */
    void onLoadGameRequested();
    
    /**
     * Se llama cuando el menú se cierra
     */
    void onMenuClosed();
}