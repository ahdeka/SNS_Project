package com.example.sns_project.activity;

import static com.example.sns_project.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.example.sns_project.BackKeyHandler;
import com.example.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends BasicActivity {

    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolbarTitle("로그인");

        // Initialize Firebase Auth = 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnLogin).setOnClickListener(onClickListener);
        findViewById(R.id.btnGoToSignUp).setOnClickListener(onClickListener);
        findViewById(R.id.btnGoToPasswordReset).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnLogin:
                    login();
                    break;
                case R.id.btnGoToSignUp:
                    startMyActivity(SignUpActivity.class);
                    break;
                case R.id.btnGoToPasswordReset:
                    startMyActivity(PasswordResetActivity.class);
                    break;
            }
        }
    };

    private void login() {
        String email = ((EditText) findViewById(R.id.etEmailAddress)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();

        if (email.length() > 0 && password.length() > 0) {
            final RelativeLayout loaderLayout = findViewById(R.id.loaderLayout);
            loaderLayout.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            loaderLayout.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                showToast(LoginActivity.this, "로그인에 성공했습니다.");
                                startMyActivity(MainActivity.class);
                            } else {
                                if (task.getException() != null)
                                    showToast(LoginActivity.this, task.getException().toString());
                            }
                        }
                    });
        } else {
            showToast(LoginActivity.this, "이메일 또는 비밀번호를 입력해주세요.");
        }

    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        if (C.equals(MainActivity.class))
            finishAffinity();
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}
