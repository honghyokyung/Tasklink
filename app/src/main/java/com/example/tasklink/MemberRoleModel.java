package com.example.tasklink;

public class MemberRoleModel {
    public String name;
    public String role;

    public MemberRoleModel() {} // Firebase 파싱용 기본 생성자

    public MemberRoleModel(String name, String role) {
        this.name = name;
        this.role = role;
    }
}
