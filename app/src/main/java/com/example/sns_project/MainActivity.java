package com.example.sns_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;


public class MainActivity extends AppCompatActivity {

    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btnLogout).setOnClickListener(onClickListener);



        if (user == null) {
            startMyActivity(LoginActivity.class);
        } else {
            // 회원가입 or 로그인
            for (UserInfo profile : user.getProviderData()) {
                String name = profile.getDisplayName();
                Log.e("이름", "ㅁㄴㅇㅁㄴ");
                if(name != null){
                    if(name.length() == 0)
                        startMyActivity(MemberInit.class);
                }
            }
        }

    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnLogout:
                    FirebaseAuth.getInstance().signOut();
                    startMyActivity(LoginActivity.class);
                    break;
            }
        }
    };

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}