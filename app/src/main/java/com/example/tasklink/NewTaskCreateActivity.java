package com.example.tasklink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NewTaskCreateActivity extends AppCompatActivity {
    private EditText etTitle;
    private Button btnPickDate, btnPickFile, btnSave;
    private Map<String,MemberRoleModel> members = new HashMap<>();
    private String projectId;

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_new_task);

        projectId = getIntent().getStringExtra("projectId");
        etTitle   = findViewById(R.id.et_task_title);
        btnPickDate = findViewById(R.id.btn_pick_date);
        btnPickFile = findViewById(R.id.btn_pick_file);
        btnSave     = findViewById(R.id.btn_save_task);

        // 담당자 선택 다이얼로그 → members.put(...)
        // 마감일/파일 선택 (DatePickerDialog, ActivityResultLauncher)

        btnSave.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            if (title.isEmpty()) { etTitle.setError("제목필수"); return; }
            TaskModel t = new TaskModel(title, members);
            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("projects").child(projectId).child("tasks");
            String key = ref.push().getKey();
            ref.child(key).setValue(t)
                    .addOnSuccessListener(a -> finish())
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "실패: "+e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }
}