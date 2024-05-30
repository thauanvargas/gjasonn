import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicBoolean;


public class Player
{
    private int playerId;
    private int playerCoordX;
    private int playerCoordY;
    private boolean isJason = false;
    private boolean isKicked = false;
    private String playerName = "";
    private int index;

    public Player(Integer playerId, String playerName, int playerIndex, int x, int y)
    {
        this.playerId = playerId;
        this.playerName = playerName;
        this.index = playerIndex;
        this.playerCoordY = y;
        this.playerCoordX = x;
    }


    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public int getPlayerCoordX() {
        return playerCoordX;
    }

    public void setPlayerCoordX(int playerCoordX) {
        this.playerCoordX = playerCoordX;
    }

    public int getPlayerCoordY() {
        return playerCoordY;
    }

    public void setPlayerCoordY(int playerCoordY) {
        this.playerCoordY = playerCoordY;
    }

    public void setIsKicked(boolean isKicked) {
        this.isKicked = isKicked;
    }

    public boolean isKicked() {
        return isKicked;
    }

    public boolean isJason() {
        return isJason;
    }

    public void setIsJason(boolean isJason) {
        this.isJason = isJason;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}