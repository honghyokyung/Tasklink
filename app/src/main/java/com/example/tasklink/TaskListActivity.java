package com.example.tasklink;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasklink.repository.TaskRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private final List<TaskModel> taskList = new ArrayList<>();
    private final TaskRepository taskRepo = new TaskRepository();
    private String projectName;
    private String projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        projectName = getIntent().getStringExtra("projectTitle");
        projectId   = getIntent().getStringExtra("projectId");

        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // 인터페이스 구현체를 익명 클래스 형태로 넘겨줍니다
        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskClick(TaskModel task) {
                Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("taskId",    task.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskSetting(TaskModel task) {
                // 설정(Setting) 버튼 클릭 시
                Intent intent = new Intent(TaskListActivity.this, TaskSettingActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("taskId", task.getId());
                intent.putExtra("taskTitle", task.getTaskTitle());
                startActivity(intent);
            }

            @Override
            public void onTaskChat(TaskModel task) {
                // 설정(Setting) 버튼 클릭 시
                Intent intent = new Intent(TaskListActivity.this, TaskChatActivity.class);
                intent.putExtra("projectId", projectId);
                intent.putExtra("taskId", task.getId());
                intent.putExtra("taskTitle", task.getTaskTitle());
                startActivity(intent);
            }

            public void onTaskDelete(TaskModel task) {
                new AlertDialog.Builder(TaskListActivity.this)
                        .setTitle("Task 삭제")
                        .setMessage("정말 이 Task를 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (dialog, which) -> {
                            // 실제 삭제
                            FirebaseDatabase.getInstance()
                                    .getReference("projects")
                                    .child(projectId)
                                    .child("tasks")
                                    .child(task.getId())
                                    .removeValue()
                                    .addOnSuccessListener(a -> {
                                        Toast.makeText(TaskListActivity.this,
                                                "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(TaskListActivity.this,
                                                "삭제 실패: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("취소", null)
                        .show();
            }
        });

        rvTasks.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.btn_add_task);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskSettingActivity.class);
            intent.putExtra("projectId",   projectId);
            intent.putExtra("projectTitle", projectName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        taskRepo.attachListener(projectId, new TaskRepository.OnTasksChanged() {
            @Override
            public void onLoaded(List<TaskModel> tasks) {
                taskList.clear();
                taskList.addAll(tasks);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onError(String message) {
                Toast.makeText(TaskListActivity.this,
                        "불러오기 실패: " + message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        taskRepo.detachListener();
    }
}
