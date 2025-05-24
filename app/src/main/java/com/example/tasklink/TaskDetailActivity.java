package com.example.tasklink;

// 생략된 import 유지
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.NonNull;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

import java.util.*;

public class TaskDetailActivity extends AppCompatActivity {

    private String projectId;
    private String taskId;

    private EditText etTitle, etDescription;
    private TextView tvDeadline, tvFile, tvAssignUser;

    private Map<String, MemberRoleModel> members = new HashMap<>();

    private final ActivityResultLauncher<String> pickFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            String name = uri.getLastPathSegment();
                            tvFile.setText("📁 파일: " + name);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail);

        projectId = getIntent().getStringExtra("projectId");
        taskId    = getIntent().getStringExtra("taskId");
        if (projectId == null) {
            Toast.makeText(this, "프로젝트 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etTitle       = findViewById(R.id.task_detail_task_title);
        etDescription = findViewById(R.id.edit_description);
        tvDeadline    = findViewById(R.id.text_deadline);
        tvFile        = findViewById(R.id.text_file);
        tvAssignUser  = findViewById(R.id.text_assign_user);

        ImageButton btnBack     = findViewById(R.id.button_back);
        ImageButton btnChat     = findViewById(R.id.button_chat);
        ImageButton btnPickDate = findViewById(R.id.button_pick_date);
        ImageButton btnPickFile = findViewById(R.id.button_pick_file);

        btnBack.setOnClickListener(v -> saveAndExit());

        // ✅ 채팅 기능 이동: ChatActivity 실행
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailActivity.this, TaskChatActivity.class);
            intent.putExtra("projectId", projectId);
            intent.putExtra("taskId", taskId);
            startActivity(intent);
        });

        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dlg, y, m, d) -> tvDeadline.setText("📅 마감: " + y + "-" + (m+1) + "-" + d),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        btnPickFile.setOnClickListener(v -> pickFileLauncher.launch("*/*"));

        if (taskId != null) {
            loadTask();
        } else {
            tvAssignUser.setText("👤 담당자: 미정");
            tvDeadline   .setText("📅 마감일");
            tvFile       .setText("📁 파일");
        }
    }

    private void loadTask() {
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId)
                .child("tasks")
                .child(taskId);

        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                TaskModel task = snap.getValue(TaskModel.class);
                if (task == null) {
                    Toast.makeText(TaskDetailActivity.this, "데이터가 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                etTitle      .setText(task.getTaskTitle());
                etTitle      .setEnabled(false);
                etDescription.setText(task.getDescription());
                etDescription.setEnabled(false);
                tvDeadline   .setText(task.getDeadline() != null ? task.getDeadline() : "");
                tvFile       .setText(task.getFileName() != null ? "📁 파일: " + task.getFileName() : "📁 파일");

                taskRef.child("members")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override public void onDataChange(@NonNull DataSnapshot msnap) {
                                GenericTypeIndicator<Map<String, MemberRoleModel>> t =
                                        new GenericTypeIndicator<Map<String, MemberRoleModel>>() {};
                                Map<String, MemberRoleModel> loaded = msnap.getValue(t);
                                if (loaded != null) {
                                    members = loaded;
                                }
                                displayMembers();
                            }
                            @Override public void onCancelled(@NonNull DatabaseError e) {}
                        });
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskDetailActivity.this, "로드 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayMembers() {
        if (members.isEmpty()) {
            tvAssignUser.setText("👤 담당자: 미정");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (MemberRoleModel m : members.values()) {
            sb.append("👤 ").append(m.nickname).append(" (").append(m.role).append(")\n");
        }
        tvAssignUser.setText(sb.toString().trim());
    }

    private void saveAndExit() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (members.isEmpty()) {
            members.put("none", new MemberRoleModel("미정","미정","미정"));
        }

        TaskModel task = new TaskModel(
                title,
                members,
                tvDeadline.getText().toString(),
                tvFile.getText().toString(),
                etDescription.getText().toString(),
                ""
        );

        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId)
                .child("tasks");

        if (taskId == null) {
            String newKey = tasksRef.push().getKey();
            tasksRef.child(newKey).setValue(task)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            tasksRef.child(taskId).setValue(task)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
