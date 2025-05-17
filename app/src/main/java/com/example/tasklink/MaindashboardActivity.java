package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.ArrayList;
import java.util.List;

public class MaindashboardActivity extends AppCompatActivity {

    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private final List<ProjectModel> projectList = new ArrayList<>();
    private String currentEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ① 이 토스트가 보이면 onCreate 진입은 성공
        Toast.makeText(this, "Dashboard onCreate 진입", Toast.LENGTH_SHORT).show();

        setContentView(R.layout.maindashboard);
        Log.d("DBG", "Dashboard setContentView 호출됨");

        setContentView(R.layout.maindashboard);

        // 1) 로그인된 사용자 이메일
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentEmail = user.getEmail();

        // 2) RecyclerView + Adapter 세팅
        rvProjects = findViewById(R.id.rvProjects);
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        // ↘ adapter 생성자에는 Context + List만 넘깁니다.
        adapter = new ProjectAdapter(this, projectList);
        rvProjects.setAdapter(adapter);

        // 3) 새 프로젝트 생성 버튼
        findViewById(R.id.btn_add_project)
                .setOnClickListener(v ->
                        startActivity(new Intent(this, NewProjectCreateActivity.class))
                );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("projects");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                projectList.clear();
                for (DataSnapshot ds : snap.getChildren()) {
                    ProjectModel proj = ds.getValue(ProjectModel.class);
                    if (proj == null) continue;

                    boolean isOwner  = currentEmail.equals(proj.getOwnerEmail());
                    boolean isMember = proj.getMembers() != null
                            && proj.getMembers().values().contains(currentEmail);
                    if (isOwner || isMember) {
                        proj.setId(ds.getKey());
                        projectList.add(proj);
                    }
                }
                adapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(MaindashboardActivity.this,
                        "불러오기 실패: " + err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
