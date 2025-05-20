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
    private Spinner      spinnerMembers;
    private TextView     tvSelectedNick;
    private EditText     etRole, etTaskTitle;
    private Button       btnAddMember, btnSave;
    private LinearLayout layoutMemberRoles;
    private TextView     tvTaskTitle;

    private String projectId, projectTitle, taskId, taskTitle;
    private Map<String, MemberRoleModel> projectMembers = new LinkedHashMap<>();
    private Map<String, MemberRoleModel> selected = new LinkedHashMap<>();

    private DatabaseReference projectRef, taskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasksetting);

        projectId    = getIntent().getStringExtra("projectId");
        projectTitle = getIntent().getStringExtra("projectTitle");
        taskId       = getIntent().getStringExtra("taskId");
        taskTitle    = getIntent().getStringExtra("taskTitle");

        tvTaskTitle   = findViewById(R.id.tv_task_setting_title);
        etTaskTitle      = findViewById(R.id.editTextTaskTitle);
        spinnerMembers   = findViewById(R.id.spinner_members);
        tvSelectedNick   = findViewById(R.id.tv_selected_nick);
        etRole           = findViewById(R.id.edit_role);
        btnAddMember     = findViewById(R.id.btn_add_member);
        layoutMemberRoles= findViewById(R.id.layout_member_roles);
        btnSave          = findViewById(R.id.btn_done);

        tvTaskTitle.setText(taskTitle + "설정");

        projectRef = FirebaseDatabase.getInstance()
                .getReference("projects")
                .child(projectId);
        taskRef    = projectRef.child("tasks")
                .child(taskId != null ? taskId : "NEW");

        // 1) 프로젝트 멤버 로드
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
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMembers.setAdapter(adapter);
                    }
                    @Override public void onCancelled(@NonNull DatabaseError e) {}
                });

        // 2) Spinner 선택 시 닉네임 표시
        spinnerMembers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String email = (String)parent.getItemAtPosition(pos);
                // email → MemberRoleModel → nickname
                for (MemberRoleModel m: projectMembers.values()) {
                    if (m.email.equals(email)) {
                        tvSelectedNick.setText(m.nickname);
                        break;
                    }
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 3) 추가 버튼
        btnAddMember.setOnClickListener(v->{
            String email = (String)spinnerMembers.getSelectedItem();
            String nick  = tvSelectedNick.getText().toString();
            String role  = etRole.getText().toString().trim();
            if (role.isEmpty()) {
                etRole.setError("역할을 입력하세요"); return;
            }
            // email→UID 찾기
            String uid = null;
            for (Map.Entry<String,MemberRoleModel> e: projectMembers.entrySet()) {
                if (e.getValue().email.equals(email)) {
                    uid = e.getKey(); break;
                }
            }
            if (uid==null || selected.containsKey(uid)) {
                Toast.makeText(this, "이미 추가되었거나 유효하지 않은 멤버", Toast.LENGTH_SHORT).show();
                return;
            }
            selected.put(uid, new MemberRoleModel(email,nick,role));
            redrawSelected();
            etRole.setText("");
        });

        // 4) 편집 모드라면 기존 task 로드해서 selected 초기화...
        if (taskId!=null) {
            taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override public void onDataChange(@NonNull DataSnapshot snap) {
                    TaskModel t = snap.getValue(TaskModel.class);
                    if (t!=null && t.getMembers()!=null) {
                        selected.putAll(t.getMembers());
                        etTaskTitle.setText(t.getTaskTitle());
                        redrawSelected();
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError e) {}
            });
        }

        // 5) 저장
        btnSave.setOnClickListener(v->{
            String newTitle = etTaskTitle.getText().toString().trim();
            if (newTitle.isEmpty()) {
                etTaskTitle.setError("제목 필요"); return;
            }
            Map<String,Object> updates = new HashMap<>();
            updates.put("taskTitle", newTitle);
            updates.put("members", selected);

            DatabaseReference ref = taskRef;
            if (taskId==null) {
                ref = projectRef.child("tasks").push();
            }
            ref.updateChildren(updates)
                    .addOnSuccessListener(a-> {
                        Toast.makeText(this,"저장 완료",Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e->
                            Toast.makeText(this,"실패: "+e.getMessage(),Toast.LENGTH_SHORT).show()
                    );
        });
    }

    private void redrawSelected() {
        // 1) 헤더 추가
        View header = LayoutInflater.from(this)
                .inflate(R.layout.item_member_role_header, layoutMemberRoles, false);
        layoutMemberRoles.addView(header);

        // 2) 실제 멤버 행들
        for (Map.Entry<String, MemberRoleModel> e : selected.entrySet()) {
            String uid = e.getKey();
            MemberRoleModel m = e.getValue();

            View row = LayoutInflater.from(this)
                    .inflate(R.layout.item_member_role_row_for_tasksetting, layoutMemberRoles, false);

            TextView tvNick = row.findViewById(R.id.edit_nickname_without_email);
            TextView tvRole = row.findViewById(R.id.edit_role_without_email);
            Button  btnDel  = row.findViewById(R.id.btn_delete_member);

            tvNick.setText(m.nickname);
            tvRole.setText(m.role);

            // 3) 삭제 버튼 리스너
            btnDel.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle("멤버 삭제")
                    .setMessage(m.nickname + "님을 이 Task에서 정말 삭제하시겠습니까?")
                    .setPositiveButton("예", (dlg, which) -> {
                        // 4) UI에서 제거
                        selected.remove(uid);
                        redrawSelected();

                        // 5) DB에서도 바로 삭제
                        DatabaseReference memberRef = FirebaseDatabase.getInstance()
                                .getReference("projects")
                                .child(projectId)
                                .child("tasks")
                                .child(taskId)
                                .child("members")
                                .child(uid);

                        memberRef.removeValue()
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e2 ->
                                        Toast.makeText(this, "삭제 실패: " + e2.getMessage(), Toast.LENGTH_SHORT).show()
                                );
                    })
                    .setNegativeButton("아니오", null)
                    .show()
            );

            layoutMemberRoles.addView(row);
        }
    }
}

