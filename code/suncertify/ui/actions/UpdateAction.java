/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: UpdateAction.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 * @author orrro
 */
public class UpdateAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(UpdateAction.class.getName());
    private final JTable _table;
    private final JFrame _parent;

    public UpdateAction(JTable table, JFrame parent) {
        super("Update Selected...");
        _table = table;
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_U, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _log.info("Update action performed");
        // TODO: implement update action
    }
}
