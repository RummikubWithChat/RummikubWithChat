package model.game;

import model.player.Player;
import network.Server;
import model.game.GamePlaying;
import static model.board.Board.onBoardTileList;

public class PlayChoice {

    // 클라이언트에게 메시지 전송
    public static void sendToClient(Player player, String message) {
    	Server.sendToClient(player, message);
    }

    // 카드 리스트 생성/추가
    public static String playerAction(Player player) {
        System.out.print("\n새로운 카드리스트 생성 : A or a / 기존 리스트에 추가 : E or e :: ");
        sendToClient(player, "\n새로운 카드리스트 생성 : A or a / 기존 리스트에 추가 : E or e :: ");
        String response = Server.getInputFromPlayer(player);
        
        System.out.println("response: " + response);
        
        // response가 null인 경우 처리
        if (response == null) {
            sendToClient(player, "응답이 없습니다. 다시 시도해주세요.");
            return null; // 혹은 특정 값을 반환하거나 재시도 로직을 구현
        }
        
        if (response.startsWith("/tileindex ")) {
            System.out.println("a: " + response);
            try {
                // "/tileIndex" 이후 숫자 추출
                String[] parts = response.split("\\s+");
                if (parts.length > 1) {
                    int index = Integer.parseInt(parts[1]);
                    if (index >= 0) {
                        GamePlaying.setIsSelectedOnTileList(true); // tileList에서 선택됨
                        return "a " + index;
                    } else {
                        sendToClient(player, "유효하지 않은 인덱스입니다. 다시 시도하세요.");
                    }
                } else {
                    sendToClient(player, "인덱스를 입력해주세요.");
                }
            } catch (NumberFormatException e) {
                sendToClient(player, "인덱스는 숫자여야 합니다. 다시 시도하세요.");
            }
        } else if (response.startsWith("/boardindex")) {
            System.out.println("e: " + response);
            try {
                // "/boardIndex" 이후 숫자 추출
                String[] parts = response.split("\\s+");
                if (parts.length > 1) {
                    int index = Integer.parseInt(parts[1]);
                    if (index >= 0) {
                        GamePlaying.setIsSelectedOnBoard(true); // board에서 선택됨
                        return "e " + index;
                    } else {
                        sendToClient(player, "유효하지 않은 인덱스입니다. 다시 시도하세요.");
                    }
                } else {
                    sendToClient(player, "인덱스를 입력해주세요.");
                }
            } catch (NumberFormatException e) {
                sendToClient(player, "인덱스는 숫자여야 합니다. 다시 시도하세요.");
            }
        } else if (response.equals("/tilepick")) {
            return "p";
        } else if (response.equals("/end")) {
            return "end";
        } else if (response.equals("/submit")) {
            return "-1";
        } else {
            System.out.println("잘못된 명령어: " + response);
            sendToClient(player, "잘못된 명령어입니다.");
            return response;
        }
        return null; // 유효하지 않은 입력일 경우 null 반환
    }

    // 보드에 있는 타일 인덱스 선택
    public static int onBoardTileIndexPick(Player player) {
        System.out.println();
        System.out.print("인덱스를 고르세요. (0~" + (onBoardTileList.size() - 1) + ") : ");
        sendToClient(player, "인덱스를 고르세요. (0~" + (onBoardTileList.size() - 1) + ") : ");
        
        String response = Server.getInputFromPlayer(player);
		 
		// 응답이 "-1"이라면 즉시 반환
	    if (response.equals("/submit")) {
	        return -1;
	    } 

		 if (response.startsWith("/boardindex ")) { // 이미 toLowerCase() 됨
	        try {
	            // "/boardIndex" 이후 숫자 추출
	            int index = Integer.parseInt(response.split("\\s+")[1]);
	            if (index >= -1 && index < player.tileList.size()) {
	                return index;
	            } else {
	                sendToClient(player, "유효하지 않은 인덱스입니다. 다시 시도하세요.");
	            }
	        } catch (Exception e) {
	            sendToClient(player, "인덱스는 숫자여야 합니다. 다시 시도하세요.");
	        }
	    } else {
	        sendToClient(player, "잘못된 명령어입니다.");
	    }
	    return 999; // 유효하지 않은 경우: 무조건 타일 리스트 인덱스보다 큰 값 넘김    
    }

    // 플레이어의 타일 목록에서 인덱스 선택
    public static int tileIndexPick(Player player) {
		 System.out.println();
		 System.out.print("인덱스를 고르세요, 완료하려면 -1을 입력하세요. (0~" + (player.tileList.size() - 1) + ") : ");
		 sendToClient(player, "인덱스를 고르세요, 완료하려면 -1을 입력하세요. (0~" + (player.tileList.size() - 1) + ") : ");
		 
		 String response = Server.getInputFromPlayer(player);
		 
		// 응답이 "-1"이라면 즉시 반환
	    if (response.equals("/submit")) {
	        return -1;
	    }

		 if (response.startsWith("/tileindex ")) {
	        try {
	            // "/tileIndex" 이후 숫자 추출
	            int index = Integer.parseInt(response.split("\\s+")[1]);
	            if (index >= -1 && index < player.tileList.size()) {
	                return index;
	            } else {
	                sendToClient(player, "유효하지 않은 인덱스입니다. 다시 시도하세요.");
	            }
	        } catch (Exception e) {
	            sendToClient(player, "인덱스는 숫자여야 합니다. 다시 시도하세요.");
	        }
	    } else {
	        sendToClient(player, "잘못된 명령어입니다.");
	    }
	    return 999; // 유효하지 않은 경우: 무조건 타일 리스트 인덱스보다 큰 값 넘김
    }
}
