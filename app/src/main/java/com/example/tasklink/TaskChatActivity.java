package com.example.tasklink;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;
import com.example.tasklink.ChatMessageModel;


public class TaskChatActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText chatInput;
    private ImageButton sendButton;
    private List<ChatMessageModel> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private String currentUserId, projectId, taskId;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_chat);

        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        chatInput = findViewById(R.id.chatInput);
        sendButton = findViewById(R.id.sendButton);

        //currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "로그인 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        projectId = getIntent().getStringExtra("projectId");
        taskId = getIntent().getStringExtra("taskId");

        if (projectId == null || taskId == null) {
            Toast.makeText(this, "잘못된 접근입니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        chatAdapter = new ChatAdapter(chatList, currentUserId);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatRecyclerView.setAdapter(chatAdapter);

        chatRef = FirebaseDatabase.getInstance().getReference("projects")
                .child(projectId).child("tasks").child(taskId).child("chats");

        chatRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String prev) {
                ChatMessageModel msg = snapshot.getValue(ChatMessageModel.class);
                if (msg != null) {
                    chatList.add(msg);
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                    chatRecyclerView.scrollToPosition(chatList.size() - 1);
                }
            }
            @Override public void onChildChanged(DataSnapshot d, String p) {}
            @Override public void onChildRemoved(DataSnapshot d) {}
            @Override public void onChildMoved(DataSnapshot d, String p) {}
            @Override public void onCancelled(DatabaseError e) {}
        });

        sendButton.setOnClickListener(v -> {
            String msg = chatInput.getText().toString().trim();
            if (!msg.isEmpty()) {
                String key = chatRef.push().getKey();
                ChatMessageModel chat = new ChatMessageModel(
                        currentUserId, msg, System.currentTimeMillis(), null, null
                );
                chatRef.child(key).setValue(chat);
                chatInput.setText("");
            }
        });
    }
}