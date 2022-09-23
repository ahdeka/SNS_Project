package com.example.sns_project;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.google.firebase.auth.UserProfileChangeRequest;

public class MemberInfo extends AppCompatActivity {

    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);

        findViewById(R.id.btnConfirm).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnConfirm:
                    profileUpdate();
                    break;
            }
        }
    };

    private void profileUpdate() {
        String name = ((EditText) findViewById(R.id.etName)).getText().toString();

        if (name.length() > 0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName("Jane Q. User")
                    .setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))
                    .build();

            if (user != null) {
                user.updateProfile(profileUpdates)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startToast("회원정보가 수정됐습니다.");
                                    finish();
                                }
                            }
                        });
            }

        } else {
            startToast("회원정보를 입력해주세요.");
        }

    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
