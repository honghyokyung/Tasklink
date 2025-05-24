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
        void onTaskChat(TaskModel task);
    }

    private final List<TaskModel> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(List<TaskModel> taskList,
                       OnTaskActionListener listener) {
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

        // 상태 표시 및 색상 처리
        String status = task.getStatus();
        holder.tvStatus.setText(status != null ? status : "진행 전");

        if (status != null) {
            switch (status) {
                case "before start":
                    holder.tvStatus.setTextColor(0xFF2196F3); // 파랑
                    break;
                case "work in process":
                    holder.tvStatus.setTextColor(0xFFF44336); // 빨강
                    break;
                case "task complete":
                    holder.tvStatus.setTextColor(0xFF9E9E9E); // 회색
                    break;
                default:
                    holder.tvStatus.setTextColor(0xFF444444); // 기본
            }
        }

        // 제목
        holder.tvTitle.setText(
                task.getTaskTitle() != null ? task.getTaskTitle() : ""
        );

        // 담당자 목록
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

        // 마감일
        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            holder.tvDeadline.setText(task.getDeadline());
        } else {
            holder.tvDeadline.setText("⏰ 미정");
        }

        // 리스너 연결
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        holder.btnSetting.setOnClickListener(v -> listener.onTaskSetting(task));
        holder.btnDelete.setOnClickListener(v -> listener.onTaskDelete(task));
        holder.btnChat.setOnClickListener(v -> listener.onTaskChat(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        TextView tvStatus;
        LinearLayout layoutTaskMembers;
        TextView tvDeadline;
        Button btnSetting;
        Button btnDelete;
        Button btnChat;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            tvStatus = itemView.findViewById(R.id.task_status);
            layoutTaskMembers = itemView.findViewById(R.id.layout_task_members);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            btnSetting = itemView.findViewById(R.id.btn_setting);
            btnDelete = itemView.findViewById(R.id.btn_delete_task);
            btnChat = itemView.findViewById(R.id.btn_chat);
        }
    }

    public void setTasks(List<TaskModel> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        notifyDataSetChanged();
    }
}
