package com.freepass.controller;

import com.freepass.dto.ConstructionDTO;
import org.json.*;
import java.awt.Point;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ConstructionAPI {
    private static final String API_URL = "https://apis.data.go.kr/6260000/BusanCnstrWorkInfoService/getCnstrWorkInfo";
    private static final String SERVICE_KEY = "TUd952AU2cghyfuHQ9xLSMAsMcL%2BU1cGzAnEOHf%2FhEz5hAbA2UTz%2FCiGQCms5K6ytOh2xeBxaFY%2FDBAfZmplUw%3D%3D";
    private static final String GOOGLE_API_KEY = "AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";

    private final JSONArray itemArray;
    private int currentIndex = 0;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LocalDate today = LocalDate.now();
    private final GoogleAPI googleAPI;
    
    // 부산 지역의 GPS 경계 설정 (보다 정확한 필터링)
    private static final double BUSAN_MIN_LAT = 34.8;
    private static final double BUSAN_MAX_LAT = 35.4;
    private static final double BUSAN_MIN_LNG = 128.7;
    private static final double BUSAN_MAX_LNG = 129.3;

    public ConstructionAPI() throws IOException, JSONException {
        this.googleAPI = new GoogleAPI();
        
        StringBuilder urlBuilder = new StringBuilder(API_URL);
        urlBuilder.append("?serviceKey=").append(SERVICE_KEY);
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

        JSONObject root = new JSONObject(sb.toString());
        JSONObject items = root.getJSONObject("response")
                               .getJSONObject("body")
                               .getJSONObject("items");
        itemArray = items.getJSONArray("item");
    }

    public boolean hasNext() {
        return currentIndex < itemArray.length();
    }

    public ConstructionDTO getNext() {
        while (hasNext()) {
            JSONObject item = itemArray.getJSONObject(currentIndex++);
            ConstructionDTO c = parseConstruction(item);
            if (c != null) return c;
        }
        return null;
    }

    private ConstructionDTO parseConstruction(JSONObject item) {
        String name = item.optString("cnstrcNm", "공사명 없음");
        String contractor = item.optString("cnstrtr", "시공사 없음");
        String location = item.optString("cnstrcLc", "위치 없음");
        String startDateStr = item.optString("bgnde", "-");
        String endDateStr = item.optString("endde", "-");

        LocalDate startDate = null, endDate = null;
        try {
            if (!startDateStr.equals("-")) startDate = LocalDate.parse(startDateStr, formatter);
            if (!endDateStr.equals("-")) endDate = LocalDate.parse(endDateStr, formatter);
        } catch (DateTimeParseException ignored) {}

        boolean ongoing = false;
        if (startDate != null) {
            ongoing = endDate == null ? !today.isBefore(startDate)
                                      : !today.isBefore(startDate) && !today.isAfter(endDate);
        }
        if (!ongoing) return null;

        // 기본 위치 (지도 범위를 벗어나지 않도록)
        int x = 300; // 기본값
        int y = 300; // 기본값

        return new ConstructionDTO(name, contractor, location, startDateStr, endDateStr, x, y);
    }
    
    /**
     * 주소를 기반으로 정확한 픽셀 위치 계산 (개선된 버전)
     */
    private Point getAccuratePosition(String locationAddress, String mapCenter, int zoomLevel, int mapWidth, int mapHeight) {
        try {
            // 1단계: 주소를 GPS 좌표로 변환
            double[] gpsCoord = geocodeAddress(locationAddress);
            if (gpsCoord == null) {
                return null;
            }

            double lat = gpsCoord[0];
            double lng = gpsCoord[1];
            
            // 부산 지역 범위 확인
            if (!isInBusanArea(lat, lng)) {
                return null;
            }
            
            // 2단계: GPS 좌표를 지도 픽셀 좌표로 변환
            Point pixelPos = googleAPI.getPixelPositionInMap(
                mapCenter, lat, lng, zoomLevel, mapWidth, mapHeight
            );
            
            // 픽셀 위치가 지도 범위 내에 있는지 확인
            if (pixelPos != null && isValidPixelPosition(pixelPos, mapWidth, mapHeight)) {
                return pixelPos;
            } else {
                return null;
            }
            
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 부산 지역 범위 내에 있는지 확인
     */
    private boolean isInBusanArea(double lat, double lng) {
        return lat >= BUSAN_MIN_LAT && lat <= BUSAN_MAX_LAT &&
               lng >= BUSAN_MIN_LNG && lng <= BUSAN_MAX_LNG;
    }
    
    /**
     * 픽셀 위치가 유효한 범위 내에 있는지 확인
     */
    private boolean isValidPixelPosition(Point pixelPos, int mapWidth, int mapHeight) {
        return pixelPos.x >= 0 && pixelPos.x <= mapWidth &&
               pixelPos.y >= 0 && pixelPos.y <= mapHeight;
    }
    
    /**
     * 주소를 GPS 좌표로 변환 (개선된 버전)
     */
    private double[] geocodeAddress(String address) {
        try {
            // 주소 정제 및 개선
            String cleanAddress = cleanAddress(address);
            String fullAddress = "부산광역시 " + cleanAddress;
            
            String geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(fullAddress, "UTF-8")
                    + "&key=" + GOOGLE_API_KEY
                    + "&region=kr"  // 한국 지역 우선
                    + "&language=ko"; // 한국어 우선
            
            URL url = new URL(geocodeUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            
            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();
            
            JSONObject response = new JSONObject(sb.toString());
            
            // API 상태 확인
            String status = response.getString("status");
            if (!"OK".equals(status)) {
                return null;
            }
            
            JSONArray results = response.getJSONArray("results");

            if (results.length() > 0) {
                JSONObject location = results.getJSONObject(0)
                        .getJSONObject("geometry")
                        .getJSONObject("location");
                
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                
                return new double[]{lat, lng};
            }
        } catch (Exception e) {
            System.err.println("Geocoding 실패: " + address + " - " + e.getMessage());
        }
        return null;
    }
    
    /**
     * 주소 정제 (더 정확한 geocoding을 위해)
     */
    private String cleanAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }
        
        // 불필요한 문자 제거 및 정규화
        String cleaned = address.trim()
                .replaceAll("\\s+", " ")  // 다중 공백을 단일 공백으로
                .replaceAll("[(),\\[\\]]", "")  // 괄호 제거
                .replaceAll("일원", "")  // '일원' 제거
                .replaceAll("일대", "")  // '일대' 제거
                .replaceAll("부근", "")  // '부근' 제거
                .replaceAll("인근", "")  // '인근' 제거
                .replaceAll("구간", "")  // '구간' 제거
                .replaceAll("~.*", "")   // '~' 이후 제거
                .replaceAll("에서.*", "")  // '에서' 이후 제거
                .trim();
        
        // 부산광역시나 부산시가 이미 포함되어 있으면 제거
        cleaned = cleaned.replaceAll("^부산(광역시|시)\\s*", "");
        
        return cleaned;
    }
    
    /**
     * 현재 지도 중심점과 줌 레벨에 맞춰 위치 재계산
     */
    public Point recalculatePosition(String locationAddress, String mapCenter, int zoomLevel, int mapWidth, int mapHeight) {
        return getAccuratePosition(locationAddress, mapCenter, zoomLevel, mapWidth, mapHeight);
    }
    
    /**
     * 지도 설정에 맞춰 모든 공사 위치를 계산하여 반환
     */
    public java.util.List<ConstructionDTO> getAllConstructionsWithPositions(String mapCenter, int zoomLevel, int mapWidth, int mapHeight) {
        java.util.List<ConstructionDTO> constructions = new ArrayList<>();
        currentIndex = 0; // 인덱스 초기화
        
        while (hasNext()) {
            JSONObject item = itemArray.getJSONObject(currentIndex++);
            ConstructionDTO c = parseConstruction(item);
            if (c != null) {
                // 정확한 위치 계산
                Point accuratePos = getAccuratePosition(c.getLocation(), mapCenter, zoomLevel, mapWidth, mapHeight);
                if (accuratePos != null) {
                    c.setX(accuratePos.x);
                    c.setY(accuratePos.y);
                    constructions.add(c);
                } else {
                    // 위치를 찾을 수 없는 경우 로그 출력 후 스킵
                    //System.out.println("위치를 찾을 수 없어 스킵: " + c.getLocation());
                }
            }
        }
        
        return constructions;
    }
}