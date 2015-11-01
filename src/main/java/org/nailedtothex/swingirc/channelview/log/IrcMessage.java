package org.nailedtothex.swingirc.channelview.log;

public class IrcMessage {
    private final String stringValue;

    public IrcMessage(String stringValue) {
        this.stringValue = stringValue;
    }

    public String getStringValue() {
        return stringValue;
    }
}