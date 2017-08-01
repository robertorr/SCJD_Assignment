/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: OpenAction.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author orrro
 */
public class OpenAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(OpenAction.class.getName());
    private final JFrame _parent;

    public OpenAction(JFrame parent) {
        super("Open...");
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        File currentDir = new File(".");
        JFileChooser chooser = new JFileChooser(currentDir);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Database Files", "db");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(_parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            _log.log(Level.INFO, "You chose to open this file: {0}", chooser.getSelectedFile().getName());
        }

        // TODO: point the server at the new database file
    }
}
