package model.game;

import model.tile.Tile;
import model.tile.TileList;

import java.util.*;

import static model.tile.TileList.noPickTileList;

public class GameInitAndEndSet {
    public static void gameInitSetting(TileList tileManage,
                                       ArrayList<Tile> playerOneTileList, 
                                       ArrayList<Tile> playerTwoTileList,
                                       ArrayList<Tile> playerThreeTileList,
                                       ArrayList<Tile> playerFourTileList) {
        tileManage.push();
        tileManage.tileShuffle(noPickTileList);

        for (int i = 0; i < 14; i++) {
            tileManage.noPickTileDivide(playerOneTileList);
            tileManage.noPickTileDivide(playerTwoTileList);
            tileManage.noPickTileDivide(playerThreeTileList);
            tileManage.noPickTileDivide(playerFourTileList);
        }
    }

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
        }

        else if(gameEndStatus == 2){
            System.out.println("플레이어 2가 이겼습니다. 게임이 종료되었습니다.");
        }
        
        else if(gameEndStatus == 3){
            System.out.println("플레이어 3이 이겼습니다. 게임이 종료되었습니다.");
        }
        
        else if(gameEndStatus == 4){
            System.out.println("플레이어 4가 이겼습니다. 게임이 종료되었습니다.");
        }

        else if (gameEndStatus == 5){
            System.out.println("카드 전체가 빠져서 무승부 처리되었습니다.");
        }
    }
}