//JavaChatServer.java
//Java Chatting Server
package network;

import java.awt.EventQueue;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import model.board.Board;
import model.player.Player;
import model.tile.TileList;
import model.game.GamePlaying;
import static model.game.GameInitAndEndSet.gameInitSetting;

public class Server extends JFrame {

    /**
     * 
     */
    private static Server instance; // 싱글톤 인스턴스
    private AcceptServer acceptServer; // AcceptServer 인스턴스 저장을 위한 필드 추가
	
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket socket; // 서버소켓
    private Socket client_socket; // accept() 에서 생성된 client 소켓
    static private Vector<UserService> UserVec = new Vector<>(); // 연결된 사용자를 저장할 벡터, ArrayList와 같이 동적 배열을 만들어주는 컬렉션 객체이나 동기화로 인해 안전성 향상
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private boolean isAcceptingClients = true; // 클라이언트 수락 여부 확인용 플래그

    private GamePlaying gamePlaying;
    private static boolean isGameStart = false;
    static TileList tileManage = new TileList();
    static Board boardManage = new Board(tileManage);
    static List<Player> players = new ArrayList<>();	
    
    // 플레이어와 UserService 간의 맵
    static Map<Player, UserService> playerToUserServiceMap = new HashMap<>();
    
    /**
     * Launch the application.
     */
    public static void main(String[] args) {   // 스윙 비주얼 디자이너를 이용해 GUI를 만들면 자동으로 생성되는 main 함수
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Server frame = new Server();      // JavaChatServer 클래스의 객체 생성
                    frame.setVisible(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public Server() {
        instance = this; // 생성자에서 인스턴스 저장
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 338, 386);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(12, 10, 300, 244);
        contentPane.add(scrollPane);

        textArea = new JTextArea();
        textArea.setEditable(false);
        scrollPane.setViewportView(textArea);

        JLabel lblNewLabel = new JLabel("Port Number");
        lblNewLabel.setBounds(12, 264, 87, 26);
        contentPane.add(lblNewLabel);

        txtPortNumber = new JTextField();
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setText("30000");

         // 서버 자동 시작
        try {
            socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
            AppendText("Chat Server Running..");
            txtPortNumber.setEnabled(false); // 포트번호 수정 불가능하게 설정
            startAcceptServer(); // AcceptServer 시작
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("Server Start Error");
        }
    }
    
    // AcceptServer 시작을 위한 별도 메소드
    private void startAcceptServer() {
        acceptServer = new AcceptServer();
        acceptServer.start();
    }

    // 새로운 참가자 accept() 하고 user thread를 새로 생성한다. 한번 만들어서 계속 사용하는 스레드
    class AcceptServer extends Thread {
    	private boolean running = true;

        public void stopAccepting() {
            running = false;
        }
    	
        @SuppressWarnings("unchecked")
        public void run() {
            while (true) { // 사용자 접속을 계속해서 받기 위해 while문
                try {
                    AppendText("Waiting clients ...");
                    client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
                    
                    // 이미 게임이 진행 중이고 4명이 찼을 경우
                    if (isGameStart && UserVec.size() >= 4) {
                        DataOutputStream tempDos = new DataOutputStream(client_socket.getOutputStream());
                        tempDos.writeUTF("서버가 가득 찼거나 게임이 진행 중입니다.");
                        client_socket.close();
                        continue;
                    }
                    
                    AppendText("새로운 참가자 from " + client_socket);
                    // User 당 하나씩 Thread 생성
                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user); // 새로운 참가자 배열에 추가
                    AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());
                    new_user.start(); // 만든 객체의 스레드 실행
                    
                    // 4명 모두 입장 시 게임 시작
                    if (UserVec.size() == 4) {
                        isAcceptingClients = false; // 더 이상 클라이언트 받지 않음
                    	GameStart();
                    }
                    
                } catch (IOException e) {
                    AppendText("!!!! accept 에러 발생... !!!!");
                }
            }
        }
    }
    
    // 해당 UserService에 맞는 Player 반환
    private static Player getPlayerByUserService(UserService userService) {
        for (Map.Entry<Player, UserService> entry : playerToUserServiceMap.entrySet()) {
            if (entry.getValue() == userService) {
                return entry.getKey();
            }
        }
        return null; // 일치하는 플레이어가 없으면 null 반환
    }
    
    public void GameStart() {
    	isGameStart = true;
    	AppendText("4명의 플레이어가 연결되었습니다. 게임을 시작합니다!");
    	// 4명의 플레이어 객체 추출
        for (UserService userService : UserVec) {
            Player player = new Player(userService.UserName); // UserService에서 Player 객체 생성 
            players.add(player); // players 리스트에 추가
            playerToUserServiceMap.put(player, userService);   
            
            // 각 플레이어에게 게임 시작 메시지 전송
            sendToClient(player, "Game Start!\n");
        }
        
        // 다른 플레이어들의 이름을 문자열로 결합
        List<UserService> otherPlayers = new ArrayList<>(UserVec); // 복사
        // 다른 플레이어들의 이름을 리스트로 보내는 부분
        for (UserService userService : UserVec) {
            List<String> otherPlayerNames = new ArrayList<>();
            for (UserService otherPlayerService : otherPlayers) {
                if (otherPlayerService != userService) { // 현재 플레이어를 제외한 나머지 플레이어 이름
                    otherPlayerNames.add(otherPlayerService.UserName);
                }
            }

            // 다른 플레이어들의 이름 리스트를 전송
            userService.WriteOne("/otherPlayersNames " + otherPlayerNames);
        }
       

        // 게임 초기화
        gameInitSetting(tileManage, players);

        // 게임 진행
        gamePlaying = new GamePlaying(boardManage, tileManage, players.get(0), players.get(1), players.get(2), players.get(3));
        gamePlaying.gamePlay();
    }
    
    // 게임 종료 후 초기화를 위한 새로운 메소드
    public static void resetGame() {
    	if (instance == null) return;
    	
        // 게임 관련 데이터 초기화
        isGameStart = false;
        UserVec.clear();
        playerToUserServiceMap.clear();
        players.clear();
        tileManage = new TileList();
        boardManage = new Board(tileManage);
        
        // 서버 상태 초기화
        instance.isAcceptingClients = true;
        instance.AppendText("게임이 종료되었습니다. 새로운 게임을 위해 대기중...");
        
        // 새로운 AcceptServer 시작
        instance.startAcceptServer();
    }
    
    //JtextArea에 문자열을 출력해 주는 기능을 수행하는 함수
    public void AppendText(String str) {
        textArea.append(str + "\n");   //전달된 문자열 str을 textArea에 추가
        textArea.setCaretPosition(textArea.getText().length());  // textArea의 커서(캐럿) 위치를 텍스트 영역의 마지막으로 이동
    }
    
    // 특정 플레이어에게 메시지를 보내는 메소드
    public static void sendToClient(Player player, String message) {
        UserService userService = playerToUserServiceMap.get(player);  // Player와 연결된 UserService 찾기
        if (userService != null) {
            userService.WriteOne(message);  // 해당 UserService로 메시지 전송
        } else {
            System.out.println("해당 플레이어를 찾을 수 없습니다.");
        }
    }

    // 플레이어에게 응답 받기
    public static String getInputFromPlayer(Player player) {
        // Player와 연결된 UserService 객체 찾기
        UserService userService = playerToUserServiceMap.get(player);

        if (userService != null) {
            // 클라이언트의 응답 대기 및 처리
            String response = userService.requestInputFromClient();
            response = response.trim().toLowerCase();
            
            // "[값]" 패턴을 제거하고 숫자만 추출
            response = response.replaceAll("^\\[.*?\\]", "").trim();
            
            System.out.println("[DEBUG] 입력값 (소문자, 공백 제거 후): " + response); // 디버깅용 로그
            
            if (response == null) {
                System.out.println("클라이언트 응답을 받지 못했습니다.");
            }
            return response; // 클라이언트의 응답 반환
        } else {
            System.out.println("해당 플레이어와 연결된 UserService를 찾을 수 없습니다.");
            return null; // 연결된 UserService가 없으면 null 반환
        }
    }
    
    public static void sendTileListToClient(Player player) {
        UserService userService = playerToUserServiceMap.get(player);
        if (userService != null) {
            // 타일 리스트를 직렬화하여 전송 (혹은 원하는 포맷으로)
            userService.WriteOne("/newTileList " + player.tileListToString());  // 예시로 간단히 출력
        }
    }
    
   public static void sendTileListSizeToClient() {
	   // 다른 플레이어의 타일 개수 전송
       for (UserService userService : UserVec) {
           List<String> otherPlayerTileCounts = new ArrayList<>();
           
           // 다른 플레이어들의 타일 개수 정보 모으기
           for (UserService otherUs : UserVec) {
               // 현재 userService의 해당 플레이어와 다른 플레이어들만 추가
               if (otherUs != userService) {
                   otherPlayerTileCounts.add(String.valueOf(getPlayerByUserService(otherUs).getTileListSize()));
               }
           }

           // 타일 개수 정보 전송
           userService.WriteOne("/otherPlayerTileCounts " + otherPlayerTileCounts);
       }
   }
    
    public static void sendBoardTileListToClient() {
        // 모든 UserService에 대해 WriteAll을 호출
        for (UserService userService : UserVec) {
            // 타일 리스트를 직렬화하여 전송 (혹은 원하는 포맷으로)
            userService.WriteOne("/newBoardTileList " + boardManage.onBoardTileListToString());  // 예시로 간단히 출력
        }
    }
    
    public static void sendTemporaryTileListToClient() {
        // 모든 UserService에 대해 WriteAll을 호출
        for (UserService userService : UserVec) {
            // 타일 리스트를 직렬화하여 전송 (혹은 원하는 포맷으로)
            userService.WriteOne("/newBoardTileList " + boardManage.temporaryTileListToString());  // 예시로 간단히 출력
        }
    }
    
    public static void sendIsTurnToClient(Player currentPlayer) {
        for (Map.Entry<Player, UserService> entry : playerToUserServiceMap.entrySet()) {
            Player player = entry.getKey();
            UserService userService = entry.getValue();

            if (player.equals(currentPlayer)) {
                // 현재 턴인 플레이어가 수행할 행동
                userService.WriteOne("/yourTurn\n");
            } else {
                List<String> isTurnList = new ArrayList<>();
                
                // 다른 플레이어들의 isTurn 상태를 리스트에 추가
                for (UserService otherUs : UserVec) {
                    
                    // 자신은 제외하고 다른 플레이어들의 isTurn 값 추가
                    if (!otherUs.equals(userService)) {
                        String isTurn = otherUs.equals(playerToUserServiceMap.get(currentPlayer)) ? "T" : "F";
                        isTurnList.add(isTurn);
                    }
                }
                
                userService.WriteOne("/otherTurn " + isTurnList);
            }
        }
    }
    
    public static void sendGameOverToClient(int winnerIndex) {
        // 기존의 게임 종료 로직
        if (winnerIndex == -1) {
            for (UserService userService : UserVec) {
                userService.WriteOne("/GameOverAndDraw");
            }
        } else {
            Player winner = players.get(winnerIndex -1);
            UserService winnerService = playerToUserServiceMap.get(winner);
            if (winnerService != null) {
                winnerService.WriteOne("/GameOverAndWin");
            }
            
            for (Player player : players) {
                if (!player.equals(winner)) {
                    UserService loser = playerToUserServiceMap.get(player);
                    if (loser != null) {
                        loser.WriteOne("/GameOverAndLose "+winner.getName());
                    }
                }
            }
        }

        // 모든 연결 종료
        for (UserService userService : UserVec) {
            try {
                userService.client_socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 게임 리셋
        resetGame();
    }
    
    // User 당 생성되는 Thread, 유저의 수만큼 스레스 생성
    // Read One 에서 대기 -> Write All
    public class UserService extends Thread {
        private InputStream is;
        private OutputStream os;
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client_socket;
        private Vector<UserService> user_vc; // 제네릭 타입 사용
        private String UserName = "";

        public UserService(Socket client_socket) {
            // 매개변수로 넘어온 자료 저장
            this.client_socket = client_socket;
            this.user_vc = UserVec;
            try {
                is = client_socket.getInputStream();
                dis = new DataInputStream(is);
                os = client_socket.getOutputStream();
                dos = new DataOutputStream(os);
                String line1 = dis.readUTF();      // 제일 처음 연결되면 SendMessage("/login " + UserName);에 의해 "/login UserName" 문자열이 들어옴
                String[] msg = line1.split(" ");   //line1이라는 문자열을 공백(" ")을 기준으로 분할
                UserName = msg[1].trim();          //분할된 문자열 배열 msg의 두 번째 요소(인덱스 1)를 가져와 trim 메소드를 사용하여 앞뒤의 공백을 제거
                AppendText("새로운 참가자 " + UserName + " 입장.");
                WriteOne("Welcome to Java chat server\n");
                WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
                String br_msg ="["+UserName+"]님이 입장 하였습니다.\n";    //broadcastMessage 생성 [추가]
                WriteAll(br_msg); // broadcastMessage 전송, 아직 user_vc에 새로 입장한 user는 포함되지 않았으므로 새로운 참가자한테는 전송하지 않음 [추가]
            } catch (Exception e) {
                AppendText("userService error");
            }
        }

        // 자신의 turn 인지 확인 후 반환
        public boolean isMyTurn() {
        	if(getPlayerByUserService(this) == gamePlaying.getCurrentPlayer()) {
        		return true;
        	}
        	else {
        		return false;
        	}
        }
        
        private final Object lock = new Object(); // 동기화를 위한 객체
        private String clientResponse = null; // 클라이언트의 응답 저장
        
        // 클라이언트로부터 입력 요청 및 대기
        public String requestInputFromClient() {
            synchronized (lock) {
                try {
                    // 클라이언트에게 메시지 요청 전송
                    //WriteOne("입력을 요청합니다. 메시지를 입력해주세요.");

                    // 클라이언트 응답 대기
                    lock.wait();
//                    lock.wait(30000); // 대기 (밀리초)

//                    if (clientResponse == null) {
//                        // 타임아웃 발생 시 기본 처리
//                        System.out.println("응답이 시간 내에 도착하지 않았습니다.");
//                        return null;
//                    }

                    String response = clientResponse; // 응답 저장
                    clientResponse = null; // 상태 초기화
                    return response;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }

        // 클라이언트로 메시지 전송
        public void WriteOne(String msg) {
            try {
                dos.writeUTF(msg);
            } catch (IOException e) {
                AppendText("dos.write() error");
                try {
                    dos.close();
                    dis.close();
                    client_socket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                UserVec.removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
            	String br_msg ="["+UserName+"]님이 퇴장 하였습니다.\n";   // 다른 User들에게 전송할 메시지 생성  [추가]
            	WriteAll(br_msg); // 다른 User들에게 전송  [추가]
                AppendText("사용자 퇴장. 현재 참가자 수 " + UserVec.size());
            }
        }

        
        //모든 다중 클라이언트에게 순차적으로 채팅 메시지 전달
        public void WriteAll(String str) {  
            for (int i = 0; i < user_vc.size(); i++) {
            	UserService user = user_vc.get(i);     // get(i) 메소드는 user_vc 컬렉션의 i번째 요소를 반환
                user.WriteOne(str);
            }
        }
        
        // 클라이언트로부터 입력을 받는 메소드
        public String getClientAction() {
            try {
                return dis.readUTF();  // 클라이언트의 입력을 받아 반환
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        
        // 타일을 색깔별로 정렬하여 클라이언트에게 전송
        private void handleTileSortToColor() {
            try {
                // 플레이어 객체를 찾기
                Player player = getPlayerByUserService(this);

                if (player != null) {
                    // 타일을 색깔별로 정렬
                    tileManage.tileSortToColor(player.tileList);
                    // 정렬된 타일 리스트를 클라이언트에게 전송
                    this.WriteOne("/newTileList " + player.tileListToString()); 

                    AppendText("타일 정렬 완료 및 전송: " + UserName);
                } else {
                    WriteOne("플레이어 정보가 없습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                WriteOne("타일 정렬 중 오류가 발생했습니다.");
            }
        }

        // 타일을 숫자순으로 정렬하여 클라이언트에게 전송
        private void handleTileSortToNumber() {
            try {
                // 플레이어 객체를 찾기
                Player player = getPlayerByUserService(this);

                if (player != null) {
                    // 타일을 색깔별로 정렬
                    tileManage.tileSortToNumber(player.tileList);
                    // 정렬된 타일 리스트를 클라이언트에게 전송
                    WriteOne("/newTileList " + player.tileListToString()); 

                    AppendText("타일 정렬 완료 및 전송: " + UserName);
                } else {
                    WriteOne("플레이어 정보가 없습니다.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                WriteOne("타일 정렬 중 오류가 발생했습니다.");
            }
        }
        
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF(); 
                    msg = msg.trim();   //msg를 가져와 trim 메소드를 사용하여 앞뒤의 공백을 제거
                    AppendText(msg); // server 화면에 출력
                    
                    // 클라이언트로부터 받은 메세지 처리 로직
                    if (msg.equals("/tileSortToColor")) {
                        handleTileSortToColor();
                    } else if (msg.equals("/tileSortToNumber")) {
                    	handleTileSortToNumber();
                    } else {
                        WriteAll(msg + "\n"); // 일반 메시지를 모든 사용자에게 전송
                    }
                    
                   synchronized(lock) {
                	   clientResponse = msg; // 응답 저장
                       lock.notify(); // 대기 중인 스레드를 깨움 
                   }
                } catch (IOException e) {
                    AppendText("dis.readUTF() error");
                    try {
                        dos.close();
                        dis.close();
                        client_socket.close();
                        UserVec.removeElement(this); // 에러가 난 현재 객체를 벡터에서 지운다
                        AppendText("사용자 퇴장. 남은 참가자 수 " + UserVec.size());
                    	String br_msg ="["+UserName+"]님이 퇴장 하였습니다.\n";   // 다른 User들에게 전송할 메시지 생성  [추가]
                    	WriteAll(br_msg); // 다른 User들에게 전송  [추가]
                    	
                    	// 게임 중이었다면 게임 종료 처리
                        if (isGameStart && UserVec.size() < 4) {
                            WriteAll("/GameOver");
                            if (acceptServer != null) {
                                acceptServer.stopAccepting(); // 기존 AcceptServer 중지
                            }
                            resetGame();  // 게임 리셋 및 새로운 AcceptServer 시작
                        }
                        // 모든 플레이어가 나갔을 경우도 리셋
                        if (UserVec.isEmpty()) {
                        	if (acceptServer != null) {
                                acceptServer.stopAccepting(); // 기존 AcceptServer 중지
                            }
                            resetGame();
                        }
                    	
                        break; 
                    } catch (Exception ee) {
                        break;
                    } 
                }
            }
        }
        
    }

    public void start() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'start'");
    }
}
