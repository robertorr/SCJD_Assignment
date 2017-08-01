/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: RedoAction.java 6 2010-09-22 00:48:42Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author orrro
 */
public class RedoAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private final UndoManager _manager;

    public RedoAction(UndoManager manager) {
        super("Redo");
        _manager = manager;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_R);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            _manager.redo();
        } catch (CannotRedoException ex) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
