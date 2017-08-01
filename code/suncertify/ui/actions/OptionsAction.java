/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: OptionsAction.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;

/**
 *
 * @author orrro
 */
public class OptionsAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(OptionsAction.class.getName());
    private final JFrame _parent;

    public OptionsAction(JFrame parent) {
        super("Options...");
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_T);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _log.info("Options action performed");
        // TODO: implement options dialog
    }
}
