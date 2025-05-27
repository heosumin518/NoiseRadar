package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.net.URL;

public class ConstructionMapViewer extends JFrame {
    private GoogleAPI googleAPI = new GoogleAPI();
    private String location;
    private JLabel mapLabel;
    private ConstructionMap constructionMap;
    private JButton coneButton;

    public ConstructionMapViewer(String location) {
        this.location = location;

        // 지도 이미지 다운로드 및 JLabel 생성
        googleAPI.downloadMap(location, 11);
        mapLabel = new JLabel(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        // 공사 위치 표시용 투명 패널 생성
        constructionMap = new ConstructionMap();
        constructionMap.setVisible(false);

        // 상대 경로에서 이미지 로드
        URL iconURL = getClass().getResource("/cone_button.png");
        ImageIcon coneIcon = new ImageIcon(iconURL);
        coneButton = new JButton(coneIcon);
        coneButton.setContentAreaFilled(false);
        coneButton.setBorderPainted(false);
        coneButton.setFocusPainted(false);
        coneButton.setSize(coneIcon.getIconWidth(), coneIcon.getIconHeight());

        // 버튼 클릭 시 공사 위치 표시/숨기기
        coneButton.addActionListener(e -> {
            boolean isVisible = constructionMap.isVisible();
            constructionMap.setVisible(!isVisible);
        });

        // mapLabel에 절대 위치 설정 (null 레이아웃)
        mapLabel.setLayout(null);
        mapLabel.add(constructionMap);
        mapLabel.add(coneButton);

        // constructionMap 크기 조정
        constructionMap.setBounds(0, 0, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);

        // 지도 크기 변경 시 버튼 위치 자동 조정
        mapLabel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButton();
                constructionMap.setBounds(0, 0, mapLabel.getWidth(), mapLabel.getHeight());
            }
        });

        // 초기 버튼 위치 설정
        SwingUtilities.invokeLater(this::repositionButton);

        // 레이아웃 및 프레임 구성
        setLayout(new BorderLayout());
        add(mapLabel, BorderLayout.CENTER);

        setTitle("부산 공사 위치 및 소음 범위");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 공사 정보 미리 불러오기
        constructionMap.fetchDataFromAPI();
    }

    private void repositionButton() {
        int margin = 10;
        int x = mapLabel.getWidth() - coneButton.getWidth() - margin;
        int y = mapLabel.getHeight() - coneButton.getHeight() - margin-50;
        coneButton.setLocation(x, y);
    }
}
