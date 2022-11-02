package com.example.sns_project.activity;

import static com.example.sns_project.Util.showToast;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.BackKeyHandler;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.adapter.PostAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;


public class MainActivity extends BasicActivity {

    private static final String TAG = "MainActivity";
    private FirebaseUser firebaseUser;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView mRecyclerView;
    private PostAdapter postAdapter;
    private ArrayList<PostInfo> postList;
    private StorageReference storageRef;
    private int successCount;
    private BackKeyHandler backKeyHandler = new BackKeyHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        if (firebaseUser == null) {
            startMyActivity(LoginActivity.class);

        } else {

            firebaseFirestore = FirebaseFirestore.getInstance();
            DocumentReference documentReference = firebaseFirestore.collection("users").document(firebaseUser.getUid());
            documentReference.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {

                        if (document.exists()) {
                            Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        } else {
                            Log.d(TAG, "No such document");
                            startMyActivity(MemberInitActivity.class);
                        }
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            });

//
//            firebaseFirestore.collection("posts")
//                    .get()
//                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                            if (task.isSuccessful()) {
//                                ArrayList<PostInfo> postList = new ArrayList<>();
//                                for (QueryDocumentSnapshot document : task.getResult()) {
//                                    Log.d(TAG, document.getId() + " => " + document.getData());
//                                    postList.add(new PostInfo(
//                                        document.getData().get("title").toString(),
//                                        (ArrayList<String>) document.getData().get("contents"),
//                                        document.getData().get("publisher").toString(),
//                                        new Date(document.getDate("createdAt").getTime()),
//                                        document.getId()));
//                                }
//                                RecyclerView mRecyclerView = findViewById(R.id.recyclerMainView);
//                                mRecyclerView.setHasFixedSize(true);
//                                mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//
//                                RecyclerView.Adapter mAdapter = new PostAdapter(MainActivity.this, postList);
//                                mRecyclerView.setAdapter(mAdapter);
//                            } else {
//                                Log.d(TAG, "Error getting documents: ", task.getException());
//                            }
//                        }
//                    });

        }

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(MainActivity.this, postList);
        postAdapter.setOnPostListener(onPostListener);

        mRecyclerView = findViewById(R.id.recyclerMainView);
        findViewById(R.id.btnWritePost).setOnClickListener(onClickListener);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mRecyclerView.setAdapter(postAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        postsUpdate();
    }

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(int position) {
            final String id = postList.get(position).getId();
            ArrayList<String> contentsList = postList.get(position).getContents();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (Patterns.WEB_URL.matcher(contents).matches() && contents.contains("https://firebasestorage.googleapis.com/v0/b/sns-project-3731f.appspot.com/o/posts")) {
                    successCount++;
                    String[] list = contents.split("\\?");
                    String[] list2 = list[0].split("%2F");
                    String name = list2[list2.length - 1];
                    StorageReference desertRef = storageRef.child("posts/" + id + "/" + name);
                    desertRef.delete().addOnSuccessListener(aVoid -> {
                                successCount--;
                                storeUploader(id);
                            })
                            .addOnFailureListener(exception -> showToast(MainActivity.this, "ERROR"));
                }
            }
            storeUploader(id);
        }

        @Override
        public void onModify(int position) {
            startMyActivity(WritePostActivity.class, postList.get(position));
        }
    };

    View.OnClickListener onClickListener = view -> {
        switch (view.getId()) {
            /*
            case R.id.btnLogout:
                FirebaseAuth.getInstance().signOut();
                startMyActivity(LoginActivity.class);
                break;
             */
            case R.id.btnWritePost:
                startMyActivity(WritePostActivity.class);
                break;
        }
    };

    private void postsUpdate() {

        if (firebaseUser != null) {
            CollectionReference collectionReference = firebaseFirestore.collection("posts");

            collectionReference
                    .orderBy("createdAt", Query.Direction.DESCENDING).get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            postList.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                postList.add(new PostInfo(
                                        document.getData().get("title").toString(),
                                        (ArrayList<String>) document.getData().get("contents"),
                                        document.getData().get("publisher").toString(),
                                        new Date(document.getDate("createdAt").getTime()),
                                        document.getId()));
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    });
        }
    }

    private void storeUploader(String id) {
        if (successCount == 0) {
            firebaseFirestore.collection("posts").document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast(MainActivity.this, "게시글을 삭제하였습니다");
                        postsUpdate();
                    })
                    .addOnFailureListener(e -> showToast(MainActivity.this, "게시글을 삭제하지 못하였습니다"));
        }

    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(this, C);
        if (C.equals(LoginActivity.class)) {
            finishAffinity();
            startActivity(intent);
        } else {
            startActivityForResult(intent, 1);
        }
    }

    private void startMyActivity(Class C, PostInfo postInfo) {
        Intent intent = new Intent(this, C);
        intent.putExtra("postInfo", postInfo);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        backKeyHandler.onBackPressed();
    }
}