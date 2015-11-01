package org.nailedtothex.swingirc.channellist;

public interface ChannelListModel {
    void addChannel(String channel);
    void updateChannel(String channel, String topic);
    void updateChannel(String channel, int users);
    void incrementUsers(String channel);
    void decrementUsers(String channel);
    void removeChannel(String channel);
}
