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
import com.example.sns_project.UserInfo;
import com.example.sns_project.activity.WritePostActivity;
import com.example.sns_project.adapter.UserListAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class UserListFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private FirebaseFirestore firebaseFirestore;
    private UserListAdapter userListAdapter;
    private ArrayList<UserInfo> userList;
    private boolean updating;
    private boolean topScrolled;

    public UserListFragment() {
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
        View view = inflater.inflate(R.layout.fragment_user_list, container, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userListAdapter = new UserListAdapter(getActivity(), userList);

        final RecyclerView mRecyclerView = view.findViewById(R.id.recyclerUserListView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(userListAdapter);
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
                    userList.clear();
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
            userList.remove(postInfo);
            userListAdapter.notifyDataSetChanged();

            Log.e("로그: ", "삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그: ", "수정 성공");
        }
    };

    private void postsUpdate(final boolean clear) {
        updating = true;
//        Date date = userList.size() == 0 || clear ? new Date() : userList.get(userList.size() - 1).getCreatedAt();
        CollectionReference collectionReference = firebaseFirestore.collection("users");
        collectionReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (clear) {
                            userList.clear();
                        }
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
                            userList.add(new UserInfo(
                                    document.getData().get("name").toString(),
                                    document.getData().get("phone").toString(),
                                    document.getData().get("birth").toString(),
                                    document.getData().get("address").toString(),
                                    document.getData().get("photoUri") == null ? null : document.getData().get("photoUri").toString()));
                        }
                        userListAdapter.notifyDataSetChanged();
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