package com.example.sns_project.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.UserInfo;
import com.example.sns_project.activity.LoginActivity;
import com.example.sns_project.activity.MemberInitActivity;
import com.example.sns_project.activity.WritePostActivity;
import com.example.sns_project.adapter.UserListAdapter;
import com.example.sns_project.listener.OnPostListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MyInfoFragment extends Fragment {
    private static final String TAG = "MyInfoFragment";

    public MyInfoFragment() {
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
        View view = inflater.inflate(R.layout.fragment_my_info, container, false);
        final ImageView myProfileImageView = view.findViewById(R.id.myProfileImageView);
        final TextView userEmail = view.findViewById(R.id.userEmail);
        final TextView nameTextView = view.findViewById(R.id.nameTextView);
        final TextView birthTextView = view.findViewById(R.id.birthTextView);
        final TextView phoneTextView = view.findViewById(R.id.phoneTextView);
        final TextView addressTextView = view.findViewById(R.id.addressTextView);
        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "로그아웃 하였습니다", Toast.LENGTH_SHORT).show();
                startMyActivity(LoginActivity.class);
                FirebaseAuth.getInstance().signOut();
                getActivity().finish();
            }
        });

        DocumentReference documentReference = FirebaseFirestore.getInstance().collection("users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {

                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if (document.getData().get("photoUri") != null) {
                            Glide.with(getActivity()).load(document.getData().get("photoUri")).centerCrop().override(500).into(myProfileImageView);
                        }
                        userEmail.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                        nameTextView.setText(document.getData().get("name").toString());
                        birthTextView.setText(document.getData().get("birth").toString());
                        phoneTextView.setText(document.getData().get("phone").toString());
                        addressTextView.setText(document.getData().get("address").toString());
                    } else {
                        Log.d(TAG, "No such document");
                    }
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        });

        return view;
    }

    private void startMyActivity(Class C) {
        Intent intent = new Intent(getActivity(), C);
        startActivity(intent);
    }
}