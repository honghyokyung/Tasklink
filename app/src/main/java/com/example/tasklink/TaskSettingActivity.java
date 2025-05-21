package com.example.tasklink;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.*;

public class TaskSettingActivity extends AppCompatActivity {
    private TextView     tvTaskTitle;
    private EditText     etTaskTitle;
    private Spinner      spinnerMembers;
    private TextView     tvSelectedNick;
    private EditText     etRole;
    private Button       btnAddMember, btnSave;
    private LinearLayout layoutMemberRoles;
    private Spinner      spinnerStatus;

    private String projectId, projectTitle, taskId, taskTitle;
    private Map<String, MemberRoleModel> projectMembers = new LinkedHashMap<>();
    private Map<String, MemberRoleModel> selected       = new LinkedHashMap<>();

    private DatabaseReference projectRef, taskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasksetting);

        // --- 1) Intent 데이터 ---
        projectId    = getIntent().getStringExtra("projectId");
        projectTitle = getIntent().getStringExtra("projectTitle");
        taskId       = getIntent().getStringExtra("taskId");
        taskTitle    = getIntent().getStringExtra("taskTitle");

        // --- 2) 뷰 바인딩 ---
        tvTaskTitle       = findViewById(R.id.tv_task_setting_title);
        etTaskTitle       = findViewById(R.id.editTextTaskTitle);
        spinnerMembers    = findViewById(R.id.spinner_members);
        tvSelectedNick    = findViewById(R.id.tv_selected_nick);
        etRole            = findViewById(R.id.edit_role);
        btnAddMember      = findViewById(R.id.btn_add_member);
        layoutMemberRoles = findViewById(R.id.layout_member_roles);
        spinnerStatus     = findViewById(R.id.spinner_status);
        btnSave           = findViewById(R.id.btn_done);

        // 제목 표시
        tvTaskTitle.setText((taskTitle != null ? taskTitle : "새 Task") + " 설정");

        // Firebase 레퍼런스
        projectRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId);
        // 임시 키 "NEW"는 편집이 아닐 때만 쓰입니다.
        taskRef = projectRef.child("tasks")
                .child(taskId != null ? taskId : "NEW");

        // --- 3) 상태 Spinner 초기화 ---
        List<String> statuses = Arrays.asList(
                "before start", "work in process", "task complete"
        );
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, statuses
        );
        statusAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );
        spinnerStatus.setAdapter(statusAdapter);

        // --- 4) 프로젝트 멤버 로드 ---
        projectRef.child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override public void onDataChange(@NonNull DataSnapshot snap) {
                        List<String> emails = new ArrayList<>();
                        for (DataSnapshot c : snap.getChildren()) {
                            MemberRoleModel m = c.getValue(MemberRoleModel.class);
                            if (m != null) {
                                projectMembers.put(c.getKey(), m);
                                emails.add(m.email);
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                TaskSettingActivity.this,
                                android.R.layout.simple_spinner_item,
                                emails
                        );
                        adapter.setDropDownViewResource(
                                android.R.layout.simple_spinner_dropdown_item
                        );
                        spinnerMembers.setAdapter(adapter);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) { }
                });

        // --- 5) 이메일 선택 시 닉네임 표시 ---
        spinnerMembers.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    @Override public void onItemSelected(
                            AdapterView<?> parent, View view, int pos, long id
                    ) {
                        String email = (String) parent.getItemAtPosition(pos);
                        for (MemberRoleModel m : projectMembers.values()) {
                            if (m.email.equals(email)) {
                                tvSelectedNick.setText(m.nickname);
                                break;
                            }
                        }
                    }
                    @Override public void onNothingSelected(AdapterView<?> parent) { }
                }
        );

        // --- 6) 멤버 추가 버튼 ---
        btnAddMember.setOnClickListener(v -> {
            String email = (String) spinnerMembers.getSelectedItem();
            String nick  = tvSelectedNick.getText().toString();
            String role  = etRole.getText().toString().trim();
            if (role.isEmpty()) {
                etRole.setError("역할을 입력하세요");
                return;
            }
            // email → UID 찾기
            String uid = null;
            for (Map.Entry<String, MemberRoleModel> e : projectMembers.entrySet()) {
                if (e.getValue().email.equals(email)) {
                    uid = e.getKey();
                    break;
                }
            }
            if (uid == null || selected.containsKey(uid)) {
                Toast.makeText(this,
                        "이미 추가되었거나 유효하지 않은 멤버",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }
            selected.put(uid, new MemberRoleModel(email, nick, role));
            redrawSelected();
            etRole.setText("");
        });

        // --- 7) 편집 모드 데이터 로드 (기존 Task) ---
        if (taskId != null) {
            taskRef.addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override public void onDataChange(@NonNull DataSnapshot snap) {
                            TaskModel t = snap.getValue(TaskModel.class);
                            if (t != null) {
                                // 제목
                                if (t.getTaskTitle() != null) {
                                    etTaskTitle.setText(t.getTaskTitle());
                                }
                                // 상태
                                if (t.getStatus() != null) {
                                    int pos = statusAdapter.getPosition(t.getStatus());
                                    if (pos >= 0) spinnerStatus.setSelection(pos);
                                }
                                // 담당자
                                if (t.getMembers() != null) {
                                    selected.putAll(t.getMembers());
                                    redrawSelected();
                                }
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError e) { }
                    }
            );
        }

        // --- 8) 저장 버튼 ---
        btnSave.setOnClickListener(v -> {
            String newTitle      = etTaskTitle.getText().toString().trim();
            String newStatus     = (String) spinnerStatus.getSelectedItem();
            if (newTitle.isEmpty()) {
                etTaskTitle.setError("제목 필요");
                return;
            }
            Map<String, Object> updates = new HashMap<>();
            updates.put("taskTitle", newTitle);
            updates.put("members", selected);
            updates.put("status", newStatus);

            DatabaseReference ref = (taskId == null)
                    ? projectRef.child("tasks").push()
                    : taskRef;

            ref.updateChildren(updates)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(this,
                                "저장 완료", Toast.LENGTH_SHORT
                        ).show();
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "저장 실패: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
        });
    }

    private void redrawSelected() {
        layoutMemberRoles.removeAllViews();
        // 헤더
        View header = LayoutInflater.from(this)
                .inflate(R.layout.item_member_role_header,
                        layoutMemberRoles, false);
        layoutMemberRoles.addView(header);

        // 각 멤버 행
        for (Map.Entry<String, MemberRoleModel> e
                : selected.entrySet()) {
            String uid = e.getKey();
            MemberRoleModel m = e.getValue();

            View row = LayoutInflater.from(this)
                    .inflate(
                            R.layout.item_member_role_row_for_tasksetting,
                            layoutMemberRoles, false
                    );

            TextView tvNick = row.findViewById(
                    R.id.edit_nickname_without_email
            );
            TextView tvRole = row.findViewById(
                    R.id.edit_role_without_email
            );
            Button btnDel  = row.findViewById(
                    R.id.btn_delete_member
            );

            tvNick.setText(m.nickname);
            tvRole.setText(m.role);

            btnDel.setOnClickListener(v ->
                    new AlertDialog.Builder(this)
                            .setTitle("멤버 삭제")
                            .setMessage(m.nickname + "님을 정말 삭제하시겠습니까?")
                            .setPositiveButton("예", (dlg, which) -> {
                                // UI & DB 삭제
                                selected.remove(uid);
                                redrawSelected();
                                projectRef.child("tasks")
                                        .child(taskId)
                                        .child("members")
                                        .child(uid)
                                        .removeValue();
                            })
                            .setNegativeButton("아니오", null)
                            .show()
            );

            layoutMemberRoles.addView(row);
        }
    }
}
