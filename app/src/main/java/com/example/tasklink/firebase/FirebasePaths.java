package com.example.tasklink.firebase;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * FirebasePaths 클래스는 Realtime Database의 주요 노드 경로들을
 * 중앙에서 관리하기 위한 유틸리티 클래스
 * String 상수로 정의된 노드명을 통해 오타를 방지하고,
 * DatabaseReference 생성 메서드를 제공하여 코드 가독성과 유지보수성을 향상시킵니다.
 */
public class FirebasePaths {
    /** 프로젝트 정보를 저장하는 노드명 상수 */
    public static final String NODE_PROJECTS = "projects";
    /** Task 정보를 저장하는 노드명 상수 */
    public static final String NODE_TASKS    = "tasks";
    /** Chat 메시지를 저장하는 노드명 상수 */
    public static final String NODE_CHATS    = "chats";

    /**
     * FirebaseDatabase 인스턴스를 반환합니다.
     * @return FirebaseDatabase 싱글톤 인스턴스
     */
    private static FirebaseDatabase db() {
        return FirebaseDatabase.getInstance();
    }

    /**
     * 특정 프로젝트의 Task 노드에 대한 DatabaseReference를 반환합니다.
     * @param projectId 프로젝트 식별자
     * @return tasks/{projectId} 경로의 DatabaseReference
     */
    public static DatabaseReference tasks(String projectId) {
        return db().getReference(NODE_TASKS).child(projectId);
    }

    /**
     * 프로젝트 전체 목록에 접근하기 위한 DatabaseReference를 반환합니다.
     * @return projects 경로의 DatabaseReference
     */
    public static DatabaseReference projects() {
        return db().getReference(NODE_PROJECTS);
    }

    /**
     * 특정 프로젝트의 특정 Task에 대한 Chat 노드의 DatabaseReference를 반환합니다.
     * @param projectId 프로젝트 식별자
     * @param taskId    Task 식별자
     * @return chats/{projectId}/{taskId} 경로의 DatabaseReference
     */
    public static DatabaseReference chats(String projectId, String taskId) {
        return db().getReference(NODE_CHATS)
                .child(projectId)
                .child(taskId);
    }
}