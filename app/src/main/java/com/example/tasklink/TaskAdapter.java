package com.example.tasklink;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter
        extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<TaskModel> taskList;
    private final OnTaskClickListener listener;

    public TaskAdapter(List<TaskModel> taskList, OnTaskClickListener listener) {
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

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {

        TaskModel task = taskList.get(position);

        Log.d("TaskAdapter", "bind title = [" + task.taskTitle + "]");


        holder.tvTitle.setText(task.taskTitle != null ? task.taskTitle : "");

        // 멤버 레이아웃 초기화
        holder.layoutTaskMembers.removeAllViews();

        List<MemberRoleModel> membersToShow = new ArrayList<>();

        // 1) 배열 형태 처리
        if (task.members != null && !task.members.isEmpty()) {
            membersToShow.addAll(task.members);
        }
        // 2) 단일 필드 처리
        else if (task.member != null && task.role != null) {
            membersToShow.add(new MemberRoleModel(task.member, task.role));
        }

        // 3) 멤버가 하나도 없으면 안내문
        if (membersToShow.isEmpty()) {
            TextView tv = new TextView(holder.itemView.getContext());
            tv.setText("담당자 정보 없음");
            holder.layoutTaskMembers.addView(tv);
        } else {
            // 실제 멤버 정보 뿌리기
            for (MemberRoleModel m : membersToShow) {
                TextView tv = new TextView(holder.itemView.getContext());
                tv.setText("담당자: " + m.name + " / 역할: " + m.role);
                holder.layoutTaskMembers.addView(tv);
            }
        }

        holder.itemView.setOnClickListener(v ->
                listener.onTaskClick(task)
        );

        // 2) “자세히 보기” 버튼 클릭
        holder.btnViewDetail.setOnClickListener(v ->
                listener.onTaskClick(task)
        );
    }

    @Override public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        LinearLayout layoutTaskMembers;  // 여기 이름을 layout_task_members 와 매핑
        Button btnViewDetail;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_task_title);
            layoutTaskMembers = itemView.findViewById(R.id.layout_task_members);
            btnViewDetail  = itemView.findViewById(R.id.btn_view_detail);
        }
    }

    //데이터 갱신 메서드
    public void setTasks(List<TaskModel> tasks) {
        this.taskList.clear();
        this.taskList.addAll(tasks);
        notifyDataSetChanged();
    }


}
