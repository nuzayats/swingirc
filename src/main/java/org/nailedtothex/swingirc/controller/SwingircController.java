package org.nailedtothex.swingirc.controller;

public interface SwingircController {
    void updateComponentForMessage(String channel, String nick, String msg);
    void updateComponentForQuit(String nick, String reason);
    void updateComponentForPart(String channel, String nick);
    void updateComponentForJoin(String channel, String nick);
    void updateComponentForOp(String channel, String nick);    
}
