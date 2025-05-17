package com.example.tasklink;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Task 상세(읽기/수정) 화면
 */
public class TaskDetailActivity extends AppCompatActivity {

    private String projectName;
    private String taskId;

    private EditText etTitle, etDescription;
    private TextView tvDeadline, tvFile, tvAssignUser;

    private List<MemberRoleModel> members = new ArrayList<>();

    // 파일 선택용 ActivityResultLauncher
    private final ActivityResultLauncher<String> pickFileLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            String name = uri.getLastPathSegment();
                            tvFile.setText("📁 파일: " + name);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail);

        // 1) Intent 키 통일: 리스트에서 putExtra("taskId", key)로 보낸다고 가정
        projectName = getIntent().getStringExtra("projectName");
        taskId      = getIntent().getStringExtra("taskId");
        if (projectName == null) {
            Toast.makeText(this, "프로젝트 정보가 없습니다", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        // 2) 뷰 바인딩
        etTitle       = findViewById(R.id.task_detail_task_title);
        etDescription = findViewById(R.id.edit_description);
        tvDeadline    = findViewById(R.id.text_deadline);
        tvFile        = findViewById(R.id.text_file);
        tvAssignUser  = findViewById(R.id.text_assign_user);

        ImageButton btnBack      = findViewById(R.id.button_back);
        ImageButton btnChat      = findViewById(R.id.button_chat);
        ImageButton btnAddUser   = findViewById(R.id.button_add_user);
        ImageButton btnPickDate  = findViewById(R.id.button_pick_date);
        ImageButton btnPickFile  = findViewById(R.id.button_pick_file);

        // 3) 뒤로가기
        btnBack.setOnClickListener(v -> saveAndExit());

        // 4) 채팅 버튼
        btnChat.setOnClickListener(v ->
            /*
            {
            Intent i = new Intent(this, ChatActivity.class);
            i.putExtra("projectName", projectName);
            i.putExtra("taskId", taskId);
            startActivity(i);
            }
             */
            Toast.makeText(this, "채팅 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
        );

        // 5) 담당자 선택 (예시)
        btnAddUser.setOnClickListener(v -> {
            // TODO: 실제 프로젝트 멤버 리스트에서 선택 UI로 교체
            members.clear();
            members.add(new MemberRoleModel("김유저", "프론트엔드"));
            members.add(new MemberRoleModel("이개발", "백엔드"));
            displayMembers();
        });

        // 6) 날짜 선택
        btnPickDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this,
                    (dlg, y, m, d) -> tvDeadline.setText("📅 마감: " + y + "-" + (m+1) + "-" + d),
                    c.get(Calendar.YEAR),
                    c.get(Calendar.MONTH),
                    c.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // 7) 파일 선택 (ActivityResult API 사용)
        btnPickFile.setOnClickListener(v ->
                pickFileLauncher.launch("*/*")
        );

        // 8) 기존 Task 로드 (taskId가 null이면 신규 생성 모드)
        if (taskId != null) {
            loadTask();
        } else {
            // 빈 화면 표시
            tvAssignUser.setText("👤 담당자: 미정");
            tvDeadline   .setText("📅 마감일");
            tvFile       .setText("📁 파일");
        }
    }

    /** Firebase 에서 TaskModel 불러오기 */
    private void loadTask() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(projectName)
                .child(taskId);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snap) {
                // GenericTypeIndicator로 List<MemberRoleModel> 꺼내기
                TaskModel task = snap.getValue(TaskModel.class);
                if (task == null) { finish(); return; }

                // 읽기 전용 모드 전환
                etTitle      .setText(task.getTaskTitle());
                etTitle      .setEnabled(false);
                etDescription.setText(task.getDescription());
                etDescription.setEnabled(false);

                tvDeadline.setText(
                        task.getDeadline()!=null ? task.getDeadline() : "📅 마감일"
                );
                tvFile    .setText(
                        task.getFileName()!=null ? "📁 파일: "+task.getFileName() : "📁 파일"
                );

                if (task.getMembers()!=null) {
                    members = task.getMembers();
                    displayMembers();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError err) {
                Toast.makeText(TaskDetailActivity.this,
                        "로드 실패: "+err.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 현재 members 리스트를 TextView에 보여주기 */
    private void displayMembers() {
        StringBuilder sb = new StringBuilder();
        for (MemberRoleModel m : members) {
            sb.append("👤 ").append(m.name)
                    .append(" (").append(m.role).append(")\n");
        }
        tvAssignUser.setText(sb.toString().trim());
    }

    /** Task를 저장하고 액티비티 종료 */
    private void saveAndExit() {
        String title = etTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "제목을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if (members.isEmpty()) {
            members.add(new MemberRoleModel("미정","미정"));
        }

        TaskModel task = new TaskModel(
                title,
                members,
                tvDeadline.getText().toString(),
                tvFile    .getText().toString(),
                etDescription.getText().toString()
        );

        DatabaseReference tasksRef = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(projectName);

        // 신규: push() key, 수정: 기존 taskId
        if (taskId == null) {
            String newKey = tasksRef.push().getKey();
            tasksRef.child(newKey).setValue(task)
                    .addOnSuccessListener(a -> finish());
        } else {
            tasksRef.child(taskId).setValue(task)
                    .addOnSuccessListener(a -> finish());
        }
    }
}
