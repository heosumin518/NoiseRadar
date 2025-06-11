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

    private final List<ConstructionDTO> allConstructionsCache = new ArrayList<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LocalDate today = LocalDate.now();
    private final GoogleAPI googleAPI;
    private final Map<String, double[]> addressCache = new HashMap<>();

    private static final double BUSAN_MIN_LAT = 34.8;
    private static final double BUSAN_MAX_LAT = 35.4;
    private static final double BUSAN_MIN_LNG = 128.7;
    private static final double BUSAN_MAX_LNG = 129.3;

    public ConstructionAPI() throws IOException, JSONException {
        this.googleAPI = new GoogleAPI();

        StringBuilder urlBuilder = new StringBuilder(API_URL)
                .append("?serviceKey=").append(SERVICE_KEY)
                .append("&pageNo=1&numOfRows=4000&resultType=json");

        HttpURLConnection conn = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
        conn.setRequestMethod("GET");

        BufferedReader rd = new BufferedReader(new InputStreamReader(
                conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300
                ? conn.getInputStream() : conn.getErrorStream(), StandardCharsets.UTF_8));

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();

        JSONArray itemArray = new JSONObject(sb.toString())
                .getJSONObject("response")
                .getJSONObject("body")
                .getJSONObject("items")
                .getJSONArray("item");

        for (int i = 0; i < itemArray.length(); i++) {
            JSONObject item = itemArray.getJSONObject(i);
            ConstructionDTO c = parseConstruction(item);
            if (c != null) {
                double[] gps = geocodeAddress(c.getLocation());
                if (gps != null && isInBusanArea(gps[0], gps[1])) {
                    c.setLatitude(gps[0]);
                    c.setLongitude(gps[1]);
                    allConstructionsCache.add(c);
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
            if (!startDateStr.equals("-")) {
                startDate = LocalDate.parse(startDateStr, formatter);
            }
            if (!endDateStr.equals("-")) {
                endDate = LocalDate.parse(endDateStr, formatter);
            }
        } catch (DateTimeParseException ignored) {
        }

        boolean ongoing = false;
        if (startDate != null) {
            ongoing = endDate == null ? !today.isBefore(startDate)
                    : !today.isBefore(startDate) && !today.isAfter(endDate);
        }

        if (!ongoing) {
            return null;
        }
        return new ConstructionDTO(name, contractor, location, startDateStr, endDateStr, 0, 0);
    }

    private double[] geocodeAddress(String address) {
    try {
        String cleaned = cleanAddress(address);
        if (cleaned.isEmpty()) return null;

        if (addressCache.containsKey(cleaned)) {
            return addressCache.get(cleaned);  // 캐시 사용
        }

        String fullAddress = "부산광역시 " + cleaned;
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
        while (sc.hasNext()) sb.append(sc.nextLine());
        sc.close();
        is.close();

        JSONObject response = new JSONObject(sb.toString());
        if (!"OK".equals(response.getString("status"))) return null;

        JSONObject location = response.getJSONArray("results").getJSONObject(0)
                                      .getJSONObject("geometry").getJSONObject("location");

        double[] latlng = new double[]{location.getDouble("lat"), location.getDouble("lng")};
        addressCache.put(cleaned, latlng);  // 캐시에 저장
        return latlng;
    } catch (Exception e) {
        return null;
    }
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

    public List<ConstructionDTO> getAllConstructionsWithPositions(String mapCenter, int zoom, int width, int height) {
        List<ConstructionDTO> visible = new ArrayList<>();

        for (ConstructionDTO c : allConstructionsCache) {
            Point p = googleAPI.getPixelPositionInMap(mapCenter, c.getLatitude(), c.getLongitude(), zoom, width, height);
            if (p != null && isValidPixelPosition(p, width, height)) {
                ConstructionDTO copy = new ConstructionDTO(c);
                copy.setX(p.x);
                copy.setY(p.y);
                visible.add(copy);
            }
        }
        return visible;
    }
}
