package com.example.batallanaval.exceptions;

/**
 * Excepción no marcada para errores críticos del juego
 * Implementa el patrón de excepciones no marcadas (RuntimeException)
 */
public class GameCriticalException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor básico con mensaje
     * @param message Mensaje descriptivo del error crítico
     */
    public GameCriticalException(String message) {
        super(message);
    }
    
    /**
     * Constructor con mensaje y causa
     * @param message Mensaje descriptivo del error crítico
     * @param cause Excepción que causó este error crítico
     */
    public GameCriticalException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Constructor solo con causa
     * @param cause Excepción que causó este error crítico
     */
    public GameCriticalException(Throwable cause) {
        super(cause);
    }
}