package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;

public class ConstructionMapViewer extends JFrame {
    private GoogleAPI googleAPI = new GoogleAPI();
    private String location;
    private JLabel mapLabel;
    private ConstructionMap constructionMap;
    private JButton toggleButton;

    public ConstructionMapViewer(String location) {
        this.location = location;

        // 구글 지도 이미지 다운로드 및 JLabel 생성
        googleAPI.downloadMap(location);
        mapLabel = new JLabel(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        // 공사 위치 표시용 투명 패널 생성 및 기본 숨김
        constructionMap = new ConstructionMap();
        constructionMap.setVisible(false);

        // 토글 버튼 생성
        toggleButton = new JButton("공사 위치 보기");
        toggleButton.addActionListener(e -> {
            boolean isVisible = constructionMap.isVisible();
            constructionMap.setVisible(!isVisible);
            toggleButton.setText(isVisible ? "공사 위치 보기" : "공사 위치 숨기기");
        });

        // 레이아웃
        setLayout(new BorderLayout());
        add(toggleButton, BorderLayout.NORTH);
        add(mapLabel, BorderLayout.CENTER);

        // mapLabel에 OverlayLayout 적용 후 constructionMap 추가
        mapLabel.setLayout(new OverlayLayout(mapLabel));
        mapLabel.add(constructionMap);

        // 창 설정
        setTitle("부산 공사 위치 및 소음 범위");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // 공사 정보 미리 불러오기 (보이기 전에 API 호출됨)
        constructionMap.fetchDataFromAPI();
    }
}
