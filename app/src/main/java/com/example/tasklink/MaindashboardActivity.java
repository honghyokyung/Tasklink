package com.example.tasklink;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

public class MaindashboardActivity extends AppCompatActivity {

    private TextView tvWelcome;
    private LinearLayout layoutProjectList;
    private Button btnAddProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maindashboard);

        tvWelcome = findViewById(R.id.tv_dashboard_title);
        layoutProjectList = findViewById(R.id.layout_project_list);
        btnAddProject = findViewById(R.id.btn_add_project);

        // ✅ 현재 로그인한 사용자 UID로 닉네임 불러오기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("nickname");

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String nickname = snapshot.getValue(String.class);
                    if (nickname != null) {
                        tvWelcome.setText(nickname + "님의 대시보드");
                    } else {
                        tvWelcome.setText("대시보드");
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    tvWelcome.setText("대시보드");
                    Toast.makeText(MaindashboardActivity.this, "닉네임 불러오기 실패", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // 프로젝트 생성 버튼 클릭 시
        btnAddProject.setOnClickListener(v -> {
            Intent intent = new Intent(MaindashboardActivity.this, NewProjectCreateActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        layoutProjectList.removeAllViews();  // 기존 카드 제거
        loadProjectsFromFirebase();          // 다시 로드
    }

    private void loadProjectsFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("projects").child(uid);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot projectSnapshot : snapshot.getChildren()) {
                    String projectId = projectSnapshot.getKey();
                    ProjectModel project = projectSnapshot.getValue(ProjectModel.class);
                    if (project != null && projectId != null) {
                        addProjectCard(project.title, project.memberEmail, projectId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MaindashboardActivity.this, "프로젝트 불러오기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 프로젝트 카드 추가 함수 (projectId도 함께 받음)
    private void addProjectCard(String projectName, String members, String projectId) {
        View projectCard = getLayoutInflater().inflate(R.layout.item_project_card, layoutProjectList, false);

        TextView tvName = projectCard.findViewById(R.id.tv_project_name);
        TextView tvMembers = projectCard.findViewById(R.id.tv_project_members);
        Button btnDelete = projectCard.findViewById(R.id.btn_delete_project);

        tvName.setText("프로젝트: " + projectName);
        tvMembers.setText("참여자: " + members);

        // 삭제 버튼 클릭 처리 (확인창 포함)
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("프로젝트 삭제")
                    .setMessage("정말 이 프로젝트를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();
                            DatabaseReference projectRef = FirebaseDatabase.getInstance()
                                    .getReference("projects")
                                    .child(uid)
                                    .child(projectId);

                            projectRef.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        layoutProjectList.removeView(projectCard);
                                        Toast.makeText(this, "프로젝트가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("취소", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        // 카드 클릭 → Task 목록으로 이동
        projectCard.setOnClickListener(v -> {
            Intent intent = new Intent(MaindashboardActivity.this, TaskListActivity.class);
            intent.putExtra("projectName", projectName);
            startActivity(intent);
        });

        layoutProjectList.addView(projectCard);
    }
}
