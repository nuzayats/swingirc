package org.nailedtothex.swingirc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.nailedtothex.swingirc.channellist.ChannelListImpl;
import org.nailedtothex.swingirc.channellist.ChannelListModel;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class SwingIrcJPanel extends JPanel {

    private static final Logger log = Logger.getLogger(SwingIrcJPanel.class.getName());
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MessageJTextPane globalTextPane = new MessageJTextPane();
    private final JTabbedPane centerNorthTabbedPane = new JTabbedPane();
    private final JTextField promptField = new JTextField();
    private final JLabel statusLabel = new JLabel("disconnected");
    private final Map<String, ChannelComponent> channelNameToChannelComponentMap = Maps.newConcurrentMap();
    private final BiMap<JSplitPane, String> tabComponentToChannelNameMap = HashBiMap.create();
    private JSplitPane selectedPane;
    private PircBot bot;
    private final ChannelListModel channelListModel;

    public SwingIrcJPanel() {
        super(new BorderLayout());

        promptField.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtils.isEmpty(promptField.getText())) {
                    return;
                }
                if (StringUtils.startsWith(promptField.getText(), "/")) {
                    bot.sendRawLineViaQueue(StringUtils.substring(promptField.getText(), 1));
                } else {
                    final String currentChannel = getSelectedChannelName();
                    if (currentChannel == null) {
                        return;
                    }
                    bot.sendMessage(currentChannel, promptField.getText());
                    getChannelComponentForChannelName(currentChannel).messageJTextPane.appendWithTime(">" + bot.getNick() + "< " + promptField.getText());
                }
                promptField.setText(StringUtils.EMPTY);
            }
        });

        add(promptField, BorderLayout.PAGE_START);
        final JSplitPane centerPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        add(centerPane, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.PAGE_END);

        //// center
        final JSplitPane centerSouthSplitPane = new JSplitPane();
        centerPane.setLeftComponent(centerNorthTabbedPane);
        centerPane.setRightComponent(centerSouthSplitPane);
        centerPane.setDividerLocation(400);

        ///// center-north
        centerNorthTabbedPane.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if (e.getSource() instanceof JTabbedPane) {
                    final JTabbedPane jtp = (JTabbedPane) e.getSource();
                    final int selectedIndex = jtp.getSelectedIndex();
                    final String title = jtp.getTitleAt(selectedIndex);
                    if (StringUtils.startsWith(title, "*")) {
                        jtp.setTitleAt(selectedIndex, StringUtils.substring(title, 1));
                    }
                    SwingIrcJPanel.this.selectedPane = (JSplitPane) jtp.getSelectedComponent();
                }
            }
        });

        ///// center-south
        final ChannelListImpl channelListImpl = new ChannelListImpl();
        this.channelListModel = channelListImpl.getChannelListModel();
        centerSouthSplitPane.setLeftComponent(new JScrollPane(globalTextPane));
        centerSouthSplitPane.setRightComponent(new JScrollPane(channelListImpl));
        centerSouthSplitPane.setDividerLocation(600);

        setupTabTraversalKeys(centerNorthTabbedPane, promptField);
    }

    public void partSelectedChannel() {
        bot.partChannel(getSelectedChannelName());
    }

    public void updateComponentForPartedChannel(final String channel) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                synchronized (tabComponentToChannelNameMap) {
                    final JSplitPane target = tabComponentToChannelNameMap.inverse().get(channel);
                    centerNorthTabbedPane.remove(target);
                    tabComponentToChannelNameMap.remove(target);
                }
                channelNameToChannelComponentMap.remove(channel);

                channelListModel.removeChannel(channel);
            }
        });
    }

    public void join(final String channel) {
        bot.joinChannel(channel);
    }

    public void updateComponentForIJoin(final String channel) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                channelListModel.addChannel(channel);
                final MessageJTextPane messagePane = new MessageJTextPane();
                final DefaultListModel userListModel = new DefaultListModel();
                final JList userList = new JList(userListModel);
                final JSplitPane channelPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                        new JScrollPane(messagePane), new JScrollPane(userList));
                channelPane.setDividerLocation(600);
                centerNorthTabbedPane.addTab(channel, channelPane);
                centerNorthTabbedPane.setSelectedComponent(channelPane);
                channelNameToChannelComponentMap.put(channel, new ChannelComponent(messagePane, userListModel));
                synchronized (tabComponentToChannelNameMap) {
                    tabComponentToChannelNameMap.put(channelPane, channel);
                }
            }
        });
    }

    public void updateComponentForSomeoneParted(final String channel, final String user) {
        addMessageToMessageBuffer(channel, user + " has left channel");
        updateComponentForRemoveUserActions(channel, user);
    }
    
    private void updateComponentForRemoveUserActions(final String channel, final String user){
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                final DefaultListModel listModel = getChannelComponentForChannelName(channel).userListModel;
                if (!(listModel.removeElement(user) || listModel.removeElement("@" + user))) {
                    assert false;
                }
                channelListModel.decrementUsers(channel);
            }
        });
    }

    public void updateComponentForSomeoneJoin(final String channel, final String user) {
        addMessageToMessageBuffer(channel, user + " has joined");
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                getChannelComponentForChannelName(channel).userListModel.addElement(user);
                channelListModel.incrementUsers(channel);
            }
        });
    }

    public void updateComponentForQuitUser(final String user, final String reason) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                for (final String channel : channelNameToChannelComponentMap.keySet()) {
                    final ChannelComponent c = channelNameToChannelComponentMap.get(channel);
                    if (c.userListModel.removeElement(user) || c.userListModel.removeElement("@" + user)) {
                        addMessageToMessageBuffer(channel, user + " has left IRC (" + reason + ")");
                        channelListModel.decrementUsers(channel);
                    }
                }
            }
        });
    }

    public void updateComponentForNickChange(final String oldNick, final String newNick) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                for (final String channel : channelNameToChannelComponentMap.keySet()) {
                    final ChannelComponent c = channelNameToChannelComponentMap.get(channel);

                    if (changeNick0(c.userListModel, oldNick, newNick) || changeNick0(c.userListModel, "@" + oldNick, "@" + newNick)) {
                        addMessageToMessageBuffer(channel, oldNick + " now known as " + newNick);
                    }
                }

                if (StringUtils.equals(newNick, bot.getNick())) {
                    updateComponentForConnected();
                }
            }

            private boolean changeNick0(final DefaultListModel model, final String oldNick, final String newNick) {
                final int index = model.indexOf(oldNick);
                if (index < 0) {
                    return false;
                }

                model.remove(index);
                model.add(index, newNick);
                return true;
            }
        });
    }

    public void addChannelMessage(final String channel, final String sender, final String message) {
        addMessageToMessageBuffer(channel, "<" + sender + "> " + message);
    }

    private void addMessageToMessageBuffer(final String channel, final String message) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                getChannelComponentForChannelName(channel).messageJTextPane.appendWithTime(message);

                if (!StringUtils.equals(getSelectedChannelName(), channel)) {

                    final JSplitPane target;
                    synchronized (tabComponentToChannelNameMap) {
                        target = tabComponentToChannelNameMap.inverse().get(channel);
                    }
                    final int index = centerNorthTabbedPane.indexOfComponent(target);
                    final String title = centerNorthTabbedPane.getTitleAt(index);
                    if (!StringUtils.startsWith(title, "*")) {
                        centerNorthTabbedPane.setTitleAt(index, "*" + title);
                    }
                }
            }
        });
    }

    public void updateComponentForUserList(final String channel, final User[] users) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                final DefaultListModel userListModel = channelNameToChannelComponentMap.get(channel).userListModel;
                userListModel.clear();
                for (final User user : users) {
                    userListModel.addElement(user.toString());
                }
                channelListModel.updateChannel(channel, users.length);
            }
        });
    }

    public void addGlobalMessage(final String message) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                globalTextPane.appendWithTime(message);
            }
        });
    }

    public void updateComponentForConnected() {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                statusLabel.setText("connected: " + bot.getNick() + "@" + bot.getServer());
            }
        });
    }

    public void updateComponentForOp(final String channel, final String user) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                final DefaultListModel model = getChannelComponentForChannelName(channel).userListModel;
                final int index = model.indexOf(user);
                if (index > -1) {
                    model.remove(index);
                    model.add(index, "@" + user);
                }
            }
        });
    }

    public void updateComponentForDeop(final String channel, final String user) {
        executeSwingEventSerial(new Runnable() {

            @Override
            public void run() {
                final DefaultListModel model = getChannelComponentForChannelName(channel).userListModel;
                final int index = model.indexOf("@" + user);
                if (index > -1) {
                    model.remove(index);
                    model.add(index, user);
                }
            }
        });
    }

    private void executeSwingEventSerial(final Runnable r) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    SwingUtilities.invokeAndWait(r);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private String getSelectedChannelName() {
        synchronized (tabComponentToChannelNameMap) {
            return tabComponentToChannelNameMap.get(selectedPane);
        }
    }

    private ChannelComponent getChannelComponentForChannelName(final String channel) {
        return channelNameToChannelComponentMap.get(channel);
    }

    public void connect(final String nickname, final String hostname, int port, String password)
            throws IrcException, NickAlreadyInUseException, IOException {
        bot = new SwingIrcBot(nickname, this);
        bot.connect(hostname, port, password);
    }

    public Set<String> getJoinedChannels() {
        return channelNameToChannelComponentMap.keySet();
    }

    private static void setupTabTraversalKeys(JTabbedPane tabbedPane, JComponent comp) {
        KeyStroke ctrlTab = KeyStroke.getKeyStroke("ctrl TAB");
        KeyStroke ctrlShiftTab = KeyStroke.getKeyStroke("ctrl shift TAB");
        KeyStroke ctrlPgdn = KeyStroke.getKeyStroke("ctrl PAGE_DOWN");
        KeyStroke ctrlPgup = KeyStroke.getKeyStroke("ctrl PAGE_UP");

        // Remove ctrl-tab from normal focus traversal
        removeForwardTraversalKey(tabbedPane, ctrlTab);
        removeForwardTraversalKey(comp, ctrlTab);

        // Remove ctrl-shift-tab from normal focus traversal
        removeBackwardTraversalKey(tabbedPane, ctrlShiftTab);
        removeBackwardTraversalKey(comp, ctrlShiftTab);

        // Add keys to the tab's input map
        InputMap inputMap = tabbedPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(ctrlTab, "navigateNext");
        inputMap.put(ctrlPgdn, "navigateNext");
        inputMap.put(ctrlShiftTab, "navigatePrevious");
        inputMap.put(ctrlPgup, "navigatePrevious");
    }

    private static void removeBackwardTraversalKey(JComponent c, KeyStroke k) {
        Set<AWTKeyStroke> backwardKeys = new HashSet<AWTKeyStroke>(c.getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS));
        backwardKeys.remove(k);
        c.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardKeys);
    }

    private static void removeForwardTraversalKey(JComponent c, KeyStroke k) {
        Set<AWTKeyStroke> forwardKeys = new HashSet<AWTKeyStroke>(c.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        forwardKeys.remove(k);
        c.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardKeys);
    }

    public void updateComponentForTopic(final String channel,
            final String topic, String setBy, long date, boolean changed) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                channelListModel.updateChannel(channel, topic);
            }
        });
    }

    public void updateComponentForKicked(final String channel, final String kicker, final String reason) {
        updateComponentForPartedChannel(channel);
    }

    public void updateComponentForSomeoneGetKicked(String channel, String kickerNick, String recipientNick, String reason) {
        addMessageToMessageBuffer(channel, String.format("%s get kicked by %s (%s)", recipientNick, kickerNick, reason));
        updateComponentForRemoveUserActions(channel, recipientNick);
    }
}
