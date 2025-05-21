package com.example.tasklink;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.google.firebase.database.GenericTypeIndicator;

import java.util.*;

/**
 * Task 상세(읽기/수정) 화면
 */
public class TaskDetailActivity extends AppCompatActivity {

    private String projectId;
    private String taskId;

    private EditText etTitle, etDescription;
    private TextView tvDeadline, tvFile, tvAssignUser;

    // UID → MemberRoleModel
    private Map<String, MemberRoleModel> members = new HashMap<>();

    // 파일 선택용 런처
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

        // 1) Intent 에서 projectId, taskId 받기
        projectId = getIntent().getStringExtra("projectId");
        taskId    = getIntent().getStringExtra("taskId");
        if (projectId == null) {
            Toast.makeText(this, "프로젝트 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) 뷰 바인딩
        etTitle       = findViewById(R.id.task_detail_task_title);
        etDescription = findViewById(R.id.edit_description);
        tvDeadline    = findViewById(R.id.text_deadline);
        tvFile        = findViewById(R.id.text_file);
        tvAssignUser  = findViewById(R.id.text_assign_user);

        ImageButton btnBack     = findViewById(R.id.button_back);
        ImageButton btnChat     = findViewById(R.id.button_chat);
        ImageButton btnPickDate = findViewById(R.id.button_pick_date);
        ImageButton btnPickFile = findViewById(R.id.button_pick_file);

        // 뒤로가기
        btnBack.setOnClickListener(v -> saveAndExit());

        // 채팅 (아직 미구현)
        btnChat.setOnClickListener(v ->
                Toast.makeText(this, "채팅 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
        );


        // 날짜 선택
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dlg, y, m, d) -> tvDeadline.setText("📅 마감: " + y + "-" + (m+1) + "-" + d),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 파일 선택
        btnPickFile.setOnClickListener(v ->
                pickFileLauncher.launch("*/*")
        );

        // 3) 기존 Task 로드 또는 신규 모드
        if (taskId != null) {
            loadTask();
        } else {
            // 신규 생성
            tvAssignUser.setText("👤 담당자: 미정");
            tvDeadline   .setText("📅 마감일");
            tvFile       .setText("📁 파일");
        }
    }


    /** Firebase에서 데이터를 읽어 화면에 표시 */
    private void loadTask() {
        DatabaseReference taskRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId)
                .child("tasks")
                .child(taskId);

        // 1) TaskModel 본문
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                TaskModel task = snap.getValue(TaskModel.class);
                if (task == null) {
                    Toast.makeText(TaskDetailActivity.this, "데이터가 없습니다", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                // 읽기 전용 모드로 폼 채우기
                etTitle      .setText(task.getTaskTitle());
                etTitle      .setEnabled(false);
                etDescription.setText(task.getDescription());
                etDescription.setEnabled(false);
                tvDeadline   .setText(
                        task.getDeadline() != null ? task.getDeadline() : ""
                );
                tvFile       .setText(
                        task.getFileName() != null ? "📁 파일: " + task.getFileName() : "📁 파일"
                );

                // 2) members Map<String,MemberRoleModel> 로드
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
                            @Override public void onCancelled(@NonNull DatabaseError e) { /* no-op */ }
                        });
            }

            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskDetailActivity.this,
                        "로드 실패: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** members 맵을 TextView에 표시 */
    private void displayMembers() {
        if (members.isEmpty()) {
            tvAssignUser.setText("👤 담당자: 미정");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (MemberRoleModel m : members.values()) {
            sb.append("👤 ")
                    .append(m.nickname)
                    .append(" (").append(m.role).append(")\n");
        }
        tvAssignUser.setText(sb.toString().trim());
    }

    /** 수정 사항을 Firebase에 쓰고 화면 종료 */
    private void saveAndExit() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (members.isEmpty()) {
            // 최소값 하나 채워두기
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
            // 신규 생성
            String newKey = tasksRef.push().getKey();
            tasksRef.child(newKey).setValue(task)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        } else {
            // 기존 덮어쓰기
            tasksRef.child(taskId).setValue(task)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        }
    }
}
