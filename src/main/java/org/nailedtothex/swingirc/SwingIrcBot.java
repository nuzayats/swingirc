package org.nailedtothex.swingirc;

import org.apache.commons.lang3.StringUtils;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.io.UnsupportedEncodingException;

public class SwingIrcBot extends PircBot {

    private final SwingIrcJPanel swingircPanel;

    public SwingIrcBot(final String nickname, final SwingIrcJPanel swingircPanel) throws UnsupportedEncodingException {
        setAutoNickChange(true);
        setName(nickname);
        setEncoding("x-windows-iso2022jp");
        this.swingircPanel = swingircPanel;
    }

    @Override
    protected void handleLine(final String line) {
        super.handleLine(line);
        swingircPanel.addGlobalMessage(line);
    }

    @Override
    protected void onJoin(final String channel, final String sender, String login, String hostname) {
        if (StringUtils.equals(getNick(), sender)) {
            swingircPanel.updateComponentForIJoin(channel);
            return;
        }
        swingircPanel.updateComponentForSomeoneJoin(channel, sender);
    }

    @Override
    protected void onUserList(final String channel, final User[] users) {
        swingircPanel.updateComponentForUserList(channel, users);
    }

    @Override
    protected void onMessage(final String channel, final String sender, String login, String hostname, final String message) {
        swingircPanel.addChannelMessage(channel, sender, message);
    }

    @Override
    protected void onPart(final String channel, final String sender, String login, String hostname) {
        if (StringUtils.equals(getNick(), sender)) {
            swingircPanel.updateComponentForPartedChannel(channel);
            return;
        }
        swingircPanel.updateComponentForSomeoneParted(channel, sender);
    }

    @Override
    protected void onConnect() {
        swingircPanel.updateComponentForConnected();
    }

    @Override
    protected void onQuit(final String sourceNick, String sourceLogin, String sourceHostname, final String reason) {
        swingircPanel.updateComponentForQuitUser(sourceNick, reason);
    }

    @Override
    protected void onOp(String channel, String sourceNick, String sourceLogin, String sourceHostname,
            final String recipient) {
        swingircPanel.updateComponentForOp(channel, recipient);
    }

    @Override
    protected void onDeop(String channel, String sourceNick, String sourceLogin, String sourceHostname,
            String recipient) {
        swingircPanel.updateComponentForDeop(channel, recipient);
    }

    @Override
    protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
        swingircPanel.updateComponentForNickChange(oldNick, newNick);
    }

    @Override
    protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
        swingircPanel.updateComponentForTopic(channel, topic, setBy, date, changed);
    }
    
    @Override
    protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
        if(StringUtils.equals(recipientNick, getNick())){
            swingircPanel.updateComponentForKicked(channel, kickerNick, reason);
            return;
        }
        swingircPanel.updateComponentForSomeoneGetKicked(channel, kickerNick, recipientNick, reason);
    }
}
