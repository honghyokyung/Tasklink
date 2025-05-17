package com.example.tasklink;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class NewProjectCreateActivity extends AppCompatActivity {
    private EditText etTitle;
    private Button   btnCreate;

    private DatabaseReference projectsRef;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project); // 아래 XML 참고

        // Firebase 참조
        projectsRef = FirebaseDatabase.getInstance()
                .getReference("projects");
        currentUserEmail = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getEmail();

        etTitle   = findViewById(R.id.et_project_title);
        btnCreate = findViewById(R.id.btn_create_project);

        btnCreate.setOnClickListener(v -> createProject());
    }

    private void createProject() {
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("프로젝트명을 입력하세요");
            return;
        }

        // push()로 새 프로젝트 키 생성
        DatabaseReference newProjRef = projectsRef.push();
        String projectId = newProjRef.getKey();

        // 데이터 모델
        ProjectModel proj = new ProjectModel();
        proj.title      = title;
        proj.ownerEmail = currentUserEmail;
        proj.members    = new java.util.HashMap<>();
        // 소유자도 members에 등록해 두면 편리
        proj.members.put(
                FirebaseAuth.getInstance().getCurrentUser().getUid(),
                new MemberRoleModel(currentUserEmail, "Owner")
        );

        // tasks 맵은 빈 채로 시작
        newProjRef.setValue(proj)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "프로젝트 생성 완료", Toast.LENGTH_SHORT).show();
                    finish(); // 이전 화면(대시보드)로 복귀
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "생성 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
