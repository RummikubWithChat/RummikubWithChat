package model.game;

import model.player.Player;
import model.tile.Tile;
import model.tile.TileList;
import network.JavaChatServer;

import java.util.*;

import static model.tile.TileList.noPickTileList;

public class GameInitAndEndSet {
	// 게임 시작 세팅
    public static void gameInitSetting(TileList tileManage, List<Player> players) {
        tileManage.push();
        tileManage.tileShuffle(noPickTileList);

        // 타일 배분
        for (int i = 0; i < 14; i++) {
            for (int j = 0; j < players.size(); j++) {
                tileManage.noPickTileDivide(players.get(j).tileList);
            }
        }
        
        // 타일 리스트를 각 클라이언트에 전달
        for (Player player : players) {
        	JavaChatServer.sendTileListToClient(player);  // 각 플레이어에게 타일 리스트 전송
        }
        JavaChatServer.sendTileListSizeToClient();
    }

    // 게임 종료 조건 체크
    public static int gameEndCheck(ArrayList<Tile> tileList, 
    							   ArrayList<Tile> playerOneTileList,
                                   ArrayList<Tile> playerTwoTileList,
                                   ArrayList<Tile> playerThreeTileList,
                                   ArrayList<Tile> playerFourTileList) {
        int tileListSize = TileList.getStackSize(tileList);
        int playerOneListSize = TileList.getStackSize(playerOneTileList);
        int playerTwoListSize = TileList.getStackSize(playerTwoTileList);
        int playerThreeListSize = TileList.getStackSize(playerThreeTileList);
        int playerFourListSize = TileList.getStackSize(playerFourTileList);
        

        if (playerOneListSize == 0) {
            return 1;
        }

        else if (playerTwoListSize == 0) {
            return 2;
        }
        
        else if (playerThreeListSize == 0) {
        	return 3;
        }
        
        else if (playerThreeListSize == 0) {
        	return 4;
        }
        
        // 타일 리스트에 더 이상 타일이 없어 게임을 끝내야 하는 경우, But. 플레이어들은 카드가 존재하는 경우
        else if (tileListSize == 0) {
            return 5;
        }

        else {
            return 0; // 게임을 정상적으로 진행 해야 하는 경우
        }
    }

    public static void gameEnd(int gameEndStatus) {
        if(gameEndStatus == 1){
            System.out.println("플레이어 1이 이겼습니다. 게임이 종료되었습니다.");
            JavaChatServer.sendGameOverToClient(1);
        }

        else if(gameEndStatus == 2){
            System.out.println("플레이어 2가 이겼습니다. 게임이 종료되었습니다.");
            JavaChatServer.sendGameOverToClient(2);
        }
        
        else if(gameEndStatus == 3){
            System.out.println("플레이어 3이 이겼습니다. 게임이 종료되었습니다.");
            JavaChatServer.sendGameOverToClient(3);
        }
        
        else if(gameEndStatus == 4){
            System.out.println("플레이어 4가 이겼습니다. 게임이 종료되었습니다.");
            JavaChatServer.sendGameOverToClient(4);
        }

        else if (gameEndStatus == 5){
            System.out.println("카드 전체가 빠져서 무승부 처리되었습니다.");
            JavaChatServer.sendGameOverToClient(-1);
        }
    }
}