package com.example.tasklink;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase 의 /projects/{projectId}/tasks/{taskId} 에 1:1 매핑되는 모델
 */
public class TaskModel {
    public String taskTitle;                       // 작업 제목
    public Map<String, MemberRoleModel> members;   // UID → MemberRoleModel
    public String deadline;                        // 마감일
    public String fileName;                        // 첨부 파일 이름
    public String description;                     // 상세 설명
    public String firebaseKey;                     // (필요하면) 이 태스크의 Firebase 키
    public String id;                             // 내부용 ID(push key)

    /** Firebase 리플렉션용 빈 생성자 (맵은 빈으로 초기화) */
    public TaskModel() {
        this.members = new HashMap<>();
        this.deadline    = "";
        this.fileName    = "";
        this.description = "";
    }

    /** 최소 생성자—제목과 멤버만 빠르게 지정 */
    public TaskModel(String taskTitle,
                     Map<String, MemberRoleModel> members) {
        this();
        this.taskTitle = taskTitle;
        this.members   = members != null ? members : new HashMap<>();
    }

    /** 전체 필드 생성자 */
    public TaskModel(String taskTitle,
                     Map<String, MemberRoleModel> members,
                     String deadline,
                     String fileName,
                     String description) {
        this.taskTitle   = taskTitle;
        this.members     = members != null ? members : new HashMap<>();
        this.deadline    = deadline;
        this.fileName    = fileName;
        this.description = description;
    }

    // ===== Getters / Setters =====

    public String getTaskTitle() {
        return taskTitle;
    }
    public Map<String, MemberRoleModel> getMembers() {
        return members;
    }
    public String getDeadline() {
        return deadline;
    }
    public String getFileName() {
        return fileName;
    }
    public String getDescription() {
        return description;
    }
    /** Firebase 키(push key) */
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
