package model.player;

import model.tile.Tile;

import java.util.ArrayList;

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
}