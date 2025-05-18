package com.example.tasklink;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TaskSettingActivity extends AppCompatActivity {

    private TextView    tvProjectTitle;
    private EditText    editTextTaskTitle;
    private EditText    editTextMember;
    private Button      btnAddMember, btnSaveTask;
    private LinearLayout layoutMemberRoles;

    // → 이제 projectId 와 projectTitle 둘 다 받습니다.
    private String projectId;
    private String projectTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasksetting);

        // ① Intent 에서 두 값을 꺼내고…
        projectId    = getIntent().getStringExtra("projectId");
        projectTitle = getIntent().getStringExtra("projectTitle");

        // ② 화면 바인딩
        tvProjectTitle    = findViewById(R.id.tv_project_title);
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextMember    = findViewById(R.id.editTextMember);
        btnAddMember      = findViewById(R.id.btn_add_member);
        btnSaveTask       = findViewById(R.id.btn_done);
        layoutMemberRoles = findViewById(R.id.layout_member_roles);

        // 화면에는 제목만 보여줍니다.
        tvProjectTitle.setText(projectTitle + " Task 설정");

        // '멤버 추가' 버튼
        btnAddMember.setOnClickListener(v -> {
            String memberInput = editTextMember.getText().toString().trim();
            if (memberInput.isEmpty()) {
                Toast.makeText(this, "멤버 이름(또는 이메일)을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }
            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_member_role_row, layoutMemberRoles, false);
            EditText etMember = row.findViewById(R.id.edit_email);
            etMember.setText(memberInput);
            layoutMemberRoles.addView(row);
            editTextMember.setText("");
        });

        // '저장' 버튼
        btnSaveTask.setOnClickListener(v -> {
            String taskTitle = editTextTaskTitle.getText().toString().trim();
            if (taskTitle.isEmpty()) {
                Toast.makeText(this, "Task 제목을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // 맵 형태로 멤버·역할 수집
            Map<String, MemberRoleModel> memberMap = new HashMap<>();
            for (int i = 0; i < layoutMemberRoles.getChildCount(); i++) {
                View row = layoutMemberRoles.getChildAt(i);
                EditText etEmail = row.findViewById(R.id.edit_email);
                EditText etNickname = row.findViewById(R.id.edit_nickname);
                EditText etRole   = row.findViewById(R.id.edit_role);

                String email    = etEmail.getText().toString().trim();
                String nickname = etNickname.getText().toString().trim();  // 닉네임 입력란이 따로 없으면 이메일로 대체
                String role     = etRole.getText().toString().trim();

                if (!email.isEmpty()&& !nickname.isEmpty() && !role.isEmpty()) {
                    String key = UUID.randomUUID().toString();
                    memberMap.put(key, new MemberRoleModel(email, nickname, role));
                }
            }

            if (memberMap.isEmpty()) {
                Toast.makeText(this, "이메일, 닉네임, 역할 정보를 하나 이상 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            // TaskModel 객체 생성
            TaskModel task = new TaskModel(taskTitle, memberMap);

            // → projectId 를 사용해서 정확한 경로로 씁니다!
            DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                    .getReference("projects")
                    .child(projectId)    // ← 여기 주의!
                    .child("tasks");

            String taskId = tasksRef.push().getKey();
            if (taskId == null) {
                Toast.makeText(this, "키 생성 실패", Toast.LENGTH_SHORT).show();
                return;
            }

            tasksRef.child(taskId)
                    .setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task 저장 완료!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "저장 실패: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
