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

public record GamePlaying(Board boardManage, TileList tileListManage,
                          Player player1, Player player2, Player player3, Player player4) {
    private static int playerTurn = 1; // 플레이어 순서

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

        // 나를 제외한 다른 플레이어 리스트 얻기
        // List<Player> otherPlayers = getOtherPlayers(currentPlayer);

        JavaChatServer.sendBoardTileListToClient();
        
        do {
        	System.out.println("현재 턴: " + playerTurn);
        	JavaChatServer.sendIsTurnToClient(currentPlayer);
            tileListManage.tileLinkListPrint(onBoardTileList); //보드 타일 출력 
            if (playerTurn == 1) {
                tileListManage.tileListPrint(player1.tileList, player1);
                playChoice = pickOrShow(currentPlayer);
                turnComplete = choiceCheck(playChoice);
                
            } else if (playerTurn == 2){
                tileListManage.tileListPrint(player2.tileList, player2);
                playChoice = pickOrShow(currentPlayer);
                turnComplete = choiceCheck(playChoice);
                
            } else if (playerTurn == 3){
                tileListManage.tileListPrint(player3.tileList, player3);
                playChoice = pickOrShow(currentPlayer);
                turnComplete = choiceCheck(playChoice);
                
            } else {
                tileListManage.tileListPrint(player4.tileList, player4);
                playChoice = pickOrShow(currentPlayer);
                turnComplete = choiceCheck(playChoice);
            }
        } while (!turnComplete);

        // 다음 턴으로 넘기기
        if ((Objects.equals(playChoice, "p")) || (Objects.equals(playChoice, "P"))) {
            if (playerTurn == 1) 
            	playerTurn = 2;
            else if (playerTurn == 2)
            	playerTurn = 3;
            else if (playerTurn == 3)
            	playerTurn = 4;
            else playerTurn = 1;
        } else {
            if (playerTurn == 1) {
                boardManage.turnChanged(player1);
                playerTurn = 2;
            } else if(playerTurn == 2) {
                boardManage.turnChanged(player2);
                playerTurn = 3;
            } else if(playerTurn == 3) {
            	boardManage.turnChanged(player3);
                playerTurn = 4;
            } else {
            	boardManage.turnChanged(player4);
                playerTurn = 1;
            }
        }
    }

    // 현재 플레이어를 제외한 다른 플레이어 리스트 반환
    public List<Player> getOtherPlayers(Player currentPlayer) {
        List<Player> allPlayers = new ArrayList<>(List.of(player1, player2, player3, player4));
        allPlayers.remove(currentPlayer); // currentPlayer를 제외
        return allPlayers;
    }
    
    public Player getCurrentPlayer() {
        return switch (playerTurn) {
            case 1 -> player1;
            case 2 -> player2;
            case 3 -> player3;
            default -> player4;
        };
    }

    // public int getCurrentPlayerTurn() {
    //     return playerTurn;
    // }

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

        // 카드 가져오기 (p)
        if (Objects.equals(playChoice, "p") || Objects.equals(playChoice, "P")) {
            if (tileListManage.isTileListNull(noPickTileList)) {
            } else {
                Tile tile = tileListManage.noPickTileDivide(playerList);
                System.out.print(playerName + "에게 [");
                tileListManage.tilePrint(tile);
                System.out.println("] 카드가 추가되었습니다.");
            }

            JavaChatServer.sendTileListToClient(player);            
            return true;
        }

        // 숫자 기준으로 정렬 (n)
        else if (Objects.equals(playChoice, "n") || Objects.equals(playChoice, "N")) {
            tileListManage.tileSortToNumber(playerList);
            return false;
        }
        
        // 색깔 기준으로 정렬 (c)
        else if (Objects.equals(playChoice, "c") || Objects.equals(playChoice, "C")) {
            tileListManage.tileSortToColor(playerList);
            return false;
        }

        // 카드 내기 (s)
        else if (Objects.equals(playChoice, "s") || Objects.equals(playChoice, "S")) {
            if (!player.isRegisterCheck) {
                boardManage.generateTemporaryTileList(player);
            } else {
                String choiceAddOrEdit = cardListAddOrEdit(player);
                if (Objects.equals(choiceAddOrEdit, "a") || Objects.equals(choiceAddOrEdit, "A")) {
                    boardManage.generateTemporaryTileList(player);
                } else if (Objects.equals(choiceAddOrEdit, "e") || Objects.equals(choiceAddOrEdit, "E")) {
                    boardManage.editOnBoardTileList(player);
                }
            }
            
            return false;
        } else if (Objects.equals(playChoice, "e") || Objects.equals(playChoice, "E")) {
            return true; //턴 종료 
        } else {
            System.out.println("잘못된 선택지입니다. 다시 입력하세요.");
            pickOrShow(player);
            return false;
        }
    }
}