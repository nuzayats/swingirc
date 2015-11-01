package org.nailedtothex.swingirc.channelview.userlist;

public interface ChannelUserListModel {
    void changeNick(String oldNick, String newNick);
    void deleteNick(String nick);
    void addNick(String nick);
    void op(String nick);
    void deOp(String nick);
}
