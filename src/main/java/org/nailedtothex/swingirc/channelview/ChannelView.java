package org.nailedtothex.swingirc.channelview;

public interface ChannelView {
    void updateComponentForMessage(String channel, String nick, String msg);
    void updateComponentForSomeoneQuit(String nick, String reason);
    void updateComponentForSomeoneParted(String channel, String nick);
    void updateComponentForSomeoneJoined(String channel, String nick);
    void updateComponentForSomeoneKicked(String channel, String kicker, String recipient, String reason);
    void updateComponentForGetJoined(String channel);
    void updateComponentForGetParted(String channel);
    void updateComponentForGetKicked(String channel);
    void updateComponentForOp(String channel, String nick);
}
