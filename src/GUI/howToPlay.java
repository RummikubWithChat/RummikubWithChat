package GUI;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class howToPlay extends JFrame {
    public howToPlay() {
        setTitle("How To Play");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // 레이아웃 매니저 비활성화

        getContentPane().setBackground(new Color(25, 25, 112));

        // 뒤로가기 버튼 추가
        JButton backButton = new JButton("BACK");
        backButton.setFont(new Font("Times New Roman", Font.BOLD, 14));
        backButton.setBounds(20, 20, 100, 40); // x, y, width, height
        backButton.addActionListener((ActionEvent e) -> {
            // startGUI로 돌아가기
            new startGUI().setVisible(true);
            this.dispose(); // 현재 창 닫기
        });
        add(backButton);

        // JTextPane 사용하여 스타일 적용
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setFont(new Font("Arial", Font.PLAIN, 16));
        textPane.setForeground(Color.WHITE);
        textPane.setBackground(new Color(25, 25, 112));

        // StyledDocument를 사용하여 텍스트 스타일 적용
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Style", null);
        Style titleStyle = textPane.addStyle("TitleStyle", null);

        // 규칙 내용 작성
        try {
            // 1. 게임 준비
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "1. 게임 준비\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 각 플레이어는 14개의 타일을 받습니다.\n", style);
            doc.insertString(doc.getLength(), "- 타일은 숫자(1~13)와 색상(블루, 레드, 옐로우, 블랙)으로 이루어져 있습니다.\n\n", style);

            // 2. 게임 목표
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "2. 게임 목표\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 손에 있는 타일을 먼저 모두 내려놓으면 승리입니다.\n\n", style);

            // 3. 타일 배치 방법
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "3. 타일 배치 방법\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 세트: 같은 숫자, 다른 색상 3개 이상의 타일.\n", style);
            doc.insertString(doc.getLength(), "- 연속: 같은 색상으로 순차적으로 나열된 숫자들.\n\n", style);

            // 4. 게임 진행
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "4. 게임 진행\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 첫 번째 턴에 각 플레이어는 30점 이상의 타일을 내려놓아야 합니다.\n", style);
            doc.insertString(doc.getLength(), "- 이후에는 점수 제한 없이 자유롭게 타일을 내려놓을 수 있습니다.\n\n", style);

            // 5. 타일 내려놓기
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "5. 타일 내려놓기\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 세트 또는 연속으로 타일을 내려놓을 수 있습니다.\n", style);
            doc.insertString(doc.getLength(), "- 자신의 타일을 누르면 하나의 세트 타일에 추가됩니다.\n", style);
            doc.insertString(doc.getLength(), "- 하나의 세트 타일을 선택 후 '✓' 버튼을 누르면 유효한지 검사한 후 최종적으로 보드에 내려놓아집니다.\n", style);
            doc.insertString(doc.getLength(), "- 유효하지 않다면 내려놓기 이전으로 되돌아갑니다.\n\n", style);
            
            // 6. 보드 위의 타일 가져오기
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "6. 보드 위의 타일 가져오기\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 먼저, 보드 위의 수정하고자 하는 타일을 누릅니다. 선택된 타일이 속한 타일 배열이 수정 대상으로 지정됩니다.\n", style);
            doc.insertString(doc.getLength(), "- 그 배열에 추가하고자 하는 자신의 타일을 차례로 선택합니다. \n", style);
            doc.insertString(doc.getLength(), "- 모두 선택 후 '✓' 버튼을 누르면 유효한지 검사한 후 최종적으로 보드에 내려놓아집니다.\n", style);
            doc.insertString(doc.getLength(), "- 유효하지 않다면 내려놓기 이전으로 되돌아갑니다.\n\n", style);

            
            // 7. 턴 종료
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "7. 턴 종료\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- **주의** 타일을 한 번 편집하기 시작하고나면, 반드시 '✓'을 통해 행동을 마친 후 턴을 종료할 수 있습니다.\n", style);
            doc.insertString(doc.getLength(), "- 모든 행동을 마치고나서 'End' 버튼을 통해 자신의 턴을 종료합니다.\n", style);
            doc.insertString(doc.getLength(), "- '+' 버튼을 눌러 타일을 가져온 후 턴을 종료합니다.\n\n", style);

            // 8. 승리 조건
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "8. 승리 조건\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 손에 있는 모든 타일을 내려놓은 플레이어가 승리합니다.\n\n", style);

            // 9. 특별 규칙
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "9. 특별 규칙\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 조커: 다른 숫자나 색상을 대신할 수 있는 특별 타일입니다.\n", style);
            doc.insertString(doc.getLength(), "- **주의** 원하는 행동의 버튼을 눌러도 작동되지 않을 시 만능 버튼 >>'✓'<<을 눌러주세요!\n\n", style);

            
            // 10. 채팅 기능
            StyleConstants.setBold(titleStyle, true);
            StyleConstants.setFontSize(titleStyle, 20); // 글자 크기 키우기
            doc.insertString(doc.getLength(), "10. 채팅 기능\n", titleStyle);
            StyleConstants.setBold(titleStyle, false);
            StyleConstants.setFontSize(titleStyle, 16); // 기본 글자 크기
            doc.insertString(doc.getLength(), "- 플레이어는 게임 진행 중 채팅 기능을 통해 다른 플레이어와 소통할 수 있습니다.\n", style);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 규칙을 스크롤 가능한 텍스트 영역에 표시
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBounds(20, 80, 740, 450);
        add(scrollPane);

        setLocationRelativeTo(null); // 화면 중앙에 배치
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            howToPlay frame = new howToPlay();
            frame.setVisible(true);
        });
    }
}
