package model.game;

import model.board.Board;
import model.player.Player;
import model.tile.Tile;
import model.tile.TileList;
import network.Server;
import java.util.*;

import static model.board.Board.onBoardTileList;
import static model.board.Board.temporaryTile;
import static model.game.GameInitAndEndSet.gameEnd;
import static model.game.GameInitAndEndSet.gameEndCheck;
import static model.game.PlayChoice.*;
import static model.tile.TileList.noPickTileList;

public class GamePlaying {
    private final Board boardManage;
    private final TileList tileListManage;
    private final Player player1;
    private final Player player2;
    private final Player player3;
    private final Player player4;
    private static int playerTurn = 1; // 플레이어 순서

    // 보드 선택과 타일 선택을 구분하는 변수
    public static boolean isSelectedOnBoard = true;
    public static boolean isSelectedOnTileList = true;

    public GamePlaying(Board boardManage, TileList tileListManage,
                      Player player1, Player player2, Player player3, Player player4) {
        this.boardManage = boardManage;
        this.tileListManage = tileListManage;
        this.player1 = player1;
        this.player2 = player2;
        this.player3 = player3;
        this.player4 = player4;
    }
    
    public static void setIsSelectedOnBoard(boolean selected) {
    	isSelectedOnBoard = selected;
    }

    public static void setIsSelectedOnTileList(boolean selected) {
    	isSelectedOnTileList = selected;
    }

    public void gamePlay() {
        // 게임 진행 중
        while (gameEndCheck(noPickTileList, player1.tileList, player2.tileList, player3.tileList, player4.tileList) == 0) {
            gamePlayToTurn();
        }

        // 게임 종료
        int result = gameEndCheck(noPickTileList, player1.tileList, player2.tileList, player3.tileList, player4.tileList);
        if (result != 0) {
            gameEnd(result);
        }
    }

    private void gamePlayToTurn() {
        boolean turnComplete = false; //턴의 완료 상태
        String playChoice = "1";
        Player currentPlayer = getCurrentPlayer();

        Server.sendBoardTileListToClient();
        
        do {
            System.out.println("현재 턴: " + playerTurn);
            Server.sendIsTurnToClient(currentPlayer);
            tileListManage.tileLinkListPrint(onBoardTileList); // 보드 타일 출력 
            tileListManage.tileListPrint(currentPlayer.tileList, currentPlayer);
            
            playChoice = playerAction(currentPlayer);
            turnComplete = choiceCheck(playChoice);
            
            if (isSelectedOnTileList) {
                setIsSelectedOnTileList(false); // 상태 변경 후 다시 초기화
                System.out.println("isSelectedOnTileList true -> false");
            }
            if (isSelectedOnBoard) {
                setIsSelectedOnBoard(false); // 상태 변경 후 다시 초기화
                System.out.println("isSelectedOnTileList true -> false");
            }
            
        } while (!turnComplete);

        boardManage.turnChanged(currentPlayer);
        advanceTurn();
    }
    
    private Boolean choiceCheck(String action) {

        Player player;
        if (playerTurn == 1) {
            player = player1;
        } else if (playerTurn == 2) {
            player = player2;
        } else if (playerTurn == 3) {
            player = player3;
        } else {
            player = player4;
        }
        
        if (action == null) {
            System.out.println("선택지가 없습니다. 다시 입력하세요.");
            return false; // 기본적으로 false를 반환하여 플레이어가 다시 시도하도록 설정
        }

        if (!player.isRegisterCheck) {
        	if (action.equals("p")) {
                handlePickAction(player);
                return true;
            } else if (action.equals("end")) {
            	return true;
            }
            boardManage.generateTemporaryTileList(player, 999);
            return false;
        } else {
//            String action = playerAction(player);
        	if (action.equals("end")) {
            	return true;
            } else if (action.startsWith("a")) {
            	// "a" 이후 숫자 추출
	            int index;
	            try {
	            	index = Integer.parseInt(action.split("\\s+")[1]);
	            } catch(Exception e) {
	            	return false;
	            }
                boardManage.generateTemporaryTileList(player, index);
                return false;
            } else if (action.startsWith("e")) {
            	// "e" 이후 숫자 추출
	            int index;
	            try {
	            	index = Integer.parseInt(action.split("\\s+")[1]);
	            } catch(Exception e) {
	            	return false;
	            }
                boardManage.editOnBoardTileList(player, index);
                return false;
            } else if (action.equals("p")) {
                handlePickAction(player);
                return true;
            } else if (action.equals("-1")) {
            	return false;
            } else {
              System.out.println("잘못된 선택지입니다. 다시 입력하세요.");
            	return false;
            }
        }
    }
    
    // 타일 가져오기 액션 (P)
    public void handlePickAction(Player currentPlayer) {
        if (!tileListManage.isTileListNull(noPickTileList)) {
            Tile tile = tileListManage.noPickTileDivide(currentPlayer.tileList);
            System.out.print(currentPlayer.name + "에게 [");
            tileListManage.tilePrint(tile);
            System.out.println("] 카드가 추가되었습니다.");
            
            Server.sendTileListToClient(currentPlayer);
            Server.sendTileListSizeToClient();
        }
    }
    
    // 다음 턴으로 넘기는 함수
    private void advanceTurn() {
        if (playerTurn == 1) {
            playerTurn = 2;
        } else if (playerTurn == 2) {
            playerTurn = 3;
        } else if (playerTurn == 3) {
            playerTurn = 4;
        } else {
            playerTurn = 1;
        }
    }
        
    public Player getCurrentPlayer() {
        return switch (playerTurn) {
            case 1 -> player1;
            case 2 -> player2;
            case 3 -> player3;
            default -> player4;
        };
    }
}