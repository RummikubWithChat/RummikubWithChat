package model.tile;

import java.util.*;

import model.player.Player;
import network.JavaChatServer;

import static model.game.GameInitAndEndSet.gameEnd;
//import static network.Server.sendToClient;
import static network.JavaChatServer.sendToClient;

public class TileList {
	//전체 타일 목록
    public static ArrayList<Tile> noPickTileList = new ArrayList<Tile>(106);

    public void push() {
        int i = 0;
        for (int r = 0; r < 2; r++) { //타일 세트 2번 생성
            for (TileColor tile : TileColor.values()) {
                if (tile == TileColor.RED) {
                    for (int j = 1; j < 14; j++) {
                        noPickTileList.add(i, new Tile(j, TileColor.RED));
                        i++;
                    }
                }

                if (tile == TileColor.YELLOW) {
                    for (int j = 1; j < 14; j++) {
                        noPickTileList.add(i, new Tile(j, TileColor.YELLOW));
                        i++;
                    }
                }

                if (tile == TileColor.BLUE) {
                    for (int j = 1; j < 14; j++) {
                        noPickTileList.add(i, new Tile(j, TileColor.BLUE));
                        i++;
                    }
                }

                if (tile == TileColor.WHITE) {
                    for (int j = 1; j < 14; j++) {
                        noPickTileList.add(i, new Tile(j, TileColor.WHITE));
                        i++;
                    }
                }
            }
        }

        noPickTileList.add(i++, new Tile(999, TileColor.WHITE));
        noPickTileList.add(i, new Tile(999, TileColor.RED));
    }

    public Tile pop() {
        int listSize = getStackSize(noPickTileList);
        Tile popElement = noPickTileList.get(listSize - 1);
        noPickTileList.remove(listSize - 1);

        return popElement;
    }
    
 // 플레이어의 타일 리스트에 랜덤으로 타일 추가
    public void addTileToPlayer(ArrayList<Tile> playerTileList) {
        if (!noPickTileList.isEmpty()) {
            // 타일 리스트에서 랜덤으로 타일을 하나 뽑아 플레이어의 타일 리스트에 추가
            int randomIndex = new Random().nextInt(noPickTileList.size());
            Tile tile = noPickTileList.remove(randomIndex); // 타일을 리스트에서 제거
            playerTileList.add(tile); // 플레이어의 타일 리스트에 추가
        }
    }

    //리스트 크기 반환
    public static int getStackSize(ArrayList<Tile> list) {
        return list.size();
    }

    // 타일 출력
    public void tilePrint(Tile tile){
        TileColor color = tile.color;

        if (color == TileColor.RED) {
            if(tile.number == 999) System.out.print(ColorCode.FONT_RED + "JOKER" + ColorCode.RESET);
            else System.out.print(ColorCode.FONT_RED + tile.number + ColorCode.RESET);
        }
        else if (color == TileColor.WHITE) {
            if(tile.number == 999) System.out.print("[" + ColorCode.FONT_WHITE + "JOKER" + ColorCode.RESET + "]");
            System.out.print(ColorCode.FONT_WHITE + tile.number + ColorCode.RESET);
        }
        else if (color == TileColor.BLUE) {
            System.out.print(ColorCode.FONT_BLUE + tile.number + ColorCode.RESET);
        }
        else if (color == TileColor.YELLOW) {
            System.out.print(ColorCode.FONT_YELLOW + tile.number + ColorCode.RESET);
        }
    }

   //연결된 타일들 리스트 출력
    public void tileLinkListPrint(ArrayList<LinkedList<Tile>> list){
        System.out.println();

        for (int i = 0; i < list.size(); i++) {
            if(list.get(i).size() > 1) System.out.print(i + ":");

            for(int j = 0; j < list.get(i).size(); j++){
                TileColor color = list.get(i).get(j).color;

                if (color == TileColor.RED) {
                    if(list.get(i).get(j).number == 999) System.out.print("[" + ColorCode.FONT_RED + "JOKER" + ColorCode.RESET + "]");
                    else System.out.print("[" + ColorCode.FONT_RED + list.get(i).get(j).number + ColorCode.RESET + "]");
                }
                else if (color == TileColor.WHITE) {
                    if(list.get(i).get(j).number == 999) System.out.print("[" + ColorCode.FONT_WHITE + "JOKER" + ColorCode.RESET + "]");
                    else System.out.print("[" + ColorCode.FONT_WHITE + list.get(i).get(j).number + ColorCode.RESET + "]");
                }
                else if (color == TileColor.BLUE) {
                    System.out.print("[" + ColorCode.FONT_BLUE + list.get(i).get(j).number + ColorCode.RESET + "]");
                }
                else if (color == TileColor.YELLOW) {
                    System.out.print("[" + ColorCode.FONT_YELLOW + list.get(i).get(j).number + ColorCode.RESET + "]");
                }
            }

            if(list.get(i).size() > 1) System.out.print("   ");
            if(list.get(i).size() > 1 && i % 5 == 0 && i != 0) System.out.println();
        }

        System.out.println();
    }

    //하나의 연결된 타일 리스트 
    public void tileLinkPrint(LinkedList<Tile> tile){
        for (Tile value : tile) {
            TileColor color = value.color;

            if (color == TileColor.RED) {
                if (value.number == 999) System.out.print("[" + ColorCode.FONT_RED + "JOKER" + ColorCode.RESET + "]");
                else System.out.print("[" + ColorCode.FONT_RED + value.number + ColorCode.RESET + "]");
            } else if (color == TileColor.WHITE) {
                if (value.number == 999) System.out.print("[" + ColorCode.FONT_WHITE + "JOKER" + ColorCode.RESET + "]");
                else System.out.print("[" + ColorCode.FONT_WHITE + value.number + ColorCode.RESET + "]");
            } else if (color == TileColor.BLUE) {
                System.out.print("[" + ColorCode.FONT_BLUE + value.number + ColorCode.RESET + "]");
            } else if (color == TileColor.YELLOW) {
                System.out.print("[" + ColorCode.FONT_YELLOW + value.number + ColorCode.RESET + "]");
            }
        }
    }

    // 플레이어의 타일 리스트 출력
    public void tileListPrint(ArrayList<Tile> list, Player player) {
        // 출력 내용 저장용 StringBuilder
        StringBuilder sb = new StringBuilder();
        sb.append("\n-----------------\n").append(player.name).append(" TileList\n");

        // 현재 플레이어 이름과 비교
        // String currentPlayerName = player.name;

        // if (player.name.equals(currentPlayerName)) {
        //     sb.append("**현재 플레이어입니다.**\n");
        // } else {
        //     sb.append("현재 플레이어가 아닙니다.\n");
        // }

        for (int i = 0; i < list.size(); i++) {
            TileColor color = list.get(i).color;

            if (color == TileColor.RED) {
                if (list.get(i).number == 999) sb.append(i).append(":[").append(ColorCode.FONT_RED).append("JOKER").append(ColorCode.RESET).append("], ");
                else sb.append(i).append(":[").append(ColorCode.FONT_RED).append(list.get(i).number).append(ColorCode.RESET).append("], ");
            } else if (color == TileColor.WHITE) {
                if (list.get(i).number == 999) sb.append(i).append(":[").append(ColorCode.FONT_WHITE).append("JOKER").append(ColorCode.RESET).append("], ");
                else sb.append(i).append(":[").append(ColorCode.FONT_WHITE).append(list.get(i).number).append(ColorCode.RESET).append("], ");
            } else if (color == TileColor.BLUE) {
                sb.append(i).append(":[").append(ColorCode.FONT_BLUE).append(list.get(i).number).append(ColorCode.RESET).append("], ");
            } else if (color == TileColor.YELLOW) {
                sb.append(i).append(":[").append(ColorCode.FONT_YELLOW).append(list.get(i).number).append(ColorCode.RESET).append("], ");
            }

            if (i % 15 == 0 && i != 0) sb.append("\n");
        }

        // 생성된 출력 내용 플레이어에게 전송
        JavaChatServer.sendTileListToClient(player);
        System.out.print(sb.toString());
    }


    // 색깔 기준으로 정렬 (조커는 항상 맨 뒤)
    public void tileSortToColor(ArrayList<Tile> tileList) {
        tileList.sort(new Comparator<Tile>() {
            @Override
            public int compare(Tile o1, Tile o2) {
                // 조커는 무조건 맨 뒤로 이동
                if (o1.number == 999) return 1;  // o1이 조커이면 뒤로
                if (o2.number == 999) return -1; // o2가 조커이면 o1이 앞

                // 색상 기준 정렬
                int colorComparison = o1.color.compareTo(o2.color);
                if (colorComparison == 0) {
                    // 색상이 같으면 숫자 기준 정렬
                    return o1.number - o2.number;
                }
                return colorComparison;
            }
        });
        System.out.println("색상 기준 정렬 완료 (조커는 맨 뒤)");
    }

    // 숫자 기준으로 정렬 (조커는 항상 맨 뒤)
    public void tileSortToNumber(ArrayList<Tile> tileList) {
        tileList.sort(new Comparator<Tile>() {
            @Override
            public int compare(Tile o1, Tile o2) {
                // 조커는 무조건 맨 뒤로 이동
                if (o1.number == 999) return 1;  // o1이 조커이면 뒤로
                if (o2.number == 999) return -1; // o2가 조커이면 o1이 앞

                // 숫자 기준 정렬
                return o1.number - o2.number;
            }
        });
        System.out.println("숫자 기준 정렬 완료 (조커는 맨 뒤)");
    }


    public void tileShuffle(ArrayList<Tile> tileList) {
        for (int i = 0; i < 3; i++) {
            Collections.shuffle(tileList);
        }
    }

    public Tile noPickTileDivide(ArrayList<Tile> playerTileList) {
        // 가져갈 카드가 있는 경우
        playerTileList.add(pop());

        int playerTileListSize = getStackSize(playerTileList);
        return playerTileList.get(playerTileListSize - 1);
    }

    public Boolean isTileListNull(ArrayList<Tile> tileList) {
        int tileListSize = getStackSize(tileList);

        // 가져갈 카드가 없어서 게임이 끝나야 하는 경우
        if (tileListSize < 0) {
            gameEnd(0);
            return true;
        }

        else return false;
    }
}