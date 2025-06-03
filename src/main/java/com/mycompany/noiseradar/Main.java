package com.mycompany.noiseradar;

import com.freepass.view.ConstructionMap;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Main extends JPanel {
    private final JTextField textField = new JTextField(30);
    private final JPanel topPanel = new JPanel();
    private final JButton searchButton = new JButton("search");
    private final GoogleAPI googleAPI = new GoogleAPI();
    private final JLabel googleMap = new JLabel();
    private final ConstructionMap constructionMap = new ConstructionMap();
    private final JButton coneButton;
    private int zoomLevel = 11;

    private final java.util.List<JLabel> mapMarkers = new ArrayList<>();

    public Main() {
        setLayout(new BorderLayout());

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
            }
        });

        // 줌 기능 처리
        googleMap.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            zoomLevel = Math.max(1, Math.min(20, zoomLevel - notches));
            if (!textField.getText().isEmpty()) {
                setMap(textField.getText());
            }
        });

        // 패널 정리 및 기본 설정
        add(googleMap, BorderLayout.CENTER);
        SwingUtilities.invokeLater(this::repositionButton);
        setMap("부산시민공원");
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
        int y = googleMap.getHeight() - coneButton.getHeight() - margin - 20;
        coneButton.setLocation(x, y);
    }

    public void setMap(String location) {
        googleAPI.downloadMap(location, zoomLevel);
        googleMap.setIcon(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        // 마커 제거
        for (JLabel marker : mapMarkers) {
            googleMap.remove(marker);
        }
        mapMarkers.clear();

        // 예시 마커
        Point markerPos = googleAPI.getPixelPositionInMap(
                location, 35.171899, 129.062228, zoomLevel,
                googleMap.getIcon().getIconWidth(), googleMap.getIcon().getIconHeight()
        );
        if (markerPos != null) {
            JLabel marker = new JLabel("📍");
            marker.setBounds(markerPos.x, markerPos.y - topPanel.getHeight(), 16, 16);
            googleMap.add(marker);
            mapMarkers.add(marker);
            googleMap.repaint();
        }

        constructionMap.setBounds(0, 0,
                googleMap.getIcon().getIconWidth(),
                googleMap.getIcon().getIconHeight());

        repositionButton();

        constructionMap.fetchDataFromAPI(currentMapIcon.getIconWidth(), currentMapIcon.getIconHeight());
    }
}
