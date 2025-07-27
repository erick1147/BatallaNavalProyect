package com.example.batallanaval.modelo;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

/**
 * Clase que representa el estado completo del juego para guardado/carga
 * Implementa Serializable para permitir la persistencia en archivos
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Estado del tablero
    private boolean[][] matrizLimpiezaPlayer;
    private boolean[][] matrizLimpiezaCpu;
    private boolean[][] matrizAtinacionPlayer;
    private boolean[][] matrizAtinacionCpu;
    private boolean[][] matrizDisparosPlayer;
    private boolean[][] matrizDisparosCpu;
    
    // Estado de los barcos
    private List<ShipState> playerShips;
    private List<ShipState> cpuShips;
    
    // Estado del juego
    private boolean gameStarted;
    private boolean gameEnded;
    private boolean isPlayerTurn;
    private boolean firstPlayerMove;
    private String winner;
    private String playerNickname;
    
    // Información de guardado
    private long saveTimestamp;
    
    /**
     * Constructor por defecto
     */
    public GameState() {
        this.playerShips = new ArrayList<>();
        this.cpuShips = new ArrayList<>();
        this.saveTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Clase interna para representar el estado de un barco
     * Implementa Serializable para ser guardada junto con GameState
     */
    public static class ShipState implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private List<int[]> coordinates;
        private int state; // 0 = intacto, 1 = dañado, 2 = hundido
        private boolean isVertical;
        private int gridCol;
        private int gridRow;
        private double width;
        private double height;
        
        public ShipState() {
            this.coordinates = new ArrayList<>();
        }
        
        // Getters y setters
        public List<int[]> getCoordinates() { return coordinates; }
        public void setCoordinates(List<int[]> coordinates) { this.coordinates = coordinates; }
        
        public int getState() { return state; }
        public void setState(int state) { this.state = state; }
        
        public boolean isVertical() { return isVertical; }
        public void setVertical(boolean vertical) { isVertical = vertical; }
        
        public int getGridCol() { return gridCol; }
        public void setGridCol(int gridCol) { this.gridCol = gridCol; }
        
        public int getGridRow() { return gridRow; }
        public void setGridRow(int gridRow) { this.gridRow = gridRow; }
        
        public double getWidth() { return width; }
        public void setWidth(double width) { this.width = width; }
        
        public double getHeight() { return height; }
        public void setHeight(double height) { this.height = height; }
    }
    
    // Getters y setters para todas las propiedades
    public boolean[][] getMatrizLimpiezaPlayer() { return matrizLimpiezaPlayer; }
    public void setMatrizLimpiezaPlayer(boolean[][] matrizLimpiezaPlayer) { 
        this.matrizLimpiezaPlayer = matrizLimpiezaPlayer; 
    }
    
    public boolean[][] getMatrizLimpiezaCpu() { return matrizLimpiezaCpu; }
    public void setMatrizLimpiezaCpu(boolean[][] matrizLimpiezaCpu) { 
        this.matrizLimpiezaCpu = matrizLimpiezaCpu; 
    }
    
    public boolean[][] getMatrizAtinacionPlayer() { return matrizAtinacionPlayer; }
    public void setMatrizAtinacionPlayer(boolean[][] matrizAtinacionPlayer) { 
        this.matrizAtinacionPlayer = matrizAtinacionPlayer; 
    }
    
    public boolean[][] getMatrizAtinacionCpu() { return matrizAtinacionCpu; }
    public void setMatrizAtinacionCpu(boolean[][] matrizAtinacionCpu) { 
        this.matrizAtinacionCpu = matrizAtinacionCpu; 
    }
    
    public boolean[][] getMatrizDisparosPlayer() { return matrizDisparosPlayer; }
    public void setMatrizDisparosPlayer(boolean[][] matrizDisparosPlayer) { 
        this.matrizDisparosPlayer = matrizDisparosPlayer; 
    }
    
    public boolean[][] getMatrizDisparosCpu() { return matrizDisparosCpu; }
    public void setMatrizDisparosCpu(boolean[][] matrizDisparosCpu) { 
        this.matrizDisparosCpu = matrizDisparosCpu; 
    }
    
    public List<ShipState> getPlayerShips() { return playerShips; }
    public void setPlayerShips(List<ShipState> playerShips) { this.playerShips = playerShips; }
    
    public List<ShipState> getCpuShips() { return cpuShips; }
    public void setCpuShips(List<ShipState> cpuShips) { this.cpuShips = cpuShips; }
    
    public boolean isGameStarted() { return gameStarted; }
    public void setGameStarted(boolean gameStarted) { this.gameStarted = gameStarted; }
    
    public boolean isGameEnded() { return gameEnded; }
    public void setGameEnded(boolean gameEnded) { this.gameEnded = gameEnded; }
    
    public boolean isPlayerTurn() { return isPlayerTurn; }
    public void setPlayerTurn(boolean playerTurn) { isPlayerTurn = playerTurn; }
    
    public boolean isFirstPlayerMove() { return firstPlayerMove; }
    public void setFirstPlayerMove(boolean firstPlayerMove) { this.firstPlayerMove = firstPlayerMove; }
    
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    
    public String getPlayerNickname() { return playerNickname; }
    public void setPlayerNickname(String playerNickname) { this.playerNickname = playerNickname; }
    
    public long getSaveTimestamp() { return saveTimestamp; }
    public void setSaveTimestamp(long saveTimestamp) { this.saveTimestamp = saveTimestamp; }
}