package GUI;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.awt.dnd.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Timer;

import model.game.GamePlaying;
import model.tile.*;
import network.JavaChatServer.UserService;

public class PlayerGUI extends JFrame {
	private String username;
    private JPanel contentPane;
    private JPanel tilePanel;
    private JPanel boardPanel;
    private JLabel nicknameLabel;
    private JLabel timeLabel;
    private Timer timer;
    private int remainingTime = 30;

    private String[] otherPlayers = {"Player2", "Player3", "Player4"};
    private int[] otherPlayersTime = {30, 30, 30};

    private List<Tile> tileList = new ArrayList<>();

    private ArrayList<LinkedList<Tile>> boardLinkedTileList = new ArrayList<>();
    
    private Map<TileColor, Map<Integer, Integer>> tileCounts = new EnumMap<>(TileColor.class);
    
    // 채팅 관련 변수들
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton endButton;
    private JButton submitButton;
    
    private JLabel lblUserName;

        
    // network 관련 변수들
    private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
    private Socket socket; // 연결소켓
    private InputStream is;
    private OutputStream os;
    private DataInputStream dis;
    private DataOutputStream dos;
    
    public PlayerGUI(String username, String ip_addr, String port_no) {	
    	this.username = username;
    	
        setTitle("Player GUI");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setBackground(new Color(25, 25, 112));
        contentPane.setLayout(new BorderLayout());

        // 다른 플레이어 패널 설정
        JPanel otherPlayersPanel = new JPanel();
        otherPlayersPanel.setLayout(new BoxLayout(otherPlayersPanel, BoxLayout.Y_AXIS));
        otherPlayersPanel.setOpaque(false);
        otherPlayersPanel.setBorder(new EmptyBorder(10, 20, 30, 20));

        for (int i = 0; i < otherPlayers.length; i++) {
            JLabel playerLabel = new JLabel(otherPlayers[i] + ": " + otherPlayersTime[i] + "초");
            playerLabel.setForeground(Color.WHITE);
            playerLabel.setFont(new Font("Arial", Font.BOLD, 15));
            otherPlayersPanel.add(playerLabel);
        }

        contentPane.add(otherPlayersPanel, BorderLayout.WEST);

     // 보드 패널 설정 (드래그 앤 드롭 가능)
        boardPanel = new JPanel();
        boardPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        boardPanel.setBackground(new Color(25, 25, 100));
        boardPanel.setTransferHandler(new BoardPanelTransferHandler());
        boardPanel.setDropTarget(new DropTarget() {
            @Override
            public synchronized void drop(DropTargetDropEvent dtde) {
                try {
                    Transferable transferable = dtde.getTransferable();
                    Tile droppedTile = (Tile) transferable.getTransferData(TileTransferable.TILE_FLAVOR);

                    boolean isTileAlreadyInBoard = false;
                    LinkedList<Tile> targetGroup = null;

                    // 보드 타일 리스트를 순회하여 타일이 이미 있는지 확인
                    for (LinkedList<Tile> group : boardLinkedTileList) {
                        if (group.contains(droppedTile)) {
                            isTileAlreadyInBoard = true;
                            targetGroup = group;
                            break;
                        }
                    }

                    if (isTileAlreadyInBoard && targetGroup != null) {
                        // 보드 그룹 내에서 타일의 현재 위치를 찾아 마지막으로 이동
                        targetGroup.remove(droppedTile);
                        targetGroup.addLast(droppedTile);
                    } else {
                        // 타일 패널에서 보드 패널로 처음 드래그되는 경우
                        // 새로운 그룹을 만들어 추가
                        tileList.remove(droppedTile);

                        LinkedList<Tile> newGroup = new LinkedList<>();
                        newGroup.add(droppedTile);
                        boardLinkedTileList.add(newGroup);
                    }

                    // 보드 패널 업데이트
                    updateBoardPanel(boardLinkedTileList);

                    // 타일 패널 업데이트
                    updateTilePanel();

                    dtde.acceptDrop(DnDConstants.ACTION_MOVE);
                    dtde.dropComplete(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    dtde.rejectDrop();
                }
            }
        });

        
        // 화면 가로, 세로 크기 구하기
        int screenWidth = getWidth();  // 화면의 가로 길이
        int screenHeight = getHeight();  // 화면의 세로 길이

        // 보드 패널의 가로, 세로 길이를 화면 크기에 비례하여 설정
        int boardPanelWidth = (int) (screenWidth * 0.65);  // 화면 가로 길이의 70%
        int boardPanelHeight = (int) (screenHeight * 0.7);  // 화면 세로 길이의 50%

        // 보드 패널 크기 설정
        boardPanel.setPreferredSize(new Dimension(boardPanelWidth, boardPanelHeight));

        
        // 보드 패널에 스크롤 기능 추가
        JScrollPane boardScrollPane = new JScrollPane(boardPanel);
        boardScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        boardScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        // 보드 패널을 contentPane에 추가
        contentPane.add(boardScrollPane, BorderLayout.CENTER);

        // 하단 패널 설정 (기존 코드와 대부분 동일)
        JPanel playerPanel = new JPanel();
        playerPanel.setBackground(new Color(25, 25, 112));
        playerPanel.setLayout(new BorderLayout());
        playerPanel.setOpaque(false);
        playerPanel.setBorder(new EmptyBorder(10, 20, 30, 10));

        nicknameLabel = new JLabel(username);
        nicknameLabel.setForeground(Color.WHITE);
        nicknameLabel.setFont(new Font("Arial", Font.BOLD, 15));
        playerPanel.add(nicknameLabel, BorderLayout.NORTH);

        timeLabel = new JLabel("남은 시간: " + remainingTime + "초");
        timeLabel.setForeground(Color.WHITE);
        playerPanel.add(timeLabel);

        // 타이머 설정 (기존 코드와 동일)
        timer = new Timer(1000, e -> {
            if (remainingTime > 0) {
                remainingTime--;
                timeLabel.setText("남은 시간: " + remainingTime + "초");
            } else {
                timer.stop();
            }
        });
        timer.start();
        
        // "나가기" 버튼 생성
        JButton exitButton = new JButton("exit");
        exitButton.setPreferredSize(new Dimension(80, 50));
        exitButton.setFont(new Font("Arial", Font.BOLD, 20));
        exitButton.setBackground(Color.RED);
        exitButton.setForeground(Color.BLACK);
        exitButton.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                this, "정말 나가시겠습니까?", "나가기 확인",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
            	// 서버에 퇴장 알림 전송
                //SendMessage("/exit " + username);
            	
            	// 연결 종료
                try {
                    dos.close();  // 데이터 출력 스트림 종료
                    dis.close();  // 데이터 입력 스트림 종료
                    socket.close();  // 소켓 종료
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            	
                // startGUI 창을 새로 열기
                new startGUI().setVisible(true);
                
                // PlayerGUI 창을 닫기
                this.dispose();
            }
        });



        JPanel exitButtonPanel = new JPanel();
        exitButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        exitButtonPanel.setBackground(new Color(25, 25, 112));
        exitButtonPanel.add(exitButton);
        playerPanel.add(exitButtonPanel, BorderLayout.SOUTH);

        // 버튼 패널 설정 (기존 코드와 동일)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(25, 25, 112));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 20, 20));

        // 777, 789, + 버튼들 (기존 코드와 동일)
        JButton btn777 = new JButton("777");
        btn777.setPreferredSize(new Dimension(100, 75));
        btn777.setMaximumSize(new Dimension(100, 75));
        btn777.setForeground(Color.BLACK);
        btn777.setFont(new Font("Arial", Font.BOLD, 20));
        btn777.addActionListener( new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                // 서버로 타일 리스트의 숫자 정렬 요청
                SendMessage("/tileSortToNumber");
            }
        });
        buttonPanel.add(btn777);

        JButton btn789 = new JButton("789");
        btn789.setPreferredSize(new Dimension(100, 75));
        btn789.setMaximumSize(new Dimension(100, 75));
        btn789.setForeground(Color.BLACK);
        btn789.setFont(new Font("Arial", Font.BOLD, 20));
        btn789.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 서버로 타일 색깔로 정렬 요청
                SendMessage("/tileSortToColor");
            }
        });
        buttonPanel.add(btn789);
        
        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(100, 75));
        addButton.setMaximumSize(new Dimension(100, 75));
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("Arial", Font.BOLD, 20));
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 서버로 "p" 메시지 전송
                SendMessage("p");
            }
        });
        buttonPanel.add(addButton);

        // 하단 패널 설정
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.setBackground(new Color(25, 25, 112));
        bottomPanel.setPreferredSize(new Dimension(800, 220));
        bottomPanel.add(playerPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // 타일 패널 설정
        tilePanel = new JPanel();
        tilePanel.setBackground(new Color(222, 184, 135));
        tilePanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        tilePanel.setPreferredSize(new Dimension(750, 400));

        // 스크롤 패널 설정
        JScrollPane scrollPane = new JScrollPane(tilePanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        contentPane.add(bottomPanel, BorderLayout.SOUTH);
        setContentPane(contentPane);
        setLocationRelativeTo(null);
        
        // 채팅 패널 설정
        JPanel chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.setBackground(new Color(25, 25, 112));
        chatPanel.setPreferredSize(new Dimension(200, 600)); // 채팅 영역 크기
        
        // 채팅 내용 표시 영역
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(new Color(240, 240, 240));
        chatArea.setForeground(Color.BLACK);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane chatScroll = new JScrollPane(chatArea);
        chatPanel.add(chatScroll, BorderLayout.CENTER);

        // 메시지 입력 필드 및 전송 버튼
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout());
        inputPanel.setBackground(new Color(25, 25, 112));
        
        messageField = new JTextField();
		messageField.addActionListener(new sendAction());

		sendButton = new JButton("⬆");
		sendButton.setBounds(288, 364, 76, 40);
		
		endButton = new JButton("End︎");
		endButton.setPreferredSize(new Dimension(86, 50));
		endButton.setFont(new Font("Gothic", Font.BOLD, 20));
		endButton.setForeground(new Color(255, 127, 80));
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 서버로 "e" 메시지 전송
                SendMessage("e");
            }
        });
        endButton.setEnabled(false);
		
		submitButton = new JButton("✓︎");
		submitButton.setPreferredSize(new Dimension(86, 50));
		submitButton.setFont(new Font("Gothic", Font.BOLD, 25));
		//submitButton.setForeground(new Color(255, 127, 80));
		submitButton.setForeground(new Color(80, 255, 150));
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 서버로 "-1" 메시지 전송
                SendMessage("-1");
            }
        });
        submitButton.setEnabled(false);
		
		
		inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        JPanel btnPanel = new JPanel(); // 추가 패널 생성
        btnPanel.setLayout(new FlowLayout()); // 버튼을 가로로 나열
        btnPanel.setBackground(new Color(25, 25, 112));

        btnPanel.add(submitButton);
        btnPanel.add(endButton);

        // 패널을 SOUTH에 추가
        inputPanel.add(btnPanel, BorderLayout.SOUTH);
		
        chatPanel.add(inputPanel, BorderLayout.SOUTH);
       	
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no + "\n");
        try {
            socket = new Socket(ip_addr, Integer.parseInt(port_no));
            is = socket.getInputStream();
            dis = new DataInputStream(is);
            os = socket.getOutputStream();
            dos = new DataOutputStream(os);

            SendMessage("/login " + username);
            ListenNetwork net = new ListenNetwork();
            net.start();
            sendAction action = new sendAction();
            sendButton.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
            sendButton.requestFocus();
        } catch (NumberFormatException | IOException e) {
            e.printStackTrace();
            AppendText("connect error");
        }
        
        contentPane.add(chatPanel, BorderLayout.EAST);
        setContentPane(contentPane);
        
		setVisible(true);
    }

    // 화면에 출력하는 함수
    public void AppendText(String msg) {
        chatArea.append(msg);
        chatArea.setCaretPosition(chatArea.getText().length());
    }
    
    // keyboard enter key 치면 서버로 전송
 	class sendAction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
 	{
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// Send button을 누르거나 메시지 입력하고 Enter key 치면
 			if (e.getSource() == sendButton || e.getSource() == messageField) {
 				String msg = null;
 				msg = String.format("[%s] %s\n", username, messageField.getText());
 				SendMessage(msg);
 				messageField.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
 				messageField.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
 				if (msg.contains("/exit")) // 종료 처리
 					System.exit(0);
 			}
 		}
 	}
 	
 	// Server Message를 수신해서 화면에 표시
    class ListenNetwork extends Thread {
        public void run() {
            while (true) {
                try {
                    // Use readUTF to read messages
                    String msg = dis.readUTF();

                    if (msg.equals("/yourTurn")){
                        submitButton.setEnabled(true);
                        endButton.setEnabled(true);
                    } else if (msg.equals("/otherTurn")){
                		submitButton.setEnabled(false);
                        endButton.setEnabled(false);
                    }

                    if (msg.equals("Game Start!")) {
                        SwingUtilities.invokeLater(() -> {
                            // PlayerGUI 보이게 하기
                            setVisible(true);
                            
                            // 이전 nameGUI 창 닫기
                            for (Window window : Window.getWindows()) {
                                if (window instanceof nameGUI) {
                                    window.dispose();
                                    break;
                                }
                            }
                        });
                    }
                    
                    // '/newTileList'로 시작하는 메시지인지 확인
                    if (msg.startsWith("/newTileList")) {
                        // 타일 리스트 메시지 처리
                        List<Tile> newTileList = parseTileListFromMessage(msg);
                        updateTilePanel(newTileList);  // 타일 패널 업데이트
                    } else if (msg.startsWith("/newBoardTileList")) {
                        // 타일 리스트 메시지 처리
                    	List<LinkedList<Tile>> newBoardTileList = parseLinkedTileListFromMessage(msg);
                    	updateBoardPanel(newBoardTileList);
//                    	List<Tile> newBoardTileList = parseLinkedTileListFromMessage(msg);
//                    	updateBoardPanel(newBoardTileList);
                        System.out.println("msg: " + msg);  // 디버깅: 배열 출력
                        System.out.println("tileArray: " + newBoardTileList.toString());  // 디버깅: 배열 출력
                    } else {
                        // 다른 메시지는 화면에 표시
                        AppendText(msg);
                    }
                } catch (IOException e) {
                    AppendText("dis.read() error");
                    try {
                        dos.close();
                        dis.close();
                        socket.close();
                        break;
                    } catch (Exception ee) {
                        break;
                    }
                }
            }
        }
    }
    
    private List<Tile> parseTileListFromMessage(String message) {
    	System.out.println("message: " + message);
    	
        // 메시지에서 "/tileList " 이후의 부분을 파싱하여 List<Tile>로 변환
        String tileListString = message.substring(12);  // "/newTileList " 이후의 부분
        System.out.println("tileListString: " + tileListString);  // 디버깅: 파싱할 문자열 출력
        List<Tile> newTileList = new ArrayList<>();

        // 메시지에서 각 타일을 "[번호, 색상]" 형식으로 구분하기 위해 파싱
        // 첫 번째 및 마지막 문자인 "["와 "]"를 제거한 후 타일들을 ',' 기준으로 분리
        String[] tileArray = tileListString.replace("[", "").replace("]", "").split(", ");
        System.out.println("tileArray: " + Arrays.toString(tileArray));  // 디버깅: 배열 출력

        // 타일을 "번호"와 "색상"으로 분리하여 Tile 객체 생성
        for (int i = 0; i < tileArray.length; i += 2) {
            try {
                int number = Integer.parseInt(tileArray[i].trim());  // 번호 부분 파싱
                TileColor color = TileColor.valueOf(tileArray[i + 1].trim().toUpperCase());  // 색상 부분 파싱

                // Tile 객체 생성 후 리스트에 추가
                newTileList.add(new Tile(number, color));
            } catch (Exception e) {
                System.err.println("Error parsing tile: " + tileArray[i] + ", " + tileArray[i + 1]);  // 예외 발생 시 오류 메시지 출력
                e.printStackTrace();
            }
        }

        return newTileList;
    }

    private List<LinkedList<Tile>> parseLinkedTileListFromMessage(String message) {
        System.out.println("Received message: " + message);

        if (!message.startsWith("/newBoardTileList ")) {
            System.err.println("Invalid message format: " + message);
            return Collections.emptyList();
        }

        List<LinkedList<Tile>> boardTileList = new ArrayList<>();
        String tileListString = message.substring(18).trim();
        System.out.println("Original tileListString: " + tileListString);

        try {
            // 바깥 대괄호 제거
            tileListString = tileListString.substring(1, tileListString.length() - 1).trim();

            // 정규식을 사용해 가장 바깥 레벨의 그룹을 분리
            Pattern groupPattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
            Matcher matcher = groupPattern.matcher(tileListString);

            while (matcher.find()) {
                String groupString = matcher.group(1); // 그룹 내부의 문자열 추출
                System.out.println("Extracted groupString: " + groupString);

                LinkedList<Tile> groupTiles = new LinkedList<>();

                // 그룹 내부 타일 분리
                String[] tileStrings = groupString.split("\\],\\s*\\[");
                for (String tileStr : tileStrings) {
                    tileStr = tileStr.replaceAll("\\[|\\]", "").trim();
                    String[] parts = tileStr.split(",\\s*");

                    if (parts.length >= 2) {
                        int number = Integer.parseInt(parts[0].trim());
                        TileColor color = TileColor.valueOf(parts[1].trim().toUpperCase());
                        groupTiles.add(new Tile(number, color));
                    }
                }

                if (!groupTiles.isEmpty()) {
                    boardTileList.add(groupTiles);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing tile list: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }

        System.out.println("Parsed board tile list: " + boardTileList);
        return boardTileList;
    }

    // Server에게 network으로 전송
    public void SendMessage(String msg) {
        try {
            // Use writeUTF to send messages
            dos.writeUTF(msg);
        } catch (IOException e) {
            AppendText("dos.write() error");
            try {
                dos.close();
                dis.close();
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(0);
            }
        }
    }
    
    // 타일 라벨 생성 메서드 수정
    private JLabel createTileLabel(Tile tile) {
        String tileImage;
        if (tile.getNumber() == 999) {
            tileImage = "joker.png";  // 999인 경우 joker 이미지 사용
        } else {
            tileImage = tile.getColor().name() + "_" + tile.getNumber() + ".png";
        }
        ImageIcon tileIcon = new ImageIcon("images/" + tileImage);
        Image image = tileIcon.getImage();

        int originalWidth = tileIcon.getIconWidth();
        int originalHeight = tileIcon.getIconHeight();
        int targetWidth = 50;
        int targetHeight = (originalHeight * targetWidth) / originalWidth;

        Image resizedImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        tileIcon = new ImageIcon(resizedImage);

        JLabel tileLabel = new JLabel(tileIcon);

        // 기본 테두리 색 설정 (처음에는 투명한 테두리)
        final Color defaultBorderColor = new Color(222, 184, 135); // 원래 테두리 색 (라이트 브라운 계열)
        tileLabel.setBorder(new RoundedBorder(20, defaultBorderColor, 2)); // 초기에는 테두리가 보이지 않도록 설정

        // 클릭 기능 추가
        tileLabel.addMouseListener(new MouseAdapter() {
            private boolean isSelected = false; // 선택 여부

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isSelected) {
                    // 선택 해제 시 기본 테두리 색으로 돌아가게
                    tileLabel.setBorder(new RoundedBorder(20, defaultBorderColor, 2)); // 기본 테두리 색으로 설정
                } else {
                    // 선택 시 노란색 테두리로 변경
                    tileLabel.setBorder(new RoundedBorder(20, Color.YELLOW, 2)); // 선택된 상태로 노란색 테두리 추가

                    // tileList에서 해당 타일의 인덱스 찾기
                    int tileIndex = tileList.indexOf(tile);
                    
                    // 서버로 타일 인덱스 전송
                    if (tileIndex != -1) {
                        SendMessage(Integer.toString(tileIndex));
                    }
                }
                isSelected = !isSelected; // 상태 토글
            }
        });

        return tileLabel;
}

    // 커스텀 둥근 테두리 Border 클래스
    class RoundedBorder implements Border {
        private int radius;
        private Color color;
        private int thickness;

        public RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); // 테두리 색상
            g2.setStroke(new BasicStroke(thickness)); // 테두리 두께
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius); // 둥근 사각형
        }

        @Override
    public Insets getBorderInsets(Component c) {
        return new Insets(thickness + 0, thickness + 0, thickness + 0, thickness + 0); // 테두리 여백을 더 적게 설정
    }
        @Override
        public boolean isBorderOpaque() {
            return false; // 투명한 테두리
        }
    }

    private void updateTilePanel() {
        tilePanel.removeAll();
        for (Tile tile : tileList) {
            JLabel tileLabel = createTileLabel(tile);
            tilePanel.add(tileLabel);
        }
        tilePanel.revalidate();
        tilePanel.repaint();
    }

    // 타일 TransferHandler
    private static class TileLabelTransferHandler extends TransferHandler {
        private Tile tile;

        public TileLabelTransferHandler(Tile tile) {
            this.tile = tile;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return MOVE;
        }

        @Override
        protected Transferable createTransferable(JComponent c) {
            return new TileTransferable(tile);
        }
    }

    // 타일 Transferable
    private static class TileTransferable implements Transferable {
        public static final DataFlavor TILE_FLAVOR = 
            new DataFlavor(Tile.class, "Tile");
        
        private Tile tile;

        public TileTransferable(Tile tile) {
            this.tile = tile;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{TILE_FLAVOR};
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return flavor.equals(TILE_FLAVOR);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return tile;
        }
    }

    // 보드 패널 TransferHandler
    private class BoardPanelTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(TileTransferable.TILE_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport support) {
            try {
                Transferable transferable = support.getTransferable();
                Tile droppedTile = (Tile) transferable.getTransferData(TileTransferable.TILE_FLAVOR);
                
                // 타일 패널에서 해당 타일 제거
                tileList.remove(droppedTile);
                
                // 새로운 그룹 생성
                LinkedList<Tile> newGroup = new LinkedList<>();
                newGroup.add(droppedTile);
                boardLinkedTileList.add(newGroup);
                
                // 패널 업데이트
                updateTilePanel();
                updateBoardPanel(boardLinkedTileList);
                
                return true;
            } catch (UnsupportedFlavorException | IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
    
    // 타일 패널 업데이트
    public void updateTilePanel(List<Tile> newTileList) {
        // 1. 기존 타일 패널 내용 제거
        tilePanel.removeAll();

        // 2. 새로운 타일 리스트에 따라 타일 추가
        for (Tile tile : newTileList) {
            JLabel tileLabel = createTileLabel(tile); // 타일에 대한 JLabel 생성
            tilePanel.add(tileLabel); // 타일 패널에 추가
        }

        // 3. 타일 패널 업데이트
        tilePanel.revalidate(); // 레이아웃 갱신
        tilePanel.repaint();    // 화면 다시 그리기

        // 4. 내부 타일 리스트 갱신 (필요한 경우)
        tileList.clear();
        tileList.addAll(newTileList);
    }
    
    public void updateBoardPanel(List<LinkedList<Tile>> newBoardTileList) {
        boardPanel.removeAll(); // 기존 UI 삭제

        boardLinkedTileList.clear();
        boardLinkedTileList.addAll(newBoardTileList);

        System.out.println("boardLinkedTileList: " + boardLinkedTileList);

        for (LinkedList<Tile> group : boardLinkedTileList) {
            System.out.println("group: " + group);

            for (Tile tile : group) {
                System.out.println("tile: " + tile);
                JLabel tileLabel = createTileLabel(tile);
                if (tileLabel != null) {
                    boardPanel.add(tileLabel);
                } else {
                    System.out.println("Warning: Null tile label for tile: " + tile);
                }
            }

            // 그룹 간격 추가 (디자인에 따라 조정)
            JLabel spacerLabel = new JLabel("      "); // 간격 증가
            boardPanel.add(spacerLabel);
        }

        // 보드 패널 컴포넌트 확인
        System.out.println("boardPanel component count: " + boardPanel.getComponentCount());

        boardPanel.revalidate(); // 레이아웃 갱신
        boardPanel.repaint();    // UI 새로고침
    }


}