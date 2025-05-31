package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.border.LineBorder;

public class IntroScreen extends JFrame {

    private JTextField searchField;
    private JButton searchButton;
    private JLabel logoLabel;

    public IntroScreen() {
        setTitle("NoiseRadar");
        setSize(800, 450);  // 16:9 비율
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // 절대 위치

        // ✅ 배경 흰색 설정     
        getContentPane().setBackground(Color.WHITE);

        // 로고 이미지 (16:9 비율로 크기 조정)
        ImageIcon originalIcon = new ImageIcon("src/main/java/com/mycompany/noiseradar/noiseradar_logo.png");
        int logoWidth = 320;
        int logoHeight = 180;
        Image scaledImage = originalIcon.getImage().getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
        ImageIcon logoIcon = new ImageIcon(scaledImage);

        logoLabel = new JLabel(logoIcon);
        logoLabel.setBounds((800 - logoWidth) / 2, 20, logoWidth, logoHeight); // 중앙 정렬
        add(logoLabel);

        // 검색창
        searchField = new JTextField();
        searchField.setBounds(250, 250, 300, 30);
        searchField.setBorder(new LineBorder(Color.BLACK, 2));
        add(searchField);

        // 검색 버튼
        searchButton = new JButton("search");
        searchButton.setBounds(560, 250, 80, 30);
        add(searchButton);

        // 검색 버튼 클릭 이벤트
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String query = searchField.getText().trim();
                if (!query.isEmpty()) {
                    dispose(); // 현재 창 닫기
                    Main mainFrame = new Main();
                    mainFrame.setMap(query); // 검색어 전달
                    mainFrame.setVisible(true); // 메인 창 열기
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new IntroScreen().setVisible(true);
        });
    }
}
