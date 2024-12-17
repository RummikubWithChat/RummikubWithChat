package network;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost"; // 서버 주소
    private static final int PORT = 12345;
    private static Scanner scan = new Scanner(System.in); // Scanner 인스턴스

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            System.out.println("서버에 연결되었습니다!");

            // 유저 이름 입력
            System.out.print("유저의 이름을 입력하세요: ");
            String playerNameInput = scan.nextLine();
            out.println("PLAYER_NAME:" + playerNameInput);

            // 서버로부터 메시지 받는 스레드
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    System.out.println("서버와의 연결이 종료되었습니다.");
                }
            }).start();

            // 사용자 입력 서버로 전송
            while (true) {
                String userInput = getUserInput(); // 외부에서 호출할 수 있는 메소드로 입력 받음
                out.println(userInput); // 서버로 메시지 전송
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 외부에서 호출하여 사용자 입력을 받을 수 있는 메소드
    public static String getUserInput() {
        // 사용자 입력을 받아서 리턴
        String userInput = scan.nextLine();
        return userInput;
    }
}
