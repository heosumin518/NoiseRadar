package com.freepass.main;

import com.freepass.controller.Construction;
import com.mycompany.noiseradar.ConstructionAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ConstructionMap extends JPanel {
    private final java.util.List<Construction> constructions = new ArrayList<>();
    private Construction lastHoveredConstruction = null;
    private JWindow popupWindow = null;

    public ConstructionMap() {
        setOpaque(false);
        setPreferredSize(new Dimension(612, 612));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hovering = false;
                for (Construction c : constructions) {
                    if (e.getX() >= c.x - 10 && e.getX() <= c.x + 10 &&
                        e.getY() >= c.y - 10 && e.getY() <= c.y + 10) {
                        if (lastHoveredConstruction != c) {
                            lastHoveredConstruction = c;
                            showPopup(e, c);
                        }
                        hovering = true;
                        break;
                    }
                }
                if (!hovering) {
                    lastHoveredConstruction = null;
                    hidePopup();
                }
            }
        });
    }

    public void fetchDataFromAPI() {
        constructions.clear();
        try {
            ConstructionAPI api = new ConstructionAPI();
            while (api.hasNext()) {
                Construction c = api.getNext();
                if (c != null) {
                    constructions.add(c);
                }
            }
            repaint(); // 새로운 데이터를 반영하여 다시 그림
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "API 데이터를 불러오는 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addConstruction(Construction c) {
        constructions.add(c);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Construction c : constructions) {
            g.setColor(new Color(0, 255, 0, 70));
            g.fillOval(c.x - 30, c.y - 30, 60, 60);
            g.setColor(Color.GREEN);
            g.fillOval(c.x - 5, c.y - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("🚧", c.x - 8, c.y - 10);
        }
    }

    private void showPopup(MouseEvent e, Construction c) {
        hidePopup();
        JLabel label = new JLabel("<html><b>" + c.name + "</b><br>시공사: " + c.contractor + "<br>위치: " + c.location + "<br>기간: " + c.startDate + " ~ " + c.endDate + "</html>");
        label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        label.setBackground(new Color(255, 255, 225));
        label.setOpaque(true);
        popupWindow = new JWindow(SwingUtilities.getWindowAncestor(this));
        popupWindow.getContentPane().add(label);
        popupWindow.pack();
        Point locationOnScreen = e.getLocationOnScreen();
        popupWindow.setLocation(locationOnScreen.x + 15, locationOnScreen.y + 15);
        popupWindow.setVisible(true);
    }

    private void hidePopup() {
        if (popupWindow != null) {
            popupWindow.setVisible(false);
            popupWindow.dispose();
            popupWindow = null;
        }
    }
}
