package model.game;

import model.player.Player;
import network.JavaChatServer;
import model.game.GamePlaying;
import static model.board.Board.onBoardTileList;

public class PlayChoice {

    // 클라이언트에게 메시지 전송
    public static void sendToClient(Player player, String message) {
    	JavaChatServer.sendToClient(player, message);
    }

    // 카드 가져오기, 카드 내기, 카드 정렬, 턴 종료 등을 선택하도록 요청하는 메서드
    public static String pickOrShow(Player player) {
        System.out.print("\n카드 가져오기 : P or p / 카드 내기 : S or s / 카드 정렬하기 N/n(숫자기준) / 턴 종료하기 E/e: ");
        // sendToClient(player, "\n카드 가져오기 : P or p / 카드 내기 : S or s / 카드 정렬하기 N/n(숫자기준) / 턴 종료하기 E/e: ");
        String input = JavaChatServer.getInputFromPlayer(player);
        
        if (input == null || input.isEmpty()) {
            System.out.println("입력이 없거나 공백입니다. 다시 요청합니다.");
            return null;
        }
        
        return input; // 올바른 입력값 반환
    }

    // 카드 리스트 생성/추가
    public static String cardListAddOrEdit(Player player) {
        System.out.print("\n새로운 카드리스트 생성 : A or a / 기존 리스트에 추가 : E or e :: ");
        sendToClient(player, "\n새로운 카드리스트 생성 : A or a / 기존 리스트에 추가 : E or e :: ");
        String response = JavaChatServer.getInputFromPlayer(player);
        
        if (response.startsWith("/tileindex ")) {
        	
            GamePlaying.setIsSelectedOnTileList(true); // tileList 에서 선택됨
	        return "a " + response.split("\\s+")[1];
	    } else if (response.startsWith("/boardindex")) {
            GamePlaying.setIsSelectedOnBoard(true); // board 에서 선택됨
	        return "e " + response.split("\\s+")[1];
	    } else {
	    	return response;
	    }
    }

    // 배열 수정/배열 나누기/완료 처리 선택
    public static int editCheck(Player player) {
        System.out.print("\n기존 배열을 수정하기 : 1 / 배열 나누기 : 2 / 완료 : -1 :: ");
        //sendToClient(player, "\n기존 배열을 수정하기 : 1 / 배열 나누기 : 2 / 완료 : -1 :: ");
        return Integer.parseInt(JavaChatServer.getInputFromPlayer(player)); // 서버에서 받은 입력 처리
    }

    // 보드에 있는 타일 인덱스 선택
    public static int onBoardTileIndexPick(Player player) {
        System.out.println();
        System.out.print("인덱스를 고르세요. (0~" + (onBoardTileList.size() - 1) + ") : ");
        sendToClient(player, "인덱스를 고르세요. (0~" + (onBoardTileList.size() - 1) + ") : ");
        
        String response = JavaChatServer.getInputFromPlayer(player);
		 
		// 응답이 "-1"이라면 즉시 반환
	    if (response.equals("-1")) {
	        return -1;
	    }

		 if (response.startsWith("/boardindex ")) { // 이미 toLowerCase() 됨
	        try {
	            // "/boardIndex" 이후 숫자 추출
	            int index = Integer.parseInt(response.split("\\s+")[1]);
	            if (index >= -1 && index < player.tileList.size()) {
//	                GamePlaying.setIsSelectedOnBoard(true); // board 에서 선택됨
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
		 
		 String response = JavaChatServer.getInputFromPlayer(player);
		 
		// 응답이 "-1"이라면 즉시 반환
	    if (response.equals("-1")) {
	        return -1;
	    }

		 if (response.startsWith("/tileindex ")) {
	        try {
	            // "/tileIndex" 이후 숫자 추출
	            int index = Integer.parseInt(response.split("\\s+")[1]);
	            if (index >= -1 && index < player.tileList.size()) {
//	                GamePlaying.setIsSelectedOnTileList(true); // tileList 에서 선택됨
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

    // 수정 기준 인덱스 선택
    public static int detailIndexPick(Player player, int index) {
    	System.out.println();
        System.out.print("수정 기준 인덱스를 고르세요. (0~" + (onBoardTileList.get(index).size() - 1) + ") : ");
        // sendToClient(player, "수정 기준 인덱스를 고르세요. (0~" + (onBoardTileList.get(index).size() - 1) + ") : ");
        return Integer.parseInt(JavaChatServer.getInputFromPlayer(player));
    }

    // 요소 앞뒤에 놓기 선택
    public static int workPick(Player player) {
        System.out.print("\n요소 앞에 놓기 : 1 / 요소 뒤에 놓기: 2 :: ");
        //sendToClient(player, "\n요소 앞에 놓기 : 1 / 요소 뒤에 놓기: 2 :: ");
        return Integer.parseInt(JavaChatServer.getInputFromPlayer(player));
    }
}
