package com.example.tasklink;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TaskDetailActivity extends AppCompatActivity {

    private static final int FILE_PICK_CODE = 1001;

    private String projectName;
    private String firebaseKey;

    private EditText editTaskTitle, editDescription;
    private TextView textDeadline, textFile, textAssignUser;

    private List<MemberRoleModel> members = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail);

        projectName = getIntent().getStringExtra("projectName");
        //taskKey를 받아와서 제목을 수정해도 수정된제목+내용 그대로 유지하도록 함.
        firebaseKey = getIntent().getStringExtra("taskKey");

        editTaskTitle = findViewById(R.id.task_detail_task_title);
        editDescription = findViewById(R.id.edit_description);
        textDeadline = findViewById(R.id.text_deadline);
        textFile = findViewById(R.id.text_file);
        textAssignUser = findViewById(R.id.text_assign_user);

        ImageButton backBtn = findViewById(R.id.button_back);
        ImageButton chatBtn = findViewById(R.id.button_chat);
        ImageButton addUserBtn = findViewById(R.id.button_add_user);
        ImageButton dateBtn = findViewById(R.id.button_pick_date);
        ImageButton fileBtn = findViewById(R.id.button_pick_file);

        if (firebaseKey != null) {
            loadTaskFromFirebase();
        } else {
            textAssignUser.setText("👤 담당자");
            textDeadline.setText("📅 마감일");
            textFile.setText("📁 파일");
        }

        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String deadline = "📅 마감: " + year + "-" + (month + 1) + "-" + dayOfMonth;
                textDeadline.setText(deadline);
            }, y, m, d);
            dialog.show();
        });

        fileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(Intent.createChooser(intent, "파일 선택"), FILE_PICK_CODE);
        });

        addUserBtn.setOnClickListener(v -> {
            members.clear();
            //임의로 넣어둠. project만들때 추가한 member뜨게해서 선택 or 검색 가능하도록 수정해야함
            members.add(new MemberRoleModel("김유저", "프론트엔드"));
            members.add(new MemberRoleModel("이개발", "백엔드"));

            StringBuilder sb = new StringBuilder();
            for (MemberRoleModel m : members) {
                sb.append("👤 담당자: ").append(m.name).append("/").append(m.role).append("\n");
            }
            textAssignUser.setText(sb.toString().trim());
        });

        backBtn.setOnClickListener(v -> {
            saveTaskToFirebase();
            finish();
        });

        chatBtn.setOnClickListener(v ->
                Toast.makeText(this, "채팅 기능은 준비 중입니다", Toast.LENGTH_SHORT).show());
    }

    private void loadTaskFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(projectName)
                .child(firebaseKey); //key를 받아옴

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                TaskModel task = snapshot.getValue(TaskModel.class);
                if (task != null) {
                    editTaskTitle.setText(task.taskTitle);
                    editDescription.setText(task.description);
                    textDeadline.setText(task.deadline != null ? task.deadline : "📅 마감일");
                    textFile.setText(task.fileName != null ? task.fileName : "📁 파일");

                    if (task.members != null) {
                        members = task.members;
                        StringBuilder sb = new StringBuilder();
                        for (MemberRoleModel m : members) {
                            sb.append("👤 담당자: ").append(m.name).append("/").append(m.role).append("\n");
                        }
                        textAssignUser.setText(sb.toString().trim());
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(TaskDetailActivity.this, "불러오기 실패: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveTaskToFirebase() {
        String taskTitleInput = editTaskTitle.getText().toString().trim();
        if (taskTitleInput.isEmpty()) {
            Toast.makeText(this, "Task 제목을 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        if (members.isEmpty()) {
            //담당자 선택 안하면 미정으로 뜨게 하였음. (담당자:미정/역할 미정)
            members.add(new MemberRoleModel("미정", "미정"));
        }

        String deadline = textDeadline.getText().toString();
        String fileName = textFile.getText().toString();
        String description = editDescription.getText().toString();

        TaskModel task = new TaskModel(taskTitleInput, members, deadline, fileName, description);

        String keyToUse = (firebaseKey != null) ? firebaseKey : taskTitleInput;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("tasks")
                .child(projectName)
                .child(keyToUse);

        ref.setValue(task)
                .addOnSuccessListener(v ->
                        Toast.makeText(this, "저장 완료", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                String fileName = uri.getLastPathSegment();
                textFile.setText("📁 파일: " + fileName);
            }
        }
    }
}
