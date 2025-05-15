package com.example.tasklink.repository;

import com.example.tasklink.firebase.FirebasePaths;
import com.google.firebase.database.ValueEventListener;

/**
 * chat관련 정보를 갖고오는 Repository
 * projectId와 taskId에 따른 chat을 갖고옴
 */
public class ChatRepository {
    public void loadChats(String projectId, String taskId, ValueEventListener listener) {
        FirebasePaths.chats(projectId, taskId)
                .addListenerForSingleValueEvent(listener);
    }
}