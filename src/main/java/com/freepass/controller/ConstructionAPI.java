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
import java.util.stream.Collectors; // Collectors 추가

public class ConstructionAPI {
    private static final String API_URL = "https://apis.data.go.kr/6260000/BusanCnstrWorkInfoService/getCnstrWorkInfo";
    private static final String SERVICE_KEY = "TUd952AU2cghyfuHQ9xLSMAsMcL%2BU1cGzAnEOHf%2FhEz5hAbA2UTz%2FCiGQCms5K6ytOh2xeBxaFY%2FDBAfZmplUw%3D%3D";
    private static final String GOOGLE_API_KEY = "AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";

    // 모든 공사 데이터를 저장할 캐시 (초기 로드 시 한 번만 지오코딩하여 저장)
    private final List<ConstructionDTO> allConstructionsCache = new ArrayList<>();
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
        urlBuilder.append("&pageNo=1&numOfRows=4000&resultType=json"); // 한 번에 모든 데이터를 가져오도록 numOfRows 조정

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(
            conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300 ?
                conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8.name())); // 인코딩 지정

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) sb.append(line);
        rd.close();

        JSONObject root = new JSONObject(sb.toString());
        JSONObject items = root.getJSONObject("response")
                               .getJSONObject("body")
                               .getJSONObject("items");
        JSONArray itemArray = items.getJSONArray("item");

        // 초기 로드 시 모든 유효한 공사 데이터를 파싱하고 GPS 좌표를 가져와 캐시에 저장
        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject item = itemArray.getJSONObject(i);
            ConstructionDTO c = parseConstruction(item); // 이 단계에서 x, y는 0으로 초기화됨
            if (c != null) {
                double[] gpsCoord = geocodeAddress(c.getLocation()); // 여기서 주소 -> GPS 변환
                if (gpsCoord != null && isInBusanArea(gpsCoord[0], gpsCoord[1])) { // 부산 지역 내에 있는 경우만
                    c.setLatitude(gpsCoord[0]);
                    c.setLongitude(gpsCoord[1]);
                    allConstructionsCache.add(c);
                } else {
                    // System.out.println("Skipped (no GPS or outside Busan): " + c.getLocation());
                }
            }
        }
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

        // X, Y 좌표는 나중에 계산할 것이므로 0으로 초기화
        return new ConstructionDTO(name, contractor, location, startDateStr, endDateStr, 0, 0);
    }

    /**
     * 주소를 GPS 좌표로 변환 (개선된 버전)
     */
    private double[] geocodeAddress(String address) {
        try {
            String cleanAddress = cleanAddress(address);
            if (cleanAddress.isEmpty()) return null;

            String fullAddress = "부산광역시 " + cleanAddress;

            String geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.name())
                    + "&key=" + GOOGLE_API_KEY
                    + "&region=kr"  // 한국 지역 우선
                    + "&language=ko"; // 한국어 우선

            URL url = new URL(geocodeUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            // HTTP_OK (200)이 아니면 에러 스트림을 읽어야 함
            InputStream is;
            if (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300) {
                is = conn.getInputStream();
            } else {
                is = conn.getErrorStream();
            }

            Scanner sc = new Scanner(is, StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();
            is.close(); // InputStream 닫기

            JSONObject response = new JSONObject(sb.toString());

            String status = response.getString("status");
            if (!"OK".equals(status)) {
                // System.err.println("Geocoding API status: " + status + " for address: " + address);
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
            // System.err.println("Geocoding 실패: " + address + " - " + e.getMessage());
        }
        return null;
    }

    /**
     * 부산 지역 범위 내에 있는지 확인
     */
    private boolean isInBusanArea(double lat, double lng) {
        return lat >= BUSAN_MIN_LAT && lat <= BUSAN_MAX_LAT &&
               lng >= BUSAN_MIN_LNG && lng <= BUSAN_MAX_LNG;
    }

    /**
     * 픽셀 위치가 유효한 범위 내에 있는지 확인 (약간의 여유를 두어 마커가 잘리지 않도록)
     */
    private boolean isValidPixelPosition(Point pixelPos, int mapWidth, int mapHeight) {
        // 마커 크기를 고려하여 화면 경계 안쪽에 있는지 확인 (예: 마커 반지름 15px)
        int margin = 30; // 마커 크기에 따라 조정
        return pixelPos.x >= -margin && pixelPos.x <= mapWidth + margin &&
               pixelPos.y >= -margin && pixelPos.y <= mapHeight + margin;
    }

    /**
     * 주소 정제 (더 정확한 geocoding을 위해)
     */
    private String cleanAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        String cleaned = address.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[(),\\[\\]]", "")
                .replaceAll("일원", "")
                .replaceAll("일대", "")
                .replaceAll("부근", "")
                .replaceAll("인근", "")
                .replaceAll("구간", "")
                .replaceAll("~.*", "")
                .replaceAll("에서.*", "")
                .replaceAll("외 [0-9]+개소", "") // "외 00개소" 제거
                .trim();

        cleaned = cleaned.replaceAll("^부산(광역시|시)\\s*", "");

        return cleaned;
    }

    /**
     * 지도 설정에 맞춰 모든 공사 위치를 계산하여 반환
     * (캐시된 GPS 좌표를 사용하여 픽셀 좌표만 재계산)
     */
    public List<ConstructionDTO> getAllConstructionsWithPositions(String mapCenter, int zoomLevel, int mapWidth, int mapHeight) {
        List<ConstructionDTO> visibleConstructions = new ArrayList<>();

        // 모든 캐시된 공사 데이터를 순회하며 현재 지도 범위 내에 있는지 확인하고 픽셀 좌표 계산
        for (ConstructionDTO c : allConstructionsCache) {
            // 캐시된 GPS 좌표를 사용하여 픽셀 위치 재계산
            // GoogleAPI.getPixelPositionInMap은 지도 중심의 GPS 좌표를 내부적으로 가져오므로 여기서는 전달 불필요
            Point accuratePos = googleAPI.getPixelPositionInMap(
                mapCenter, c.getLatitude(), c.getLongitude(), zoomLevel, mapWidth, mapHeight
            );

            if (accuratePos != null && isValidPixelPosition(accuratePos, mapWidth, mapHeight)) {
                // 화면 내에 보이는 공사만 리스트에 추가 (원본 캐시를 건드리지 않도록 복사)
                ConstructionDTO copy = new ConstructionDTO(c);
                copy.setX(accuratePos.x);
                copy.setY(accuratePos.y);
                visibleConstructions.add(copy);
            }
        }
        return visibleConstructions;
    }
}