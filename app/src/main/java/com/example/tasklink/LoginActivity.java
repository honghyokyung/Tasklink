package com.example.tasklink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextId, editTextPassword;
    private Button buttonLogin, buttonSignup;
    public FirebaseAuth mAuth = FirebaseAuth.getInstance(); //For Mock Test

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login); // login.xml 레이아웃 사용

        mAuth = FirebaseAuth.getInstance();

        editTextId = findViewById(R.id.editTextId);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignup = findViewById(R.id.buttonSignup);

        buttonLogin.setOnClickListener(view -> loginUser());

        buttonSignup.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = editTextId.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                        // ✅ 메인 대시보드로 이동
                        Intent intent = new Intent(LoginActivity.this, MaindashboardActivity.class);
                        intent.putExtra("userEmail", email); // 이메일 전달 (선택사항)
                        startActivity(intent);
                        finish(); // 로그인 액티비티 종료

                    } else {
                        Toast.makeText(this, "아이디 또는 비밀번호가 틀렸습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
