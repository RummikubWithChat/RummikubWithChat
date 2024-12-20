package model.player;

import model.tile.Tile;

import java.util.ArrayList;
import java.util.List;

public class Player {
    public String name;
    public int cardCount;
    public boolean isRegisterCheck;
    public ArrayList<Tile> tileList = new ArrayList<Tile>(106);

    public Player(String inputPlayerName) {
        name = inputPlayerName;
        cardCount = 0; //TODO 추후구현
        isRegisterCheck = false;
    }
    
    public String getName() {
    	return this.name;
    }
    
    public String tileListToString() {
        StringBuilder sb = new StringBuilder();
        for (Tile tile : tileList) {
            sb.append(tile.toString()).append(", ");
        }
        // 마지막 쉼표 제거
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }
    
    public int getTileListSize() {
    	return tileList.size();
    }

}