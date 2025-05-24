package com.example.tasklink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Map;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskActionListener {
        void onTaskClick(TaskModel task);
        void onTaskSetting(TaskModel task);
        void onTaskDelete(TaskModel task);
        // void onTaskChat(TaskModel task); ← 제거됨
    }

    private final List<TaskModel> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(List<TaskModel> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);

        holder.tvTitle.setText(task.getTaskTitle() != null ? task.getTaskTitle() : "");

        holder.layoutTaskMembers.removeAllViews();
        Map<String, MemberRoleModel> members = task.getMembers();
        if (members == null || members.isEmpty()) {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText("담당자 정보 없음");
            holder.layoutTaskMembers.addView(tv);
        } else {
            for (MemberRoleModel m : members.values()) {
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText("👤 " + m.nickname + " (" + m.role + ")");
                holder.layoutTaskMembers.addView(tv);
            }
        }

        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            holder.tvDeadline.setText(task.getDeadline());
        } else {
            holder.tvDeadline.setText("⏰ 미정");
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.btnSetting.setOnClickListener(v -> listener.onTaskSetting(task));
        holder.btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));

        // btnChat 클릭 리스너 제거됨
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        LinearLayout layoutTaskMembers;
        TextView tvDeadline;
        Button btnSetting;
        Button btnDelete;
        Button btnChat; // 뷰는 남겨둠 (필요시 XML에서도 제거 가능)

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            layoutTaskMembers = itemView.findViewById(R.id.layout_task_members);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            btnSetting = itemView.findViewById(R.id.btn_setting);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);// 사용은 안 함
        }
    }

    public void setTasks(List<TaskModel> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        notifyDataSetChanged();
    }
}
