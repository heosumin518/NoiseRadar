package com.freepass.main;

import com.mycompany.noiseradar.Main;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public static final int W_FRAME = 800;
    public static final int H_FRAME = 600;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardPanel = new JPanel(cardLayout);
    private final MainScreen mainScreen;
    private final Main mapPanel;
    
    public MainFrame() {
        super("Noise Radar");
        setSize(W_FRAME, H_FRAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        mainScreen = new MainScreen(this);   // this 전달
        mapPanel = new Main();
        
        cardPanel.add(mainScreen, "MAIN_SCREEN");
        cardPanel.add(mapPanel, "MAP_SCREEN");
        
        add(cardPanel);
        setVisible(true);
    }
    
    public void showMapScreen(String address) {
        mapPanel.setMap(address);         // 지도 갱신
        cardLayout.show(cardPanel, "MAP_SCREEN");  // 화면 전환
        
        // 화면 전환 후 지도 패널에 포커스 설정
        SwingUtilities.invokeLater(() -> {
            mapPanel.onScreenShown();
        });
    }
}