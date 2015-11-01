package org.nailedtothex.swingirc;

import org.apache.commons.lang3.time.DateFormatUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

public class MessageJTextPane extends JTextPane {

    public MessageJTextPane() {
        setEditable(false);
        getDocument().addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        setCaretPosition(e.getDocument().getLength());
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                });
    }

    private static String getTimeString() {
        return DateFormatUtils.ISO_TIME_NO_T_FORMAT.format(System.currentTimeMillis());
    }

    public void appendWithTime(String s) {
        try {
            getDocument().insertString(getDocument().getLength(),
                    getTimeString() + ": " + s + "\n", null);
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
