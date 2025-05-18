// MemberRoleModel.java
package com.example.tasklink;

public class MemberRoleModel {
    public String email;
    public String nickname;
    public String role;

    // Firebase 리플렉션용 빈 생성자
    public MemberRoleModel() {}

    public MemberRoleModel(String email, String nickname, String role) {
        this.email    = email;
        this.nickname = nickname;
        this.role     = role;
    }
}