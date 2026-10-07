package com.pucmm.chat.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class ChatSummary {

    private List<String> participants;
    private String lastMessage;
    private Date lastMessageTime;
    private String lastSenderId;
    private Map<String, Long> unread;

    public ChatSummary() {
    }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Date getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getLastSenderId() { return lastSenderId; }
    public void setLastSenderId(String lastSenderId) { this.lastSenderId = lastSenderId; }

    public Map<String, Long> getUnread() { return unread; }
    public void setUnread(Map<String, Long> unread) { this.unread = unread; }
}