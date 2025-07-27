package com.example.batallanaval.exceptions;

/**
 * Excepción marcada para errores de carga del juego
 * Se utiliza específicamente para problemas al cargar partidas guardadas
 */
public class GameLoadException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor básico con mensaje
     * @param message Mensaje descriptivo del error de carga
     */
    public GameLoadException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa
     * @param message Mensaje descriptivo del error de carga
     * @param cause Excepción que causó este error de carga
     */
    public GameLoadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor solo con causa
     * @param cause Excepción que causó este error de carga
     */
    public GameLoadException(Throwable cause) {
        super(cause);
    }
}