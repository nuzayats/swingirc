package org.nailedtothex.swingirc.channellist;

import org.apache.commons.lang3.StringUtils;

public class Channel implements Comparable<Channel> {
    private final String name;
    private String topic;
    private Integer users;

    public Channel(String name) {
        this(name, StringUtils.EMPTY, null);
    }

    public Channel(String name, String topic, Integer users) {
        this.name = name;
        this.topic = topic;
        this.users = users;
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Integer getUsers() {
        return users;
    }

    public void setUsers(Integer users) {
        this.users = users;
    }

    private String getUsersString(){
        return (users != null) ? String.format(" %d users", users) : StringUtils.EMPTY;
    }
    
    @Override
    public String toString() {
        return String.format("%s %s%s", name, topic, getUsersString());
    }

    @Override
    public int compareTo(Channel o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Channel other = (Channel) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
