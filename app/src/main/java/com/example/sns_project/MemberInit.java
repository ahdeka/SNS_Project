package com.example.sns_project;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MemberInit extends AppCompatActivity {

    private String profilePath;
    private static final String TAG = "MemberInfoActivity";
    private ImageView profileImageView;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);

        profileImageView = findViewById(R.id.ivProfile);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.btnConfirm).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnConfirm:
                    profileUpdate();
                    break;
                case R.id.ivProfile:
                    startMyActivity(CameraActivity.class);
                    break;
            }
        }
    };

    private void profileUpdate() {
        final String name = ((EditText) findViewById(R.id.etName)).getText().toString();
        final String birth = ((EditText) findViewById(R.id.etBirth)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.etPhone)).getText().toString();
        final String address = ((EditText) findViewById(R.id.etAddress)).getText().toString();

        if (name.length() > 0 && phone.length() > 9 && birth.length() > 5 && address.length() > 0) {

            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImages.jpg");

            try {
                InputStream stream = new FileInputStream(new File(profilePath));

                UploadTask uploadTask = mountainImagesRef.putStream(stream);
                uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return mountainImagesRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            Log.e("성공", "성공: " + downloadUri);
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            MemberInfo memberInfo = new MemberInfo(name, birth, phone, address, downloadUri.toString());
                            db.collection("users").document(user.getUid()).set(memberInfo)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            startToast("회원정보 등록을 성공하였습니다.");
                                            startMyActivity(MainActivity.class);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            startToast("회원정보 등록에 실패하였습니다.");
                                            Log.w(TAG, "Error writing document", e);
                                        }
                                    });
                        } else {
                            Log.e("로그", "실패");
                        }
                    }
                });

            } catch (FileNotFoundException e) {
                Log.e("로그: ", "에러: " + e.toString());
            }

        } else {
            startToast("회원정보를 입력해주세요.");
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    profilePath = data.getStringExtra("profilePath");
                    Log.e("로그: ", "profilePath: " + profilePath);
                    Bitmap bitmap = BitmapFactory.decodeFile(profilePath);
                    profileImageView.setImageBitmap(bitmap);
                }
                break;
        }
    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        if(C.equals(CameraActivity.class))
            startActivityForResult(intent, 0);
        else{
            finishAffinity();
            startActivity(intent);
        }
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}
