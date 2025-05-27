package com.example.tasklink;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onProjectClick(ProjectModel project);
        void onDeleteClick(ProjectModel project);
        void onSettingClick(ProjectModel project);
    }

    private final Context context;
    private final List<ProjectModel> projects;
    private final OnProjectActionListener listener;

    public ProjectAdapter(Context context,
                          List<ProjectModel> projects,
                          OnProjectActionListener listener) {
        this.context = context;
        this.projects = projects;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_project_card, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        ProjectModel project = projects.get(position);
        Log.d("DBG", "[Adapter] onBindViewHolder pos=" + position + " id=" + project.getId());

        holder.tvName.setText(project.getTitle());
        holder.tvMembers.setText("참여자: " + project.getOwnerEmail());

        // ✅ 진행률 계산
        Map<String, TaskModel> taskMap = project.getTasks();
        int total = taskMap.size();
        float totalProgressScore = 0f;

        for (TaskModel task : taskMap.values()) {
            String status = task.getStatus();
            if ("work in process".equalsIgnoreCase(status)) {
                totalProgressScore += 0.5f;
            } else if ("task complete".equalsIgnoreCase(status)) {
                totalProgressScore += 1.0f;
            }
        }

        int progressPercent = (total == 0) ? 0 : (int)((totalProgressScore / total) * 100);
        holder.donutProgressView.setProgress(progressPercent);

        // 설정 버튼 클릭
        holder.btnSetting.setOnClickListener(v -> {
            Log.d("DBG", "[Adapter] Setting clicked: " + project.getId());
            listener.onSettingClick(project);
        });

        // 삭제 버튼 클릭
        holder.btnDelete.setOnClickListener(v -> {
            Log.d("DBG", "[Adapter] Delete clicked: " + project.getId());
            listener.onDeleteClick(project);
        });

        // 카드 자체 클릭
        holder.itemView.setOnClickListener(v -> {
            Log.d("DBG", "[Adapter] Card clicked: " + project.getId());
            listener.onProjectClick(project);
        });
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMembers;
        Button btnSetting, btnDelete;
        DonutProgressView donutProgressView;

        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tv_project_name);
            tvMembers  = itemView.findViewById(R.id.tv_project_members);
            btnSetting = itemView.findViewById(R.id.btn_setting_project);
            btnDelete  = itemView.findViewById(R.id.btn_delete_project);
            donutProgressView = itemView.findViewById(R.id.donut_progress_view);
        }
    }
}
