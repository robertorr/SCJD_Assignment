/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: ClientUI.java 11 2010-10-05 00:04:18Z robertorr $
 */
package suncertify.ui;

import java.text.ParseException;
import suncertify.ui.util.UIFocusListener;
import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.undo.UndoManager;
import suncertify.Constants;
import suncertify.ui.actions.BookAction;
import suncertify.ui.actions.CreateAction;
import suncertify.ui.actions.DeleteAction;
import suncertify.ui.actions.HelpAction;
import suncertify.ui.actions.OpenAction;
import suncertify.ui.actions.OptionsAction;
import suncertify.ui.actions.PrintAction;
import suncertify.ui.actions.RedoAction;
import suncertify.ui.actions.UndoAction;
import suncertify.ui.actions.UpdateAction;
import suncertify.ui.util.CustomIntegerMaskFormatter;

/**
 *
 * @author Robert J. Orr
 */
public class ClientUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final Logger _log = Logger.getLogger(ClientUI.class.getName());
    //
    private final String _appTitle;
    private final String _customerName;
    private final String _appVersion;
    private final Map<String, Integer> _fieldsMap;
    private final Map<String, Integer> _fieldLengthsMap;
    private final List<Action> _listTableActions;
    private final List<Action> _listTextActions;
    private JTable _tableResults;
    private JLabel _labelResults;
    private JTextField _txtfldName;
    private JTextField _txtfldLocation;
    private JTextField _txtfldSpecialty;
    private JFormattedTextField _txtfldSize;
    private JFormattedTextField _txtfldRate;
    private JFormattedTextField _txtfldOwner;
    private JButton _btnCreate;
    private JButton _btnUpdate;
    private JButton _btnDelete;
    private JButton _btnBook;
    private Action _actionCreate;
    private Action _actionDelete;
    private Action _actionUpdate;
    private Action _actionBook;
    private UndoManager _undoManager;

    /**
     * 
     * @throws Exception 
     */
    public ClientUI() throws Exception {

        super();
        _log.info("Constructing UI...");

        // load Look-And-Feel
        for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

        Properties properties = new Properties();
        FileReader reader = new FileReader(Constants.PROPERTIES_FILE);
        properties.load(reader);

        _appTitle = properties.getProperty("application.title", "");
        _customerName = properties.getProperty("application.customer", "");
        _appVersion = properties.getProperty("application.version", "");
        String frameTitle = _appTitle + " - " + _customerName;
        this.setTitle(frameTitle);

        // populate field name-to-field length table
        String fieldsBlob = properties.getProperty("database.fields.names", "");
        String lengthsBlob = properties.getProperty("database.fields.lengths", "");
        String[] fieldNames = fieldsBlob.split(",");
        String[] fieldLengths = lengthsBlob.split(",");
        assert (fieldNames.length == fieldLengths.length);
        _fieldsMap = new HashMap<String, Integer>();
        _fieldLengthsMap = new HashMap<String, Integer>();
        for (int i = 0; i < fieldNames.length; ++i) {
            _fieldsMap.put(fieldNames[i], i);
            _fieldLengthsMap.put(fieldNames[i], Integer.parseInt(fieldLengths[i]));
        }

        // create results table first so it can be used in subsequent setup
        TableModel model = new DefaultTableModel(fieldNames, 50);
        _tableResults = new JTable(model) {

            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                // disable editing for all cells
                return false;
            }
        };
        RowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
        _tableResults.setRowSorter(sorter);

        // create actions first so that they can be used in the UI setup
        _actionCreate = new CreateAction(this);
        _actionDelete = new DeleteAction(_tableResults, this);
        _actionUpdate = new UpdateAction(_tableResults, this);
        _actionBook = new BookAction(_tableResults, _fieldsMap.get("owner"), this);
        _actionDelete.setEnabled(false);
        _actionUpdate.setEnabled(false);
        _actionBook.setEnabled(false);
        _listTextActions = new ArrayList<Action>();
        _listTableActions = new ArrayList<Action>();
        _listTableActions.add(_actionDelete);
        _listTableActions.add(_actionUpdate);
        _listTableActions.add(_actionBook);
        _undoManager = new UndoManager();

        this.initLayout();
        this.setupMenus();
        Toolkit.getDefaultToolkit().addAWTEventListener(new UIFocusListener(_listTextActions), AWTEvent.FOCUS_EVENT_MASK);
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent evt) {
                shutdown(0);
            }
        });
        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
        _log.info("...done constructing UI");
    }

    /**
     *
     */
    private void initLayout() throws ParseException {

        this.setLayout(new BorderLayout());

        //
        // layout search panel
        //
        JPanel searchPanel = new JPanel();
        TitledBorder searchBorder = new TitledBorder(new LineBorder(Color.GRAY, 1, true), "Search Parameters",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.CENTER);
        searchPanel.setBorder(searchBorder);
        searchPanel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridheight = 1;
        c.gridwidth = 2;
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.anchor = GridBagConstraints.LINE_END;
        c.fill = GridBagConstraints.NONE;
        // inset values taken from "Java Look And Feel Design Guidelines, 2nd Ed."
        c.insets = new Insets(0, 11, 5, 0);

        c.gridy = 0;
        JLabel label = new JLabel("Subcontractor Name:");
        searchPanel.add(label, c);

        c.gridy = 1;
        label = new JLabel("City:");
        searchPanel.add(label, c);

        c.gridy = 2;
        label = new JLabel("Specialties:");
        searchPanel.add(label, c);

        c.gridy = 3;
        label = new JLabel("Number of Staff:");
        searchPanel.add(label, c);

        c.gridy = 4;
        label = new JLabel("Hourly Charge:");
        searchPanel.add(label, c);

        c.gridy = 5;
        label = new JLabel("Customer Number:");
        searchPanel.add(label, c);

        c.gridx = 2;
        c.gridwidth = 3;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.LINE_START;
        c.fill = GridBagConstraints.HORIZONTAL;
        int numColumns = 15;

        c.gridy = 0;
        // inset values taken from "Java Look And Feel Design Guidelines, 2nd Ed."
        c.insets = new Insets(0, 11, 5, 12);
        _txtfldName = new JTextField();
        _txtfldName.setColumns(numColumns);
        _txtfldName.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldName, c);

        c.gridy = 1;
        _txtfldLocation = new JTextField();
        _txtfldLocation.setColumns(numColumns);
        _txtfldLocation.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldLocation, c);

        c.gridy = 2;
        _txtfldSpecialty = new JTextField();
        _txtfldSpecialty.setColumns(numColumns);
        _txtfldSpecialty.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldSpecialty, c);

        c.gridy = 3;
        // TODO: fix spaces and required digits problem
        CustomIntegerMaskFormatter sizeFormatter = new CustomIntegerMaskFormatter();
        StringBuilder builder = new StringBuilder();
        // TODO: can we avoid hard-coding the field names for these
        int numDigits = _fieldLengthsMap.get("size");
        for (int i = 0; i < numDigits; ++i) {
            builder.append('#');
        }
        sizeFormatter.setMask(builder.toString());
        _txtfldSize = new JFormattedTextField(sizeFormatter);
        _txtfldSize.setColumns(numColumns);
        _txtfldSize.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldSize, c);

        c.gridy = 4;
        // TODO: fix issues with currency field
        NumberFormat nfRate = NumberFormat.getCurrencyInstance();
        nfRate.setGroupingUsed(false);
        nfRate.setMinimumIntegerDigits(0);
        nfRate.setMaximumIntegerDigits(_fieldLengthsMap.get("rate") - 4);
        nfRate.setMaximumFractionDigits(2);
        _txtfldRate = new JFormattedTextField(nfRate);
        _txtfldRate.setColumns(numColumns);
        _txtfldRate.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldRate, c);

        c.gridy = 5;
        NumberFormat nfOwner = NumberFormat.getIntegerInstance();
        nfOwner.setGroupingUsed(false);
        nfOwner.setMinimumIntegerDigits(0);
        nfOwner.setMaximumIntegerDigits(_fieldLengthsMap.get("owner"));
        _txtfldOwner = new JFormattedTextField(nfOwner);
        _txtfldOwner.setColumns(numColumns);
        _txtfldOwner.getDocument().addUndoableEditListener(_undoManager);
        searchPanel.add(_txtfldOwner, c);

        JPanel buttonPanel = new JPanel();
        BoxLayout buttonPanelLayout = new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS);
        buttonPanel.setLayout(buttonPanelLayout);
        JButton btnSearch = new JButton("Search");
        btnSearch.setMnemonic('S');
        JButton btnClear = new JButton("Clear");
        btnClear.setMnemonic('C');
        buttonPanel.add(btnSearch);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(btnClear);
        buttonPanel.add(Box.createHorizontalGlue());

        btnClear.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                _txtfldName.setText(null);
                _txtfldLocation.setText(null);
                _txtfldSpecialty.setText(null);
                _txtfldSize.setText(null);
                _txtfldRate.setText(null);
                _txtfldOwner.setText(null);
                _labelResults.setText(null);
            }
        });

        c.gridx = 2;
        c.gridy = 6;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;
        searchPanel.add(buttonPanel, c);

        c.gridy = 7;
        c.insets = new Insets(5, 15, 5, 12);
        _labelResults = new JLabel("No matching records");
        searchPanel.add(_labelResults, c);

        c.gridx = 0;
        c.gridy = 8;
        c.gridwidth = 5;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        label = new JLabel(" ");
        searchPanel.add(label, c);

        //
        // layout results panel
        //
        JPanel resultsPanel = new JPanel();
        TitledBorder resultsBorder = new TitledBorder(new LineBorder(Color.GRAY, 1, true), "Search Results",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.CENTER);
        resultsPanel.setBorder(resultsBorder);
        resultsPanel.setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(_tableResults, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        resultsPanel.add(scrollPane, BorderLayout.CENTER);
        ListSelectionModel listSelModel = new DefaultListSelectionModel();
        listSelModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelModel.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent evt) {
                if (evt.getValueIsAdjusting()) {
                    return;
                }

                boolean rowSelected = (evt.getFirstIndex() > -1);
                for (Action action : _listTableActions) {
                    action.setEnabled(rowSelected);
                }
            }
        });
        _tableResults.setSelectionModel(listSelModel);
        JPanel mainButtonPanel = new JPanel();
        BoxLayout mainButtonPanelLayout = new BoxLayout(mainButtonPanel, BoxLayout.LINE_AXIS);
        mainButtonPanel.setLayout(mainButtonPanelLayout);
        _btnBook = new JButton(_actionBook);
        _btnCreate = new JButton(_actionCreate);
        _btnDelete = new JButton(_actionDelete);
        _btnUpdate = new JButton(_actionUpdate);
        mainButtonPanel.add(Box.createHorizontalGlue());
        mainButtonPanel.add(_btnCreate);
        mainButtonPanel.add(Box.createHorizontalStrut(5));
        mainButtonPanel.add(_btnDelete);
        mainButtonPanel.add(Box.createHorizontalStrut(5));
        mainButtonPanel.add(_btnUpdate);
        mainButtonPanel.add(Box.createHorizontalStrut(5));
        mainButtonPanel.add(_btnBook);
        mainButtonPanel.add(Box.createHorizontalGlue());
        resultsPanel.add(mainButtonPanel, BorderLayout.SOUTH);

        //
        // compose main panel
        //
        JSplitPane mainPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, searchPanel, resultsPanel);
        mainPane.setDividerSize(3);
        this.add(mainPane, BorderLayout.CENTER);
    }

    /**
     * 
     */
    private void setupMenus() throws IOException {
        // set up menu bar and menus
        final ClientUI parent = this;
        JMenuBar menuBar = new JMenuBar();
        this.add(menuBar, BorderLayout.NORTH);

        // file menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');

        Action openAction = new OpenAction(parent);
        JMenuItem itemOpen = new JMenuItem(openAction);
        fileMenu.add(itemOpen);

        Action printAction = new PrintAction(_tableResults, parent);
        JMenuItem itemPrint = new JMenuItem(printAction);
        fileMenu.add(itemPrint);

        Action optionsAction = new OptionsAction(parent);
        JMenuItem itemOptions = new JMenuItem(optionsAction);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(itemOptions);

        Action exitAction = new AbstractAction("Exit") {

            private static final long serialVersionUID = 1L;

            // instance initializer, runs after superclass constructor
            {
                this.putValue(Action.MNEMONIC_KEY, KeyEvent.VK_X);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                parent.shutdown(0);
            }
        };
        JMenuItem itemExit = new JMenuItem(exitAction);
        fileMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        fileMenu.add(itemExit);
        menuBar.add(fileMenu);

        // edit menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic('E');
        menuBar.add(editMenu);

        Action undoAction = new UndoAction(_undoManager);
        _listTextActions.add(undoAction);
        JMenuItem itemUndo = new JMenuItem(undoAction);
        editMenu.add(itemUndo);

        Action redoAction = new RedoAction(_undoManager);
        _listTextActions.add(redoAction);
        JMenuItem itemRedo = new JMenuItem(redoAction);
        editMenu.add(itemRedo);

        editMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        JTextField textField = new JTextField();

        Action cutAction = textField.getActionMap().get(DefaultEditorKit.cutAction);
        _listTextActions.add(cutAction);
        JMenuItem itemCut = new JMenuItem(cutAction);
        itemCut.setText("Cut");
        itemCut.setMnemonic('t');
        itemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(itemCut);

        Action copyAction = textField.getActionMap().get(DefaultEditorKit.copyAction);
        _listTextActions.add(copyAction);
        JMenuItem itemCopy = new JMenuItem(copyAction);
        itemCopy.setText("Copy");
        itemCopy.setMnemonic('C');
        itemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(itemCopy);

        Action pasteAction = textField.getActionMap().get(DefaultEditorKit.pasteAction);
        _listTextActions.add(pasteAction);
        JMenuItem itemPaste = new JMenuItem(pasteAction);
        itemPaste.setText("Paste");
        itemPaste.setMnemonic('P');
        itemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(itemPaste);

        Action deleteAction = new TextAction("Delete") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent evt) {
                JTextComponent component = super.getTextComponent(evt);
                component.replaceSelection("");
            }
        };
        _listTextActions.add(deleteAction);
        JMenuItem itemDelete = new JMenuItem(deleteAction);
        itemDelete.setMnemonic('D');
        itemDelete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        editMenu.add(itemDelete);

        editMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        Action selectAllAction = textField.getActionMap().get(DefaultEditorKit.selectAllAction);
        _listTextActions.add(selectAllAction);
        JMenuItem itemSelectAll = new JMenuItem(selectAllAction);
        itemSelectAll.setText("Select All");
        itemSelectAll.setMnemonic('A');
        itemSelectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_DOWN_MASK));
        editMenu.add(itemSelectAll);

        // actions menu
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.setMnemonic('A');
        JMenuItem itemCreateRec = new JMenuItem(_actionCreate);
        actionsMenu.add(itemCreateRec);
        JMenuItem itemDeleteRec = new JMenuItem(_actionDelete);
        actionsMenu.add(itemDeleteRec);
        JMenuItem itemUpdateRec = new JMenuItem(_actionUpdate);
        actionsMenu.add(itemUpdateRec);
        JMenuItem itemBookRec = new JMenuItem(_actionBook);
        actionsMenu.add(itemBookRec);
        menuBar.add(actionsMenu);

        // help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        Action helpAction = new HelpAction(parent);
        JMenuItem itemHelp = new JMenuItem(helpAction);
        helpMenu.add(itemHelp);

        Action aboutAction = new AbstractAction("About") {

            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent e) {
                String revision = "$Revision: 11 $";
                String build = revision.replace("Revision:", "").replace("$", "");
                String msg = "<html><h2>" + _appTitle + "</h2><h3>" + _customerName + "</h3><br/>"
                        + "<b>Version:</b> " + _appVersion + " (build " + build + ")<br/>"
                        + "<b>Java:</b> " + System.getProperty("java.runtime.version") + "<br/>"
                        + "<b>System:</b> " + System.getProperty("os.name") + " on " + System.getProperty("os.arch")
                        + "</html>";
                JOptionPane.showMessageDialog(parent, msg, "About", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        JMenuItem itemAbout = new JMenuItem(aboutAction);
        itemAbout.setMnemonic('A');
        helpMenu.add(itemAbout);
        menuBar.add(helpMenu);
    }

    /**
     *
     * @param code
     */
    public void shutdown(int code) {

        String[] options = {"Exit", "Cancel"};
        String msg = "Are you sure you want to exit?";
        Toolkit.getDefaultToolkit().beep();
        int choice = JOptionPane.showOptionDialog(this, msg, "Confirm Exit", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }

        _log.log(Level.INFO, "exiting client with code: {0}", code);
        this.setVisible(false);
        this.dispose();
        System.exit(code);
    }
}
