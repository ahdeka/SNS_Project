package com.example.sns_project.activity;

import static com.example.sns_project.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.example.sns_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends BasicActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setToolbarTitle("회원가입");

        // Initialize Firebase Auth = 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUp).setOnClickListener(onClickListener);
        findViewById(R.id.btnBack).setOnClickListener(onClickListener);

    }

    View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            case R.id.btnSignUp:
                signUp();
                break;
            case R.id.btnBack:
                startLoginActivity();
                break;
        }
    };

    private void signUp() {
        String email = ((EditText) findViewById(R.id.etEmailAddress)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.etPasswordCheck)).getText().toString();

        if (email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            if (password.equals(passwordCheck)) {
                final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
                loaderLayout.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, task -> {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                showToast(SignUpActivity.this, "회원가입이 완료되었습니다.");
                                startLoginActivity();
                            } else {
                                if (task.getException() != null)
                                    showToast(SignUpActivity.this, task.getException().toString());
                            }
                        });
            } else {
                showToast(SignUpActivity.this, "비밀번호가 일치하지 않습니다.");
            }
        } else {
            showToast(SignUpActivity.this, "이메일 또는 비밀번호를 입력해주세요.");
        }

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        finishAffinity();
        startActivity(intent);
    }

}
