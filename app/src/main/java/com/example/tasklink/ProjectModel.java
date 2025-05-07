package com.example.tasklink;

public class ProjectModel {
    public String title;
    public String memberEmail;

    public ProjectModel() {}  // Firebase용 기본 생성자

    public ProjectModel(String title, String memberEmail) {
        this.title = title;
        this.memberEmail = memberEmail;
    }
}
