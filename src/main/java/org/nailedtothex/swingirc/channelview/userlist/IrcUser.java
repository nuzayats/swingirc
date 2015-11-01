package org.nailedtothex.swingirc.channelview.userlist;

public class IrcUser {
    private String nick;
    private boolean op;

    public IrcUser(String nick) {
        this.nick = nick;
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean op) {
        this.op = op;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final IrcUser other = (IrcUser) obj;
        if ((this.nick == null) ? (other.nick != null) : !this.nick.equals(other.nick)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + (this.nick != null ? this.nick.hashCode() : 0);
        return hash;
    }
}
