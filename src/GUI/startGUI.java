package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class startGUI extends JFrame {
    public startGUI() {
        setTitle("Welcome to Rummikub with Chat Game!");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // 레이아웃 매니저 비활성화
        
        getContentPane().setBackground(new Color(25, 25, 112));

        // 안내 라벨 추가
        JLabel logoLabel = new JLabel("Rummikub", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Times New Roman", Font.BOLD, 80));
        logoLabel.setForeground(new Color(200, 0, 0)); // 버건디
        logoLabel.setBounds(100, 150, 600, 100); // x, y, width, height
        add(logoLabel);

        JLabel wcLabel = new JLabel("With Chat", SwingConstants.CENTER);
        wcLabel.setFont(new Font("Times New Roman", Font.BOLD, 30));
        wcLabel.setForeground(new Color(255, 255, 255)); 
        wcLabel.setBounds(430, 220, 200, 50); // x, y, width, height
        add(wcLabel);

        //시작 버튼 추가
        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        startButton.setBounds(225, 350, 150, 50); // x, y, width, height
        startButton.addActionListener((ActionEvent e) -> {
            // PlayerGUI로 전환
            new configurationGUI().setVisible(true);
            // 현재 창 숨기기 또는 닫기
            this.dispose();
        });
        add(startButton);
        
        //시작 버튼 추가
        JButton howButton = new JButton("How To Play");
        howButton.setFont(new Font("Times New Roman", Font.BOLD, 20));
        howButton.setBounds(425, 350, 150, 50); // x, y, width, height
        howButton.addActionListener((ActionEvent e) -> {
            // PlayerGUI로 전환
            new howToPlay().setVisible(true);
            // 현재 창 숨기기 또는 닫기
            this.dispose();
        });
        add(howButton);

        setLocationRelativeTo(null); // 화면 중앙에 배치
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            startGUI frame = new startGUI();
            frame.setVisible(true);
        });
    }
}
