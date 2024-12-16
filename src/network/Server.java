package network;

import model.board.Board;
import model.player.Player;
import model.tile.TileList;
import model.game.GamePlaying;

import static model.game.GameInitAndEndSet.gameInitSetting;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    private static final List<ClientHandler> clients = new ArrayList<>(); // 연결된 클라이언트 관리
    private static final Map<Player, ClientHandler> playerMap = new LinkedHashMap<>(); // 순서를 보장하기 위해 LinkedHashMap 사용
    private static final Set<Player> readyPlayers = new HashSet<>(); // 준비된 플레이어를 저장
    static TileList tileManage = new TileList();
    static Board boardManage = new Board(tileManage);

    public static void main(String[] args) {
        System.out.println("서버가 시작되었습니다...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept(); // 클라이언트 연결 대기
                System.out.println("새 클라이언트 연결: " + socket.getInetAddress());

                // 새로운 클라이언트 핸들러 생성 및 시작
                ClientHandler clientHandler = new ClientHandler(socket);
                new Thread(clientHandler).start();
                clients.add(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 모든 클라이언트에게 메시지 전송
    private static void sendToAllClients(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // 특정 클라이언트(Player 객체로 구분)에게 메시지 전송
    public static void sendToClient(Player player, String message) {
        ClientHandler clientHandler = playerMap.get(player);
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }
    
 // 특정 클라이언트에게 입력 요청 후 행동 반환 받기
    public static String getClientActionAndReturn(Player player) {
        ClientHandler clientHandler = playerMap.get(player);

        if (clientHandler != null) {
            synchronized (clientHandler) {
                try {
                    clientHandler.sendMessage("당신의 차례입니다. 행동을 선택하세요.");
                    System.out.println(player.getName() + "의 행동을 대기 중...");
                    clientHandler.wait(); // 클라이언트의 입력 대기
                    System.out.println(player.getName() + "의 행동을 받았습니다: " + clientHandler.getUserAction());
                    return clientHandler.getUserAction(); // 행동 반환
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    // 클라이언트가 모두 이름과 준비 상태를 입력했을 때 게임 시작
    private static void checkAndStartGame() {
        if (playerMap.size() == 4 && readyPlayers.size() == 4) {
        	System.out.println("playerMap 크기: " + playerMap.size());
            System.out.println("readyPlayers 크기: " + readyPlayers.size());
            startGame();
        }
    }

    private static void startGame() {
        System.out.println("4명의 플레이어가 연결되었습니다. 게임을 시작합니다!");

        // 4명의 클라이언트에서 플레이어 객체 추출
        List<Player> players = new ArrayList<>(playerMap.keySet());

        // 게임 초기 설정
        gameInitSetting(tileManage, players);

        // GamePlaying 객체 생성 및 실행
        GamePlaying gamePlaying = new GamePlaying(boardManage, tileManage, players.get(0), players.get(1), players.get(2), players.get(3));
        gamePlaying.gamePlay();
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private Player player;
        private boolean isReady = false;
        private String userAction;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // 클라이언트로부터 수신
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("PLAYER_NAME:")) {
                        String playerName = message.substring(12); // "PLAYER_NAME:" 이후 이름 추출
                        this.player = new Player(playerName); // 고유 Player 객체 생성
                        System.out.println("플레이어 이름 등록: " + playerName);
                        out.println("이름이 성공적으로 등록되었습니다: " + playerName);
                        out.println("준비 완료 상태를 입력하세요(READY): "); // 준비 완료 상태 요청

                        // Player 객체를 playerMap에 추가
                        synchronized (playerMap) {
                            playerMap.put(player, this);
                            System.out.println("현재 연결된 플레이어들:");
                            playerMap.forEach((key, value) -> System.out.println("Player: " + key.getName()));
                        }

                        // 4명이 모두 이름을 입력했을 때 게임 시작 확인
                        checkAndStartGame();
                    } else if (message.toUpperCase().equals("READY")) {
                        // "READY" 메시지를 받으면 준비 완료 상태 처리
                        synchronized (readyPlayers) {
                            readyPlayers.add(player); // 준비된 플레이어 추가
                        }
                        System.out.println(player.getName() + "가 준비 완료 상태입니다.");
                        out.println("준비 완료! 기다리는 중...");
                        
                        // 모든 클라이언트가 준비되면 게임 시작 확인
                        checkAndStartGame();
                        System.out.println("클라이언트 연결 상태: " + socket.getInetAddress() + " 연결됨");
                    } else if (isGameAction(message)) {
                        // 행동 메시지 처리
                    	handleClientAction(message);
                        
                    } else {
                        // 클라이언트로부터 일반 메시지 처리
                        System.out.println(player.getName() + "로부터 받은 메시지: " + message);
                        sendToAllClients("[" + player.getName() + "]: " + message); // 모든 클라이언트에게 메시지 브로드캐스트
                    }
                }
            } catch (IOException e) {
                System.out.println("클라이언트 연결 해제.");
            } finally {
                // 클라이언트 연결 종료 시 자원 정리
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                synchronized (clients) {
                    clients.remove(this);
                }
                synchronized (playerMap) {
                    playerMap.remove(player);
                }
                synchronized (readyPlayers) {
                    readyPlayers.remove(player);
                }
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String getUserAction() {
            return userAction;
        }

        private boolean isGameAction(String message) {
            return Set.of("P", "S", "N", "E", "R", "T").contains(message.toUpperCase());
        }
        public synchronized void handleClientAction(String message) {
            userAction = message;
            System.out.println(player.getName() + "의 행동: " + message);
            System.out.println("notify 호출: " + player.getName());
            notifyAll(); // 이 부분에서 notify 호출
        }
    }
}