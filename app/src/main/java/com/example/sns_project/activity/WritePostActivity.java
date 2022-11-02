package com.example.sns_project.activity;

import static com.example.sns_project.Util.showToast;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private RelativeLayout btnBackgroundLayout;
    private RelativeLayout loaderLayout;
    private ImageView selectedImageView;
    private EditText selectedEditText;
    private EditText etWriteTitle;
    private EditText etWriteContents;
    private PostInfo postInfo;
    private StorageReference storageRef;
    private int pathCount, successCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wirte_post);


        loaderLayout = findViewById(R.id.loaderLayout);
        parent = findViewById(R.id.contentsLayout);
        btnBackgroundLayout = findViewById(R.id.btnBackgroundLayout);
        etWriteTitle = findViewById(R.id.etWriteTitle);
        etWriteContents = findViewById(R.id.etWriteContents);

        findViewById(R.id.btnCheck).setOnClickListener(onClickListener);
        findViewById(R.id.btnImage).setOnClickListener(onClickListener);
        findViewById(R.id.btnVideo).setOnClickListener(onClickListener);
        findViewById(R.id.imageModify).setOnClickListener(onClickListener);
        findViewById(R.id.videoModify).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);

        btnBackgroundLayout.setOnClickListener(onClickListener);
        etWriteTitle.setOnFocusChangeListener(onFocusChangeListener);
        etWriteContents.setOnFocusChangeListener((view, b) -> {
            if (b) {
                selectedEditText = null;
            }
        });


        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
        postInit();

    }

    private void postInit() {
        if (postInfo != null) {
            etWriteTitle.setText(postInfo.getTitle());
            ArrayList<String> contentsList = postInfo.getContents();

            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-3731f.appspot.com/o/posts")) {
                    pathList.add(contents);
                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    parent.addView(linearLayout);

                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setOnClickListener(view -> {
                        btnBackgroundLayout.setVisibility(View.VISIBLE);
                        selectedImageView = (ImageView) view;
                    });
                    Glide.with(this).load(contents).override(1000).into(imageView);
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    if (i < contentsList.size() - 1) {
                        String nextContents = contentsList.get(i + 1);
                        if (!Patterns.WEB_URL.matcher(nextContents).matches() || !nextContents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-3731f.appspot.com/o/posts")) {
                            editText.setText(nextContents);
                        }
                    }
                    editText.setOnFocusChangeListener(onFocusChangeListener);
                    linearLayout.addView(editText);
                } else if (i == 0) {
                    etWriteContents.setText(contents);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {

                    String profilePath = data.getStringExtra("profilePath");
                    pathList.add(profilePath);

                    ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);

                    LinearLayout linearLayout = new LinearLayout(WritePostActivity.this);
                    linearLayout.setLayoutParams(layoutParams);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);

                    if (selectedEditText == null) {
                        parent.addView(linearLayout);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(linearLayout, i + 1);
                                break;
                            }
                        }
                    }


                    ImageView imageView = new ImageView(WritePostActivity.this);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setAdjustViewBounds(true);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setOnClickListener(view -> {
                        btnBackgroundLayout.setVisibility(View.VISIBLE);
                        selectedImageView = (ImageView) view;
                    });
                    Glide.with(this).load(profilePath).override(1000).into(imageView);
                    linearLayout.addView(imageView);

                    EditText editText = new EditText(WritePostActivity.this);
                    editText.setLayoutParams(layoutParams);
                    editText.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_CLASS_TEXT);
                    editText.setHint("내용");
                    editText.setOnFocusChangeListener(onFocusChangeListener);
                    linearLayout.addView(editText);
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String profilePath = data.getStringExtra("profilePath");
                    pathList.set(parent.indexOfChild((View) selectedImageView.getParent()) - 1, profilePath);
                    Glide.with(this).load(profilePath).override(1000).into(selectedImageView);
                }
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnCheck:
                    storageUpload();
                    break;
                case R.id.btnImage:
                    startMyActivity(GalleryActivity.class, "image", 0);
                    break;
                case R.id.btnVideo:
                    startMyActivity(GalleryActivity.class, "video", 0);
                    break;

                case R.id.btnBackgroundLayout:
                    if (btnBackgroundLayout.getVisibility() == View.VISIBLE) {
                        btnBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.imageModify:
                    startMyActivity(GalleryActivity.class, "image", 1);
                    btnBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.videoModify:
                    startMyActivity(GalleryActivity.class, "video", 1);
                    btnBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.delete:
                    View selectedView = (View) selectedImageView.getParent();

                    String[] list = pathList.get(parent.indexOfChild(selectedView) - 1).split("\\?");
                    String[] list2 = list[0].split("%2F");
                    String name = list2[list2.length - 1];

                    StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/" + name);
                    desertRef.delete().addOnSuccessListener(aVoid -> {
//                                successCount--;
//                                storeUploader(id);
                                showToast(WritePostActivity.this, "파일을 삭제하였습니다.");
                            })
                            .addOnFailureListener(exception -> showToast(WritePostActivity.this, "파일을 삭제하는데 실패했습니다."));

                    pathList.remove(parent.indexOfChild(selectedView) - 1);
                    parent.removeView(selectedView);
                    btnBackgroundLayout.setVisibility(View.GONE);
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) {
                selectedEditText = (EditText) view;
            }
        }
    };

    private void storageUpload() {
        final String title = ((EditText) findViewById(R.id.etWriteTitle)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            final DocumentReference documentReference = postInfo == null ?
                    firebaseFirestore.collection("posts").document()
                    : firebaseFirestore.collection("posts").document(postInfo.getId());
            final Date date = postInfo == null ? new Date() : postInfo.getCreatedAt();

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);

                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);

                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();

                        if (text.length() > 0) {
                            contentsList.add(text);
                        }
                    } else if (!Patterns.WEB_URL.matcher(pathList.get(pathCount)).matches()) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        String[] pathArray = path.split("\\.");
                        final StorageReference mountainImagesRef = storageRef.child
                                ("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);

                        try {
                            InputStream stream = new FileInputStream(pathList.get(pathCount));
                            StorageMetadata metadata = new StorageMetadata.Builder().
                                    setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(exception -> {
                                // Handle unsuccessful uploads

                            }).addOnSuccessListener(taskSnapshot -> {
                                final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));

                                mountainImagesRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    contentsList.set(index, uri.toString());
                                    successCount--;
                                    if (successCount == 0) {
                                        // 완료
                                        PostInfo postInfo1 = new PostInfo(title, contentsList, user.getUid(), date);
                                        storeUploader(documentReference, postInfo1);
                                    }
                                });
                            });

                        } catch (FileNotFoundException e) {
                            Log.e("로그: ", "에러: " + e);
                        }

                        pathCount++;
                    }
                }

            }
            if (successCount == 0) {
                storeUploader(documentReference, new PostInfo(title, contentsList, user.getUid(), date));
            }

        } else {
            showToast(WritePostActivity.this, "제목을 입력해주세요.");
        }
    }

    private void storeUploader(DocumentReference documentReference, PostInfo postInfo) {
        documentReference.set(postInfo)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    showToast(WritePostActivity.this, "게시글을 등록했습니다.");
                    loaderLayout.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document", e);
                    loaderLayout.setVisibility(View.GONE);
                });
    }

    private void startMyActivity(Class C, String media, int requestCode) {
        Intent intent = new Intent(this, C);
        if (C.equals(MainActivity.class)) {
            finishAffinity();
            startActivity(intent);
        } else {
            intent.putExtra("media", media);
            startActivityForResult(intent, requestCode);
        }
    }

}
