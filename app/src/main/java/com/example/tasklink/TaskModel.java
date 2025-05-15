package com.example.tasklink;

import java.util.List;

public class TaskModel {
    public String taskTitle;
    public List<MemberRoleModel> members;

    public String id;

    // 단일 기반
    public String member;
    public String role;

    public TaskModel() {}

    public TaskModel(String taskTitle, List<MemberRoleModel> members) {
        this.taskTitle = taskTitle;
        this.members = members;
    }

    public String getTaskTitle() {
        return taskTitle;
    }
}
