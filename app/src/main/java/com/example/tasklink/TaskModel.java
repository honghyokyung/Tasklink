package com.example.tasklink;

import java.util.List;

public class TaskModel {
    public String taskTitle;
    public List<MemberRoleModel> members;

    public TaskModel() {}

    public TaskModel(String taskTitle, List<MemberRoleModel> members) {
        this.taskTitle = taskTitle;
        this.members = members;
    }
}
