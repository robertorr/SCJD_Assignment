/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: PrintAction.java 9 2010-09-23 04:08:18Z robertorr $
 */
package suncertify.ui.actions;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 *
 * @author orrro
 */
public class PrintAction extends AbstractAction {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(PrintAction.class.getName());
    private final MessageFormat _headerFormat = new MessageFormat("Search Results");
    private final MessageFormat _footerFormat = new MessageFormat("Page {0,number,integer}");
    private final JTable _table;
    private final JFrame _parent;

    public PrintAction(JTable table, JFrame parent) {
        super("Print...");
        _table = table;
        _parent = parent;
        this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            // Slow dialog is due to Java Bug ID 6539061:
            //  Cross platform print dialog slow first time because of call to GraphicsDevice.getConfigurations()
            _table.print(JTable.PrintMode.NORMAL, _headerFormat, _footerFormat);
        } catch (PrinterException ex) {
            _log.warning(ex.toString());
            String msg = ex.getLocalizedMessage();
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(_parent, msg, "Printing Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
