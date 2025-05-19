package com.example.tasklink;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * RecyclerView Adapter for displaying a list of projects with click and delete actions.
 */
public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {

    public interface OnProjectActionListener {
        void onProjectClick(ProjectModel project);
        void onDeleteClick(ProjectModel project);
        void onSettingClick(ProjectModel project);
    }

    private final Context context;
    private final List<ProjectModel> projects;
    private final OnProjectActionListener listener;

    /**
     * @param context   호출한 Activity 컨텍스트
     * @param projects  화면에 표시할 프로젝트 리스트
     * @param listener  클릭, 삭제 액션을 처리할 리스너
     */
    public ProjectAdapter(Context context,
                          List<ProjectModel> projects,
                          OnProjectActionListener listener) {
        this.context = context;
        this.projects = projects;
        this.listener = listener;
    }

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

    /**
     * ViewHolder: item_project_card.xml 내 뷰들을 바인딩
     */
    static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMembers;
        Button btnSetting, btnDelete;
        ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tv_project_name);
            tvMembers = itemView.findViewById(R.id.tv_project_members);
            btnSetting= itemView.findViewById(R.id.btn_setting_project);
            btnDelete = itemView.findViewById(R.id.btn_delete_project);
        }
    }
}
