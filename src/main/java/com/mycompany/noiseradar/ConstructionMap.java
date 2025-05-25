package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ConstructionMap extends JPanel {
    private java.util.List<Construction> constructions = new ArrayList<>();

    // 공사 정보 객체
    private static class Construction {
        String name, contractor, location, startDate, endDate;
        int x, y;

        public Construction(String name, String contractor, String location, String startDate, String endDate, int x, int y) {
            this.name = name;
            this.contractor = contractor;
            this.location = location;
            this.startDate = startDate;
            this.endDate = endDate;
            this.x = x;
            this.y = y;
        }
    }

    // 팝업 관련 변수
    private Construction lastHoveredConstruction = null;
    private JWindow popupWindow = null;

    public ConstructionMap() {
        setOpaque(false);  // 배경 투명
        setPreferredSize(new Dimension(612, 612));

        // 마우스 호버 이벤트
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hovering = false;

                for (Construction c : constructions) {
                    if (e.getX() >= c.x - 10 && e.getX() <= c.x + 10 &&
                        e.getY() >= c.y - 10 && e.getY() <= c.y + 10) {

                        if (lastHoveredConstruction != c) {
                            lastHoveredConstruction = c;
                            showPopup(e, c);  // 팝업 표시
                        }
                        hovering = true;
                        break;
                    }
                }

                if (!hovering) {
                    lastHoveredConstruction = null;
                    hidePopup();  // 벗어나면 팝업 숨김
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Construction c : constructions) {
            // 반투명 소음 범위 원
            g.setColor(new Color(0, 255, 0, 60));
            g.fillOval(c.x - 30, c.y - 30, 60, 60);

            // 중심 마커
            g.setColor(Color.GREEN);
            g.fillOval(c.x - 5, c.y - 5, 10, 10);

            // 아이콘 (공사 이모지)
            g.setColor(Color.BLACK);
            g.drawString("🚧", c.x - 8, c.y - 10);
        }
    }

    // API 호출 및 데이터 파싱
    public void fetchDataFromAPI() {
        try {
            StringBuilder urlBuilder = new StringBuilder("https://apis.data.go.kr/6260000/BusanCnstrWorkInfoService/getCnstrWorkInfo");
            urlBuilder.append("?serviceKey=TUd952AU2cghyfuHQ9xLSMAsMcL%2BU1cGzAnEOHf%2FhEz5hAbA2UTz%2FCiGQCms5K6ytOh2xeBxaFY%2FDBAfZmplUw%3D%3D");
            urlBuilder.append("&pageNo=1&numOfRows=4000&resultType=json");

            URL url = new URL(urlBuilder.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader rd = new BufferedReader(new InputStreamReader(
                    conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300 ?
                            conn.getInputStream() : conn.getErrorStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) sb.append(line);
            rd.close();

            constructions.clear();
            Random rand = new Random();

            LocalDate today = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            JSONObject root = new JSONObject(sb.toString());
            JSONObject response = root.getJSONObject("response");
            JSONObject body = response.getJSONObject("body");
            JSONObject items = body.getJSONObject("items");
            JSONArray itemArray = items.getJSONArray("item");

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                addConstructionFromJson(item, rand, today, formatter);
            }

            repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "API 불러오기 오류: " + e.getMessage());
        }
    }

    private void addConstructionFromJson(JSONObject item, Random rand, LocalDate today, DateTimeFormatter formatter) {
        String name = item.optString("cnstrcNm", "공사명 없음");
        String contractor = item.optString("cnstrtr", "시공사 없음");
        String location = item.optString("cnstrcLc", "위치 없음");
        String startDateStr = item.optString("bgnde", "-");
        String endDateStr = item.optString("endde", "-");

        LocalDate startDate = null;
        LocalDate endDate = null;
        try {
            if (!startDateStr.equals("-")) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (!endDateStr.equals("-")) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            // 날짜 오류 무시
        }

        boolean ongoing = false;
        if (startDate != null) {
            if (endDate != null) {
                ongoing = !today.isBefore(startDate) && !today.isAfter(endDate);
            } else {
                ongoing = !today.isBefore(startDate);
            }
        }

        if (!ongoing) return;

        int x = rand.nextInt(700) + 50;
        int y = rand.nextInt(500) + 50;

        try {
            if (item.has("markerX")) x = Integer.parseInt(item.get("markerX").toString());
            else if (item.has("marker_x")) x = Integer.parseInt(item.get("marker_x").toString());

            if (item.has("markerY")) y = Integer.parseInt(item.get("markerY").toString());
            else if (item.has("marker_y")) y = Integer.parseInt(item.get("marker_y").toString());
        } catch (NumberFormatException ignored) {}

        constructions.add(new Construction(name, contractor, location, startDateStr, endDateStr, x, y));
    }

    private void showPopup(MouseEvent e, Construction c) {
        hidePopup();

        JLabel label = new JLabel("<html><b>" + c.name + "</b><br>" +
                "시공사: " + c.contractor + "<br>" +
                "위치: " + c.location + "<br>" +
                "기간: " + c.startDate + " ~ " + c.endDate + "</html>");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setBackground(new Color(255, 255, 225));
        label.setOpaque(true);

        popupWindow = new JWindow(SwingUtilities.getWindowAncestor(this));
        popupWindow.getContentPane().add(label);
        popupWindow.pack();

        Point locationOnScreen = e.getLocationOnScreen();
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
