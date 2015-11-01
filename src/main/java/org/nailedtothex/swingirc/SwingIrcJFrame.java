package org.nailedtothex.swingirc;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.prefs.BackingStoreException;

public class SwingIrcJFrame extends JFrame {

    private final SwingIrcPreferences prefs = SwingIrcPreferences.getInstance();
    private String name;
    private String hostname;
    private int port;

    public SwingIrcJFrame() {
        // exception handler
        Thread.setDefaultUncaughtExceptionHandler(
                new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread t, Throwable e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(SwingIrcJFrame.this, ExceptionUtils.getStackTrace(e), "Exception", JOptionPane.ERROR_MESSAGE);
                        System.exit(-1);
                    }
                });

        final SwingIrcJPanel sjp = new SwingIrcJPanel();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (SwingIrcJFrame.this.name != null)
                    prefs.setName(SwingIrcJFrame.this.name);
                if (SwingIrcJFrame.this.hostname != null)
                    prefs.setServer(SwingIrcJFrame.this.hostname);
                prefs.setPort(SwingIrcJFrame.this.port);
                if (sjp.getJoinedChannels() != null)
                    prefs.setChannels(sjp.getJoinedChannels());
                try {
                    prefs.sync();
                } catch (BackingStoreException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        // actions
        final Action joinAction = new AbstractAction("Join") {

            {
                putValue(MNEMONIC_KEY, KeyEvent.VK_J);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                final String channel = JOptionPane.showInputDialog(SwingIrcJFrame.this, "join to");
                if (StringUtils.isNotBlank(channel)) {
                    sjp.join(channel);
                }
            }
        };

        // actions
        final Action partAction = new AbstractAction("Part") {

            {
                putValue(MNEMONIC_KEY, KeyEvent.VK_P);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                sjp.partSelectedChannel();
            }
        };

        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(joinAction);
        fileMenu.add(partAction);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);

        addWindowListener(
                new WindowAdapter() {

                    @Override
                    public void windowOpened(WindowEvent e) {
                        try {
                            final SwingIrcServerJDialog d = new SwingIrcServerJDialog(SwingIrcJFrame.this);
                            d.setVisible(true);

                            SwingIrcJFrame.this.name = d.getNick();
                            SwingIrcJFrame.this.hostname = d.getHostname();
                            SwingIrcJFrame.this.port = d.getPort();
                            final String password = d.getPassword();
                            final boolean confirmed = d.isConfirmed();

                            d.dispose();

                            if (!confirmed) {
                                System.exit(-1);
                            }

                            sjp.connect(name, hostname, port, password);
                            for (final String channel : prefs.getChannelSet()) {
                                sjp.join(channel);
                            }
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                });
        add(sjp);
        pack();
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationByPlatform(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new SwingIrcJFrame().setVisible(true);
            }
        });
    }
}
