package com.example.sns_project;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private final static String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth = 인스턴스 초기화
        mAuth = FirebaseAuth.getInstance();

        findViewById(R.id.btnSignUp).setOnClickListener(onClickListener);
        findViewById(R.id.btnBack).setOnClickListener(onClickListener);

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
                case R.id.btnSignUp:
                    signUp();
                    break;
                case R.id.btnBack:
                    startLoginActivity();
                    break;
            }
        }
    };

    private void signUp() {
        String email = ((EditText) findViewById(R.id.etEmailAddress)).getText().toString();
        String password = ((EditText) findViewById(R.id.etPassword)).getText().toString();
        String passwordCheck = ((EditText) findViewById(R.id.etPasswordCheck)).getText().toString();

        if (email.length() > 0 && password.length() > 0 && passwordCheck.length() > 0) {
            if (password.equals(passwordCheck)) {
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information = 로그인 성공했을 때 로직
//                                Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    startToast("회원가입이 완료되었습니다.");
                                    startLoginActivity();
//                            updateUI(user);
                                } else {
                                    if (task.getException() != null)
                                        startToast(task.getException().toString());
                                    // If sign in fails, display a message to the user. = 로그인 실패했을 때 로직
//                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
//                            updateUI(null);
                                }
                            }
                        });
            } else {
                startToast("비밀번호가 일치하지 않습니다.");
            }
        } else {
            startToast("이메일 또는 비밀번호를 입력해주세요.");
        }

    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        finishAffinity();
        startActivity(intent);
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
