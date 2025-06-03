package com.freepass.view; 

import com.freepass.view.ConstructionMap; 
import com.mycompany.noiseradar.GoogleAPI; 
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
        googleMap.setLayout(null); // 오버레이를 위한 null 레이아웃 

        // 공사정보 맵 설정 (초기에는 숨김) 
        constructionMap.setVisible(false); 
        googleMap.add(constructionMap); // constructionMap을 googleMap에 추가 (오버레이로) 

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
                // GoogleMap 아이콘이 null이 아닐 때만 작업 수행 (NullPointerException 방지) 
                ImageIcon currentMapIcon = (ImageIcon) googleMap.getIcon(); 
                if (currentMapIcon != null) { 
                    constructionMap.setBounds(0, 0, currentMapIcon.getIconWidth(), currentMapIcon.getIconHeight()); 
                    constructionMap.setMapImage(currentMapIcon.getImage()); // 지도 이미지 업데이트 
                    // 공사 지도가 보일 때만 데이터 다시 가져오기 
                    if (constructionMap.isVisible()) { 
                        constructionMap.fetchDataFromAPI(currentMapIcon.getIconWidth(), currentMapIcon.getIconHeight()); 
                    } 
                }
                // 맵 크기 변경 시에도 현재 텍스트로 맵을 다시 로드하여 줌 레벨을 적용합니다.
                // 이 부분을 추가하여 줌 변경 시 맵이 리로드 되도록 합니다.
                if (!textField.getText().isEmpty()) {
                    setMap(textField.getText());
                } else {
                    setMap("부산시민공원"); // 초기 로딩 시 맵 크기 변경이 일어나면 초기 지도 다시 설정
                }
            } 
        }); 

        // 줌 기능 처리 
        googleMap.addMouseWheelListener(e -> { 
            int notches = e.getWheelRotation(); 
            zoomLevel = Math.max(1, Math.min(20, zoomLevel - notches)); 
            if (!textField.getText().isEmpty()) { 
                setMap(textField.getText()); // 줌 레벨 변경 시 지도 다시 로드
            } else {
                setMap("부산시민공원"); // 초기 지도 상태에서 줌 변경 시
            }
        }); 

        // 패널 정리 및 기본 설정 
        add(googleMap, BorderLayout.CENTER); 
        SwingUtilities.invokeLater(this::repositionButton); 
        setMap("부산시민공원"); // 초기 지도 설정 (이때 마커도 부산시민공원에 표시될 것) 
    } 

    private void performSearch() { 
        setMap(textField.getText()); 
        googleMap.setFocusable(true); 
        googleMap.requestFocusInWindow(); 
    } 

    private void toggleConstructionMap() { 
        constructionMap.setVisible(!constructionMap.isVisible()); 
        if (constructionMap.isVisible()) { 
            // 지도가 표시될 때, 데이터를 가져오고 현재 지도 이미지 위에 그려지도록 합니다. 
            ImageIcon currentMapIcon = (ImageIcon) googleMap.getIcon(); 
            if (currentMapIcon != null) { // NullPointerException 방지 
                constructionMap.setMapImage(currentMapIcon.getImage()); 
                constructionMap.fetchDataFromAPI(currentMapIcon.getIconWidth(), currentMapIcon.getIconHeight()); 
            } 
        } 
    } 

    private void repositionButton() { 
        int margin = 10; 
        int x = googleMap.getWidth() - coneButton.getWidth() - margin; 
        int y = googleMap.getHeight() - coneButton.getHeight() - margin - 20; 
        coneButton.setLocation(x, y); 
    } 

    public void setMap(String location) { 
        // 맵 로딩 시 현재 googleMap의 너비와 높이를 가져와서 전달
        int currentWidth = googleMap.getWidth();
        int currentHeight = googleMap.getHeight();

        // 최소 크기 보장 (컴포넌트 크기가 0일 때 발생할 수 있는 문제 방지)
        if (currentWidth <= 0) currentWidth = 800; // 기본값 설정
        if (currentHeight <= 0) currentHeight = 450; // 기본값 설정

        // **수정된 부분: downloadMap에 width와 height 인자 추가**
        googleAPI.downloadMap(location, zoomLevel, currentWidth, currentHeight);
        
        // **수정된 부분: getMap에 width와 height 인자 추가**
        ImageIcon mapIcon = googleAPI.getMap(location, currentWidth, currentHeight);
        googleMap.setIcon(mapIcon);
        
        // **수정된 부분: fileDelete에 width와 height 인자 추가**
        googleAPI.fileDelete(location, currentWidth, currentHeight);

        // constructionMap의 경계를 googleMap 아이콘 크기와 일치하도록 설정 
        if (mapIcon != null) { 
            constructionMap.setBounds(0, 0, mapIcon.getIconWidth(), mapIcon.getIconHeight()); 
            constructionMap.setMapImage(mapIcon.getImage()); 
            if (constructionMap.isVisible()) { 
                constructionMap.fetchDataFromAPI(mapIcon.getIconWidth(), mapIcon.getIconHeight()); 
            } 
        } 

        // 이전 마커 제거 
        for (JLabel marker : mapMarkers) { 
            googleMap.remove(marker); 
        } 
        mapMarkers.clear(); 

        // 검색된 주소 위치에 마커를 추가합니다. 
        if (mapIcon != null) { 
            // GoogleAPI에 'getGeoCoding(String address)' 메서드가 있다고 가정합니다.
            // Main.java에서는 이전에 getGeoCoding 호출을 제거했지만,
            // 줌 기능이 작동하려면 검색된 주소의 GPS 좌표를 사용해야 합니다.
            // 따라서 getGeoCoding 호출을 다시 추가합니다.
            Point locationGPS = googleAPI.getGeoCoding(location); // 다시 추가!

            if (locationGPS != null) {
                double lat = locationGPS.getY() / 1000000.0; 
                double lng = locationGPS.getX() / 1000000.0; 

                // GPS 좌표를 현재 지도 이미지 내의 픽셀 좌표로 변환합니다. 
                // **수정된 부분: getPixelPositionInMap에 mapIcon의 실제 너비/높이 전달**
                Point markerPos = googleAPI.getPixelPositionInMap(
                        location, lat, lng, zoomLevel,
                        mapIcon.getIconWidth(), mapIcon.getIconHeight() 
                ); 

                if (markerPos != null) { 
                    JLabel marker = new JLabel("📍"); 
                    marker.setOpaque(false); 
                    marker.setBackground(new Color(0, 0, 0, 0)); 
                    marker.setBorder(null); 

                    marker.setBounds(markerPos.x - 8, markerPos.y - 16, 16, 16); 
                    googleMap.add(marker); 
                    mapMarkers.add(marker); 
                } else { 
                    System.out.println("Warning: Could not get pixel position for " + location); 
                } 
            } else {
                System.out.println("Warning: Could not get GPS coordinates for " + location + ". Marker not added.");
            }
        } 

        googleMap.revalidate(); 
        googleMap.repaint(); 
        repositionButton(); 
    } 
}