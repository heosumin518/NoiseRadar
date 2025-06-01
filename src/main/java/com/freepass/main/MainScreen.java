/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.freepass.main;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author heosumin518
 */
public class MainScreen extends JPanel {
    
    private JPanel logoPane, searchPane;
    private JLabel label_icon, label_address;
    private JButton searchButton;
    private JTextField textField_address;
    
    public MainScreen() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(10, 0, 10, 0); // 위, 좌, 아래, 우 여백
        
        // 어플리케이션 로고
        logoPane = new JPanel();
        logoPane.setOpaque(false);
        label_icon = new JLabel(new ImageIcon("src\\icon\\main_logo.png"));
        logoPane.add(label_icon);
        
        // 검색창
        searchPane = new JPanel();
        searchPane.setOpaque(false);
        searchPane.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        label_address = new JLabel("주소");
        searchPane.add(label_address);
        textField_address = new JTextField();
        textField_address.setPreferredSize(new Dimension(300, 30));
        searchButton = new JButton("검색");
        searchButton.setPreferredSize(new Dimension(80, 30));
        searchButton.addActionListener(e -> performSearch());
        searchPane.add(textField_address);
        searchPane.add(searchButton);
        
        gbc.gridy = 0;
        add(logoPane, gbc);
        gbc.gridy = 1;
        add(searchPane, gbc);
    }
    
    public JButton getSearchButton() {
        return searchButton;
    }
    
    private void performSearch() {
        
    }
}
