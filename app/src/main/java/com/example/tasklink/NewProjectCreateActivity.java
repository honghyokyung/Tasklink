package com.example.tasklink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewProjectCreateActivity extends AppCompatActivity {

    private EditText editTextProjectTitle;
    private EditText editTextMemberEmail;
    private Button btnSaveProject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.projectcreate);

        editTextProjectTitle = findViewById(R.id.editTextProjectTitle);
        editTextMemberEmail = findViewById(R.id.editTextMemberEmail);
        btnSaveProject = findViewById(R.id.btnSaveProject);

        btnSaveProject.setOnClickListener(v -> {
            String title = editTextProjectTitle.getText().toString().trim();
            String memberEmail = editTextMemberEmail.getText().toString().trim();

            if (title.isEmpty() || memberEmail.isEmpty()) {
                Toast.makeText(NewProjectCreateActivity.this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                String uid = user.getUid();

                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference ref = db.getReference("projects").child(uid);

                String projectId = ref.push().getKey();
                ProjectModel project = new ProjectModel(title, memberEmail);

                ref.child(projectId).setValue(project)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(NewProjectCreateActivity.this, "프로젝트 저장 성공", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(NewProjectCreateActivity.this, "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(NewProjectCreateActivity.this, "로그인 정보가 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
