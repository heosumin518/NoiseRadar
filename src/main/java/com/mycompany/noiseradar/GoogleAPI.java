package com.mycompany.noiseradar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLEncoder;
import javax.swing.ImageIcon;

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
}
