package com.mycompany.noiseradar;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class EntryPoint {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ConstructionMapViewer("부산");
            
            Main main = new Main();
        });
    }
}