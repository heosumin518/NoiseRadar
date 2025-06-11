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
    private String currentLocation = "부산시민공원";
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
                constructionMap.updateMapParameters(currentLocation, zoomLevel,
                        googleMap.getWidth(), googleMap.getHeight());
            }
        });

        initZoomInOutButtonStyleAndEvent();

        googleMap.add(zoomInButton);
        googleMap.add(zoomOutButton);

        googleMap.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                googleMap.requestFocusInWindow();
            }
        });

        add(googleMap, BorderLayout.CENTER);

        // Z-Order 및 위치 설정
        SwingUtilities.invokeLater(() -> {
            repositionButton();

            googleMap.setComponentZOrder(coneButton, 0);
            googleMap.setComponentZOrder(zoomInButton, 0);
            googleMap.setComponentZOrder(zoomOutButton, 0);
            googleMap.setComponentZOrder(constructionMap, 2);
        });

        setMap("부산시민공원");
    }

    private void initZoomInOutButtonStyleAndEvent() {
        zoomInButton.setSize(50, 40);
        zoomOutButton.setSize(50, 40);

        zoomInButton.setFocusPainted(false);
        zoomOutButton.setFocusPainted(false);

        zoomInButton.setFont(new Font("Arial", Font.BOLD, 18));
        zoomOutButton.setFont(new Font("Arial", Font.BOLD, 18));

        zoomInButton.addActionListener(e -> {
            System.out.println("zoomIn clicked");
            changeZoomLevel(1);
        });

        zoomOutButton.addActionListener(e -> {
            System.out.println("zoomOut clicked");
            changeZoomLevel(-1);
        });
    }

    private void performSearch() {
        setMap(textField.getText());
        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }

    private void toggleConstructionMap() {
        constructionMap.setVisible(!constructionMap.isVisible());
        if (constructionMap.isVisible()) {
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
    }

    public void setMap(String location) {
        currentLocation = location;

        googleAPI.downloadMap(location, zoomLevel);
        googleMap.setIcon(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        constructionMap.setBounds(0, 0,
                googleMap.getIcon().getIconWidth(),
                googleMap.getIcon().getIconHeight());

        constructionMap.updateMapParameters(location, zoomLevel,
                googleMap.getIcon().getIconWidth(),
                googleMap.getIcon().getIconHeight());

        // 공사 데이터 항상 새로 계산 (가시성 여부와 무관하게)
        constructionMap.fetchDataFromAPI();

        repositionButton();

        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }

    public void onScreenShown() {
        SwingUtilities.invokeLater(() -> {
            googleMap.requestFocusInWindow();
        });
    }
}
