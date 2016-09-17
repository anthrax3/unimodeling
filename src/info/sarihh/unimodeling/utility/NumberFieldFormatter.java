package info.sarihh.unimodeling.utility;

import javax.swing.text.AttributeSet;
import javax.swing.text.DocumentFilter;

/**
 * This class restricts input in a JFormattedTextField to an integer that has a
 * specific number of digits.
 * Author: Sari Haj Hussein
 */
public class NumberFieldFormatter extends DocumentFilter {

    private int max;

    public NumberFieldFormatter(int max) {
        this.max = max;
    }

    /** This method is invoked prior to insertion of text into the specified
     * Document. */
    public final void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws javax.swing.text.BadLocationException {
        StringBuilder sb = new StringBuilder(text);
        for (int i = sb.length() - 1; i >= 0; i--) {
            int cp = sb.codePointAt(i);
            if (!Character.isDigit(cp) // Character is not digit ?
                    ) {
                sb.deleteCharAt(i);
                if (Character.isSupplementaryCodePoint(cp)) {
                    i--;
                    sb.deleteCharAt(i);
                }
            }
        }
        super.insertString(fb, offset, sb.toString(), attr);
    }

    /** This method is invoked prior to replacing a region of text in the
     * specified Document. */
    public final void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws javax.swing.text.BadLocationException {
        if (text != null) {
            StringBuilder sb = new StringBuilder(text);
            for (int i = sb.length() - 1; i >= 0; i--) {
                int cp = sb.codePointAt(i);
                if ((!Character.isDigit(cp)) // Character is not digit ?
                        && (cp != '-') // Character is not hyphen ?
                        ) {
                    sb.deleteCharAt(i);
                    if (Character.isSupplementaryCodePoint(cp)) {
                        i--;
                        sb.deleteCharAt(i);
                    }
                }
            }
            text = sb.toString();
        }
        if ((fb.getDocument().getLength() + text.length() - length) <= max) {
            super.replace(fb, offset, length, text, attr);
        } else {
            super.replace(fb, offset, length, text.substring(0, max - fb.getDocument().getLength()), attr);
        }
    }
}