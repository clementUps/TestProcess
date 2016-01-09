package settings;

        import java.awt.Color;
        import java.awt.Graphics;
        import java.awt.Graphics2D;
        import java.awt.Toolkit;
        import java.awt.event.MouseAdapter;
        import java.awt.event.MouseEvent;
        import java.awt.event.MouseMotionAdapter;

        import javax.swing.JDialog;
        import javax.swing.JOptionPane;
        import javax.swing.JPanel;
        import javax.swing.border.EtchedBorder;

public class Sensitivity {

    private double[] sens;
    private static final int LEFT = 0;
    private static final int RIGHT = 1;
    private static final int UP = 2;
    private static final int DOWN = 3;

    public Sensitivity(){
        //"Default" values. Not accurate to everyone
        sens = new double[4];
        sens[LEFT] = 0.38;
        sens[RIGHT] = 0.62;
        sens[UP] = 0.4;
        sens[DOWN] = 0.8;
    }

    public boolean isRight(double x){
        return x >= sens[RIGHT];
    }

    public boolean isLeft(double x){
        return x <= sens[LEFT];
    }

    public boolean isUp(double y){
        return y >= sens[UP];
    }

    public boolean isDown(double y){
        return y <= sens[DOWN];
    }

    public boolean isXSafe(double x){
        return (x < sens[RIGHT] && x > sens[LEFT]);
    }

    public boolean isYSafe(double y){
        return (y < sens[UP]&& y < sens[DOWN]);
    }

    public void showPanel(){
        if(optionPane == null){
            createSensitivityPanel();
        }
        JDialog dialog = optionPane.createDialog(null, "Emulation sensitivity");

        java.awt.Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        dialog.setLocation(d.width - 300, d.height - 300);
        dialog.setSize(200,150);

        dialog.setVisible(true);
    }

    private JOptionPane optionPane;

    private int selected;
    private DrawPanel p;

    private void createSensitivityPanel() {
        optionPane = new JOptionPane();

        p = new DrawPanel();
        p.setBackground(Color.white);
        p.setSize(200,150);
        p.setBorder(new EtchedBorder(EtchedBorder.LOWERED));

        p.addMouseListener(new MouseAdapter(){

            public void mousePressed(MouseEvent e){
                double x = e.getX();
                double y = e.getY();

                if(selected != -1) return;

                for(int i = 0; i < 2; ++i){
                    if (x < p.localSpace(i)+5 && x > p.localSpace(i)-5){
                        selected = i;
                        break;
                    } else if(y < p.localSpace(i+2)+5 && y > p.localSpace(i+2)-5){
                        selected = i+2;
                        break;
                    }
                }
            }

            public void mouseReleased(MouseEvent e){
                updateSens(e.getX(),e.getY());
                selected = -1;
            }

        });

        p.addMouseMotionListener(new MouseMotionAdapter(){

            public void mouseDragged(MouseEvent e){
                updateSens(e.getX(),e.getY());
            }

        });


        Object complexMsg[] = {p};
        optionPane.setMessage(complexMsg);

    }

    private void updateSens(double x, double y){
        if(selected == -1) return;

        if(selected < 2){
            sens[selected] = x/p.getWidth();

            if(selected == RIGHT && sens[RIGHT] <= sens[LEFT] + 0.1){
                sens[RIGHT] = sens[LEFT] + 0.1;
            } else if (selected == LEFT && sens[LEFT] >= sens[RIGHT] - 0.1){
                sens[LEFT] = sens[RIGHT] - 0.1;
            }

        } else if(selected < 4){
            sens[selected] = y/p.getHeight();
            if(selected == UP && sens[UP] >= sens[DOWN] - 0.1){
                sens[UP] = sens[DOWN] - 0.1;
            } else if (selected == DOWN && sens[DOWN] <= sens[UP] + 0.1){
                sens[DOWN] = sens[UP] + 0.1;
            }

        }
        p.repaint();
    }

    class DrawPanel extends JPanel {

        private static final long serialVersionUID = -3211610104102599476L;

        public void paintComponent(Graphics g){

            Graphics2D g2 = (Graphics2D)g;

            g2.clearRect(0, 0, this.getWidth(), this.getHeight());

            g2.setColor(Color.blue);
            g2.drawLine(localSpace(LEFT), 0, localSpace(LEFT), this.getHeight());
            g2.setColor(Color.cyan);
            g2.drawLine(localSpace(RIGHT), 0, localSpace(RIGHT), this.getHeight());

            g2.setColor(Color.red);
            g2.drawLine(0, localSpace(UP), this.getWidth(), localSpace(UP));
            g2.setColor(Color.magenta);
            g2.drawLine(0, localSpace(DOWN), this.getWidth(), localSpace(DOWN));

        }

        public int localSpace(int direction){
            if(direction < 2){
                return (int)(sens[direction] * this.getWidth());
            } else {
                return (int)(sens[direction] * this.getHeight());
            }
        }
    }
}
