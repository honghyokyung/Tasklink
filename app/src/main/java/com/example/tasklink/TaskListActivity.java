package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

public class TaskListActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private Button btnAddTask;
    private LinearLayout layoutTaskList;
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        tvProjectTitle = findViewById(R.id.tv_tasklist_project_title);
        btnAddTask = findViewById(R.id.btn_add_task);
        layoutTaskList = findViewById(R.id.layout_task_list);

        projectName = getIntent().getStringExtra("projectName");
        if (projectName != null) {
            tvProjectTitle.setText(projectName + " 프로젝트");
        }

        btnAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, TaskSettingActivity.class);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTasksFromFirebase();
    }

    private void loadTasksFromFirebase() {
        layoutTaskList.removeAllViews();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(projectName);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasValidTasks = false;

                for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                    TaskModel task = taskSnapshot.getValue(TaskModel.class);
                    if (task != null && task.taskTitle != null && task.members != null && !task.members.isEmpty()) {
                        addTaskCard(task);
                        hasValidTasks = true;
                    }
                }

                if (!hasValidTasks) {
                    TextView tvEmpty = new TextView(TaskListActivity.this);
                    tvEmpty.setText("생성된 Task가 없습니다. + 버튼을 눌러 새 Task를 추가해보세요.");
                    tvEmpty.setTextSize(16f);
                    layoutTaskList.addView(tvEmpty);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TaskListActivity.this, "Task 불러오기 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTaskCard(TaskModel task) {
        View card = getLayoutInflater().inflate(R.layout.item_task_card, layoutTaskList, false);

        TextView tvTitle = card.findViewById(R.id.tv_task_title);
        LinearLayout layoutMembers = card.findViewById(R.id.layout_task_members);
        Button btnViewDetail = card.findViewById(R.id.btn_view_detail);

        tvTitle.setText(task.taskTitle);

        for (MemberRoleModel member : task.members) {
            TextView memberView = new TextView(this);
            memberView.setText("담당자: " + member.name + " / 역할: " + member.role);
            memberView.setTextSize(14f);
            layoutMembers.addView(memberView);
        }

        btnViewDetail.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
            intent.putExtra("taskTitle", task.taskTitle);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        });

        layoutTaskList.addView(card);
    }
}
