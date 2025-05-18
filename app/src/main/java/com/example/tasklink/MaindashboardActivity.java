package com.example.tasklink;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.example.tasklink.ProjectAdapter.OnProjectActionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MaindashboardActivity extends AppCompatActivity {
    private TextView tvWelcome;
    private RecyclerView rvProjects;
    private ProjectAdapter adapter;
    private final List<ProjectModel> projectList = new ArrayList<>();
    private String currentEmail;
    private String currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maindashboard);

        // 뷰 바인딩
        tvWelcome      = findViewById(R.id.tv_dashboard_title);
        rvProjects     = findViewById(R.id.rvProjects);
        FloatingActionButton btnAdd = findViewById(R.id.btn_add_project);

        // 로그인 사용자 확인
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            finish();
            return;
        }
        currentUid   = user.getUid();
        currentEmail = user.getEmail();

        // 환영 메시지 로드
        DatabaseReference nickRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(currentUid)
                .child("nickname");
        nickRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                String nick = snap.getValue(String.class);
                tvWelcome.setText(nick != null ? nick + "님의 대시보드" : "대시보드");
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                tvWelcome.setText("대시보드");
                Toast.makeText(MaindashboardActivity.this,
                        "닉네임 로드 실패", Toast.LENGTH_SHORT).show();
            }
        });

        // RecyclerView 설정
        rvProjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter(this, projectList, new ProjectAdapter.OnProjectActionListener() {
            @Override
            public void onProjectClick(ProjectModel proj) {
                Intent i = new Intent(MaindashboardActivity.this, TaskListActivity.class);
                i.putExtra("projectId",   proj.getId());
                i.putExtra("projectTitle", proj.getTitle());
                startActivity(i);
            }
            @Override public void onDeleteClick(ProjectModel proj) {
                new AlertDialog.Builder(MaindashboardActivity.this)
                        .setTitle("프로젝트 삭제")
                        .setMessage("정말 삭제하시겠습니까?")
                        .setPositiveButton("삭제", (d, w) -> {
                            DatabaseReference delRef = FirebaseDatabase.getInstance()
                                    .getReference("projects")
                                    .child(proj.getId());  // ← currentUid 제거

                            delRef.removeValue()
                                    .addOnSuccessListener(a -> {
                                        projectList.remove(proj);
                                        adapter.notifyDataSetChanged();
                                        Toast.makeText(MaindashboardActivity.this,
                                                "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(MaindashboardActivity.this,
                                                "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("취소", (d, w) -> d.dismiss())
                        .show();
            }
        });
        rvProjects.setAdapter(adapter);

        // 새 프로젝트 버튼
        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, NewProjectCreateActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        projectList.clear();
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
                        "프로젝트 로드 실패: " + err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
