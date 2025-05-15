package com.example.tasklink;

import java.util.List;

public class TaskModel {
    public String taskTitle;
    public List<MemberRoleModel> members;
    public String deadline;
    public String fileName;
    public String description;
    public String firebaseKey;

    public String id;

    // 단일 기반
    public String member;
    public String role;

    public TaskModel() {}

    // 2개 인자 생성자 (프로젝트 설정 시 사용)
    public TaskModel(String taskTitle, List<MemberRoleModel> members) {
        this.taskTitle = taskTitle;
        this.members = members;
        this.deadline = "";
        this.fileName = "";
        this.description = "";
    }

    // 5개 인자 생성자 (Task 저장 시 사용)
    public TaskModel(String taskTitle, List<MemberRoleModel> members,
                     String deadline, String fileName, String description) {
        this.taskTitle = taskTitle;
        this.members = members;
        this.deadline = deadline;
        this.fileName = fileName;
        this.description = description;
    }

    public String getTaskTitle() {
        return taskTitle;
    }
}
