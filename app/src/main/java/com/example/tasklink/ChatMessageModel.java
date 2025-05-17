package com.example.tasklink;
// ChatActivity.java onCreate()
//String projectName = getIntent().getStringExtra("projectName");
//String taskId      = getIntent().getStringExtra("taskId");

/**DatabaseReference chatRef = FirebaseDatabase.getInstance()
        .getReference("chats")
        .child(projectName)
        .child(taskId);                       // 제목이 아니라 ID
 */
public class ChatMessageModel {
    public String sender;
    public String message;

    public ChatMessageModel() {}

    public ChatMessageModel(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }
}
