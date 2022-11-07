package com.example.sns_project.activity;

import static com.example.sns_project.Util.INTENT_PATH;
import static com.example.sns_project.Util.showToast;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;
import com.example.sns_project.BackKeyHandler;
import com.example.sns_project.MemberInfo;
import com.example.sns_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MemberInitActivity extends BasicActivity {

    private String profilePath;
    private FirebaseUser user;
    private RelativeLayout loaderLayout;
    private static final String TAG = "MemberInfoActivity";
    private ImageView profileImageView;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_info);
        setToolbarTitle("회원정보");

        loaderLayout = findViewById(R.id.loaderLayout);
        profileImageView = findViewById(R.id.ivProfile);
        profileImageView.setOnClickListener(onClickListener);

        findViewById(R.id.btnConfirm).setOnClickListener(onClickListener);
        findViewById(R.id.picture).setOnClickListener(onClickListener);
        findViewById(R.id.gallery).setOnClickListener(onClickListener);
    }

    View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            case R.id.btnConfirm:
                storageUpload();
                break;
            case R.id.ivProfile:
                CardView cardView = findViewById(R.id.buttonsCardView);
                Log.e("로그", "로그: " + cardView.getVisibility());
                if (cardView.getVisibility() == view.VISIBLE) {
                    cardView.setVisibility(view.GONE);
                } else {
                    cardView.setVisibility(view.VISIBLE);
                }
                break;
            case R.id.picture:
                startMyActivity(CameraActivity.class);
                break;
            case R.id.gallery:
                startMyActivity(GalleryActivity.class);
                break;
        }
    };

    private void storageUpload() {
        final String name = ((EditText) findViewById(R.id.etName)).getText().toString();
        final String birth = ((EditText) findViewById(R.id.etBirth)).getText().toString();
        final String phone = ((EditText) findViewById(R.id.etPhone)).getText().toString();
        final String address = ((EditText) findViewById(R.id.etAddress)).getText().toString();

        if (name.length() > 0 && phone.length() > 9 && birth.length() > 5 && address.length() > 0) {

            loaderLayout.setVisibility(View.VISIBLE);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            user = FirebaseAuth.getInstance().getCurrentUser();

            final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImages.jpg");

            if (profilePath == null) {
                MemberInfo memberInfo = new MemberInfo(name, birth, phone, address);
                storeUploader(memberInfo);
            } else {
                try {
                    InputStream stream = new FileInputStream(profilePath);

                    UploadTask uploadTask = mountainImagesRef.putStream(stream);
                    uploadTask.continueWithTask(task -> {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        return mountainImagesRef.getDownloadUrl();
                    }).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            MemberInfo memberInfo = new MemberInfo(name, birth, phone, address, downloadUri.toString());
                            storeUploader(memberInfo);

                            Log.e("성공", "성공: " + downloadUri);

                        } else {
                            Log.e("로그", "실패");
                        }
                    });

                } catch (FileNotFoundException e) {
                    Log.e("로그: ", "에러: " + e);
                }
            }

        } else {
            showToast(MemberInitActivity.this, "회원정보를 입력해주세요.");
        }

    }

    private void storeUploader(MemberInfo memberInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(aVoid -> {
                    showToast(MemberInitActivity.this, "회원정보 등록을 성공하였습니다.");
                    loaderLayout.setVisibility(View.GONE);
                    startMyActivity(MainActivity.class);
                })
                .addOnFailureListener(e -> {
                    showToast(MemberInitActivity.this, "회원정보 등록에 실패하였습니다.");
                    loaderLayout.setVisibility(View.GONE);
                    Log.w(TAG, "Error writing document", e);
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    profilePath = data.getStringExtra(INTENT_PATH);
                    Log.e("로그: ", "profilePath: " + profilePath);
                    Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageView);
                }
                break;
        }
    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        if (C.equals(CameraActivity.class) || C.equals(GalleryActivity.class))
            startActivityForResult(intent, 0);
        else {
            finishAffinity();
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}
