package org.nailedtothex.swingirc.channellist;

import javax.swing.*;

public class ChannelListImpl extends JList implements ChannelList {
    private final ChannelListModelImpl model = new ChannelListModelImpl();
    
    public ChannelListImpl(){
        super();
        setModel(model);
    }

    @Override
    public ChannelListModel getChannelListModel() {
        return model;
    }
}
