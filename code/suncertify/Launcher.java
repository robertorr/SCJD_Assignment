/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: Launcher.java 10 2010-09-25 06:01:25Z robertorr $
 */
package suncertify;

import java.awt.Toolkit;
import java.net.InetAddress;
import java.rmi.server.UID;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import suncertify.db.Data;
import suncertify.ui.ClientUI;

/**
 * This is the main launcher for the client/server system.
 * 
 * @author Robert J. Orr
 */
public class Launcher {

    public static void main(String[] args) {

        try {
            final InetAddress localhost = InetAddress.getLocalHost();
            final Data d = new Data();

            // TODO: remove testing code before submitting
            for (int tNum = 0; tNum < 3; ++tNum) {
                Thread t = new Thread("Thread " + tNum) {

                    @Override
                    public void run() {
                        UID uid = new UID();
                        String guid = localhost.getHostAddress() + ":" + uid.toString();
                        this.setName(guid);

                        for (int i = 0; i < 28; ++i) {
                            try {
                                d.lock(i);
                                String[] fields = d.read(i);
                                System.out.println(this.getName() + ": record " + i + ": " + Arrays.toString(fields));
                                Thread.sleep(10);
                                d.unlock(i);
                            } catch (Exception ex) {
                                Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                };
                t.start();
            }

            ClientUI ui = new ClientUI();
        } catch (Exception ex) {
            Toolkit.getDefaultToolkit().beep();
            Logger.getLogger(Launcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Launcher() {
    }
}
