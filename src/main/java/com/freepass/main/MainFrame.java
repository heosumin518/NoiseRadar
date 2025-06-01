/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.freepass.main;

import java.awt.BorderLayout;
import javax.swing.JFrame;


/**
 * 메인 프레임
 * 어플리케이션의 여러 화면을 관리한다. 즉, 패널들을 관리한다.
 * @author heosumin518
 */
public class MainFrame extends JFrame {
    public static final int W_FRAME = 800;
    public static final int H_FRAME = 600;

    private MainScreen mainScreen;

    public MainFrame() {
        super("Noise Radar");
        setSize(W_FRAME, H_FRAME);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout());
        
        mainScreen = new MainScreen();
        setContentPane(mainScreen);
        
        setVisible(true);
    }
}
