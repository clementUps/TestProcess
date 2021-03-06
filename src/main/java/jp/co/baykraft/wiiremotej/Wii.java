package jp.co.baykraft.wiiremotej;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import wiiremotej.BalanceBoard;
import wiiremotej.WiiDevice;
import wiiremotej.WiiRemote;
import wiiremotej.WiiRemoteJ;
import wiiremotej.event.*;

public class Wii extends WiiRemoteAdapter implements WiiDeviceDiscoveryListener
{
    private WiiDevice _remote;

    public static void main(String... args) {
        Wii wii = new Wii();
        try {
            BalanceBoard b = WiiRemoteJ.findBalanceBoard();
            b.addBalanceBoardListener(new BalanceBoardListener() {
                public void buttonInputReceived(BBButtonEvent bbButtonEvent) {
                    System.out.println("Bonjour");
                }

                public void statusReported(BBStatusEvent bbStatusEvent) {

                }

                public void massInputReceived(BBMassEvent bbMassEvent) {

                }

                public void combinedInputReceived(BBCombinedEvent bbCombinedEvent) {

                }

                public void disconnected() {

                }
            });
        } catch(Exception e){


        }
    }


    @Override
    public void disconnected()
    {
    }


    @Override
    public void statusReported(WRStatusEvent evt)
    {
    }

    @Override
    public void accelerationInputReceived(WRAccelerationEvent evt)
    {
    }

    @Override
    public void buttonInputReceived(WRButtonEvent evt)
    {
        if (evt.wasPressed(WRButtonEvent.TWO)) {
            System.out.println("2");
        }
        else if (evt.wasPressed(WRButtonEvent.ONE)) {
            System.out.println("1");
        }
        else if (evt.wasPressed(WRButtonEvent.B)) {
            System.out.println("B");
        }
        else if (evt.wasPressed(WRButtonEvent.A)) {
            System.out.println("A");
        }
        else if (evt.wasPressed(WRButtonEvent.MINUS)) {
            System.out.println("Minus");
        }
        else if (evt.wasPressed(WRButtonEvent.PLUS)) {
            System.out.println("Plus");
        }
        else if (evt.wasPressed(WRButtonEvent.LEFT)) {
            System.out.println("Left");
        }
        else if (evt.wasPressed(WRButtonEvent.RIGHT)) {
            System.out.println("Right");
        }
        else if (evt.wasPressed(WRButtonEvent.DOWN)) {
            System.out.println("Down");
        }
        else if (evt.wasPressed(WRButtonEvent.UP)) {
            System.out.println("Up");
        }
        else if (evt.wasPressed(WRButtonEvent.HOME)) {
            if (null != _remote && _remote.isConnected()) {
                _remote.disconnect();
            }
            System.exit(0);
        }
    }

    public void wiiDeviceDiscovered(WiiDeviceDiscoveredEvent wiiDeviceDiscoveredEvent) {
        System.out.print("Coucou");
    }

    public void findFinished(int i) {

    }
}
