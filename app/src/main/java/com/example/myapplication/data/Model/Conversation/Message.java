package com.example.myapplication.data.Model.Conversation;

import java.util.Date;

public class Message {
    public String message_content;
    public String sender_id;
    public Date time;

    public Message() {}

    public Message(String message_content, String sender_id) {
        this.message_content = message_content;
        this.sender_id = sender_id;
        this.time = new Date();
    }
}
