package com.mycompany.noiseradar;

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

public class GoogleAPI {
    public void downloadMap(String location, int zoom) {
        try {
            String imageURL = "https://maps.googleapis.com/maps/api/staticmap?center="
                    + URLEncoder.encode(location, "UTF-8")
                    + "&key=AIzaSyB62YTIt4eKHYlVrf9mjioCksFADR_9CQg"
                    + "&zoom=" + zoom 
                    + "&size=800x450"   // 16:9 비율 크기 설정
                    + "&scale=2";
            
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

        int newW = 800;             // 가로 고정 크기
        int newH = newW * 9 / 16;   // 세로를 16:9 비율로 고정
        
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
}
