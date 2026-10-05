package com.pucmm.chat.model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {

    private String senderId;
    private String senderName;
    private String text;
    private Date timestamp;

    public Message() {
    }

    public Message(String senderId, String senderName, String text) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
    }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    @ServerTimestamp
    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}