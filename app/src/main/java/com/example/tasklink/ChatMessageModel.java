package com.example.tasklink;

public class ChatMessageModel {
    private String senderId;
    private String message;
    private long timestamp;

    public ChatMessageModel() {
        // Firebase 사용을 위한 기본 생성자
    }

    public ChatMessageModel(String senderId, String message, long timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }
}