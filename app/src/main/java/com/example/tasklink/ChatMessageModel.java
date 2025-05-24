package com.example.tasklink;

public class ChatMessageModel {
    private String senderId;
    private String message;
    private long timestamp;
    private String fileUrl;
    private String fileName;

    public ChatMessageModel() {}

    public ChatMessageModel(String senderId, String message, long timestamp, String fileUrl, String fileName) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
        this.fileUrl = fileUrl;
        this.fileName = fileName;
    }

    public String getSenderId() { return senderId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public String getFileUrl() { return fileUrl; }
    public String getFileName() { return fileName; }

    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}
