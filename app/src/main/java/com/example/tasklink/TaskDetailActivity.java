package com.example.tasklink;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView textViewChatName;
    private EditText editTextMessage;
    private ImageButton buttonSend;

    private String projectName, taskTitle;
    private DatabaseReference chatRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail);

        // 전달받은 값
        projectName = getIntent().getStringExtra("projectName");
        taskTitle = getIntent().getStringExtra("taskTitle");

        textViewChatName = findViewById(R.id.textViewChatName);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);

        // Chat 노드 경로: /chats/{projectName}/{taskTitle}/
        chatRef = FirebaseDatabase.getInstance()
                .getReference("chats")
                .child(projectName)
                .child(taskTitle);

        // 전송 버튼 처리
        buttonSend.setOnClickListener(v -> sendMessage());

        // 예시로 이름 출력 (로그인한 사용자 이름 활용 가능)
        textViewChatName.setText("max48");

        // 채팅 메시지 불러오기 등은 이후 구현
    }

    private void sendMessage() {
        String message = editTextMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        String sender = "max48"; // 실제 앱에서는 사용자 정보에서 가져오기
        ChatMessageModel msg = new ChatMessageModel(sender, message);

        chatRef.push().setValue(msg)
                .addOnSuccessListener(aVoid -> {
                    editTextMessage.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "전송 실패", Toast.LENGTH_SHORT).show();
                });
    }
}
