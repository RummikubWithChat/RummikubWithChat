package model.board;

import model.player.Player;
import model.tile.Tile;
import model.tile.TileColor;
import model.tile.TileList;
import network.JavaChatServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static model.game.PlayChoice.*;

public class Board {
    public static ArrayList<LinkedList<Tile>> onBoardTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> turnCheckCompleteTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> previousTileList = new ArrayList<>(106);
    public static ArrayList<LinkedList<Tile>> playerPutTileList = new ArrayList<>(106);

    // 임시 타일 배열
    public static LinkedList<Tile> temporaryTile = new LinkedList<Tile>();
    // 임시 타일리스트 배열
    public static ArrayList<LinkedList<Tile>> temporaryTileList = new ArrayList<>(106);
    
    // 보드 수정 시 임시 배열
    public static LinkedList<Tile> temporaryEditTile = new LinkedList<Tile>();
    // 보드 수정 실패 시 돌아갈 배열
    public static LinkedList<Tile> previousEditTile = new LinkedList<Tile>();
    
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
                    int tileValue = tile.number == 999 ? getJokerValue(tiles) : tile.number;
                    System.out.println("타일 값: " + tileValue);
                    onBoardTileSum += tileValue;
                }
            }

            if (onBoardTileSum >= 30) {
                player.isRegisterCheck = true;
                System.out.println("카드의 총 합이 " + onBoardTileSum + "으로, 등록이 완료되었습니다!");
                return true;
            } else {
                if(!Objects.equals(player.name, "ai") && !Objects.equals(player.name, "AI"))
                    System.out.println("기존에 등록이 진행되지 않았고, 낸 카드의 합이 " + onBoardTileSum + "으로 30 미만입니다.");
                return false;
            }
        }

        return true;
    }

    private int getJokerValue(LinkedList<Tile> tiles) {
        int jokerIndex = -1;

        // 조커가 들어있는 위치를 찾음
        for (int i = 0; i < tiles.size(); i++) {
            if (tiles.get(i).number == 999) {
                jokerIndex = i;
                break;
            }
        }

        if (jokerIndex == -1) {
            return 0; // 조커가 없을 경우
        }

        Tile prevTile = jokerIndex > 0 ? tiles.get(jokerIndex - 1) : null;
        Tile nextTile = jokerIndex < tiles.size() - 1 ? tiles.get(jokerIndex + 1) : null;

        // 연속된 숫자 패턴 확인
        boolean isSameColor = true;
        int expectedValue = 0;

        if (tiles.size() >= 3) {
            // 색상이 모두 같은지 확인 (조커 제외)
            TileColor baseColor = null;
            for (Tile tile : tiles) {
                if (tile.number != 999) {
                    if (baseColor == null) {
                        baseColor = tile.color;
                    } else if (tile.color != baseColor) {
                        isSameColor = false;
                        break;
                    }
                }
            }

            if (isSameColor) {
                // 연속된 숫자 패턴
                if (prevTile != null && nextTile != null) {
                    // 조커가 중간에 있는 경우
                    expectedValue = (prevTile.number + nextTile.number) / 2;
                } else if (prevTile != null) {
                    // 조커가 끝에 있는 경우
                    expectedValue = prevTile.number + 1;
                } else if (nextTile != null) {
                    // 조커가 처음에 있는 경우
                    expectedValue = nextTile.number - 1;
                }
            } else {
                // 같은 숫자 패턴
                for (Tile tile : tiles) {
                    if (tile.number != 999) {
                        expectedValue = tile.number;
                        break;
                    }
                }
            }
        }

        return expectedValue;
    }

    public void turnIsFailed(Player player) {
        System.out.println("조건이 충족되지 않았으므로, 기존 배열로 돌아갑니다.");

        // 보드를 다시 되돌리기 (조건 충족X)
        onBoardTileList.removeAll(turnCheckCompleteTileList);

        for (LinkedList<Tile> tiles : turnCheckCompleteTileList) {
            player.tileList.addAll(tiles);
        }
        JavaChatServer.sendTileListToClient(player);

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
    public void generateTemporaryTileList(Player player, int startIndex) {
        int result = startIndex;  // 시작 인덱스를 result에 할당
        while (true) {        
            try {
                // startIndex로 결과를 시작하고, 그 후 입력값을 받는다
                if (result == -1) break; // 종료 조건

                // result가 잘못된 인덱스인 경우에 대한 처리
                if (result < 0 || result >= player.tileList.size()) {
                    System.out.println("잘못된 값을 입력하였습니다. 다시 입력하세요.");
                } else {
                    temporaryTile.add(player.tileList.get(result));
                    player.tileList.remove(result);
                    
                    // 타일이 추가될 때마다 temporaryTileList 갱신
                    temporaryTileList.clear();
                    temporaryTileList.addAll(onBoardTileList);
                    if (!temporaryTile.isEmpty()) {
                        temporaryTileList.add(temporaryTile);
                    }
                    JavaChatServer.sendTemporaryTileListToClient();
                }
                
                tileManage.tileListPrint(player.tileList, player);
                System.out.print("\n\n현재 임시 배열 : ");
                tileManage.tileLinkPrint(temporaryTile);

                // 사용자 입력을 받을 때마다 result 값을 업데이트
                result = tileIndexPick(player);

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
            temporaryTileList.clear();
            temporaryTileList.addAll(onBoardTileList);
            JavaChatServer.sendTemporaryTileListToClient();
        }        
        temporaryTile = new LinkedList<Tile>();
    }

    public Boolean generateTempCheck(Player player) {
        int size = temporaryTile.size();

        // 타일 개수가 3개 미만이면 유효하지 않음
        if (size < 3) {
            System.out.println("임시 배열의 카드가 3개 미만입니다. 등록에 실패하였습니다.");
            return false;
        }

        System.out.print("현재 검사 중인 임시 배열: ");
        tileManage.tileLinkPrint(temporaryTile);

        int jokerCount = 0;
        List<Tile> nonJokerTiles = new ArrayList<>();

        // 조커와 비조커 타일 분리
        for (Tile tile : temporaryTile) {
            if (tile.number == 999) {
                jokerCount++;
            } else {
                nonJokerTiles.add(tile);
            }
        }

        // 비조커 타일들의 패턴 확인
        if (nonJokerTiles.isEmpty()) {
            System.out.println("모든 타일이 조커입니다.");
            return true;
        }

        // 연속된 숫자 패턴인지 체크
        boolean isConsecutivePattern = checkConsecutivePattern(nonJokerTiles, jokerCount);
        // 같은 숫자 패턴인지 체크
        boolean isSameNumberPattern = checkSameNumberPattern(nonJokerTiles, jokerCount);

        if (!isConsecutivePattern && !isSameNumberPattern) {
            System.out.println("유효하지 않은 타일 배치입니다.");
            return false;
        }

        System.out.println("타일이 유효합니다.");
        return true;
    }

    private boolean checkConsecutivePattern(List<Tile> nonJokerTiles, int jokerCount) {
        if (nonJokerTiles.isEmpty()) return false;
        
        // 숫자로 정렬
        nonJokerTiles.sort(Comparator.comparingInt(tile -> tile.number));
        
        // 같은 색상인지 확인
        TileColor color = nonJokerTiles.get(0).color;
        boolean sameColor = nonJokerTiles.stream().allMatch(tile -> tile.color == color);
        if (!sameColor) return false;

        // 연속된 숫자인지 확인 (조커 고려)
        int gaps = 0; // 연속되지 않은 간격의 합
        for (int i = 1; i < nonJokerTiles.size(); i++) {
            int gap = nonJokerTiles.get(i).number - nonJokerTiles.get(i-1).number - 1;
            if (gap < 0) return false; // 잘못된 순서
            gaps += gap;
        }

        // 조커로 채울 수 있는지 확인
        return gaps <= jokerCount;
    }

    private boolean checkSameNumberPattern(List<Tile> nonJokerTiles, int jokerCount) {
        if (nonJokerTiles.isEmpty()) return false;

        // 같은 숫자인지 확인
        int firstNumber = nonJokerTiles.get(0).number;
        boolean sameNumber = nonJokerTiles.stream().allMatch(tile -> tile.number == firstNumber);
        if (!sameNumber) return false;

        // 서로 다른 색상인지 확인
        Set<TileColor> colors = nonJokerTiles.stream()
            .map(tile -> tile.color)
            .collect(Collectors.toSet());
        
        // 색상이 중복되지 않아야 함
        return colors.size() == nonJokerTiles.size();
    }
    
    public void editOnBoardTileList(Player player, int startIndex) {
        int result = 0;

        while (true) {
            // 더 이상 edit 값을 입력받지 않음
            tileManage.tileLinkListPrint(onBoardTileList);

            int index = startIndex;  // startIndex를 index에 할당하여 시작
            try {
                // startIndex로 시작하는 인덱스를 사용
                if (index == -1) break;

                if (index > onBoardTileList.size() - 1 || index < 0) {
                    System.out.println("인덱스의 범위가 잘못되었습니다.");
                    continue;
                }

                temporaryEditTile.addAll(onBoardTileList.get(index));
                previousEditTile.addAll(onBoardTileList.get(index));

                System.out.println("temporaryEditTile: " + temporaryEditTile);
                tileManage.tileLinkPrint(onBoardTileList.get(index));

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
                            JavaChatServer.sendTileListToClient(player);
                            
                            // 타일이 추가될 때마다 temporaryTileList 갱신
                            System.out.println("temporaryEditTile: " + temporaryEditTile);
                            temporaryTileList.clear();
                            temporaryTileList.addAll(onBoardTileList);
                            // temporaryEditTile이 비어있지 않은 경우에만 추가
                            if (!temporaryEditTile.isEmpty()) {
                                temporaryTileList.remove(index);
                                temporaryTileList.add(temporaryEditTile);
                            }
                            JavaChatServer.sendTemporaryTileListToClient();
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("숫자를 입력해야 합니다. 다시 입력하세요.");
                    }
                } 

                ArrayList<LinkedList<Tile>> vaildTileList = validateAndSplitGroups(temporaryEditTile);
                
                System.out.println("vaildTileList: " + vaildTileList);
                if (vaildTileList != null) {
                    onBoardTileList.remove(index);
                    
                    for (LinkedList<Tile> tile : vaildTileList) {
                        turnCheckCompleteTileList.add(tile);
                        onBoardTileList.add(tile);
                    }
                    
                    // 최종 결과 갱신
                    temporaryTileList.clear();
                    temporaryTileList.addAll(onBoardTileList);
                    JavaChatServer.sendTemporaryTileListToClient();
                } else {
                    // 실패 시 tileList 재설정, temporaryTileList 재설정
                    player.tileList.addAll(temporaryTile);
                    JavaChatServer.sendTileListToClient(player);
                    temporaryTileList.clear();
                    temporaryTileList.addAll(onBoardTileList);
                    JavaChatServer.sendTemporaryTileListToClient();
                }        
                temporaryTile = new LinkedList<Tile>();
                temporaryEditTile = new LinkedList<Tile>();
                
                break;
            } catch (NumberFormatException e) {
                System.out.println("잘못된 입력입니다. 숫자를 입력하세요.");
            }
        }
    }

    public static ArrayList<LinkedList<Tile>> validateAndSplitGroups(LinkedList<Tile> temporaryEditTile) {
        if (temporaryEditTile.size() < 3) {
            System.out.println("타일이 3개 미만입니다. 유효하지 않습니다.");
            return null;
        }

        // 모든 가능한 분할 지점을 시도
        for (int i = 3; i <= temporaryEditTile.size() - 3; i++) {
            for (List<Tile> subGroup : getCombinations(temporaryEditTile, i)) {
                ArrayList<LinkedList<Tile>> validGroups = new ArrayList<>();
                LinkedList<Tile> remainingTiles = new LinkedList<>(temporaryEditTile);
                
                LinkedList<Tile> firstGroup = new LinkedList<>(subGroup);
                if (isValidGroup(firstGroup)) {
                    validGroups.add(firstGroup);
                    remainingTiles.removeAll(firstGroup);
                    
                    LinkedList<Tile> secondGroup = new LinkedList<>(remainingTiles);
                    if (isValidGroup(secondGroup)) {
                        validGroups.add(secondGroup);
                        return validGroups;
                    }
                }
            }
        }
        
        // 단일 그룹으로도 시도
        LinkedList<Tile> singleGroup = new LinkedList<>(temporaryEditTile);
        if (isValidGroup(singleGroup)) {
            ArrayList<LinkedList<Tile>> validGroups = new ArrayList<>();
            validGroups.add(singleGroup);
            
            for (LinkedList<Tile> group : validGroups) {
                sortTileGroup(group);
            }
            
            return validGroups;
        }

        System.out.println("유효한 그룹을 찾을 수 없습니다.");
        return null;
    }

    private static List<List<Tile>> getCombinations(LinkedList<Tile> tiles, int size) {
        List<List<Tile>> combinations = new ArrayList<>();
        getCombinationsHelper(tiles, size, 0, new ArrayList<>(), combinations);
        return combinations;
    }

    private static void getCombinationsHelper(LinkedList<Tile> tiles, int size, int start, 
                                            List<Tile> current, List<List<Tile>> result) {
        if (current.size() == size) {
            result.add(new ArrayList<>(current));
            return;
        }
        
        for (int i = start; i < tiles.size(); i++) {
            current.add(tiles.get(i));
            getCombinationsHelper(tiles, size, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }
    
    private static void sortTileGroup(LinkedList<Tile> group) {
        // 먼저 첫 번째 타일로 그룹 타입 파악 (연속된 숫자인지, 같은 숫자인지)
        boolean isConsecutive = isConsecutiveGroup(group);
        
        if (isConsecutive) {
            // 연속된 숫자 그룹은 숫자 기준 오름차순 정렬
            group.sort(Comparator.comparingInt(tile -> tile.number == 999 ? 
                findJokerNumber(group, tile) : tile.number));
        } else {
            // 같은 숫자 그룹은 색상 기준 정렬
            group.sort(Comparator.comparingInt(tile -> tile.color.ordinal()));
        }
    }
    
    private static boolean isConsecutiveGroup(LinkedList<Tile> group) {
        // 조커를 제외한 타일들의 색상이 모두 같으면 연속된 숫자 그룹
        TileColor firstColor = null;
        for (Tile tile : group) {
            if (tile.number != 999) {
                if (firstColor == null) {
                    firstColor = tile.color;
                } else if (tile.color != firstColor) {
                    return false;
                }
            }
        }
        return true;
    }

    private static int findJokerNumber(LinkedList<Tile> group, Tile joker) {
        // 조커의 숫자 값을 결정 (연속된 숫자에서 빈 곳 채우기)
        List<Integer> numbers = new ArrayList<>();
        for (Tile tile : group) {
            if (tile.number != 999) {
                numbers.add(tile.number);
            }
        }
        Collections.sort(numbers);
        
        // 첫 번째 빈 숫자 찾기
        for (int i = 1; i < numbers.size(); i++) {
            if (numbers.get(i) - numbers.get(i-1) > 1) {
                return numbers.get(i-1) + 1;
            }
        }
        
        // 앞뒤에 없으면 양 끝 확인
        if (!numbers.isEmpty()) {
            if (numbers.get(0) > 1) return numbers.get(0) - 1;
            return numbers.get(numbers.size() - 1) + 1;
        }
        
        return 999; // 기본값
    }

    private static boolean isValidGroup(LinkedList<Tile> group) {
        if (group.size() < 3) return false;
        
        // 색상이 같은지 확인
        boolean sameColor = true;
        TileColor firstColor = null;
        for (Tile tile : group) {
            if (tile.number != 999) {
                if (firstColor == null) {
                    firstColor = tile.color;
                } else if (tile.color != firstColor) {
                    sameColor = false;
                    break;
                }
            }
        }
        
        if (sameColor) {
            return checkConsecutiveNumbers(group);
        }
        
        // 숫자가 같은지 확인
        return checkSameNumbers(group);
    }
    
    private static boolean checkConsecutiveNumbers(LinkedList<Tile> group) {
        List<Integer> numbers = new ArrayList<>();
        int jokerCount = 0;
        
        // 조커와 일반 타일 분리
        for (Tile tile : group) {
            if (tile.number == 999) {
                jokerCount++;
            } else {
                numbers.add(tile.number);
            }
        }
        
        // 숫자 정렬
        Collections.sort(numbers);
        
        // 연속된 숫자 확인
        for (int i = 1; i < numbers.size(); i++) {
            int gap = numbers.get(i) - numbers.get(i-1) - 1;
            if (gap > 0) {
                jokerCount -= gap; // 빈 칸을 채우는데 조커 사용
                if (jokerCount < 0) return false;
            }
        }
        
        return true;
    }

    private static boolean checkSameNumbers(LinkedList<Tile> group) {
        Set<TileColor> colors = new HashSet<>();
        Integer targetNumber = null;
        int jokerCount = 0;
        
        for (Tile tile : group) {
            if (tile.number == 999) {
                jokerCount++;
                continue;
            }
            
            if (targetNumber == null) {
                targetNumber = tile.number;
            } else if (tile.number != targetNumber) {
                return false;
            }
            
            if (!colors.add(tile.color)) { // 같은 색상이 이미 존재
                return false;
            }
        }
        
        return (colors.size() + jokerCount) >= 3;
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