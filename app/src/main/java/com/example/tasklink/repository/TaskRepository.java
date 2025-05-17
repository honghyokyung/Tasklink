package com.example.tasklink.repository;

import androidx.annotation.NonNull;
import com.example.tasklink.firebase.FirebasePaths;
import com.example.tasklink.TaskModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * TaskRepository 클래스는 Firebase Realtime Database에서
 * 특정 프로젝트의 Task 목록을 읽고 변경사항을 감지하기 위한
 * 리스너 등록 및 관리 기능을 제공합니다.
 */
public class TaskRepository {

    /**
     * Firebase에서 등록한 ValueEventListener 객체를 보관합니다.
     * detachListener() 호출 시 제거할 때 사용됩니다.
     */
    private ValueEventListener listener;

    /**
     * Task 데이터 로드 및 에러 처리를 위한 콜백 인터페이스
     */
    public interface OnTasksChanged {
        /**
         * Task 목록을 성공적으로 로드했을 때 호출됩니다.
         * @param tasks 로드된 TaskModel 리스트
         */
        void onLoaded(List<TaskModel> tasks);

        /**
         * 데이터 로드 중 오류가 발생했을 때 호출됩니다.
         * @param message 오류 메시지
         */
        void onError(String message);
    }

    /**
     * 지정한 프로젝트의 Task 노드에 실시간 리스너를 등록합니다.
     * 변경이 발생할 때마다 onLoaded 또는 onError가 호출됩니다.
     *
     * @param projectId 프로젝트 식별자 (FirebasePaths.tasks 경로의 child)
     * @param callback  데이터 수신 및 에러 처리를 위한 콜백 구현체
     */
    public void attachListener(@NonNull String projectId, @NonNull OnTasksChanged callback) {
        // 기존 리스너가 남아있다면 제거
        if (listener != null) {
            detachListener(projectId);
        }
        // FirebasePaths를 통해 tasks/{projectId} 경로에 리스너를 등록
        listener = FirebasePaths.tasks(projectId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        List<TaskModel> tasks = new ArrayList<>();
                        // 스냅샷의 각 자식 노드를 TaskModel로 변환
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            TaskModel t = ds.getValue(TaskModel.class);
                            if (t != null) {
                                t.id = ds.getKey();               // 키 보존
                                if (t.taskTitle == null) {
                                    t.taskTitle = ds.getKey();
                                }
                                tasks.add(t);
                            }
                        }
                        // 로드된 데이터를 콜백으로 전달
                        callback.onLoaded(tasks);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // 오류 발생 시 에러 메시지를 콜백으로 전달
                        callback.onError(error.getMessage());
                    }
                });
    }

    /**
     * 등록된 리스너를 제거하여 중복 호출 및 메모리 누수를 방지합니다.
     *
     * @param projectId 프로젝트 식별자 (등록 시 사용한 동일 키)
     */
    public void detachListener(@NonNull String projectId) {
        if (listener != null) {
            // FirebasePaths.tasks 경로에서 해당 리스너 제거
            FirebasePaths.tasks(projectId).removeEventListener(listener);
            listener = null;
        }
    }
}
