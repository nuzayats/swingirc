package org.nailedtothex.swingirc.channellist;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.swing.*;
import java.util.List;
import java.util.Map;

class ChannelListModelImpl extends AbstractListModel implements ChannelListModel {

    private final List<Channel> channels = Lists.newArrayList();
    private final Map<String, Channel> channelMap = Maps.newHashMap();

    @Override
    public synchronized int getSize() {
        return channels.size();
    }

    @Override
    public synchronized Object getElementAt(int index) {
        return channels.get(index);
    }

    @Override
    public synchronized void addChannel(String channel) {
        assert !channelMap.containsKey(channel);
        final Channel newChannel = new Channel(channel);
        channels.add(newChannel);
        channelMap.put(channel, newChannel);
        final int index = channels.size() - 1;
        fireIntervalAdded(this, index, index);
    }

    @Override
    public synchronized void removeChannel(String channel) {
        final int index = channels.indexOf(channelMap.remove(channel));
        channels.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    @Override
    public synchronized void updateChannel(String channel, String topic) {
        System.out.println("update: " + channel + topic);
        final Channel c = channelMap.get(channel);
        c.setTopic(topic);
        final int index = channels.indexOf(c);
        fireContentsChanged(this, index, index);
    }

    @Override
    public synchronized void updateChannel(String channel, int users) {
        final Channel c = channelMap.get(channel);
        c.setUsers(users);
        final int index = channels.indexOf(c);
        fireContentsChanged(this, index, index);
    }

    @Override
    public void incrementUsers(String channel) {
        final Channel c = channelMap.get(channel);
        c.setUsers(c.getUsers()+1);
        final int index = channels.indexOf(c);
        fireContentsChanged(this, index, index);
    }

    @Override
    public void decrementUsers(String channel) {
        final Channel c = channelMap.get(channel);
        c.setUsers(c.getUsers()-1);
        final int index = channels.indexOf(c);
        fireContentsChanged(this, index, index);
    }
}
