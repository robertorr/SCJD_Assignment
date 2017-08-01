/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: UIFocusListener.java 9 2010-09-23 04:08:18Z robertorr $
 */
package suncertify.ui.util;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.text.JTextComponent;

/**
 *
 * @author orrro
 */
public class UIFocusListener implements AWTEventListener {

    private final List<Action> _actionList;
    private final Color _colorFocused;
    private final Color _colorInactiveTextField;
    private final Color _colorInactiveTextArea;
    private final Color _colorInactiveEditorPane;

    public UIFocusListener(List<Action> actionList) {
        super();
        _actionList = actionList;
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        _colorFocused = new Color(255, 255, 153);
        _colorInactiveTextField = (Color) defaults.get("TextField.background");
        _colorInactiveTextArea = (Color) defaults.get("TextArea.background");
        _colorInactiveEditorPane = (Color) defaults.get("EditorPane.background");
    }

    @Override
    public void eventDispatched(AWTEvent event) {

        // we only want focus events
        if (!(event instanceof FocusEvent)) {
            return;
        }

        FocusEvent focusEvent = (FocusEvent) event;
        if (focusEvent.isTemporary()) {
            return;
        }
        // we only want JTextComponents (JTextField, JTextArea, etc.)
        Component component = focusEvent.getComponent();
        if (!(component instanceof JTextComponent)) {
            return;
        }
        JTextComponent textComponent = (JTextComponent) component;

        switch (focusEvent.getID()) {
            case FocusEvent.FOCUS_GAINED:
                // change the background color to indicate focus
                textComponent.setBackground(_colorFocused);
                for (Action action : _actionList) {
                    action.setEnabled(true);
                }
                break;

            case FocusEvent.FOCUS_LOST:
                if (textComponent instanceof JTextField) {
                    textComponent.setBackground(_colorInactiveTextField);
                } else if (textComponent instanceof JTextArea) {
                    textComponent.setBackground(_colorInactiveTextArea);
                } else if (textComponent instanceof JEditorPane) {
                    textComponent.setBackground(_colorInactiveEditorPane);
                }

                for (Action action : _actionList) {
                    action.setEnabled(false);
                }
                break;

            default:
                break;
        }
    }
}
