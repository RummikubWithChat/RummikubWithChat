package model.board;

import model.player.Player;
import model.tile.Tile;
import model.tile.TileColor;
import model.tile.TileList;
import network.JavaChatServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Objects;

import static model.game.PlayChoice.*;

public class Board {
    public static ArrayList<LinkedList<Tile>> onBoardTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> turnCheckCompleteTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> previousTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> playerPutTileList = new ArrayList<>(106);

    public static LinkedList<Tile> temporaryTile = new LinkedList<Tile>();
    
    public static LinkedList<Tile> temporaryEditTile = new LinkedList<Tile>();
    
    public static ArrayList<LinkedList<Tile>> temporaryTileList = new ArrayList<>(106);
    
    private final TileList tileManage;
    private int onBoardTileSum = 0;

    public Board(TileList tileManage) {
        this.tileManage = tileManage;
    }

    public void turnChanged(Player player) {
        // Skip turn check if player just ended turn without playing
        if (turnCheckCompleteTileList.isEmpty()) {
            // Only update previousTileList
            previousTileList = new ArrayList<>(106);
            previousTileList.addAll(onBoardTileList);
            return;
        }

        boolean isPlayerTurnSucceed = turnCheck(player);
        if (isPlayerTurnSucceed) {
            turnIsSucceed();
        } else {
            turnIsFailed(player);
        }
    }

    public boolean turnCheck(Player player) {
        for (LinkedList<Tile> tileLinkedList : onBoardTileList) {
            if (tileLinkedList.size() < 3 && player.isRegisterCheck) {
                return false;
            }
        }

        // 등록이 되지 않은 경우에 모든 숫자를 가져와서 더해줌
        if (!player.isRegisterCheck) {
            for (LinkedList<Tile> tiles : turnCheckCompleteTileList) {
                for (Tile tile : tiles) {
                    onBoardTileSum += tile.number;
                }
            }

            if (onBoardTileSum >= 30) {
                player.isRegisterCheck = true;
                System.out.println("카드의 총 합이 30을 넘어, 등록이 완료되었습니다!");
                return true;
            } else {
                if(!Objects.equals(player.name, "ai") && !Objects.equals(player.name, "AI"))
                    System.out.println("기존에 등록이 진행되지 않았고, 낸 카드의 합이 30 미만입니다.");
                return false;
            }
        }

        return true;
    }

    public void turnIsFailed(Player player) {
        System.out.println("조건이 충족되지 않았으므로, 기존 배열로 돌아갑니다.");

        // 보드를 다시 되돌리기 (조건 충족X)
        onBoardTileList.removeAll(turnCheckCompleteTileList);
        onBoardTileList.addAll(previousTileList);

        for (LinkedList<Tile> tiles : turnCheckCompleteTileList) {
            player.tileList.addAll(tiles);
        }

        turnCheckCompleteTileList = new ArrayList<>(106);
        onBoardTileSum = 0;
    }

    public void turnIsSucceed() {
        turnCheckCompleteTileList = new ArrayList<>(106);
        onBoardTileSum = 0;

        previousTileList = new ArrayList<>(106);
        previousTileList.addAll(onBoardTileList);
    }

    // 각 플레이어의 임시 배열
    public void generateTemporaryTileList(Player player) {
        int result = 0;
        while (true) {
            tileManage.tileListPrint(player.tileList, player);
            System.out.print("\n\n현재 임시 배열 : ");
            tileManage.tileLinkPrint(temporaryTile);
                        
            try {
                result = tileIndexPick(player); // result를 메서드 호출로 받음
                if (result == -1) break;

                if (result < -1 || result >= player.tileList.size()) {
                    System.out.println("잘못된 값을 입력하였습니다. 다시 입력하세요.");
                } else {
                    temporaryTile.add(player.tileList.get(result));
                    player.tileList.remove(result);
                    
                    // 타일이 추가될 때마다 temporaryTileList 갱신
                    temporaryTileList.clear();
                    temporaryTileList.addAll(onBoardTileList);
                    // temporaryTile이 비어있지 않은 경우에만 추가
                    if (!temporaryTile.isEmpty()) {
                        temporaryTileList.add(temporaryTile);
                    }
                    JavaChatServer.sendTemporaryTileListToClient();
                }
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해야 합니다. 다시 입력하세요.");
            }
        }

        boolean isTemporaryComplete = generateTempCheck(player);
        if (isTemporaryComplete) {
            turnCheckCompleteTileList.add(temporaryTile);
            onBoardTileList.add(temporaryTile);
            
            // 최종 결과 갱신
            temporaryTileList.clear();
            temporaryTileList.addAll(onBoardTileList);
            JavaChatServer.sendTemporaryTileListToClient();
        } else {
            player.tileList.addAll(temporaryTile);
            // 실패 시 temporaryTileList에서 temporaryTile 제거
            temporaryTileList.clear();
            temporaryTileList.addAll(onBoardTileList);
            JavaChatServer.sendTemporaryTileListToClient();
        }        
        temporaryTile = new LinkedList<Tile>();
    }

    public Boolean generateTempCheck(Player player) {
        int size = temporaryTile.size();

        if (size < 3) {
            System.out.println("임시 배열의 카드가 3개 미만입니다. 등록에 실패하였습니다.");
            return false;
        }

        Boolean sameNumber = false;
        int numberStack = 0;

        Boolean sameColor = false;
        int colorStack = 0;

        for (int i = 0; i < size - 1; i++) {
            if ((temporaryTile.get(i).number == temporaryTile.get(i + 1).number - 1) && (temporaryTile.get(i).color == temporaryTile.get(i + 1).color)) {
                colorStack += 1;
            }

            if ((temporaryTile.get(i).number == temporaryTile.get(i + 1).number) && (temporaryTile.get(i).color != temporaryTile.get(i + 1).color)) {
                numberStack += 1;
            }

            if (temporaryTile.get(i).number == 999 || temporaryTile.get(i + 1).number == 999) {
                colorStack += 1;
                numberStack += 1;
            }
        }

        if (size - 1 == colorStack) {
            return true;
        } else if (size - 1 == numberStack) {
            return true;
        } else {
            System.out.println("색깔이나 숫자가 연속되지 않았습니다. 등록에 실패하였습니다.");
            return false;
        }
    }

    public void editOnBoardTileList(Player player) {
        int result = 0;

        while (true) {
            int edit;
            try {
                edit = editCheck(player); // 숫자 입력을 기대
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
                continue;
            }

            if (edit == -1) break;

            tileManage.tileLinkListPrint(onBoardTileList);

            int index;
            try {
                index = onBoardTileIndexPick(player); // 숫자 입력을 기대
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
                continue;
            }

            if (index > onBoardTileList.size() - 1) {
                System.out.println("인덱스의 범위가 잘못되었습니다.");
                continue;
            }

            temporaryEditTile.addAll(onBoardTileList.get(index));
            System.out.println("temporaryEditTile: " + temporaryEditTile);
            tileManage.tileLinkPrint(onBoardTileList.get(index));

//            int detailIndex;
//            try {
//                detailIndex = detailIndexPick(player, index); // 숫자 입력을 기대
//            } catch (NumberFormatException e) {
//                System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
//                continue;
//            }
//
//            if (detailIndex > onBoardTileList.get(index).size() - 1) {
//                System.out.println("인덱스의 범위가 잘못되었습니다.");
//                continue;
//            }

            while (true) {
                tileManage.tileListPrint(player.tileList, player);
                            
                try {
                    result = tileIndexPick(player); // result를 메서드 호출로 받음
                    if (result == -1) break;

                    if (result < -1 || result >= player.tileList.size()) {
                        System.out.println("잘못된 값을 입력하였습니다. 다시 입력하세요.");
                    } else {
                    	temporaryEditTile.add(player.tileList.get(result));
                    	temporaryTile.add(player.tileList.get(result));
                        player.tileList.remove(result);
                        
                        // 타일이 추가될 때마다 temporaryTileList 갱신
                        System.out.println("temporaryEditTile: " + temporaryEditTile);
                        temporaryTileList.clear();
                        temporaryTileList.addAll(onBoardTileList);
                        // temporaryTile이 비어있지 않은 경우에만 추가
                        if (!temporaryEditTile.isEmpty()) {
                            temporaryTileList.add(temporaryEditTile);
                        }
                        JavaChatServer.sendTemporaryTileListToClient();
                    }
                } catch (NumberFormatException e) {
                    System.out.println("숫자를 입력해야 합니다. 다시 입력하세요.");
                }
//                tileManage.tileListPrint(player.tileList, player);
//
//                try {
//                    result = tileIndexPick(player); // 숫자 입력을 기대
//                } catch (NumberFormatException e) {
//                    System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
//                    continue;
//                }
//
//                if (result == -1) break;
//
//                if (result < -1 || result >= player.tileList.size()) {
//                    System.out.println("잘못된 값을 입력하였습니다. 다시 입력하세요");
//                    continue;
//                }
//
//                int work;
//                try {
//                    work = workPick(player); // 숫자 입력을 기대
//                } catch (NumberFormatException e) {
//                    System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
//                    continue;
//                }
//
//                if (work == 1) {
//                    boolean workCheck = workChecking(index, detailIndex, result, player);
//
//                    if (workCheck) {
//                        onBoardTileList.get(index).add(detailIndex, player.tileList.get(result));
//                        player.tileList.remove(result);
//                    }
//                } else if (work == 2) {
//                    boolean workCheck = workChecking(index, detailIndex, result, player);
//
//                    if (workCheck) {
//                        onBoardTileList.get(index).add(detailIndex + 1, player.tileList.get(result));
//                        player.tileList.remove(result);
//                    }
//                }
//            } else if (edit == 2) {
//                boolean splitCheck = splitCheck(index, detailIndex, player);
//
//                if (splitCheck) splitOnBoardTileList(player, index, detailIndex);
            } 
            
            LinkedList<Tile> isList = EditTempCheck(temporaryEditTile);
            System.out.println(isList);
            if (isList != null) {
                turnCheckCompleteTileList.add(isList);
                onBoardTileList.remove(index);
                onBoardTileList.add(isList);
                
                // 최종 결과 갱신
                temporaryTileList.clear();
                temporaryTileList.addAll(onBoardTileList);
                JavaChatServer.sendTemporaryTileListToClient();
            } else {
                player.tileList.addAll(temporaryTile);
                // 실패 시 temporaryTileList에서 temporaryTile 제거
                temporaryTileList.clear();
                temporaryTileList.addAll(onBoardTileList);
                JavaChatServer.sendTemporaryTileListToClient();
            }        
            temporaryTile = new LinkedList<Tile>();
            temporaryEditTile = new LinkedList<Tile>();
        }
    }
    
    public static LinkedList<Tile> EditTempCheck(LinkedList<Tile> temporaryEditTile) {
        int size = temporaryEditTile.size();

        // 타일이 3개 미만이면 Rummikub 규칙에 어긋남
        if (size < 3) {
            System.out.println("타일이 3개 미만입니다. 유효하지 않습니다.");
            return null;
        }

        // 색상이 같은지 확인하는 변수
        boolean sameColor = true;
        TileColor firstColor = null;

        // 첫 번째 색상을 조커가 아닐 때 초기화
        for (Tile tile : temporaryEditTile) {
            if (tile.number != 999) {
                firstColor = tile.color;
                break;
            }
        }

        // 숫자가 같은지 확인하는 변수
        boolean sameNumber = true;
        int firstNumber = temporaryEditTile.get(0).number;

        // 조커 개수를 추적
        int jokerCount = 0;

        for (Tile tile : temporaryEditTile) {
            if (tile.number == 999) {
                jokerCount++;
            } else {
                if (tile.color != firstColor) {
                    sameColor = false;
                }
                if (tile.number != firstNumber) {
                    sameNumber = false;
                }
            }
        }

        // 색상과 숫자 패턴이 맞는지 검사
        if (sameColor) {
            // 숫자를 기준으로 정렬
            temporaryEditTile.sort(Comparator.comparingInt(tile -> tile.number == 999 ? Integer.MAX_VALUE : tile.number));

            int neededJokers = 0;

            for (int i = 1; i < size; i++) {
                int prevNumber = temporaryEditTile.get(i - 1).number == 999 ? -1 : temporaryEditTile.get(i - 1).number;
                int currentNumber = temporaryEditTile.get(i).number == 999 ? -1 : temporaryEditTile.get(i).number;

                if (currentNumber == -1) {
                    // 조커가 필요한 경우
                    neededJokers++;
                    currentNumber = prevNumber + 1; // 조커가 이전 숫자를 기반으로 연속성 유지
                }

                if (currentNumber != prevNumber + 1) {
                    System.out.println("연속된 숫자가 아닙니다. 유효하지 않습니다.");
                    return null;
                }
            }

            if (neededJokers > jokerCount) {
                System.out.println("조커가 부족합니다. 유효하지 않습니다.");
                return null;
            }
        } else if (sameNumber) {
            // 같은 숫자 타일은 색상이 중복되지 않아야 함
            temporaryEditTile.sort(Comparator.comparingInt(tile -> tile.color.ordinal()));

            for (int i = 1; i < size; i++) {
                TileColor prevColor = temporaryEditTile.get(i - 1).color;
                TileColor currentColor = temporaryEditTile.get(i).color;

                if (temporaryEditTile.get(i - 1).number != 999 && temporaryEditTile.get(i).number != 999 && currentColor == prevColor) {
                    System.out.println("같은 색상이 반복됩니다. 유효하지 않습니다.");
                    return null;
                }
            }
        } else {
            System.out.println("타일이 Rummikub 규칙을 충족하지 않습니다.");
            return null;
        }

        // 유효하다면 정렬된 리스트를 반환
        System.out.println("타일이 유효합니다.");
        return temporaryEditTile;
    }



    private boolean workChecking(int index, int detailIndex, int result, Player player) {
        // 숫자가 같은 경우, 색깔이 달라야 함
        if (isNumSameAndColorDiffer(index, detailIndex, result, player)) {
            return true;
        }

        // 색깔이 같은 경우, 숫자가 달라야 함
        else if (isNumDifferAndColorSame(index, detailIndex, result, player)) {
            return true;
        }

        else if (player.tileList.get(result).number == 999) {
            return true;
        }

        else {
            System.out.println("해당 요소는 해당 위치에 들어갈 수 없습니다.");
            return false;
        }
    }

    private boolean isNumSameAndColorDiffer(int index, int detailIndex, int result, Player player){
        return (onBoardTileList.get(index).get(detailIndex).number == player.tileList.get(result).number) &&
                (onBoardTileList.get(index).get(detailIndex).color != player.tileList.get(result).color);
    }

    private boolean isNumDifferAndColorSame(int index, int detailIndex, int result, Player player){
        return ((onBoardTileList.get(index).get(detailIndex).number == player.tileList.get(result).number + 1)
                || (onBoardTileList.get(index).get(detailIndex).number == player.tileList.get(result).number - 1))
                && (onBoardTileList.get(index).get(detailIndex).color == player.tileList.get(result).color);
    }

    private boolean splitCheck(int index, int detailIndex, Player player) {
        // 온보딩의 사이즈가 3이 넘는 지 확인, 왼쪽 오른쪽 기준으로 3이 넘는지 확인
        if (onBoardTileList.get(index).size() > 3) {
            if(onBoardTileList.get(detailIndex - 3) != null && onBoardTileList.get(detailIndex + 3) != null){
                return true;
            }

            else {
                System.out.println("타일을 구분 시, 좌/우로 3개가 남지 않으므로 구분이 불가능합니다.");
                return false;
            }
        } else {
            System.out.println("타일이 3개 이하이므로, 타일 구분이 불가능합니다.");
            return false;
        }
    }

    public void splitOnBoardTileList(Player player, int index, int detailIndex) {
        LinkedList<Tile> temp = new LinkedList<Tile>();

        for(int i = detailIndex; i<onBoardTileList.size(); i++){
            temp.add(onBoardTileList.get(index).get(i));
        }

        onBoardTileList.add(temp);
        onBoardTileList.get(index).removeAll(temp);
    }

    public int getOnBoardTileListSize() {
        return onBoardTileList.size();
    }
    
    // ArrayList<LinkedList<Tile>>를 문자열로 변환하는 메서드
    public String previousTileListToString() {
    	return previousTileList.toString();
    }
    
    public String onBoardTileListToString() {
    	return onBoardTileList.toString();
    }
    public String temporaryTileListToString() {
    	return temporaryTileList.toString();
    }
}