package io;

import java.awt.GridLayout;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import settings.Sensitivity;



import edu.unsw.cse.wiiboard.event.WiiBoardMassEvent;

public class OutputHandler {

    private Robot robot;

    private int[] keyMap;
    private boolean[] keyPressed;
    private String[] keyValue;

    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;
    private static final int JUMP = 4;

    private List<WiiBoardMassEvent> events;

    private JLabel inputUp;
    private JLabel inputDown;
    private JLabel inputLeft;
    private JLabel inputRight;
    private JLabel inputJump;

    private Sensitivity sensitivity;

    public OutputHandler(Robot r, Sensitivity s){

        this.sensitivity = s;
        jumpTimer = System.currentTimeMillis();
        keyValue = new String[5];
        keyMap = new int[5];

        inputUp = new JLabel();
        inputDown = new JLabel();
        inputLeft = new JLabel();
        inputRight = new JLabel();
        inputJump = new JLabel();

        defaultSettings();

        keyPressed = new boolean[5];
        for(int i = 0; i < 5; ++i){
            keyPressed[i] = false;
        }
        robot = r;
        events = new LinkedList<WiiBoardMassEvent>();


    }


    public void defaultSettings() {

        keyMap[LEFT] = KeyEvent.VK_A;
        keyMap[RIGHT] = KeyEvent.VK_D;
        keyMap[UP] = KeyEvent.VK_W;
        keyMap[DOWN] = KeyEvent.VK_S;
        keyMap[JUMP] = KeyEvent.VK_SPACE;

        for(int i = 0; i < 5; ++i){
            keyValue[i] = KeyEvent.getKeyText(keyMap[i]).toUpperCase();
        }

        inputLeft.setText(keyValue[LEFT]);
        inputRight.setText(keyValue[RIGHT]);
        inputUp.setText(keyValue[UP]);
        inputDown.setText(keyValue[DOWN]);
        inputJump.setText(keyValue[JUMP]);
    }


    public void outputEmulation(WiiBoardMassEvent e){
        double x = (e.getBottomLeft() + e.getTopLeft())/e.getTotalWeight();

        if(sensitivity.isRight(x)){
            //right action
            pressKey(RIGHT);
        } else if (sensitivity.isLeft(x)){
            //left action
            pressKey(LEFT);
        }

        double y = (e.getBottomLeft() + e.getBottomRight())/e.getTotalWeight();

        if( sensitivity.isUp(y)){
            //top action
            pressKey(UP);
        } else if(sensitivity.isDown(y)){
            //bottom action
            pressKey(DOWN);
        }

        if(sensitivity.isXSafe(x)){
            releaseKey(LEFT);
            releaseKey(RIGHT);
        }
        if(sensitivity.isYSafe(y)){
            releaseKey(UP);
            releaseKey(DOWN);
        }

        events.add(e);
        if(events.size() > 7){
            events.remove(0);

            if(Math.abs(events.get(2).getTotalWeight() - events.get(6).getTotalWeight()) > 20) {
                jump();
            }
        }

    }

    private long jumpTimer;

    private void jump() {
        if(System.currentTimeMillis() - jumpTimer < 200){
            return;
        }

        jumpTimer = System.currentTimeMillis();
        pressKey(JUMP);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        releaseKey(JUMP);

    }

    private void pressKey(int key){
        if(!keyPressed[key]){
            if(key == JUMP){
                System.out.println("Robot has pressed jump");
            }
            robot.keyPress(keyMap[key]);
            keyPressed[key] = true;
        }
    }

    private void releaseKey(int key){
        if(keyPressed[key]){
            if(key == JUMP){
                System.out.println("Robot has released jump\n");
            }
            robot.keyRelease(keyMap[key]);
            keyPressed[key] = false;
        }
    }

    public void releaseAll(){
        for(int i = 0; i < 5; ++i){
            releaseKey(i);
        }
    }

    private void attachListeners(final JLabel component, final int direction, final JOptionPane pane){

        component.setFocusable(true);

        component.addMouseListener(new MouseAdapter(){

            public void mouseClicked(MouseEvent e){
                component.requestFocus();
            }

        });

        component.addKeyListener(new KeyAdapter(){

            public void keyPressed(KeyEvent e){
                String value = KeyEvent.getKeyText(e.getKeyCode()).toUpperCase();
                keyValue[direction] = value;
                keyMap[direction] = e.getKeyCode();
                component.setText(value);
            }

            public void keyReleased(KeyEvent e){
                pane.requestFocus();
            }

        });

        component.addFocusListener(new FocusListener(){

            public void focusGained(FocusEvent fe) {
                component.setBorder(new BevelBorder(BevelBorder.LOWERED));
            }

            public void focusLost(FocusEvent fe) {
                component.setBorder(null);
            }
        });

    }

    public void showInputSettings() {
        if(optionPane == null){
            createInputChoicePanel();
        }
        JDialog dialog = optionPane.createDialog(null, "WiiBoard emulation settings");

        java.awt.Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(d.width - 300, d.height - 300);
        dialog.setVisible(true);
    }

    private JOptionPane optionPane;
    private void createInputChoicePanel(){

        optionPane = new JOptionPane();
        JPanel p = new JPanel();
        p.setLayout(new GridLayout(5,2));

        JLabel lblUp = new JLabel("Up");
        JLabel lblDown = new JLabel("Down");
        JLabel lblLeft = new JLabel("Left");
        JLabel lblRight = new JLabel("Right");
        JLabel lblJump = new JLabel("Jump");

        attachListeners(inputUp, UP, optionPane);
        attachListeners(inputDown, DOWN, optionPane);
        attachListeners(inputLeft, LEFT, optionPane);
        attachListeners(inputRight, RIGHT, optionPane);
        attachListeners(inputJump, JUMP, optionPane);

        p.add(lblUp);
        p.add(inputUp);

        p.add(lblDown);
        p.add(inputDown);

        p.add(lblLeft);
        p.add(inputLeft);

        p.add(lblRight);
        p.add(inputRight);

        p.add(lblJump);
        p.add(inputJump);

        Object complexMsg[] = { "Keyboard emulation settings",p};
        optionPane.setMessage(complexMsg);

    }



}
