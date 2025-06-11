package com.freepass.view;

import com.freepass.dto.ConstructionDTO;
import com.freepass.controller.ConstructionAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import org.json.JSONException;

public class ConstructionMap extends JPanel {
    private List<ConstructionDTO> constructions = new ArrayList<>(); // 직접 설정할 수 있도록 변경
    private ConstructionDTO lastHoveredConstruction = null;
    private JWindow popupWindow = null;

    private final ConstructionAPI constructionAPI;

    private String currentMapCenter = "부산시민공원";
    private int currentZoomLevel = 11;
    private int currentMapWidth = 612; // 초기값
    private int currentMapHeight = 612; // 초기값

    public ConstructionMap() {
        setOpaque(false); // 투명하게 설정하여 아래 지도 JLabel이 보이도록 함
        setPreferredSize(new Dimension(612, 612)); // 초기 선호 크기

        try {
            this.constructionAPI = new ConstructionAPI(); // API 초기화 시 모든 데이터 캐싱
        } catch (JSONException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "건설 API 데이터 파싱 중 오류가 발생했습니다: " + e.getMessage() + "\n일부 기능이 제한될 수 있습니다.",
                "API 데이터 오류", JOptionPane.ERROR_MESSAGE);
            throw new RuntimeException("Construction API 데이터 파싱 실패", e);
        }


        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hovering = false;
                for (ConstructionDTO c : constructions) {
                    // 마우스 좌표가 공사 마커 범위 내에 있는지 확인 (마커 크기 고려)
                    if (e.getX() >= c.getX() - 15 && e.getX() <= c.getX() + 15 &&
                        e.getY() >= c.getY() - 15 && e.getY() <= c.getY() + 15) {
                        if (lastHoveredConstruction != c) {
                            lastHoveredConstruction = c;
                            showPopup(e, c);
                        }
                        hovering = true;
                        break;
                    }
                }
                if (!hovering) {
                    lastHoveredConstruction = null;
                    hidePopup();
                }
            }
        });
    }

    public void updateMapParameters(String mapCenter, int zoomLevel, int mapWidth, int mapHeight) {
        this.currentMapCenter = mapCenter;
        this.currentZoomLevel = zoomLevel;
        this.currentMapWidth = mapWidth;
        this.currentMapHeight = mapHeight;
    }

    // Main에서 SwingWorker를 통해 데이터를 받아와 설정하는 메서드
    public void setConstructions(List<ConstructionDTO> newConstructions) {
        this.constructions = newConstructions; // 리스트 자체를 교체 (불변성 유지)
        hidePopup(); // 데이터 업데이트 시 팝업 닫기
        repaint(); // 새 데이터로 다시 그리기
    }

    // 이 메서드는 더 이상 API 호출을 직접 하지 않고, Main에서 호출될 때 캐시된 데이터를 사용
    public void fetchDataFromAPI() {
        if (constructionAPI == null) { // API 초기화 실패 시
            JOptionPane.showMessageDialog(this,
                "API 초기화에 실패하여 데이터를 불러올 수 없습니다. 프로그램을 다시 시작해 주세요.",
                "데이터 불러오기 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 실제 API 호출 및 필터링/계산은 SwingWorker를 통해 Main에서 처리됨
        // 이 메서드는 Main에서 setConstructions를 호출하기 위한 인터페이스 역할
        // 여기서는 캐시된 데이터를 바탕으로 현재 뷰포트에 맞는 공사 데이터를 가져옵니다.
        List<ConstructionDTO> validConstructions = constructionAPI.getAllConstructionsWithPositions(
            currentMapCenter, currentZoomLevel, currentMapWidth, currentMapHeight
        );
        setConstructions(validConstructions); // 바로 업데이트
    }

    // Main에서 ConstructionAPI 인스턴스를 가져갈 수 있도록 getter 추가
    public ConstructionAPI getConstructionAPI() {
        return this.constructionAPI;
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // JPanel의 기본 페인팅 (투명 설정)
        Graphics2D g2d = (Graphics2D) g.create(); // Graphics2D를 사용하여 더 나은 렌더링
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (ConstructionDTO c : constructions) {
            int x = c.getX();
            int y = c.getY();

            // 반투명 녹색 원 (공사 구역 표시)
            g2d.setColor(new Color(0, 255, 0, 70));
            g2d.fillOval(x - 30, y - 30, 60, 60);

            // 녹색 점 (마커 위치 표시)
            g2d.setColor(Color.GREEN);
            g2d.fillOval(x - 5, y - 5, 10, 10);

            // 공사 아이콘 (이모지)
            g2d.setColor(Color.BLACK);
            Font currentFont = g2d.getFont();
            g2d.setFont(currentFont.deriveFont(Font.BOLD, 20f)); // 폰트 크기 20f로 설정
            g2d.drawString("🚧", x - 10, y + 5); // 이모지 위치 조정
            g2d.setFont(currentFont); // 원래 폰트로 복원
        }
        g2d.dispose(); // Graphics2D 객체 해제
    }

    private void showPopup(MouseEvent e, ConstructionDTO c) {
        hidePopup();
        JLabel label = new JLabel("<html><b>" + c.getName() + "</b><br>시공사: " + c.getContractor() + "<br>위치: " + c.getLocation() + "<br>기간: " + c.getStartDate() + " ~ " + c.getEndDate() + "</html>");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setBackground(new Color(255, 255, 225));
        label.setOpaque(true);
        popupWindow = new JWindow(SwingUtilities.getWindowAncestor(this));
        popupWindow.getContentPane().add(label);
        popupWindow.pack();
        Point locationOnScreen = e.getLocationOnScreen();
        // 팝업 위치 조정: 마우스 커서 옆에 나타나도록
        popupWindow.setLocation(locationOnScreen.x + 15, locationOnScreen.y + 15);
        popupWindow.setVisible(true);
    }

    private void hidePopup() {
        if (popupWindow != null) {
            popupWindow.setVisible(false);
            popupWindow.dispose();
            popupWindow = null;
        }
    }
}