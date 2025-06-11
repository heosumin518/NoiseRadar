package com.freepass.main;

import javax.swing.*;

public class EntryPoint {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
        }); 
    }
}
