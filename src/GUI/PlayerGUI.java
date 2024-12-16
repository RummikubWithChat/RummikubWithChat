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
import javax.swing.border.EmptyBorder;

//import GUI.PlayerGUI.TileTransferable;

import java.util.*;
import java.util.List;
import javax.swing.Timer;

import model.tile.*;

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
    private List<Tile> boardTileList = new ArrayList<>();  // 보드 패널에 있는 타일 리스트
    
    private Map<TileColor, Map<Integer, Integer>> tileCounts = new EnumMap<>(TileColor.class);
    
    // 채팅 관련 변수들
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    
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
                    
                    // 보드 타일 리스트에 이미 있는 타일인지 확인
                    if (boardTileList.contains(droppedTile)) {
                        // 보드 타일 리스트에서 해당 타일의 현재 인덱스 찾기
                        int currentIndex = boardTileList.indexOf(droppedTile);
                        
                        // 타일을 리스트의 마지막으로 이동
                        boardTileList.remove(currentIndex);
                        boardTileList.add(droppedTile);
                    } else {
                        // 타일 패널에서 보드 패널로 처음 드래그되는 경우
                        // 기존 코드 유지 (타일 리스트에서 제거하고 보드 타일 리스트에 추가)
                        tileList.remove(droppedTile);
                        boardTileList.add(droppedTile);
                    }
                    
                    // 보드 패널 업데이트
                    updateBoardPanel();
                    
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
        btn777.addActionListener(e -> sortTilesByNumber());
        buttonPanel.add(btn777);

        JButton btn789 = new JButton("789");
        btn789.setPreferredSize(new Dimension(100, 75));
        btn789.setMaximumSize(new Dimension(100, 75));
        btn789.setForeground(Color.BLACK);
        btn789.setFont(new Font("Arial", Font.BOLD, 20));
        btn789.addActionListener(e -> sortTilesByColor());
        buttonPanel.add(btn789);
        
        JButton addButton = new JButton("+");
        addButton.setPreferredSize(new Dimension(100, 75));
        addButton.setMaximumSize(new Dimension(100, 75));
        addButton.setForeground(Color.BLACK);
        addButton.setFont(new Font("Arial", Font.BOLD, 20));
        addButton.addActionListener(e -> {
            TileColor randomColor = getRandomColor();
            int randomNumber = new Random().nextInt(13) + 1;
            Tile newTile = new Tile(randomNumber, randomColor);
            
            tileList.add(newTile);
            
            JLabel tileLabel = createTileLabel(newTile);
            tilePanel.add(tileLabel);
            
            tilePanel.revalidate();
            tilePanel.repaint();
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

        // 초기 타일 랜덤 생성
//        for (int i = 0; i < 14; i++) {
//            TileColor randomColor = getRandomColor();
//            int randomNumber = new Random().nextInt(13) + 1;
//            Tile tile = new Tile(randomNumber, randomColor);
//            tileList.add(tile);
//
//            JLabel tileLabel = createTileLabel(tile);
//            tilePanel.add(tileLabel);
//        }

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
		
		inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
		
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
                    
                    // '/tileList'로 시작하는 메시지인지 확인
                    if (msg.startsWith("/tileList")) {
                        // 타일 리스트 메시지 처리
                        List<Tile> newTileList = parseTileListFromMessage(msg);
                        updateTilePanel(newTileList);  // 타일 패널 업데이트
                    } else {
                        // 다른 메시지는 화면에 표시
                        AppendText(msg);
                    }
                    
                    //AppendText(msg);
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
        // 메시지에서 "/tileList " 이후의 부분을 파싱하여 List<Tile>로 변환
        String tileListString = message.substring(9);  // "/tileList " 이후의 부분
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
        int targetWidth = 60;
        int targetHeight = (originalHeight * targetWidth) / originalWidth;

        Image resizedImage = image.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
        tileIcon = new ImageIcon(resizedImage);

        JLabel tileLabel = new JLabel(tileIcon);
        
        // 드래그 기능 추가
        tileLabel.setTransferHandler(new TileLabelTransferHandler(tile));
        tileLabel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                JLabel label = (JLabel) e.getSource();
                TransferHandler handler = label.getTransferHandler();
                handler.exportAsDrag(label, e, TransferHandler.MOVE);
            }
        });

        return tileLabel;
    }

    // 타일 정렬 메서드들 (기존 코드와 동일)
    private void sortTilesByNumber() {
        tileList.sort(Comparator.comparingInt(Tile::getNumber));
        updateTilePanel();
    }

    private void sortTilesByColor() {
        tileList.sort(Comparator.comparing(Tile::getColor));
        updateTilePanel();
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
    
 // 보드 패널을 갱신하는 메서드
    private void updateBoardPanel() {
        boardPanel.removeAll();  // 기존의 모든 타일 제거
        
        // boardTileList에 있는 모든 타일을 보드 패널에 추가
        for (Tile tile : boardTileList) {
            JLabel tileLabel = createTileLabel(tile);  // 타일에 해당하는 레이블 생성
            boardPanel.add(tileLabel);  // 보드 패널에 레이블 추가
        }
        
        // 보드 패널 갱신 (revalidate 및 repaint 호출)
        boardPanel.revalidate();
        boardPanel.repaint();
    }

    private TileColor getRandomColor() {
        TileColor[] colors = TileColor.values();
        Random rand = new Random();
        return colors[rand.nextInt(colors.length)];
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
    private static class BoardPanelTransferHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(TileTransferable.TILE_FLAVOR);
        }

        @Override
        public boolean importData(TransferSupport support) {
            return true;
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

}