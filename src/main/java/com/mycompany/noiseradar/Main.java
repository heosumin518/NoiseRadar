/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noiseradar;

import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author user
 */
public class Main extends JFrame {

    private GoogleAPI googleAPI = new GoogleAPI();
    private String location = "부산";
    private JLabel googleMap;
    
    public Main() {
        googleAPI.downloadMap(location);
        googleMap = new JLabel(googleAPI.getMap(location));
        googleAPI.fileDelete(location);
        add(googleMap);
        setTitle("Google Maps");
        setVisible(true);
        pack();
    }

}
