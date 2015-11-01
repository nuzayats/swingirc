package org.nailedtothex.swingirc;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class SwingIrcPreferences {

    private static final String NAME = "name";
    private static final String SERVER = "server";
    private static final String PORT = "port";
    private static final String CHANNELS = "channels";
    private static final SwingIrcPreferences instance = new SwingIrcPreferences();
    private final Preferences prefs = Preferences.userNodeForPackage(SwingIrcPreferences.class);

    private SwingIrcPreferences() {
    }

    public synchronized static SwingIrcPreferences getInstance() {
        return instance;
    }

    public Set<String> getChannelSet() {
        final Preferences channels = prefs.node(CHANNELS);
        final Set<String> set = Sets.newLinkedHashSet();
        try {
            for (final String key : channels.keys()) {
                set.add(key);
            }
            return set;
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
    }

    public String getServer() {
        return prefs.get(SERVER, "localhost");
    }

    public int getPort() {
        return prefs.getInt(PORT, 6667);
    }

    public String getName() {
        return prefs.get(NAME, "mybot");
    }

    public void setServer(final String server) {
        prefs.put(SERVER, server);
    }

    public void setPort(final int port) {
        prefs.putInt(PORT, port);
    }

    public void setName(final String name) {
        prefs.put(NAME, name);
    }

    public void setChannels(final Set<String> newChannels) {
        final Preferences channels = prefs.node(CHANNELS);
        try {
            channels.clear();
        } catch (BackingStoreException ex) {
            throw new RuntimeException(ex);
        }
        for (final String s : newChannels) {
            channels.put(s, StringUtils.EMPTY);
        }
    }

    public void sync() throws BackingStoreException {
        try {
            prefs.sync();
        } catch (BackingStoreException e) {
            throw new RuntimeException(e);
        }
    }
}
