//JavaChatServer.java
//Java Chatting Server
package network;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.JButton;
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

public class JavaChatServer extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    JTextArea textArea;
    private JTextField txtPortNumber;

    private ServerSocket socket; // 서버소켓
    private Socket client_socket; // accept() 에서 생성된 client 소켓
    static private Vector<UserService> UserVec = new Vector<>(); // 연결된 사용자를 저장할 벡터, ArrayList와 같이 동적 배열을 만들어주는 컬렉션 객체이나 동기화로 인해 안전성 향상
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

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
                    JavaChatServer frame = new JavaChatServer();      // JavaChatServer 클래스의 객체 생성
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public JavaChatServer() {
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
        txtPortNumber.setBounds(111, 264, 199, 26);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnServerStart = new JButton("Server Start");
        btnServerStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
                } catch (NumberFormatException | IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                AppendText("Chat Server Running..");
                btnServerStart.setText("Chat Server Running..");
                btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
                txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
                AcceptServer accept_server = new AcceptServer();   // 멀티 스레드 객체 생성
                accept_server.start();
            }
        });
        btnServerStart.setBounds(12, 300, 300, 35);
        contentPane.add(btnServerStart);
    }

    // 새로운 참가자 accept() 하고 user thread를 새로 생성한다. 한번 만들어서 계속 사용하는 스레드
    class AcceptServer extends Thread {
        @SuppressWarnings("unchecked")
        public void run() {
            while (true) { // 사용자 접속을 계속해서 받기 위해 while문
                try {
                    AppendText("Waiting clients ...");
                    client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
                    AppendText("새로운 참가자 from " + client_socket);
                    // User 당 하나씩 Thread 생성
                    UserService new_user = new UserService(client_socket);
                    UserVec.add(new_user); // 새로운 참가자 배열에 추가
                    AppendText("사용자 입장. 현재 참가자 수 " + UserVec.size());
                    new_user.start(); // 만든 객체의 스레드 실행
                    
                    // 4명 모두 입장 시 게임 시작
                    if (UserVec.size() == 4) {
                    	GameStart();
                    }
                    
                } catch (IOException e) {
                    AppendText("!!!! accept 에러 발생... !!!!");
                }
            }
        }
    }
    
    public void GameStart() {
    	AppendText("4명의 플레이어가 연결되었습니다. 게임을 시작합니다!");
    	// 4명의 플레이어 객체 추출
        for (UserService userService : UserVec) {
            Player player = new Player(userService.UserName); // UserService에서 Player 객체 생성 
            players.add(player); // players 리스트에 추가
            playerToUserServiceMap.put(player, userService);   
            
            sendToClient(player, "Game Start!");
        }

        // 게임 초기화
        gameInitSetting(tileManage, players.get(0).tileList, players.get(1).tileList, players.get(2).tileList, players.get(3).tileList);

        // 게임 진행
        GamePlaying gamePlaying = new GamePlaying(boardManage, tileManage, players.get(0), players.get(1), players.get(2), players.get(3));
        gamePlaying.gamePlay();

        // 게임 시작 메시지 전송
        String startMessage = "게임을 시작합니다!";
        for (UserService user : UserVec) {
            user.WriteOne(startMessage); // 각 클라이언트에게 게임 시작 메시지 전송
        }
    	
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

    public static String getInputFromPlayer(Player player) {
        // Player와 연결된 UserService 객체 찾기
        UserService userService = playerToUserServiceMap.get(player);

        if (userService != null) {
            // 클라이언트의 응답 대기 및 처리
            String response = userService.requestInputFromClient();
            response = response.trim().toLowerCase();
            
            // "[숫자]" 패턴을 제거하고 숫자만 추출
            response = response.replaceAll("^\\[\\d+\\]", "").trim();
            
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


    
    // User 당 생성되는 Thread, 유저의 수만큼 스레스 생성
    // Read One 에서 대기 -> Write All
    class UserService extends Thread {
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
        
        private final Object lock = new Object(); // 동기화를 위한 객체
        private String clientResponse = null; // 클라이언트의 응답 저장
        
        // 클라이언트로부터 입력 요청 및 대기
        public String requestInputFromClient() {
            synchronized (lock) {
                try {
                    // 클라이언트에게 메시지 요청 전송
                    WriteOne("입력을 요청합니다. 메시지를 입력해주세요.");

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
        
        
        public void run() {
            while (true) {
                try {
                    String msg = dis.readUTF(); 
                    msg = msg.trim();   //msg를 가져와 trim 메소드를 사용하여 앞뒤의 공백을 제거
                    AppendText(msg); // server 화면에 출력
                    WriteAll(msg + "\n"); // Write All
                    
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
                        break; 
                    } catch (Exception ee) {
                        break;
                    } 
                }
            }
        }
        
    }
}
