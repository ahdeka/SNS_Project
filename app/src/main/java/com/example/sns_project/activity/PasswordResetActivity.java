package com.example.sns_project.activity;

import static com.example.sns_project.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.example.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class PasswordResetActivity extends BasicActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_reset);

        // Initialize Firebase Auth = 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSend).setOnClickListener(onClickListener);
        findViewById(R.id.btnBack).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            case R.id.btnSend:
                send();
                break;
            case R.id.btnBack:
                startLoginActivity();
                break;
        }
    };

    private void send() {
        String email = ((EditText) findViewById(R.id.etEmailAddress)).getText().toString();

        if (email.length() > 0) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);
            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        loaderLayout.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            showToast(PasswordResetActivity.this, "이메일을 보냈습니다.");
                        }
                    });
        } else {
            showToast(PasswordResetActivity.this, "이메일을 입력하세요.");
        }

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        finishAffinity();
        startActivity(intent);
    }

}
