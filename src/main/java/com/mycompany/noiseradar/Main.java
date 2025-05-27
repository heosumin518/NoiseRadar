package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

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
        setVisible(true);

        panel.add(textField);
        panel.add(button);
        button.addMouseListener(new Event());

        add(BorderLayout.NORTH, panel);

        // googleMap null layout, 버튼과 constructionMap 추가
        googleMap.setLayout(null);

        constructionMap.setVisible(false);
        constructionMap.setBounds(0, 0, 612, 612);

        URL iconURL = getClass().getResource("/cone_button.png");
        ImageIcon coneIcon = new ImageIcon(iconURL);
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

        // 여기서 줌 이벤트 유지!
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

        // 이미지 크기에 맞게 공사 위치 표시 영역 크기 변경
        constructionMap.setBounds(0, 0, googleMap.getIcon().getIconWidth(), googleMap.getIcon().getIconHeight());

        repositionButton();

        // 공사 위치 정보 API 새로 불러오기
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
