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
import java.util.concurrent.ConcurrentHashMap;

public class ConstructionAPI {

    private static final String API_URL = "https://apis.data.go.kr/6260000/BusanCnstrWorkInfoService/getCnstrWorkInfo";
    private static final String SERVICE_KEY = "TUd952AU2cghyfuHQ9xLSMAsMcL%2BU1cGzAnEOHf%2FhEz5hAbA2UTz%2FCiGQCms5K6ytOh2xeBxaFY%2FDBAfZmplUw%3D%3D";
    private static final String GOOGLE_API_KEY = "AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";

    private final List<ConstructionDTO> allConstructionsCache = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LocalDate today = LocalDate.now();
    private final GoogleAPI googleAPI;

    private final Map<String, double[]> addressCache = new ConcurrentHashMap<>();
    private final Map<String, List<ConstructionDTO>> positionCache = new ConcurrentHashMap<>();

    private static final double BUSAN_MIN_LAT = 34.8;
    private static final double BUSAN_MAX_LAT = 35.4;
    private static final double BUSAN_MIN_LNG = 128.7;
    private static final double BUSAN_MAX_LNG = 129.3;

    private boolean isInitialized = false;
    private String initializationError = null;

    public ConstructionAPI() {
        this.googleAPI = new GoogleAPI();
        initializeConstructionData();
    }

    private void initializeConstructionData() {
        try {
            loadConstructionDataFromAPI();
            isInitialized = true;
        } catch (Exception e) {
            initializationError = e.getMessage();
            e.printStackTrace();
        }
    }

    private void loadConstructionDataFromAPI() throws IOException, JSONException {
        StringBuilder urlBuilder = new StringBuilder(API_URL)
                .append("?serviceKey=").append(SERVICE_KEY)
                .append("&pageNo=1&numOfRows=4000&resultType=json");

        HttpURLConnection conn = null;
        BufferedReader rd = null;

        try {
            conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            int responseCode = conn.getResponseCode();
            if (responseCode < 200 || responseCode >= 300) {
                throw new IOException("API 호출 실패. 응답 코드: " + responseCode);
            }

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            processAPIResponse(sb.toString());

        } finally {
            if (rd != null) {
                try { rd.close(); } catch (IOException ignored) {}
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void processAPIResponse(String jsonResponse) throws JSONException {
        JSONObject responseObj = new JSONObject(jsonResponse);

        if (!responseObj.has("response")) {
            throw new JSONException("올바르지 않은 API 응답 형식");
        }

        JSONObject response = responseObj.getJSONObject("response");
        JSONObject body = response.getJSONObject("body");
        JSONObject items = body.getJSONObject("items");
        JSONArray itemArray = items.getJSONArray("item");

        for (int i = 0; i < itemArray.length(); i++) {
            try {
                JSONObject item = itemArray.getJSONObject(i);
                ConstructionDTO construction = parseConstruction(item);

                if (construction != null) {
                    double[] gps = geocodeAddressWithCache(construction.getLocation());
                    if (gps != null && isInBusanArea(gps[0], gps[1])) {
                        construction.setLatitude(gps[0]);
                        construction.setLongitude(gps[1]);
                        allConstructionsCache.add(construction);
                    }
                }
            } catch (Exception e) {
                System.err.println("공사 데이터 파싱 오류 (인덱스 " + i + "): " + e.getMessage());
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
            if (!startDateStr.equals("-") && !startDateStr.trim().isEmpty()) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (!endDateStr.equals("-") && !endDateStr.trim().isEmpty()) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException e) {
            System.err.println("날짜 파싱 오류: " + startDateStr + " ~ " + endDateStr);
        }

        boolean ongoing = isOngoingConstruction(startDate, endDate);

        if (!ongoing) {
            return null;
        }

        return new ConstructionDTO(name, contractor, location, startDateStr, endDateStr, 0, 0);
    }

    private boolean isOngoingConstruction(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            return false;
        }

        if (endDate == null) {
            return !today.isBefore(startDate);
        } else {
            return !today.isBefore(startDate) && !today.isAfter(endDate);
        }
    }

    private double[] geocodeAddressWithCache(String address) {
        String cleaned = cleanAddress(address);
        if (cleaned.isEmpty()) {
            return null;
        }

        if (addressCache.containsKey(cleaned)) {
            return addressCache.get(cleaned);
        }

        double[] coordinates = geocodeAddress(cleaned);
        if (coordinates != null) {
            addressCache.put(cleaned, coordinates);
        }

        return coordinates;
    }

    private double[] geocodeAddress(String address) {
        try {
            String fullAddress = "부산광역시 " + address;
            String geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(fullAddress, StandardCharsets.UTF_8.name())
                    + "&key=" + GOOGLE_API_KEY + "&region=kr&language=ko";

            HttpURLConnection conn = (HttpURLConnection) new URL(geocodeUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            InputStream is = (conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
                    ? conn.getInputStream() : conn.getErrorStream();

            Scanner sc = new Scanner(is, StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();
            is.close();

            JSONObject response = new JSONObject(sb.toString());
            if (!"OK".equals(response.getString("status"))) {
                return null;
            }

            JSONObject location = response.getJSONArray("results").getJSONObject(0)
                    .getJSONObject("geometry").getJSONObject("location");

            return new double[]{location.getDouble("lat"), location.getDouble("lng")};

        } catch (Exception e) {
            System.err.println("주소 변환 오류: " + address + " - " + e.getMessage());
            return null;
        }
    }

    private String cleanAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "";
        }

        return address.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[(),\\[\\]]", "")
                .replaceAll("일원|일대|부근|인근|구간|~.*|에서.*|외 \\d+개소", "")
                .replaceAll("^부산(광역시|시)\\s*", "")
                .trim();
    }

    private boolean isInBusanArea(double lat, double lng) {
        return lat >= BUSAN_MIN_LAT && lat <= BUSAN_MAX_LAT
                && lng >= BUSAN_MIN_LNG && lng <= BUSAN_MAX_LNG;
    }

    private boolean isValidPixelPosition(Point p, int mapWidth, int mapHeight) {
        int margin = 30;
        return p.x >= -margin && p.x <= mapWidth + margin
                && p.y >= -margin && p.y <= mapHeight + margin;
    }

    public List<ConstructionDTO> getAllConstructionsWithPositions(String mapCenter, int zoom, int width, int height) {
        if (!isInitialized) {
            System.err.println("ConstructionAPI가 초기화되지 않았습니다: " + initializationError);
            return new ArrayList<>();
        }

        String cacheKey = mapCenter + "_" + zoom + "_" + width + "_" + height;

        if (positionCache.containsKey(cacheKey)) {
            return new ArrayList<>(positionCache.get(cacheKey));
        }

        List<ConstructionDTO> visibleConstructions = new ArrayList<>();

        for (ConstructionDTO construction : allConstructionsCache) {
            Point pixelPosition = googleAPI.getPixelPositionInMap(
                    mapCenter, construction.getLatitude(), construction.getLongitude(),
                    zoom, width, height);

            if (pixelPosition != null && isValidPixelPosition(pixelPosition, width, height)) {
                ConstructionDTO visibleConstruction = new ConstructionDTO(construction);
                visibleConstruction.setX(pixelPosition.x);
                visibleConstruction.setY(pixelPosition.y);
                visibleConstructions.add(visibleConstruction);
            }
        }

        if (positionCache.size() < 50) {
            positionCache.put(cacheKey, new ArrayList<>(visibleConstructions));
        }

        return visibleConstructions;
    }

    public List<ConstructionDTO> getAllConstructions() {
        return new ArrayList<>(allConstructionsCache);
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public String getInitializationError() {
        return initializationError;
    }

    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalConstructions", allConstructionsCache.size());
        stats.put("addressCacheSize", addressCache.size());
        stats.put("positionCacheSize", positionCache.size());
        return stats;
    }

    public void clearPositionCache() {
        positionCache.clear();
    }

    public void refreshData() {
        allConstructionsCache.clear();
        addressCache.clear();
        positionCache.clear();
        isInitialized = false;
        initializationError = null;

        initializeConstructionData();
    }
}
