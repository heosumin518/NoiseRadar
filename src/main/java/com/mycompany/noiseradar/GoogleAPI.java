/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noiseradar;

import java.awt.Point;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.swing.ImageIcon;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *      
 * @author heosumin518
 */
public class GoogleAPI {
    public void downloadMap(String location, int zoom) {
        try {
            String imageURL = "https://maps.googleapis.com/maps/api/staticmap?center="
                    + URLEncoder.encode(location, "UTF-8")
                    + "&key=AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg"
                    + "&zoom=" + zoom + "&size=800x450&scale=2";
            
            URL url = new URL(imageURL);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(location);
            
            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }
            is.close();
            os.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    public void downloadMap(double lat, double lng, int zoom) {
        try {
            String marker = "color:red|label:P|" + lng + "," + lat;

            String imageURL = "https://maps.googleapis.com/maps/api/staticmap"
                    + "?center=" + lat + "," + lng
                    + "&zoom=" + zoom
                    + "&size=800x450&scale=2"
                    + "&markers=" + URLEncoder.encode(marker, "UTF-8")
                    + "&key=AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";

            System.out.println("STATIC MAP URL: " + imageURL);
            
            URL url = new URL(imageURL);
            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream("temp_map.png");

            byte[] b = new byte[2048];
            int length;
            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public ImageIcon getMap(String location) {
        ImageIcon icon = new ImageIcon(location);
        int w = icon.getIconWidth();
        int h = icon.getIconHeight();
        
        int newW = 800;
        int newH = (int) ((double) h / w * newW);
        
        return new ImageIcon(icon.getImage().getScaledInstance(newW, newH, java.awt.Image.SCALE_SMOOTH));
    }
    
    public void fileDelete(String fileName) {
        File f = new File(fileName);
        f.delete();
    }
    
    // 위도, 경도를 주소로 변환
    public String reverseGeocode(double lat, double lng) {
        try {
            String urlStr = "https://maps.googleapis.com/maps/api/geocode/json?latlng="
                    + lat + "," + lng
                    + "&key=AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            
            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();
            
            JSONObject response = new JSONObject(sb.toString());
            JSONArray results = response.getJSONArray("results");
            if (results.length() > 0) {
                return results.getJSONObject(0).getString("formatted_address");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Point getPixelPositionInMap(String mapAddress, double targetLat, double targetLng, int zoom, int width, int height) {
        try {
            String geocodeUrl = "https://maps.googleapis.com/maps/api/geocode/json?address="
                    + URLEncoder.encode(mapAddress, "UTF-8")
                    + "&key=AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg";

            URL url = new URL(geocodeUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            Scanner sc = new Scanner(conn.getInputStream(), StandardCharsets.UTF_8.name());
            StringBuilder sb = new StringBuilder();
            while (sc.hasNext()) {
                sb.append(sc.nextLine());
            }
            sc.close();

            JSONObject obj = new JSONObject(sb.toString());
            JSONObject location = obj.getJSONArray("results")
                    .getJSONObject(0)
                    .getJSONObject("geometry")
                    .getJSONObject("location");

            double centerLat = location.getDouble("lat");
            double centerLng = location.getDouble("lng");

            // Mercator projection 기반 픽셀 계산
            double scale = Math.pow(2, zoom);
            double tileSize = 256;
            double worldSize = tileSize * scale;

            // 대상 좌표
            double x = (targetLng + 180.0) / 360.0 * worldSize;
            double siny = Math.sin(Math.toRadians(targetLat));
            siny = Math.min(Math.max(siny, -0.9999), 0.9999);
            double y = (0.5 - Math.log((1 + siny) / (1 - siny)) / (4 * Math.PI)) * worldSize;

            // 중심 좌표
            double centerX = (centerLng + 180.0) / 360.0 * worldSize;
            double centerSiny = Math.sin(Math.toRadians(centerLat));
            centerSiny = Math.min(Math.max(centerSiny, -0.9999), 0.9999);
            double centerY = (0.5 - Math.log((1 + centerSiny) / (1 - centerSiny)) / (4 * Math.PI)) * worldSize;

            // 상대 좌표 계산
            int pixelX = (int) Math.round(width / 2 + (x - centerX));
            int pixelY = (int) Math.round(height / 2 + (y - centerY));

            return new Point(pixelX, pixelY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
