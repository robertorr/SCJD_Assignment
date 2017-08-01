/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: HelpAction.java 9 2010-09-23 04:08:18Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.text.html.HTMLEditorKit;

/**
 *
 * @author orrro
 */
public class HelpAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(HelpAction.class.getName());
    private static final String HELP_FILE = "docs/help.html";
    //
    private final JFrame _parent;
    private final JDialog _dialog;

    public HelpAction(JFrame parent) throws IOException {
        super("Help");
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_H);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_H, KeyEvent.CTRL_DOWN_MASK));

        _dialog = new JDialog(_parent, "Help", false);
        JEditorPane edPane = new JEditorPane();
        edPane.setEditorKit(new HTMLEditorKit());
        FileReader reader = new FileReader(HELP_FILE);
        edPane.read(reader, HELP_FILE);
        _dialog.add(edPane, BorderLayout.CENTER);
        _dialog.pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _log.info("Help action performed");
        _dialog.setLocationRelativeTo(_parent);
        _dialog.setVisible(true);
    }
}
