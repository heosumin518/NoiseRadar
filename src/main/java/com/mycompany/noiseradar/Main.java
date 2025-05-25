/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.noiseradar;

import java.awt.BorderLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author heosumin518
 */
public class Main extends JFrame {

    private JTextField textField = new JTextField(30);//사용자에세 직접 입력받을 수 있게
    private JPanel panel = new JPanel();//하나의 패널을 만들어준다
    private JButton button = new JButton("search");//버튼을 눌러 검색할 수 있도록
	
    private GoogleAPI googleAPI = new GoogleAPI();
    private JLabel googleMap= new JLabel();//처음 한번만 초기화
    
    private int zoomLevel = 11;
    
    public class Event implements MouseListener{//실행될수 있는 함수를 만들었으니까 실행되는 조건을 만들어준다.

        @Override
        public void mouseClicked(MouseEvent e) {
            setMap(textField.getText());
            //클릭했을때 현재 사용자가 입력한 텍스트를 매개변수르 활영하여 함수가 실행되도록
            
            googleMap.setFocusable(true);
            googleMap.requestFocusInWindow();
        }

        @Override
        public void mousePressed(MouseEvent e) {			
        }

        @Override
        public void mouseReleased(MouseEvent e) {			
        }

        @Override
        public void mouseEntered(MouseEvent e) {			
        }

        @Override
        public void mouseExited(MouseEvent e) {			
        }
    }
    
    public void setMap(String location) {//입력된 내용에 따라 맵을 바꿔주는 함수
        googleAPI.downloadMap(location, zoomLevel);//해당 주소를 실제로 검색 후 이미지로 다운
        googleMap.setIcon(googleAPI.getMap(location));//JLabel을 초기화하지 않고 그림만 바뀌도록
        googleAPI.fileDelete(location);//다운로드 받은 이미지 파일은 우리 프로젝트 내에서 삭제
        add(BorderLayout.SOUTH, googleMap);
        pack();
    }
    
    public Main() {
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//창을 껐을떄 프로그램이 성공적으로 완전히 종료될 수 있도록
        setResizable(false);//창의 크기를 바꿀 수 없도록
        setTitle("Google Maps");
        setVisible(true);
		
        panel.add(textField);//페널에는 텍스트 필드 추가
        panel.add(button);//버튼 추가
        button.addMouseListener(new Event());//실제로 버튼을 클릭했을때 이벤트가 입력되록
		
        add(BorderLayout.NORTH, panel);//BorderLayout:특정한 위치를 기준으로 각각의 요소들을 배열
        add(BorderLayout.SOUTH,googleMap);//구글 맵을 프레임 안에 추가
        
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
}
