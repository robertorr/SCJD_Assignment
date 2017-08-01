/*
 * Sun Certified Java 2 Developer (SCJD) Exam Assignment
 * Exam #: CX-310-252A
 * Contract #: 1099225
 * Candidate name: Robert J. Orr
 *
 * $Id: CustomIntegerMaskFormatter.java 10 2010-09-25 06:01:25Z robertorr $
 */
package suncertify.ui.util;

import java.text.ParseException;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author Robert J. Orr
 */
public class CustomIntegerMaskFormatter extends MaskFormatter {

    private static final long serialVersionUID = 1L;
    private String _emptyVal = null;

    public CustomIntegerMaskFormatter() {
        super();
    }

    public CustomIntegerMaskFormatter(String mask) throws ParseException {
        super(mask);
    }

    @Override
    public Object stringToValue(String value) throws ParseException {
        if (_emptyVal == null || !_emptyVal.equals(value)) {
            return super.stringToValue(value);
        }

        return null;
    }

    @Override
    public String valueToString(Object value) throws ParseException {
        String s = super.valueToString(value);
//        if (s != null) {
//            s = s.trim();
//        }
        return s;
    }

    @Override
    public void setPlaceholderCharacter(char placeholder) {
        try {
            super.setPlaceholderCharacter(placeholder);
            _emptyVal = super.valueToString(null);
        } catch (ParseException ex) {
            _emptyVal = null;
        }
    }

    @Override
    public void setMask(String mask) throws ParseException {
        super.setMask(mask);
        _emptyVal = super.valueToString(null);
    }
}
