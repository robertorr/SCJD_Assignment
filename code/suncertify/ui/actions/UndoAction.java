/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: UndoAction.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author orrro
 */
public class UndoAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private final UndoManager _manager;

    public UndoAction(UndoManager manager) {
        super("Undo");
        _manager = manager;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_U);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            _manager.undo();
        } catch (CannotUndoException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
