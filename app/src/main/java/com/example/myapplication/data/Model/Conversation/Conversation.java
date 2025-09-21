package com.example.myapplication.data.Model.Conversation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Conversation {
    public String id;

    public String name;
    public String guest_id;
    public String host_id;
    public List<Message> messages;
    public String avatar_url;
    public Conversation() {}

    public Conversation(String name, String guest_id, String host_id, String image_url) {
        this.id = UUID.randomUUID().toString();
        this.guest_id = guest_id;
        this.name = name;
        this.host_id = host_id;
        this.messages = new ArrayList<>();
        this.avatar_url = image_url;
    }
}
