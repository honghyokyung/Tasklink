package com.example.tasklink;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private EditText editTextId, editTextPassword;
    private Button buttonRegister;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        mAuth = FirebaseAuth.getInstance();

        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonRegister = findViewById(R.id.buttonRegister);

        buttonRegister.setOnClickListener(view -> registerUser());
    }

    private void registerUser() {
        String email = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
                        finish(); // 회원가입 후 로그인 화면으로 돌아가기
                    } else {
                        Toast.makeText(this, "회원가입 실패: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

