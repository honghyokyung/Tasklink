package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

    /**
     * onCreate: Activity 생성 시 레이아웃 설정,
     * Intent로부터 projectName을 받아 RecyclerView와 Adapter를 초기화합니다.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasklist);

        // 프로젝트 식별자 가져오기
        projectName = getIntent().getStringExtra("projectName");

        // RecyclerView 설정
        rvTasks = findViewById(R.id.rvTasks);
        rvTasks.setLayoutManager(new LinearLayoutManager(this));

        // Adapter 생성 및 클릭 이벤트 처리
        adapter = new TaskAdapter(taskList, task -> {
            Intent i = new Intent(this, TaskDetailActivity.class);
            i.putExtra("projectName", projectName);
            i.putExtra("taskId", task.id);
            startActivity(i);
        });
        rvTasks.setAdapter(adapter);
    }

    /**
     * onResume: Activity가 화면에 표시될 때 TaskRepository에 리스너를 등록하여
     * 실시간으로 Task 데이터를 로드 및 변화 감지를 시작합니다.
     */
    @Override
    protected void onResume() {
        super.onResume();
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

    /**
     * onPause: Activity가 백그라운드로 전환될 때 등록된 리스너를 해제하여
     * 메모리 누수 및 중복 콜백을 방지합니다.
     */
    @Override
    protected void onPause() {
        super.onPause();
        taskRepo.detachListener(projectName);
    }
}
