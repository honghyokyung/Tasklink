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

    private String projectId;
    private String projectTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        // 1) Intent에서 projectId(푸시 키)와 projectTitle(화면용 이름) 꺼내기
        projectId    = getIntent().getStringExtra("projectId");
        projectTitle = getIntent().getStringExtra("projectTitle");
        if (projectId == null || projectTitle == null) {
            Toast.makeText(this, "프로젝트 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 2) 액션바 타이틀에 프로젝트명 표시 (선택사항)
        setTitle(projectTitle + " Tasks");

        // 3) RecyclerView + Adapter 세팅
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TaskAdapter(taskList, task -> {
            // Task 클릭 → 상세 화면으로 이동
            Intent detailI = new Intent(this, TaskDetailActivity.class);
            detailI.putExtra("projectId", projectId);
            detailI.putExtra("projectTitle", projectTitle);
            detailI.putExtra("taskId", task.getId());
            startActivity(detailI);
        });
        rvTasks.setAdapter(adapter);

        // 4) FAB 클릭 → TaskSettingActivity 로 이동
        FloatingActionButton fab = findViewById(R.id.btn_add_task);
        fab.setOnClickListener(v -> {
            Intent settingI = new Intent(this, TaskSettingActivity.class);
            settingI.putExtra("projectId", projectId);
            settingI.putExtra("projectTitle", projectTitle);
            startActivity(settingI);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 5) projectId로 리스너 등록
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
                        "불러오기 실패: " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 6) projectId로 리스너 해제
        taskRepo.detachListener();
    }
}
