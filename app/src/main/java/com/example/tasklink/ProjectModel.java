package com.example.tasklink;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase의 /projects 노드와 1:1 매핑되는 모델 클래스.
 */
public class ProjectModel {
    // 화면에 노출되는 데이터
    public String title;                          // 프로젝트명
    public String ownerEmail;                     // 소유자 이메일
    public Map<String, MemberRoleModel> members;  // 참여자(UID → MemberRoleModel)
    public Map<String, TaskModel>     tasks;      // 작업(키 → TaskModel)

    // 내부적으로 사용하는 push 키
    private String id;

    /** Firebase용 빈 생성자 (필수) */
    public ProjectModel() {
        // 맵은 빈 채로 초기화해 두면 null 체크 불필요
        this.members = new HashMap<>();
        this.tasks   = new HashMap<>();
    }

    /** 제목·소유자만 빠르게 설정하고 싶을 때 */
    public ProjectModel(String title, String ownerEmail) {
        this();
        this.title      = title;
        this.ownerEmail = ownerEmail;
    }

    // ===== 필드 접근자(Getter) =====

    /** 프로젝트명 */
    public String getTitle() {
        return title;
    }

    /** 소유자 이메일 */
    public String getOwnerEmail() {
        return ownerEmail;
    }

    /** 참여자 목록 */
    public Map<String, MemberRoleModel> getMembers() {
        return members;
    }

    /** Task 목록 */
    public Map<String, TaskModel> getTasks() {
        return tasks;
    }

    /** 이 모델이 저장된 Firebase 키(push key) */
    public String getId() {
        return id;
    }

    /** Firebase 키(push key) 저장 */
    public void setId(String id) {
        this.id = id;
    }
}
