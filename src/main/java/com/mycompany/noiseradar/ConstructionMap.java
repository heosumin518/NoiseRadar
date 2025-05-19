package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

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

    public ConstructionMap() {
        setOpaque(false);  // 투명 배경으로 만들어서 밑에 지도가 보이게
        setPreferredSize(new Dimension(612, 612));

        // 마커 클릭 시 공사 정보 팝업
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (Construction c : constructions) {
                    if (e.getX() >= c.x - 10 && e.getX() <= c.x + 10 &&
                        e.getY() >= c.y - 10 && e.getY() <= c.y + 10) {
                        String text = "<html><b>" + c.name + "</b><br>" +
                                "시공사: " + c.contractor + "<br>" +
                                "위치: " + c.location + "<br>" +
                                "기간: " + c.startDate + " ~ " + c.endDate + "</html>";
                        JOptionPane.showMessageDialog(null, text, "공사 정보", JOptionPane.INFORMATION_MESSAGE);
                        break;
                    }
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        for (Construction c : constructions) {
            // 반투명 소음 범위 원 그리기 (반경 30픽셀)
            g.setColor(new Color(0, 255, 0, 60));
            g.fillOval(c.x - 30, c.y - 30, 60, 60);

            // 중심 마커 (작은 초록 원)
            g.setColor(Color.GREEN);
            g.fillOval(c.x - 5, c.y - 5, 10, 10);

            // 아이콘(공사 이모지)
            g.setColor(Color.BLACK);
            g.drawString("🚧", c.x - 8, c.y - 10);
        }
    }

    // API 호출 및 데이터 파싱 (public으로 변경)
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

            JSONObject root = new JSONObject(sb.toString());
            JSONObject response = root.getJSONObject("response");
            JSONObject body = response.getJSONObject("body");
            JSONObject items = body.getJSONObject("items");
            JSONArray itemArray = items.getJSONArray("item");

            for (int i = 0; i < itemArray.length(); i++) {
                JSONObject item = itemArray.getJSONObject(i);
                addConstructionFromJson(item, rand);
            }

            repaint();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "API 불러오기 오류: " + e.getMessage());
        }
    }

    private void addConstructionFromJson(JSONObject item, Random rand) {
        String name = item.optString("cnstrcNm", "공사명 없음");
        String contractor = item.optString("cnstrtr", "시공사 없음");
        String location = item.optString("cnstrcLc", "위치 없음");
        String startDate = item.optString("bgnde", "-");
        String endDate = item.optString("endde", "-");

        int x = rand.nextInt(700) + 50;
        int y = rand.nextInt(500) + 50;

        try {
            if (item.has("markerX")) {
                x = Integer.parseInt(item.get("markerX").toString());
            } else if (item.has("marker_x")) {
                x = Integer.parseInt(item.get("marker_x").toString());
            }
            if (item.has("markerY")) {
                y = Integer.parseInt(item.get("markerY").toString());
            } else if (item.has("marker_y")) {
                y = Integer.parseInt(item.get("marker_y").toString());
            }
        } catch (NumberFormatException ignored) {
        }

        constructions.add(new Construction(name, contractor, location, startDate, endDate, x, y));
    }
}
