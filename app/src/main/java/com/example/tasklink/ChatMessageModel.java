package com.example.tasklink;

public class ChatMessageModel {
    public String sender;
    public String message;

    public ChatMessageModel() {}

    public ChatMessageModel(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }
}
