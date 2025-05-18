package com.example.tasklink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Map;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    public interface OnTaskClickListener {
        void onTaskClick(TaskModel task);
    }

    private final java.util.List<TaskModel> taskList;
    private final OnTaskClickListener listener;

    public TaskAdapter(java.util.List<TaskModel> taskList,
                       OnTaskClickListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull @Override
    public TaskViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task_card, parent, false);
        return new TaskViewHolder(view);
    }

    @Override public void onBindViewHolder(
            @NonNull TaskViewHolder holder, int position) {

        TaskModel task = taskList.get(position);

        // 1) 제목 세팅
        holder.tvTitle.setText(
                task.taskTitle != null ? task.taskTitle : ""
        );

        // 2) 멤버 레이아웃 초기화
        holder.layoutTaskMembers.removeAllViews();

        Map<String, MemberRoleModel> members = task.members;
        if (members == null || members.isEmpty()) {
            // 담당자 없으면 안내문
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText("담당자 정보 없음");
            holder.layoutTaskMembers.addView(tv);
        } else {
            // members 맵을 순회하며 TextView 추가
            for (MemberRoleModel m : members.values()) {
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText("담당자: " + m.nickname
                        + " / 역할: " + m.role);
                holder.layoutTaskMembers.addView(tv);
            }
        }

        // 3) 카드 전체 클릭
        holder.itemView.setOnClickListener(v ->
                listener.onTaskClick(task)
        );
        // 4) “자세히 보기” 버튼 클릭
        holder.btnViewDetail.setOnClickListener(v ->
                listener.onTaskClick(task)
        );
    }

    @Override public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView     tvTitle;
        LinearLayout layoutTaskMembers;
        Button       btnViewDetail;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle           = itemView.findViewById(R.id.tv_task_title);
            layoutTaskMembers = itemView.findViewById(R.id.layout_task_members);
            btnViewDetail     = itemView.findViewById(R.id.btn_view_detail);
        }
    }

    /** 외부에서 리스트 전체를 교체할 때 호출 */
    public void setTasks(java.util.List<TaskModel> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        notifyDataSetChanged();
    }
}
