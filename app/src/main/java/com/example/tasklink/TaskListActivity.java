package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;

import com.example.tasklink.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

public class TaskListActivity extends AppCompatActivity {
    private RecyclerView rvTasks;
    private TaskAdapter adapter;
    private List<TaskModel> taskList = new ArrayList<>();

    private TaskRepository taskRepo = new TaskRepository();
    private String projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        // 1) Intent에서 프로젝트 이름 받아오기
        projectName = getIntent().getStringExtra("projectName");

        // 2) RecyclerView 초기화
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, task -> {
            // 3) 각 Task 클릭 시 상세 화면으로 이동
            Intent intent = new Intent(this, TaskDetailActivity.class);
            intent.putExtra("projectName", projectName);
            intent.putExtra("taskId", task.id);
            startActivity(intent);
        });
        rvTasks.setAdapter(adapter);

        // 4) + 버튼 클릭 시 새 Task 생성 화면으로 이동
        FloatingActionButton fab = findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, TaskDetailActivity.class);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 5) 화면에 보일 때 데이터 리스너 등록
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
        // 6) 백그라운드 전환 시 리스너 해제
        taskRepo.detachListener(projectName);
    }
}
