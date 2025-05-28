package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main extends JFrame {
    private JTextField textField = new JTextField(30);
    private JPanel panel = new JPanel();
    private JButton button = new JButton("search");

    private GoogleAPI googleAPI = new GoogleAPI();
    private JLabel googleMap = new JLabel();

    private int zoomLevel = 11;

    private ConstructionMap constructionMap = new ConstructionMap();
    private JButton coneButton;

    public Main() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setTitle("Google Maps");

        panel.add(textField);
        panel.add(button);
        button.addMouseListener(new Event());

        add(BorderLayout.NORTH, panel);

        // googleMap null layout, 버튼과 constructionMap 추가
        googleMap.setLayout(null);

        constructionMap.setVisible(false);
        constructionMap.setBounds(0, 0, 612, 612);

        // 🟡 로컬 상대 경로로 이미지 불러오기
        ImageIcon coneIcon = new ImageIcon("src/main/java/com/mycompany/noiseradar/cone_button.png");
        coneButton = new JButton(coneIcon);
        coneButton.setContentAreaFilled(false);
        coneButton.setBorderPainted(false);
        coneButton.setFocusPainted(false);
        coneButton.setSize(coneIcon.getIconWidth(), coneIcon.getIconHeight());

        coneButton.addActionListener(e -> constructionMap.setVisible(!constructionMap.isVisible()));

        googleMap.add(constructionMap);
        googleMap.add(coneButton);

        googleMap.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionButton();
                constructionMap.setBounds(0, 0, googleMap.getWidth(), googleMap.getHeight());
            }
        });

        SwingUtilities.invokeLater(this::repositionButton);

        add(BorderLayout.SOUTH, googleMap);

        // 줌 이벤트 유지
        googleMap.addMouseWheelListener(e -> {
            int notches = e.getWheelRotation();
            if (notches < 0) {
                zoomLevel = Math.min(zoomLevel + 1, 20);
            } else {
                zoomLevel = Math.max(zoomLevel - 1, 1);
            }

            if (!textField.getText().isEmpty()) {
                setMap(textField.getText());
            }
        });

        pack();

        // 초기 지도는 부산으로 띄우기
        setMap("Busan");

        setVisible(true);
    }

    private void repositionButton() {
        int margin = 10;
        int x = googleMap.getWidth() - coneButton.getWidth() - margin;
        int y = googleMap.getHeight() - coneButton.getHeight() - margin - 50;
        coneButton.setLocation(x, y);
    }

    public void setMap(String location) {
        googleAPI.downloadMap(location, zoomLevel);
        googleMap.setIcon(googleAPI.getMap(location));
        googleAPI.fileDelete(location);

        constructionMap.setBounds(0, 0, googleMap.getIcon().getIconWidth(), googleMap.getIcon().getIconHeight());

        repositionButton();
        constructionMap.fetchDataFromAPI();

        pack();
    }

    public class Event implements MouseListener {
        @Override
        public void mouseClicked(MouseEvent e) {
            setMap(textField.getText());
            googleMap.setFocusable(true);
            googleMap.requestFocusInWindow();
        }
        @Override public void mousePressed(MouseEvent e) {}
        @Override public void mouseReleased(MouseEvent e) {}
        @Override public void mouseEntered(MouseEvent e) {}
        @Override public void mouseExited(MouseEvent e) {}
    }
}
