package com.example.batallanaval.interfaces;

/**
 * Interface para la gestión de menús
 * Define los métodos básicos para cualquier menú del juego
 */
public interface MenuInterface {
    
    /**
     * Muestra el menú en pantalla
     */
    void showMenu();
    
    /**
     * Oculta el menú de la pantalla
     */
    void hideMenu();
    
    /**
     * Establece el listener para eventos del menú
     * @param listener Listener que manejará los eventos del menú
     */
    void setMenuListener(MenuListener listener);
}