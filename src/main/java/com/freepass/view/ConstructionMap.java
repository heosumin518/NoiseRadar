package com.freepass.view;

import com.freepass.dto.ConstructionDTO;
import com.freepass.controller.ConstructionAPI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class ConstructionMap extends JPanel {
    private final java.util.List<ConstructionDTO> constructions = new ArrayList<>();
    private ConstructionDTO lastHoveredConstruction = null;
    private JWindow popupWindow = null;

    public ConstructionMap() {
        setOpaque(false);
        setPreferredSize(new Dimension(612, 612));

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                boolean hovering = false;
                for (ConstructionDTO c : constructions) {
                    if (e.getX() >= c.getX() - 10 && e.getX() <= c.getX() + 10 &&
                        e.getY() >= c.getY() - 10 && e.getY() <= c.getY() + 10) {
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

    public void fetchDataFromAPI(int iconWidth, int iconHeight) {
        constructions.clear();
        try {
            ConstructionAPI api = new ConstructionAPI();
            while (api.hasNext()) {
                ConstructionDTO c = api.getNext();
                if (c != null) {
                    constructions.add(c);
                }
            }
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "API 데이터를 불러오는 중 오류가 발생했습니다.", "에러", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void addConstruction(ConstructionDTO c) {
        constructions.add(c);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (ConstructionDTO c : constructions) {
            int x = c.getX();
            int y = c.getY();

            g.setColor(new Color(0, 255, 0, 70));
            g.fillOval(x - 30, y - 30, 60, 60);
            g.setColor(Color.GREEN);
            g.fillOval(x - 5, y - 5, 10, 10);
            g.setColor(Color.BLACK);
            g.drawString("🚧", x - 8, y - 10);
        }
    }

    private void showPopup(MouseEvent e, ConstructionDTO c) {
        hidePopup();
        JLabel label = new JLabel("<html><b>" + c.getName() + "</b><br>시공사: " + c.getContractor() + "<br>위치: " + c.getLocation() + "<br>기간: " + c.getStartDate() + " ~ " + c.getEndDate() + "</html>");
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

    void setMapImage(Image image) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
