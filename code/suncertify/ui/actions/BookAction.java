/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: BookAction.java 11 2010-10-05 00:04:18Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 * @author orrro
 */
public class BookAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(BookAction.class.getName());
    private final JTable _table;
    private final int _colIndex;
    private final JFrame _parent;
    private final JDialog _bookingDialog;

    public BookAction(JTable table, int colIndex, JFrame parent) {
        super("Book Selected...");
        _table = table;
        _colIndex = colIndex;
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_B);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_B, KeyEvent.CTRL_DOWN_MASK));

        _bookingDialog = new JDialog(_parent, "Book Selected Record");

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        _log.info("Book action performed");
        int selectedRow = _table.getSelectedRow();
        String owner = (String) _table.getValueAt(selectedRow, _colIndex);
        if (owner != null && !owner.isEmpty()) {
            // record is already booked -- show error dialog
            String msg = "<html><b>The selected record has already been booked.</b><br/>"
                    + "Please select another record and try again.</html>";
            JOptionPane.showMessageDialog(_parent, msg, "Booking Error", JOptionPane.WARNING_MESSAGE);
        } else {
            _bookingDialog.setVisible(true);
            String result = JOptionPane.showInputDialog(_parent, "Customer Number:", "Book Selected Record", JOptionPane.QUESTION_MESSAGE);
            _log.info("Customer number: " + result);
            // TODO: finish booking action
        }
    }
}
