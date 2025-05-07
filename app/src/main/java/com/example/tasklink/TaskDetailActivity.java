package com.example.tasklink;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TaskDetailActivity extends AppCompatActivity {

    private static final int FILE_PICK_CODE = 1001;

    TextView textDeadline, textFile, textAssignUser;
    EditText editDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.taskdetail);

        // 버튼과 뷰 연결
        ImageButton backBtn = findViewById(R.id.button_back);
        ImageButton chatBtn = findViewById(R.id.button_chat);
        ImageButton addUserBtn = findViewById(R.id.button_add_user);
        ImageButton dateBtn = findViewById(R.id.button_pick_date);
        ImageButton fileBtn = findViewById(R.id.button_pick_file);

        textDeadline = findViewById(R.id.text_deadline);
        textFile = findViewById(R.id.text_file);
        textAssignUser = findViewById(R.id.text_assign_user);

        editDescription = findViewById(R.id.edit_description);

// 화면 전체 높이
        int screenHeight = getResources().getDisplayMetrics().heightPixels;

// 상단바 + 담당자/마감 등 예상 높이 (400dp)
        int estimatedHeaderHeight = (int) (getResources().getDisplayMetrics().density * 400);

// 남은 공간을 설명창에 최소 높이로 설정
        int desiredMinHeight = screenHeight - estimatedHeaderHeight;

// 설정 (내부 스크롤 방지 + 아래까지 확장)
        editDescription.setMinHeight(desiredMinHeight);
        editDescription.setVerticalScrollBarEnabled(false);
        editDescription.setMovementMethod(null); // 내부 스크롤 막음



        // 1. 뒤로가기
        backBtn.setOnClickListener(v -> finish());

        // 2. 채팅 버튼
        chatBtn.setOnClickListener(v ->
                Toast.makeText(this, "채팅 기능은 준비 중입니다", Toast.LENGTH_SHORT).show()
        );

        // 3. 담당자 추가 (예시: 임의 사용자 추가)
        addUserBtn.setOnClickListener(v -> {
            textAssignUser.setText("👤 담당자: 김담당");
            Toast.makeText(this, "담당자가 추가되었습니다", Toast.LENGTH_SHORT).show();
        });

        // 4. 마감일 선택
        dateBtn.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int y = calendar.get(Calendar.YEAR);
            int m = calendar.get(Calendar.MONTH);
            int d = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String dateStr = "📅 마감: " + year + "-" + (month + 1) + "-" + dayOfMonth;
                textDeadline.setText(dateStr);
            }, y, m, d);
            dialog.show();
        });

        // 5. 파일 선택
        fileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // 모든 파일 허용
            startActivityForResult(Intent.createChooser(intent, "파일 선택"), FILE_PICK_CODE);
        });
    }

    // 파일 선택 결과 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            String fileName = uri.getLastPathSegment();
            textFile.setText("📁 파일: " + fileName);
        }
    }
}
