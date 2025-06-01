package com.freepass.controller;

import com.freepass.dto.ConstructionDTO;
import org.json.*;
import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class ConstructionAPI {
    private static final String API_URL = "https://apis.data.go.kr/6260000/BusanCnstrWorkInfoService/getCnstrWorkInfo";
    private static final String SERVICE_KEY = "TUd952AU2cghyfuHQ9xLSMAsMcL%2BU1cGzAnEOHf%2FhEz5hAbA2UTz%2FCiGQCms5K6ytOh2xeBxaFY%2FDBAfZmplUw%3D%3D";

    private final JSONArray itemArray;
    private int currentIndex = 0;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final LocalDate today = LocalDate.now();

    public ConstructionAPI() throws IOException, JSONException {
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

        int x = new Random().nextInt(700) + 50;
        int y = new Random().nextInt(500) + 50;
        try {
            if (item.has("markerX")) x = Integer.parseInt(item.get("markerX").toString());
            else if (item.has("marker_x")) x = Integer.parseInt(item.get("marker_x").toString());
            if (item.has("markerY")) y = Integer.parseInt(item.get("markerY").toString());
            else if (item.has("marker_y")) y = Integer.parseInt(item.get("marker_y").toString());
        } catch (NumberFormatException ignored) {}

        return new ConstructionDTO(name, contractor, location, startDateStr, endDateStr, x, y);
    }
}
