package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class winnerGUI extends JFrame {
    private JLabel congLabel;
    private JLabel winnerLabel;
    private JButton exitButton;

    public winnerGUI(String winnerName) {
        // 기본 창 설정
        setTitle("Winner GUI");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // 배경 색상 설정
        getContentPane().setBackground(new Color(25, 25, 112));

        // 중앙 패널 생성
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS)); // 수직 박스 레이아웃
        centerPanel.setBackground(new Color(25, 25, 112));

        // 축하 문구 라벨
        congLabel = new JLabel("🎉"+"Congratulations!"+"🎉", SwingConstants.CENTER);
        congLabel.setFont(new Font("Times New Roman", Font.BOLD, 36));
        congLabel.setForeground(Color.WHITE);
        congLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
        centerPanel.add(congLabel);

        // 라벨 사이 간격 추가
        centerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // 세로로 10px 간격

        // 승자 표시 라벨
        winnerLabel = new JLabel(winnerName + " is WINNER!", SwingConstants.CENTER);
        winnerLabel.setFont(new Font("Times New Roman", Font.BOLD, 36));
        winnerLabel.setForeground(Color.WHITE);
        winnerLabel.setAlignmentX(Component.CENTER_ALIGNMENT); // 가운데 정렬
        centerPanel.add(winnerLabel);

        // 중앙 패널 여백 설정
        centerPanel.setBorder(BorderFactory.createEmptyBorder(200, 0, 50, 0)); // 위아래 여백 추가
        add(centerPanel, BorderLayout.CENTER);

        // 닫기 버튼
        exitButton = new JButton("EXIT");
        exitButton.setFont(new Font("Times New Roman", Font.BOLD, 18));
        exitButton.setPreferredSize(new Dimension(100, 40)); // 버튼 크기 설정
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // 창 닫기
            }
        });

        // 버튼 패널
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 왼쪽 정렬
        buttonPanel.setBackground(new Color(25, 25, 112));
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.NORTH);
    }

    // 메인 메서드 (테스트용)
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            winnerGUI gui = new winnerGUI("Player1");
            gui.setVisible(true);
        });
    }
}
