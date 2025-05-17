package com.example.tasklink;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of projects.
 */
public class ProjectAdapter
        extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    private final Context context;
    private final List<ProjectModel> projects;
    private final String userId;

    /**
     * @param context   호출한 Activity 컨텍스트
     * @param projects  화면에 표시할 프로젝트 리스트
     */
    public ProjectAdapter(Context context, List<ProjectModel> projects) {
        this.context = context;
        this.projects = projects;
        // 현재 로그인한 사용자의 UID
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        this.userId = (user != null) ? user.getUid() : null;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        // item_project_card.xml 레이아웃 inflate
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ProjectViewHolder holder, int position) {
        ProjectModel project = projects.get(position);

        // 1) 텍스트 세팅: 프로젝트명, 참여자 이메일
        holder.tvName.setText(project.title);
        holder.tvMembers.setText("참여자: " + project.ownerEmail);

        // 2) 카드 클릭 → Task 목록 화면으로 이동
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TaskListActivity.class);
            intent.putExtra("projectName", project.title);
            context.startActivity(intent);
        });

        // 3) 삭제 버튼 클릭 → 확인 다이얼로그 띄운 뒤 Firebase에서 삭제
        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("프로젝트 삭제")
                    .setMessage("정말 이 프로젝트를 삭제하시겠습니까?")
                    .setPositiveButton("삭제", (dialog, which) -> {
                        if (userId != null) {
                            DatabaseReference ref = FirebaseDatabase.getInstance()
                                    .getReference("projects")
                                    .child(userId)
                                    .child(project.getId());
                            ref.removeValue()
                                    .addOnSuccessListener(aVoid -> {
                                        // 리스트에서 제거 후 갱신
                                        projects.remove(position);
                                        notifyItemRemoved(position);
                                    })
                                    .addOnFailureListener(e -> {
                                        // 실패 시 토스트
                                        Toast.makeText(context,
                                                "삭제 실패: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    /**
     * ViewHolder: item_project_card.xml 내 뷰들을 바인딩
     */
    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvMembers;
        Button btnDelete;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tv_project_name);
            tvMembers = itemView.findViewById(R.id.tv_project_members);
            btnDelete = itemView.findViewById(R.id.btn_delete_project);
        }
    }
}
