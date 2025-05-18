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

    public interface OnTaskActionListener {
        void onTaskClick(TaskModel task);
        void onTaskDelete(TaskModel task);
    }

    private final java.util.List<TaskModel> taskList;
    private final OnTaskActionListener listener;

    public TaskAdapter(java.util.List<TaskModel> taskList,
                       OnTaskActionListener listener) {
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
                task.getTaskTitle() != null ? task.getTaskTitle() : ""
        );

        // 2) 멤버 레이아웃 초기화
        holder.layoutTaskMembers.removeAllViews();

        Map<String, MemberRoleModel> members = task.getMembers();
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

        // 3) 카드 전체 클릭 → 상세 보기
        holder.itemView.setOnClickListener(v ->
                listener.onTaskClick(task)
        );
        holder.btnViewDetail.setOnClickListener(v ->
                listener.onTaskClick(task)
        );

        // 4) 삭제 버튼 클릭 → 삭제 콜백
        holder.btnDelete.setOnClickListener(v ->
                listener.onTaskDelete(task)
        );
    }

    @Override public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView     tvTitle;
        LinearLayout layoutTaskMembers;
        Button       btnViewDetail;
        Button       btnDelete;            // 삭제 버튼

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle           = itemView.findViewById(R.id.tv_task_title);
            layoutTaskMembers = itemView.findViewById(R.id.layout_task_members);
            btnViewDetail     = itemView.findViewById(R.id.btn_view_detail);
            btnDelete         = itemView.findViewById(R.id.btn_delete_task); // 새로 추가된 ID
        }
    }

    /** 외부에서 리스트 전체를 교체할 때 호출 */
    public void setTasks(java.util.List<TaskModel> tasks) {
        taskList.clear();
        taskList.addAll(tasks);
        notifyDataSetChanged();
    }
}
