package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextId, editTextPassword, editTextNickname, editTextOrganization;
    private Button buttonRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();

        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextNickname = findViewById(R.id.editTextNickname);
        editTextOrganization = findViewById(R.id.editTextOrganization);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String email = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String nickname = editTextNickname.getText().toString().trim();
        String institution = editTextOrganization.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || nickname.isEmpty() || institution.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // ✅ 사용자 정보 저장
                            DatabaseReference userRef = FirebaseDatabase.getInstance()
                                    .getReference("users").child(uid);

                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("email", email);
                            userInfo.put("nickname", nickname);
                            userInfo.put("institution", institution);
                            userInfo.put("uid", uid);

                            userRef.setValue(userInfo).addOnCompleteListener(dbTask -> {
                                if (dbTask.isSuccessful()) {
                                    Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    Toast.makeText(this, "회원정보 저장 실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
