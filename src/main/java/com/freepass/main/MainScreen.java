package com.freepass.main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MainScreen extends JPanel {
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("검색");
    private final MainFrame parentFrame;

    public MainScreen(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 로고
        JLabel logo = new JLabel(new ImageIcon("src\\icon\\main_logo.png"));
        logo.setAlignmentX(CENTER_ALIGNMENT);
        add(Box.createVerticalGlue());
        add(logo);

        // 검색창 패널
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("주소"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        add(Box.createVerticalStrut(20));
        add(searchPanel);
        add(Box.createVerticalGlue());

        // 버튼 이벤트
        searchButton.addActionListener((ActionEvent e) -> {
            String address = searchField.getText().trim();
            if (!address.isEmpty()) {
                parentFrame.showMapScreen(address);
            }
        });
    }
}
