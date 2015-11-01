package org.nailedtothex.swingirc.channelview;

import org.nailedtothex.swingirc.channelview.log.ChannelLog;
import org.nailedtothex.swingirc.channelview.userlist.ChannelUserList;

public interface ChannelViewComponent {
    ChannelLog getChannelLog();
    ChannelUserList getChannelUserList();
}
