package com.example.tasklink;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.gson.Gson;

import java.util.HashMap;

public class NewProjectCreateActivity extends AppCompatActivity {
    private static final String TAG = "DBG";

    private EditText etTitle;
    private Button   btnCreate;

    private DatabaseReference projectsRef;
    private String currentUserEmail;
    private String currentUserNickname = "";    // 기본값 설정
    private String currentUserUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_project);

        // Firebase Realtime DB “projects” 루트 참조
        projectsRef = FirebaseDatabase.getInstance()
                .getReference("projects");
        currentUserUid   = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // 뷰 바인딩
        etTitle   = findViewById(R.id.et_project_title);
        btnCreate = findViewById(R.id.btn_create_project);
        btnCreate.setEnabled(false); // 닉네임 로드 완료 전까지 비활성화

        // 현재 사용자의 닉네임 미리 읽어두기
        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUserUid)
                .child("nickname")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        String nick = snap.getValue(String.class);
                        currentUserNickname = (nick != null ? nick : "");
                        Log.d(TAG, "Loaded nickname = " + currentUserNickname);
                        btnCreate.setEnabled(true);  // 로드 완료 후 버튼 활성화
                    }
                    @Override public void onCancelled(@NonNull DatabaseError err) {
                        Log.w(TAG, "Failed to load nickname: " + err.getMessage());
                        // 그래도 진행 가능하게...
                        btnCreate.setEnabled(true);
                    }
                });

        // 클릭 시 생성
        btnCreate.setOnClickListener(v -> createProject());
    }

    private void createProject() {
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            etTitle.setError("프로젝트명을 입력하세요");
            return;
        }

        // 1) push()로 새 프로젝트 키 생성
        DatabaseReference newProjRef = projectsRef.push();
        Log.d(TAG, "createProject will write to: " + newProjRef.toString());

        // 2) 모델 객체 준비
        ProjectModel proj = new ProjectModel();
        proj.title      = title;
        proj.ownerEmail = currentUserEmail;
        proj.members    = new HashMap<>();
        // 소유자도 멤버로 추가 (email, nickname, role)
        proj.members.put(
                currentUserUid,
                new MemberRoleModel(
                        currentUserEmail,
                        currentUserNickname,
                        "Owner"
                )
        );

        // 3) Gson 으로 payload 찍어보기
        String jsonPayload = new Gson().toJson(proj);
        Log.d(TAG, "생성 요청 payload = " + jsonPayload);

        // 4) 실제로 Firebase에 쓰기
        newProjRef.setValue(proj)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "프로젝트 생성 완료", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG,
                            "Permission denied writing to: " + newProjRef.toString() +
                                    "   (root: " + FirebaseDatabase.getInstance()
                                    .getReference().toString() + ")"
                    );
                    Toast.makeText(this,
                            "생성 실패: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
