package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasklink.repository.TaskRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private final List<TaskModel> taskList = new ArrayList<>();
    private final TaskRepository taskRepo = new TaskRepository();
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        // 1) Intent에서 받은 프로젝트 이름
        projectName = getIntent().getStringExtra("projectName");

        // 2) RecyclerView 초기화
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, task -> {
            // 3) Task 클릭 시 상세 화면으로 이동 (TaskDetailActivity 정의에 맞춰 key 조정)
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("projectName", projectName);
            intent.putExtra("taskTitle", task.getTaskTitle());
            startActivity(intent);
        });
        rvTasks.setAdapter(adapter);

        // 4) + 버튼 클릭 → TaskSettingActivity 로
        FloatingActionButton fab = findViewById(R.id.btn_add_task);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, TaskSettingActivity.class);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 5) 리스너 등록: 변경사항 실시간 반영
        taskRepo.attachListener(projectName, new TaskRepository.OnTasksChanged() {
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
        // 6) 리스너 해제
        taskRepo.detachListener(projectName);
    }
}
