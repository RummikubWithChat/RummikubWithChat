package model.game;

import model.board.Board;
import model.player.Player;
import model.tile.Tile;
import model.tile.TileList;
import network.JavaChatServer;
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
        System.out.println("isSelectedOnBoard 변경: " + selected);
    	isSelectedOnBoard = selected;
    }

    public static void setIsSelectedOnTileList(boolean selected) {
        System.out.println("isSelectedOnTileList 변경: " + selected);
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

        JavaChatServer.sendBoardTileListToClient();
        
        do {
            System.out.println("현재 턴: " + playerTurn);
            JavaChatServer.sendIsTurnToClient(currentPlayer);
            tileListManage.tileLinkListPrint(onBoardTileList); // 보드 타일 출력 
            tileListManage.tileListPrint(currentPlayer.tileList, currentPlayer);
            
            playChoice = cardListAddOrEdit(currentPlayer);
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

        // 다음 턴으로 넘기기
//        if ((Objects.equals(playChoice, "p")) || (Objects.equals(playChoice, "P"))) {
//            advanceTurn();
//        } else {
            boardManage.turnChanged(currentPlayer);
            advanceTurn();
//        }
    }
    
    private Boolean choiceCheck(String playChoice) {
        ArrayList<Tile> playerList = null;
        String playerName = null;
        Player player;

        if (playerTurn == 1) {
            playerList = player1.tileList;
            playerName = player1.name;
            player = player1;
        } else if (playerTurn == 2) {
            playerList = player2.tileList;
            playerName = player2.name;
            player = player2;
        } else if (playerTurn == 3) {
            playerList = player3.tileList;
            playerName = player3.name;
            player = player3;
        } else {
            playerList = player4.tileList;
            playerName = player4.name;
            player = player4;
        }
        
        if (playChoice == null) {
            System.out.println("선택지가 없습니다. 다시 입력하세요.");
            return false; // 기본적으로 false를 반환하여 플레이어가 다시 시도하도록 설정
        }

//        // 카드 가져오기 (p)
//        if (Objects.equals(playChoice, "p") || Objects.equals(playChoice, "P")) {
//            handlePickAction(player);           
//            return true;
//        }
//
//        // 숫자 기준으로 정렬 (n)
//        else if (Objects.equals(playChoice, "n") || Objects.equals(playChoice, "N")) {
//            tileListManage.tileSortToNumber(playerList);
//            return false;
//        }
//        
//        // 색깔 기준으로 정렬 (c)
//        else if (Objects.equals(playChoice, "c") || Objects.equals(playChoice, "C")) {
//            tileListManage.tileSortToColor(playerList);
//            return false;
//        }

        // 카드 내기 (s)
//        else if (Objects.equals(playChoice, "s") || Objects.equals(playChoice, "S")) {
            if (!player.isRegisterCheck) {
            	if (playChoice.equals("p")) {
                    handlePickAction(player);
                    return true;
                } else if (playChoice.equals("e")) {
                	return true;
                }
                boardManage.generateTemporaryTileList(player, 999);
                return false;
            } else {
                String choiceAddOrEdit = cardListAddOrEdit(player);
                System.out.println("choiceAddOrEdit" + choiceAddOrEdit);
//                if (Objects.equals(choiceAddOrEdit, "a") || Objects.equals(choiceAddOrEdit, "A")
//                		|| isSelectedOnTileList == true) {
//                    boardManage.generateTemporaryTileList(player);
//                } else if (Objects.equals(choiceAddOrEdit, "e") || Objects.equals(choiceAddOrEdit, "E")
//                		|| isSelectedOnBoard == true) {
//                if (isSelectedOnTileList == true) {
//                    boardManage.generateTemporaryTileList(player);
//                } else if (isSelectedOnBoard == true) {
                if (choiceAddOrEdit.startsWith("a")) {
                	// "a" 이후 숫자 추출
    	            int index;
    	            try {
    	            	index = Integer.parseInt(choiceAddOrEdit.split("\\s+")[1]);
    	            } catch(Exception e) {
    	            	index = 999; // 유효하지 않은 값 넘겨 generateTemporaryTileList에서 처리하도록
    	            }
                    boardManage.generateTemporaryTileList(player, index);
                    return false;
                } else if (choiceAddOrEdit.startsWith("e")) {
                	// "e" 이후 숫자 추출
    	            int index;
    	            try {
    	            	index = Integer.parseInt(choiceAddOrEdit.split("\\s+")[1]);
    	            } catch(Exception e) {
    	            	index = 999; // 유효하지 않은 값 넘겨 generateTemporaryTileList에서 처리하도록
    	            }
                    boardManage.editOnBoardTileList(player, index);
                    return false;
                } else if (choiceAddOrEdit.equals("p")) {
                    handlePickAction(player);
                    return true;
                } else if (choiceAddOrEdit.equals("e")) {
                	return true;
                } else {
                  System.out.println("잘못된 선택지입니다. 다시 입력하세요.");
                	return false;
                }
            }
//            
//            return false;
//        } else if (Objects.equals(playChoice, "e") || Objects.equals(playChoice, "E")) {
//            return true; //턴 종료 
//        } else {
//            System.out.println("잘못된 선택지입니다. 다시 입력하세요.");
//            pickOrShow(player);
//            return false;
//        }
    }
    
    // 타일 가져오기 액션 (P)
    public void handlePickAction(Player currentPlayer) {
        if (!tileListManage.isTileListNull(noPickTileList)) {
            Tile tile = tileListManage.noPickTileDivide(currentPlayer.tileList);
            System.out.print(currentPlayer.name + "에게 [");
            tileListManage.tilePrint(tile);
            System.out.println("] 카드가 추가되었습니다.");
            
            JavaChatServer.sendTileListToClient(currentPlayer);
            JavaChatServer.sendTileListSizeToClient();
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

    //연속된 숫자의 타일인지 확인하는 메서드 
    private Boolean isConstantNumber(ArrayList<Tile> playerList, int i){
        return (playerList.get(i).number == playerList.get(i + 1).number &&
                    playerList.get(i + 1).number == playerList.get(i + 2).number) &&

                (playerList.get(i).color != playerList.get(i + 1).color &&
                        playerList.get(i + 1).color != playerList.get(i + 2).color
                        && playerList.get(i).color != playerList.get(i + 2).color);
    }

    //동일한 숫자의 타일인지 확인하는 메서드 
    private Boolean isSameNumber(ArrayList<Tile> playerList, int i){
        return (playerList.get(i).number == playerList.get(i + 1).number - 1 &&
                playerList.get(i + 1).number - 1 == playerList.get(i + 2).number - 2) &&

                (playerList.get(i).color == playerList.get(i + 1).color &&
                playerList.get(i + 1).color == playerList.get(i + 2).color &&
                playerList.get(i).color == playerList.get(i + 2).color);
    }

    private void cardInsert(ArrayList<Tile> playerList, int i){
        for (int j = 0; j < 3; j++) {
            temporaryTile.add(playerList.get(i)); //임시 타일에 추가 
            playerList.remove(i); //플레이어 타일 리스트에서 해당 타일 제거
        }

        onBoardTileList.add(temporaryTile); //보드 타일에 추가 
        temporaryTile = new LinkedList<Tile>(); //플레이어 타일 리스트에서 해당 타일 제거 
    }
}