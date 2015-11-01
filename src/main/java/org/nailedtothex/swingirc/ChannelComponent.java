package org.nailedtothex.swingirc;

import javax.swing.*;

public class ChannelComponent {

    public final MessageJTextPane messageJTextPane;
    public final DefaultListModel userListModel;

    public ChannelComponent(MessageJTextPane messageJTextPane, DefaultListModel userListModel) {
        this.messageJTextPane = messageJTextPane;
        this.userListModel = userListModel;
    }
}
