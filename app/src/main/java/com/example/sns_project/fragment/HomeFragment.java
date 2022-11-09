package com.example.sns_project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.activity.WritePostActivity;
import com.example.sns_project.adapter.HomeAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Date;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore firebaseFirestore;
    private HomeAdapter postAdapter;
    private ArrayList<PostInfo> postList;
    private boolean updating;
    private boolean topScrolled;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        postList = new ArrayList<>();
        postAdapter = new HomeAdapter(getActivity(), postList);
        postAdapter.setOnPostListener(onPostListener);

        RecyclerView mRecyclerView = view.findViewById(R.id.recyclerMainView);
        view.findViewById(R.id.btnWritePost).setOnClickListener(onClickListener);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(postAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();

                if (newState == 1 && firstVisibleItemPosition == 0) {
                    topScrolled = true;
                }
                if (newState == 0 && topScrolled) {
                    postList.clear();
                    postsUpdate(true);
                    topScrolled = false;
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getChildCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                int lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();

                if (totalItemCount - 3 <= lastVisibleItemPosition && updating) {
                    postsUpdate(false);
                }

                if (firstVisibleItemPosition > 0) {
                    topScrolled = false;
                }

            }
        });

        postsUpdate(false);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        postAdapter.playerStop();
    }

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

    OnPostListener onPostListener = new OnPostListener() {
        @Override
        public void onDelete(PostInfo postInfo) {
            postList.remove(postInfo);
            postAdapter.notifyDataSetChanged();

            Log.e("로그: ", "삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그: ", "수정 성공");
        }
    };

    private void postsUpdate(final boolean clear) {
        updating = true;
        Date date = postList.size() == 0 || clear ? new Date() : postList.get(postList.size() - 1).getCreatedAt();
        CollectionReference collectionReference = firebaseFirestore.collection("posts");
        collectionReference
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .whereLessThan("createdAt", date).limit(10).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (clear) {
                            postList.clear();
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            postList.add(new PostInfo(
                                    document.getData().get("title").toString(),
                                    (ArrayList<String>) document.getData().get("contents"),
                                    (ArrayList<String>) document.getData().get("formats"),
                                    document.getData().get("publisher").toString(),
                                    new Date(document.getDate("createdAt").getTime()),
                                    document.getId()));
                        }
                        postAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                    updating = false;
                });
    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(getActivity(), C);
        startActivityForResult(intent, 0);
    }

}