package jp.co.baykraft.wiiremotej;

import io.OutputHandler;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import settings.Sensitivity;

import edu.unsw.cse.wiiboard.*;
import edu.unsw.cse.wiiboard.event.*;



public class WiiBoardController implements WiiBoardListener, WiiBoardDiscoveryListener {

    private WiiBoard board;
    private SystemTray tray;
    private Robot robot = null;
    private TrayIcon trayIcon;
    private boolean inputting;
    private boolean emulating;
    private PopupMenu popup;
    private MenuItem connect;
    private OutputHandler out;
    private Sensitivity sensitivity;
    private JTextArea console;
    private JFrame consoleFrame;

    private WiiBoardController(){

        emulating = false;
        lightTimer = System.currentTimeMillis();
        inputting = false;
        useMessages = true;
        console = new JTextArea();
        consoleFrame = new JFrame();
        consoleFrame.setSize(400, 500);


        consoleFrame.add(console);
        //consoleFrame.setVisible(false);
        System.setOut(new PrintStream(new TextAreaOutputStream(console)));
        System.setErr(new PrintStream(new TextAreaOutputStream(console)));

        System.out.println("Console started..");


        if(!SystemTray.isSupported()) {
            JOptionPane.showMessageDialog(new Canvas(), "Error 00: System Tray unsupported",
                    "WiiBoardController Startup Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        try {
            robot = new Robot();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(new Canvas(), "Error 01: Couldn't create input replication",
                    "WiiBoardController Startup Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getLocalizedMessage());
            System.exit(2);
        }

        sensitivity = new Sensitivity();
        out = new OutputHandler(robot, sensitivity);

        tray = SystemTray.getSystemTray();
        Image base = Toolkit.getDefaultToolkit().createImage("icons\\disconnected.JPG");


        initMenu();

        trayIcon = new TrayIcon(base,"Wii Board Controller",popup);

        trayIcon.setImageAutoSize(true);
        trayIcon.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                connectBoard();

            }});

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            JOptionPane.showMessageDialog(new Canvas(), "Error 03: Tray Image exception",
                    "WiiBoardController Startup Error", JOptionPane.ERROR_MESSAGE);
            System.out.println(e.getMessage());
            System.exit(3);
        }


        if(!WiiBoardDiscoverer.isBluetoothReady()){
            JOptionPane.showMessageDialog(new Canvas(), "Error 04: Bluetooth device not ready",
                    "WiiBoardController Startup Error", JOptionPane.ERROR_MESSAGE);
            System.out.println("Bluetooth not ready");
            System.exit(4);
        }

        WiiBoardDiscoverer.getWiiBoardDiscoverer().addWiiBoardDiscoveryListener(this);

    }

    public static void main(String[] args){
        new WiiBoardController();

    }

    public void wiiBoardButtonEvent(WiiBoardButtonEvent e) {
        if(e.isPressed()){
            emulating = !emulating;
            if(emulating) {
                board.setLight(true);
                if(useMessages){
                    trayIcon.displayMessage("Wii Board Event",
                            "The wii board is active!",
                            TrayIcon.MessageType.INFO);
                }
            } else {
                if(useMessages){
                    trayIcon.displayMessage("Wii Board Event",
                            "The wii board is in standby",
                            TrayIcon.MessageType.INFO);
                }
            }
        }
    }


    public void wiiBoardDisconnected() {
        if(useMessages)
            trayIcon.displayMessage("Wii Board Event",
                    "The balance board has been disconnected",
                    TrayIcon.MessageType.INFO);

        connect.setLabel("Connect");
        connect.setEnabled(true);
        trayIcon.setImage(Toolkit.getDefaultToolkit().createImage("icons\\disconnected.JPG"));
    }

    private Image tempStore;

    private void storeImage(Image i){
        tempStore = i;
    }

    long lightTimer;


    public void wiiBoardMassReceived(WiiBoardMassEvent e) {

        if(!emulating) {
            if(System.currentTimeMillis() - lightTimer > 1000){
                board.setLight(!board.isLEDon());
                lightTimer = System.currentTimeMillis();
            }
            return;
        }

        if(!inputting && e.getTotalWeight() > 20){
            inputting = true;
            storeImage(trayIcon.getImage());
            trayIcon.setImage(Toolkit.getDefaultToolkit().createImage("icons\\inputAck.JPG"));
        } else if(inputting && e.getTotalWeight() < 20){
            inputting = false;
            trayIcon.setImage(tempStore);
        }

        if(e.getTotalWeight() > 20){
            out.outputEmulation(e);
        } else {
            out.releaseAll();
        }


    }


    public void shutdown(){
        if(board != null){
            board = null;
        }
        System.exit(0);
    }


    public void wiiBoardStatusReceived(WiiBoardStatusEvent e) {
        if(useMessages && e.batteryLife() < 0.2)
            trayIcon.displayMessage("Board Battery Level Low: " + e.batteryLife()*100+"%",
                    "Please replace batteries in the board soon",
                    TrayIcon.MessageType.WARNING);

    }



    public void wiiBoardDiscovered(WiiBoard board) {
        if(useMessages)
            trayIcon.displayMessage("Wii Board Event",
                    "Balance Board connected!",
                    TrayIcon.MessageType.INFO);

        connect.setLabel("Disconnect");
        connect.setEnabled(true);
        WiiBoardDiscoverer.getWiiBoardDiscoverer().stopWiiBoardSearch();
        this.board = board;
        board.addListener(this);
        trayIcon.setImage(Toolkit.getDefaultToolkit().createImage("icons\\connected.JPG"));

    }


    private boolean useMessages;

    private void useInfoBalloons(boolean b) {
        useMessages = b;

    }

    private void connectBoard() {
        if(board == null || !board.isConnected()){
            trayIcon.setImage(Toolkit.getDefaultToolkit().createImage("icons\\connecting.JPG"));
            if(useMessages)
                trayIcon.displayMessage("Connect the board",
                        "Press the red sync button now",
                        TrayIcon.MessageType.INFO);

            WiiBoardDiscoverer.getWiiBoardDiscoverer().startWiiBoardSearch();
        }


    }

    private void displayConsole() {

        if(!consoleFrame.isVisible()) {
            consoleFrame.setVisible(true);
        }

    }

    private void initMenu(){
        popup = new PopupMenu();
        MenuItem exit;
        Menu settings;

        MenuItem consoleItem;

        exit = new MenuItem("Exit");
        exit.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                shutdown();
            }});

        consoleItem = new MenuItem("Show Console");

        consoleItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){

                displayConsole();
            }



        });
        settings = new Menu("Settings",true);

        CheckboxMenuItem showBalloonCheck = new CheckboxMenuItem("Show info balloons",true);
        showBalloonCheck.addItemListener(new ItemListener(){

            public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange() == ItemEvent.SELECTED){
                    useInfoBalloons(true);
                } else {
                    useInfoBalloons(false);
                }
            }});

        CheckboxMenuItem emulate = new CheckboxMenuItem("Emulate keyboard", false);
        emulate.addItemListener(new ItemListener(){

            public void itemStateChanged(ItemEvent e) {
                emulating = e.getStateChange() == ItemEvent.SELECTED;
            }});

        MenuItem restoreSettings = new MenuItem("Restore default settings");
        restoreSettings.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                out.defaultSettings();


            }
        });


        MenuItem configSettings = new MenuItem("Configure emulation...");
        configSettings.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                out.showInputSettings();
            }
        });

        MenuItem configSens = new MenuItem("Configure sensitivity...");
        configSens.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                sensitivity.showPanel();
            }
        });


        settings.add(configSettings);
        settings.add(configSens);
        settings.add(new MenuItem("-"));
        settings.add(emulate);
        settings.add(showBalloonCheck);
        settings.add(new MenuItem("-"));
        settings.add(restoreSettings);


        connect = new MenuItem("Connect");

        connect.addActionListener(new ActionListener(){

            public void actionPerformed(ActionEvent e) {
                if(board == null || !board.isConnected()){
                    connectBoard();
                    connect.setEnabled(false);
                } else if(board.isConnected()){
                    board.cleanup();
                    board = null;

                }

            }

        });



        popup.add(connect);
        popup.add(new MenuItem("-"));
        popup.add(settings);
        popup.add(new MenuItem("-"));
        popup.add(consoleItem);
        popup.add(new MenuItem("-"));
        popup.add(exit);
    }

}

class TextAreaOutputStream extends OutputStream {

    JTextArea textArea;

    public TextAreaOutputStream(JTextArea text){
        textArea = text;
    }

    public void flush(){
        textArea.repaint();
    }

    @Override
    public void write(int b) throws IOException {
        textArea.append(new String(new byte[] {(byte)b}));

    }


}

