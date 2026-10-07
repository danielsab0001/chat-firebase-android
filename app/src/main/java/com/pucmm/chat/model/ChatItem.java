package com.pucmm.chat.model;

import java.util.Date;

public class ChatItem {

    private final User user;
    private final String lastMessage;
    private final Date lastMessageTime;
    private final boolean lastMessageFromMe;
    private final long unreadCount;

    public ChatItem(User user, String lastMessage, Date lastMessageTime,
                    boolean lastMessageFromMe, long unreadCount) {
        this.user = user;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.lastMessageFromMe = lastMessageFromMe;
        this.unreadCount = unreadCount;
    }

    public User getUser() { return user; }
    public String getLastMessage() { return lastMessage; }
    public Date getLastMessageTime() { return lastMessageTime; }
    public boolean isLastMessageFromMe() { return lastMessageFromMe; }
    public long getUnreadCount() { return unreadCount; }
}