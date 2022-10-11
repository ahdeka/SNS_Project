package com.example.sns_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.sns_project.R;
import com.example.sns_project.WriteInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

public class WritePostActivity extends BasicActivity {

    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirte_post);

        parent = findViewById(R.id.contentsLayout);

        findViewById(R.id.btnCheck).setOnClickListener(onClickListener);
        findViewById(R.id.btnImage).setOnClickListener(onClickListener);
        findViewById(R.id.btnVideo).setOnClickListener(onClickListener);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {

                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);
                    System.out.println("잘됐나: " + pathList.size());

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    parent.addView(imageView);

                    TextInputEditText textInputEditText = new TextInputEditText(WritePostActivity.this);
                    textInputEditText.setLayoutParams(layoutParams);
                    textInputEditText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    parent.addView(textInputEditText);
                }
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnCheck:
                    postUpload();
                    break;
                case R.id.btnImage:
                    startMyActivity(GalleryActivity.class, "image");
                    break;
                case R.id.btnVideo:
                    startMyActivity(GalleryActivity.class, "video");
                    break;
            }
        }
    };

    private void postUpload() {
        final String title = ((TextInputEditText) findViewById(R.id.etWriteTitle)).getText().toString();

        if (title.length() > 0) {
            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();

            for (int i = 0; i < 1; i++) {
                View view = parent.getChildAt(i);

                if (view instanceof TextInputEditText) {
                    String text = ((TextInputEditText) view).getText().toString();
                    if (text.length() > 0) {
                        contentsList.add(text);
                    }
                } else {
                    contentsList.add(pathList.get(pathCount));
                    final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/" + pathCount + "jpg");

                    try {
                        InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                        StorageMetadata metadata = new StorageMetadata.Builder().
                                setCustomMetadata("index", "" + (contentsList.size()-1)).build();
                        UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));

                                mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        Log.e("로그:", "로그: " + uri);
                                        Log.v("로그:", "로그: " + uri);
                                        contentsList.set(index, uri.toString());
                                        successCount++;
                                        if (pathList.size() == successCount) {
                                            // 완료
                                            WriteInfo writeInfo = new WriteInfo(title, contentsList, user.getUid(), new Date());
                                            uploader(writeInfo);
                                            for(int j = 0; j<contentsList.size(); j++) {
                                                System.out.println(contentsList.size());
                                                Log.e("로그: ", "콘텐츠: " + contentsList.get(j));
                                            }
                                        }
                                    }
                                });
                            }
                        });

                    } catch (FileNotFoundException e) {
                        Log.e("로그: ", "에러: " + e.toString());
                    }

                    pathCount++;
                }
            }

        } else {
            startToast("회원정보를 입력해주세요.");
        }
    }

    private void uploader(WriteInfo writeInfo) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").add(writeInfo)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        startToast("게시글을 등록했습니다.");
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }

    private void startMyActivity(Class C, String media) {
        Intent intent = new Intent(this, C);
        if (C.equals(MainActivity.class)) {
            finishAffinity();
            startActivity(intent);
        } else {
            intent.putExtra("media", media);
            startActivityForResult(intent, 0);
        }
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

}
