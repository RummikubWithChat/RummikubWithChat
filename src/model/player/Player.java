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

    // 나를 제외한 다른 플레이어들의 리스트를 반환하는 메서드
    public static List<Player> getOtherPlayers(Player currentPlayer, List<Player> players) {
        List<Player> otherPlayers = new ArrayList<>();
        
        for (Player player : players) {
            if (!player.getName().equals(currentPlayer.getName())) {
                otherPlayers.add(player);
            }
        }
        
        return otherPlayers;
    }

    // public static String getCurrentPlayerName(Player currentPlayer, List<Player> players) {
    //     for (Player player : players) {
    //         if (player.getName().equals(currentPlayer.getName())) {
    //             return player.getName(); // 현재 플레이어의 이름을 반환
    //         }
    //     }
    //     return null; // 만약 현재 플레이어를 찾을 수 없으면 null 반환
    // }
    

}