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

import java.util.ArrayList;
import java.util.List;

public class TaskSettingActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private EditText editTextTaskTitle;
    private EditText editTextMember;
    private Button btnAddMember, btnSaveTask;
    private LinearLayout layoutMemberRoles;

    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasksetting);

        projectName = getIntent().getStringExtra("projectName");

        tvProjectTitle = findViewById(R.id.tv_project_title);
        editTextTaskTitle = findViewById(R.id.editTextTaskTitle);
        editTextMember = findViewById(R.id.editTextMember);
        btnAddMember = findViewById(R.id.btn_add_member);
        btnSaveTask = findViewById(R.id.btn_done);
        layoutMemberRoles = findViewById(R.id.layout_member_roles);

        tvProjectTitle.setText(projectName + " Task 설정");

        btnAddMember.setOnClickListener(v -> {
            String memberName = editTextMember.getText().toString().trim();
            if (memberName.isEmpty()) {
                Toast.makeText(this, "멤버 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            View row = LayoutInflater.from(this).inflate(R.layout.item_member_role_row, layoutMemberRoles, false);
            EditText editMember = row.findViewById(R.id.edit_member);
            editMember.setText(memberName);
            layoutMemberRoles.addView(row);
            editTextMember.setText("");
        });

        btnSaveTask.setOnClickListener(v -> {
            String taskTitle = editTextTaskTitle.getText().toString().trim();
            if (taskTitle.isEmpty()) {
                Toast.makeText(this, "Task 제목을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            List<MemberRoleModel> memberList = new ArrayList<>();
            for (int i = 0; i < layoutMemberRoles.getChildCount(); i++) {
                View row = layoutMemberRoles.getChildAt(i);
                EditText editMember = row.findViewById(R.id.edit_member);
                EditText editRole = row.findViewById(R.id.edit_role);

                String member = editMember.getText().toString().trim();
                String role = editRole.getText().toString().trim();

                if (!member.isEmpty() && !role.isEmpty()) {
                    memberList.add(new MemberRoleModel(member, role));
                }
            }

            if (memberList.isEmpty()) {
                Toast.makeText(this, "멤버와 역할 정보를 하나 이상 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            TaskModel task = new TaskModel(taskTitle, memberList);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("tasks")
                    .child(projectName);
            String taskId = ref.push().getKey();
            ref.child(taskId).setValue(task)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Task 저장 완료!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}