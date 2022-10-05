package com.example.sns_project.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.sns_project.BackKeyHandler;
import com.example.sns_project.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);
    private TextView userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        findViewById(R.id.btnLogout).setOnClickListener(onClickListener);
        findViewById(R.id.btnWritePost).setOnClickListener(onClickListener);
        userName = findViewById(R.id.tvUser);

        if (user == null) {
            startMyActivity(LoginActivity.class);

        } else {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {

                            if (document.exists()) {
                                userName.setText(user.getUid());
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");

                                startMyActivity(MemberInitActivity.class);
                            }
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
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
                case R.id.btnWritePost:
                    startMyActivity(WritePostActivity.class);
                    break;
            }
        }
    };

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        if (C.equals(LoginActivity.class)) {
            finishAffinity();
            startActivity(intent);
        } else {
            startActivityForResult(intent, 1);
        }
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}