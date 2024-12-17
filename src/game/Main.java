//package game;
//
//import model.board.Board;
//import model.game.GamePlaying;
//import model.player.Player;
//import model.tile.TileList;
//
//import java.util.Scanner;
//
//import static model.game.GameInitAndEndSet.gameInitSetting;
//
//public class Main {
//    public static void main(String[] args) {
//        // 01. 초기 게임 세팅
//        Scanner scan = new Scanner(System.in);
//
//        System.out.print("유저1의 이름을 입력하세요 : ");
//        String playerOneNameInput = scan.next();
//        Player playerOne = new Player(playerOneNameInput);
//
//        System.out.print("유저2의 이름을 입력하세요 : ");
//        String playerTwoNameInput = scan.next();
//        Player playerTwo = new Player(playerTwoNameInput);
//        
//        System.out.print("유저3의 이름을 입력하세요 : ");
//        String playerThreeNameInput = scan.next();
//        Player playerThree = new Player(playerThreeNameInput);
//        
//        System.out.print("유저4의 이름을 입력하세요 : ");
//        String playerFourNameInput = scan.next();
//        Player playerFour = new Player(playerFourNameInput);
//
//        TileList tileManage = new TileList();
//        Board boardManage = new Board(tileManage);
//        gameInitSetting(tileManage, playerOne.tileList, playerTwo.tileList, playerThree.tileList, playerFour.tileList);
//
//        // 02. 게임 시작 (여기 안에서 자체 루프 돌림) + 알아서 종료조건 찾아줌
//        GamePlaying gamePlaying = new GamePlaying(boardManage, tileManage, playerOne, playerTwo, playerThree, playerFour);
//        gamePlaying.gamePlay();
//    }
//}