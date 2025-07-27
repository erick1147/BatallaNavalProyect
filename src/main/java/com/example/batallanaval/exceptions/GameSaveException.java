package com.example.batallanaval.exceptions;

/**
 * Excepción marcada para errores relacionados con el guardado del juego
 * Implementa el patrón de excepciones marcadas de Java
 */
public class GameSaveException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor básico con mensaje
     * @param message Mensaje descriptivo del error
     */
    public GameSaveException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa
     * @param message Mensaje descriptivo del error
     * @param cause Excepción que causó este error
     */
    public GameSaveException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor solo con causa
     * @param cause Excepción que causó este error
     */
    public GameSaveException(Throwable cause) {
        super(cause);
    }
}