package com.example.sns_project.activity;

import static com.example.sns_project.Util.GALLERY_IMAGE;
import static com.example.sns_project.Util.GALLERY_VIDEO;
import static com.example.sns_project.Util.INTENT_MEDIA;
import static com.example.sns_project.Util.INTENT_PATH;
import static com.example.sns_project.Util.isImageFile;
import static com.example.sns_project.Util.isStorageUri;
import static com.example.sns_project.Util.isVideoFile;
import static com.example.sns_project.Util.showToast;
import static com.example.sns_project.Util.storageUriToName;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.view.ContentsItemView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

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
        setToolbarTitle("게시글 작성");

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
                if (isStorageUri(contents)) {
                    pathList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);
                    contentsItemView.setImage(contents);
                    contentsItemView.setOnClickListener(view -> {
                        btnBackgroundLayout.setVisibility(View.VISIBLE);
                        selectedImageView = (ImageView) view;
                    });
                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                    if (i < contentsList.size() - 1) {
                        String nextContents = contentsList.get(i + 1);
                        if (!isStorageUri(nextContents)) {
                            contentsItemView.setText(nextContents);
                        }
                    }
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
                    String path = data.getStringExtra(INTENT_PATH);
                    pathList.add(path);

                    ContentsItemView contentsItemView = new ContentsItemView(this);

                    if (selectedEditText == null) {
                        parent.addView(contentsItemView);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(contentsItemView, i + 1);
                                break;
                            }
                        }
                    }

                    contentsItemView.setImage(path);
                    contentsItemView.setOnClickListener(view -> {
                        btnBackgroundLayout.setVisibility(View.VISIBLE);
                        selectedImageView = (ImageView) view;
                    });
                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                }
                break;
            case 1:
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra(INTENT_PATH);
                    pathList.set(parent.indexOfChild((View) selectedImageView.getParent()) - 1, path);
                    Glide.with(this).load(path).override(1000).into(selectedImageView);
                }
                break;
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
                    startMyActivity(GalleryActivity.class, GALLERY_IMAGE, 0);
                    break;
                case R.id.btnVideo:
                    startMyActivity(GalleryActivity.class, GALLERY_VIDEO, 0);
                    break;

                case R.id.btnBackgroundLayout:
                    if (btnBackgroundLayout.getVisibility() == View.VISIBLE) {
                        btnBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                case R.id.imageModify:
                    startMyActivity(GalleryActivity.class, GALLERY_IMAGE, 1);
                    btnBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.videoModify:
                    startMyActivity(GalleryActivity.class, GALLERY_VIDEO, 1);
                    btnBackgroundLayout.setVisibility(View.GONE);
                    break;
                case R.id.delete:
                    final View selectedView = (View) selectedImageView.getParent();
                    String path = pathList.get(parent.indexOfChild(selectedView) - 1);
                    if (isStorageUri(path)) {
                        StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/"
                                + storageUriToName(path));
                        desertRef.delete().addOnSuccessListener(aVoid -> {
                                    showToast(WritePostActivity.this, "파일을 삭제하였습니다.");
                                    pathList.remove(parent.indexOfChild(selectedView) - 1);
                                    parent.removeView(selectedView);
                                    btnBackgroundLayout.setVisibility(View.GONE);
                                })
                                .addOnFailureListener(exception -> showToast(WritePostActivity.this, "파일을 삭제하는데 실패했습니다."));

                    } else {
                        pathList.remove(parent.indexOfChild(selectedView) - 1);
                        parent.removeView(selectedView);
                        btnBackgroundLayout.setVisibility(View.GONE);
                    }
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
            final ArrayList<String> formatList = new ArrayList<>();
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
                            formatList.add("text");
                        }
                    } else if (!isStorageUri(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);

                        if (isImageFile(path)) {
                            formatList.add("image");
                        } else if (isVideoFile(path)) {
                            formatList.add("video");
                        } else {
                            formatList.add("text");
                        }

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
                                        PostInfo postInfo = new PostInfo(title, contentsList, formatList, user.getUid(), date);
                                        storeUpload(documentReference, postInfo);
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
                storeUpload(documentReference, new PostInfo(title, contentsList, formatList, user.getUid(), date));
            }

        } else {
            showToast(WritePostActivity.this, "제목을 입력해주세요.");
        }
    }

    private void storeUpload(DocumentReference documentReference, final PostInfo postInfo) {
        documentReference.set(postInfo.getPostInfo())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "DocumentSnapshot successfully written!");
                    showToast(WritePostActivity.this, "게시글을 등록했습니다.");
                    loaderLayout.setVisibility(View.GONE);
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("postInfo", postInfo);
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error writing document", e);
                    loaderLayout.setVisibility(View.GONE);
                });
    }

    private void startMyActivity(Class C, int media, int requestCode) {
        Intent intent = new Intent(this, C);
        if (C.equals(MainActivity.class)) {
            finishAffinity();
            startActivity(intent);
        } else {
            intent.putExtra(INTENT_MEDIA, media);
            startActivityForResult(intent, requestCode);
        }
    }

}
