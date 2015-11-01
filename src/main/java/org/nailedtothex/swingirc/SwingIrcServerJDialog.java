package org.nailedtothex.swingirc;

import org.apache.commons.lang3.math.NumberUtils;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingIrcServerJDialog extends JDialog {

    private final JTextField name = new JTextField();
    private final JTextField hostname = new JTextField();
    private final JTextField port = new JTextField();
    private final JTextField password = new JTextField();
    private final SwingIrcPreferences prefs = SwingIrcPreferences.getInstance();
    private boolean confirmed;

    public boolean isConfirmed(){
        return confirmed;
    }
    
    public String getNick(){
        return name.getText();
    }
    
    public String getHostname(){
        return hostname.getText();
    }
    
    public int getPort(){
        return NumberUtils.toInt(port.getText());
    }
    
    public String getPassword(){
        return password.getText();
    }
    
    public SwingIrcServerJDialog(Frame owner) {
        super(owner, true);
        setLocationByPlatform(true);
        
        name.setText(prefs.getName());
        name.setBorder(new TitledBorder("name"));
        hostname.setText(prefs.getServer());
        hostname.setBorder(new TitledBorder("hostname"));
        port.setText(Integer.toString(prefs.getPort()));
        port.setBorder(new TitledBorder("port"));
        password.setBorder(new TitledBorder("password"));

        ActionListener al = e -> {
            confirmed = true;
            setVisible(false);
        };

        name.addActionListener(al);
        
        final JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(name);
        panel.add(hostname);
        panel.add(port);
        panel.add(password);
        panel.add(new JButton("connect"){
            {
                addActionListener(al);
            }
        });
        add(panel);
        pack();
    }

    public static void main(String[] args) {
        SwingIrcServerJDialog d = new SwingIrcServerJDialog(null);
        d.setVisible(true);
        d.dispose();
    }
}
