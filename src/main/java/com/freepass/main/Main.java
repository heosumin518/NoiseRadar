package com.freepass.main;

import com.freepass.view.ConstructionMap;
import com.freepass.controller.GoogleAPI;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JPanel {

    private final JTextField textField = new JTextField(30);
    private final JPanel topPanel = new JPanel();
    private final JButton searchButton = new JButton("search");
    private final GoogleAPI googleAPI = new GoogleAPI();
    private final JLabel googleMap = new JLabel();
    private final ConstructionMap constructionMap = new ConstructionMap();
    private final JButton coneButton;
    private int zoomLevel = 11;
    private String currentLocation = "부산시민공원"; // 현재 지도 중심점 추적
    private final JButton zoomInButton = new JButton("+");
    private final JButton zoomOutButton = new JButton("-");

    public Main() {
        setLayout(new BorderLayout());

        // 상단 검색 패널 설정
        topPanel.add(textField);
        topPanel.add(searchButton);
        searchButton.addActionListener(e -> performSearch());
        add(topPanel, BorderLayout.NORTH);

        // 지도 레이블 레이아웃 설정
        googleMap.setLayout(null);
        // 포커스 가능하도록 설정
        googleMap.setFocusable(true);

        // 공사정보 맵 설정
        constructionMap.setVisible(false);
        googleMap.add(constructionMap);

        // 콘 아이콘 버튼 설정
        ImageIcon coneIcon = new ImageIcon("src/main/java/icon/cone_button.png");
        Image resizedImage = coneIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
        coneButton = new JButton(new ImageIcon(resizedImage));
        coneButton.setContentAreaFilled(false);
        coneButton.setBorderPainted(false);
        coneButton.setFocusPainted(false);
        coneButton.setSize(60, 60);
        coneButton.addActionListener(e -> toggleConstructionMap());
        googleMap.add(coneButton);

        // 지도 크기 변경 시 처리
        googleMap.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButton();
                constructionMap.setBounds(0, 0, googleMap.getWidth(), googleMap.getHeight());
                // 지도 크기가 변경되면 공사 위치도 재계산
                constructionMap.updateMapParameters(currentLocation, zoomLevel,
                        googleMap.getWidth(), googleMap.getHeight());
            }
        });
        
        initZoomInOutButtonStyleAndEvent();
        
        googleMap.add(zoomInButton);
        googleMap.add(zoomOutButton);

        // 마우스 클릭 시 포커스 설정
        googleMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                googleMap.requestFocusInWindow();
            }
        });

        // 패널 정리 및 기본 설정
        add(googleMap, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::repositionButton);

        setMap("부산시민공원");
    }
    
    private void initZoomInOutButtonStyleAndEvent() {
        zoomInButton.setSize(50, 40);
        zoomOutButton.setSize(50, 40);
        
        zoomInButton.setFocusPainted(false);
        zoomOutButton.setFocusPainted(false);
        
        zoomInButton.setFont(new Font("Arial", Font.BOLD, 18));
        zoomOutButton.setFont(new Font("Arial", Font.BOLD, 18));
        
        zoomInButton.addActionListener(e -> changeZoomLevel(1));
        zoomOutButton.addActionListener(e -> changeZoomLevel(-1));
    }

    private void performSearch() {
        setMap(textField.getText());
        // 검색 후 지도에 포커스 설정
        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }

    // Main.java 파일 내
    private void toggleConstructionMap() {
        constructionMap.setVisible(!constructionMap.isVisible());
        if (constructionMap.isVisible()) {
            // 공사 지도가 보이게 되면 데이터를 가져온다
            constructionMap.fetchDataFromAPI();
        }
    }

    private void repositionButton() {
        int margin = 10;
        int x = googleMap.getWidth() - coneButton.getWidth() - margin;
        int y = googleMap.getHeight() - coneButton.getHeight() - margin - 20;

        coneButton.setLocation(x, y);
        zoomInButton.setLocation(x, y - 100);
        zoomOutButton.setLocation(x, y - 50);
    }
    
    private void changeZoomLevel(int delta) {
        zoomLevel = Math.max(1, Math.min(20, zoomLevel + delta));
        if (!textField.getText().isEmpty()) {
            setMap(textField.getText());
        } else {
            setMap(currentLocation);
        }
        /*
        if (constructionMap.isVisible()) {
            constructionMap.fetchDataFromAPI();
        }
        */
    }

    public void setMap(String location) {
        System.out.println("setMap");
        currentLocation = location; // 현재 위치 업데이트

        googleAPI.downloadMap(location, zoomLevel);
        googleMap.setIcon(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        // 공사정보 맵 크기와 매개변수 업데이트
        constructionMap.setBounds(0, 0,
                googleMap.getIcon().getIconWidth(),
                googleMap.getIcon().getIconHeight());

        // 공사 맵에 현재 지도 정보 전달
        constructionMap.updateMapParameters(location, zoomLevel,
                googleMap.getIcon().getIconWidth(),
                googleMap.getIcon().getIconHeight());

        repositionButton();

        /*
        // API 데이터 새로 가져오기 (위치가 정확하게 계산됨)
        constructionMap.fetchDataFromAPI();
         */
        // 지도 업데이트 후 포커스 설정
        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }

    // 화면 전환 시 호출될 메서드 추가
    public void onScreenShown() {
        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }
}
