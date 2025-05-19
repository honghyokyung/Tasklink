package com.example.tasklink;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectSettingActivity extends AppCompatActivity {
    // --- 뷰 ---
    private EditText etName;
    private EditText etMemberEmail;
    private EditText etMemberRole;
    private Button btnAddMember;
    private Button btnSave;
    private Button btnDelete;
    private LinearLayout layoutMembers;

    // --- 데이터 ---
    private String projectId;
    private ProjectModel project;
    private String currentUid;
    private String currentEmail;

    // 모든 사용자 이메일→UID, 이메일→닉네임 맵
    private final Map<String, String> allUserUid  = new HashMap<>();

    // 현재 프로젝트의 멤버 리스트
    private final List<MemberRoleModel> memberList = new ArrayList<>();

    // Firebase
    private DatabaseReference projectsRef;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_setting);

        // 1) 인증 정보
        currentUid   = FirebaseAuth.getInstance().getCurrentUser().getUid();
        currentEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        // 2) Intent → projectId
        projectId = getIntent().getStringExtra("projectId");
        if (projectId == null) {
            finish();
            return;
        }

        // 3) Firebase 참조
        projectsRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId);
        usersRef    = FirebaseDatabase.getInstance()
                .getReference("users");

        // 4) 뷰 바인딩
        etName         = findViewById(R.id.et_project_name);
        etMemberEmail  = findViewById(R.id.et_member_email);
        etMemberRole   = findViewById(R.id.et_member_role);
        btnAddMember   = findViewById(R.id.btn_add_member);
        btnSave        = findViewById(R.id.btn_save_project);
        btnDelete      = findViewById(R.id.btn_delete_project);
        layoutMembers  = findViewById(R.id.layout_members);

        // 5) 프로젝트 데이터 로드
        loadProject();

        // 6) 멤버 추가
        btnAddMember.setOnClickListener(v -> {
            String email = etMemberEmail.getText().toString().trim();
            String role  = etMemberRole .getText().toString().trim();
            if (email.isEmpty()) {
                etMemberEmail.setError("이메일을 입력하세요");
                return;
            }
            if (role.isEmpty()) {
                etMemberRole.setError("역할을 입력하세요");
                return;
            }
            // 중복 확인
            for (MemberRoleModel m : memberList) {
                if (m.email.equals(email)) {
                    Toast.makeText(this, "이미 추가된 멤버입니다", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            // users/{uid}/email 필드로 조회
            usersRef.orderByChild("email").equalTo(email)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snap) {
                            if (!snap.exists()) {
                                Toast.makeText(ProjectSettingActivity.this,
                                        "등록된 사용자가 아닙니다", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            DataSnapshot userSnap = snap.getChildren().iterator().next();
                            String uid  = userSnap.getKey();
                            String nick = userSnap.child("nickname").getValue(String.class);
                            // 리스트에 추가
                            MemberRoleModel m = new MemberRoleModel(email,
                                    nick != null ? nick : email,
                                    role);
                            memberList.add(m);
                            allUserUid.put(email, uid);
                            refreshMemberViews();
                            etMemberEmail.setText("");
                            etMemberRole .setText("");
                        }
                        @Override public void onCancelled(@NonNull DatabaseError err) {
                            Toast.makeText(ProjectSettingActivity.this,
                                    "사용자 조회 중 오류", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // 7) 저장
        btnSave.setOnClickListener(v -> saveProject());

        // 8) 삭제 (확인 다이얼로그)
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("프로젝트 삭제")
                    .setMessage("정말 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (d,w) -> deleteProject())
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    private void loadProject() {
        projectsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                project = snap.getValue(ProjectModel.class);
                if (project == null) return;
                // 제목
                etName.setText(project.getTitle());
                // 멤버 리스트
                memberList.clear();
                allUserUid.clear();
                if (project.getMembers() != null) {
                    for (Map.Entry<String, MemberRoleModel> e
                            : project.getMembers().entrySet()) {
                        String uid = e.getKey();
                        MemberRoleModel m = e.getValue();
                        memberList.add(m);
                        allUserUid.put(m.email, uid);
                    }
                }
                refreshMemberViews();
                // 오너가 아니면 비활성화
                boolean isOwner = project.getOwnerEmail().equals(currentEmail);
                etName        .setEnabled(isOwner);
                etMemberEmail .setEnabled(isOwner);
                etMemberRole  .setEnabled(isOwner);
                btnAddMember  .setEnabled(isOwner);
                btnSave       .setEnabled(isOwner);
                btnDelete     .setEnabled(isOwner);
            }
            @Override public void onCancelled(@NonNull DatabaseError err) { }
        });
    }

    private void refreshMemberViews() {
        layoutMembers.removeAllViews();
        for (MemberRoleModel m : memberList) {
            TextView tv = new TextView(this);
            tv.setText("👤 " + m.email
                    + " (" + m.nickname + ")  /  " + m.role);
            layoutMembers.addView(tv);
        }
    }

    private void saveProject() {
        String newName = etName.getText().toString().trim();
        if (newName.isEmpty()) {
            etName.setError("프로젝트명을 입력하세요");
            return;
        }

        // 1) 제목 업데이트
        projectsRef.child("title")
                .setValue(newName)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "제목 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        // 2) members 맵 생성
        Map<String, MemberRoleModel> map = new HashMap<>();
        for (MemberRoleModel m : memberList) {
            String uid = allUserUid.get(m.email);
            if (uid != null) {
                map.put(uid, m);
            }
        }

        // 3) 멤버들만 별도로 쓰기
        projectsRef.child("members")
                .setValue(map)
                .addOnSuccessListener(a ->
                        Toast.makeText(this, "멤버 정보가 업데이트되었습니다", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "멤버 저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void deleteProject() {
        projectsRef.removeValue()
                .addOnSuccessListener(a -> {
                    Toast.makeText(this,
                            "삭제되었습니다",Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                        "삭제 실패: "+e.getMessage(),Toast.LENGTH_SHORT).show());
    }
}
