package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {
    private final JTextField textField = new JTextField(30);
    private final JPanel topPanel = new JPanel();
    private final JButton searchButton = new JButton("search");
    private final GoogleAPI googleAPI = new GoogleAPI();
    private final JLabel googleMap = new JLabel();
    private final ConstructionMap constructionMap = new ConstructionMap();
    private final JButton coneButton;
    private int zoomLevel = 11;

    public Main() {
        setTitle("Google Maps");
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // 상단 검색 패널 설정
        topPanel.add(textField);
        topPanel.add(searchButton);
        searchButton.addActionListener(e -> performSearch());
        add(topPanel, BorderLayout.NORTH);

        // 지도 레이블 레이아웃 설정
        googleMap.setLayout(null);

        // 공사정보 맵 설정
        constructionMap.setVisible(false);
        googleMap.add(constructionMap);

        // 콘 아이콘 버튼 설정
        ImageIcon coneIcon = new ImageIcon("src/main/java/com/mycompany/noiseradar/cone_button.png");
        coneButton = new JButton(coneIcon);
        coneButton.setContentAreaFilled(false);
        coneButton.setBorderPainted(false);
        coneButton.setFocusPainted(false);
        coneButton.setSize(coneIcon.getIconWidth(), coneIcon.getIconHeight());
        coneButton.addActionListener(e -> toggleConstructionMap());
        googleMap.add(coneButton);

        // 지도 크기 변경 시 처리
        googleMap.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButton();
                constructionMap.setBounds(0, 0, googleMap.getWidth(), googleMap.getHeight());
            }
        });

        SwingUtilities.invokeLater(this::repositionButton);

        add(BorderLayout.SOUTH, googleMap);
        
        // 줌 기능 처리
        googleMap.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            zoomLevel = Math.max(1, Math.min(20, zoomLevel - notches));
            if (!textField.getText().isEmpty()) {
                setMap(textField.getText());
            }
        });

        add(googleMap, BorderLayout.SOUTH);

        // 기본 지도 로딩
        SwingUtilities.invokeLater(this::repositionButton);
        setMap("Busan");

        pack();
        setVisible(true);
    }
    
    private void performSearch() {
        setMap(textField.getText());
        googleMap.setFocusable(true);
        googleMap.requestFocusInWindow();
    }
    
    private void toggleConstructionMap() {
        constructionMap.setVisible(!constructionMap.isVisible());
    }

    private void repositionButton() {
        int margin = 10;
        int x = googleMap.getWidth() - coneButton.getWidth() - margin;
        int y = googleMap.getHeight() - coneButton.getHeight() - margin - 50;
        coneButton.setLocation(x, y);
    }

    public void setMap(String location) {
        /*
        googleAPI.downloadMap(location, zoomLevel);
        googleMap.setIcon(googleAPI.getMap(location));
        googleAPI.fileDelete(location);
        */
        
        // 좌표 기반으로 지도 다운로드 및 표시 방법1
        String address = googleAPI.reverseGeocode(35.0912398946, 129.0678888023);
        if (address != null) {
            googleAPI.downloadMap(address, zoomLevel);
            googleMap.setIcon(googleAPI.getMap(address));
            googleAPI.fileDelete(address);
        }
        
        // 좌표 기반으로 지도 다운로드 및 표시 방법2
        /*
        googleAPI.downloadMap(35.0912398946, 129.0678888023, zoomLevel);
        googleMap.setIcon(googleAPI.getMap("temp_map.png"));
        googleAPI.fileDelete("temp_map.png");
        */

        constructionMap.setBounds(0, 0,
            googleMap.getIcon().getIconWidth(),
            googleMap.getIcon().getIconHeight());
        
        repositionButton();
        constructionMap.fetchDataFromAPI();
        pack();
    }
}
