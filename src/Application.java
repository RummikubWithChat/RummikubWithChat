

import GUI.startGUI;
import network.Server;

public class Application {
    public static void main(String[] args) {
        // 서버 시작
        new Thread(() -> {
            Server server = new Server();
            server.start();
        }).start();

        // GUI 시작 (약간의 딜레이를 주어 서버가 먼저 시작되도록 함)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // startGUI 실행
        new Thread(() -> {
            startGUI.main(args);
        }).start();

        System.out.println("게임 서버와 GUI가 모두 시작되었습니다.");
    }
}