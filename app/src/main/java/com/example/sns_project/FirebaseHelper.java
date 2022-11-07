package com.example.sns_project;

import static com.example.sns_project.Util.isStorageUri;
import static com.example.sns_project.Util.showToast;
import static com.example.sns_project.Util.storageUriToName;

import android.app.Activity;

import com.example.sns_project.listener.OnPostListener;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class FirebaseHelper {
    private Activity activity;
    private OnPostListener onPostListener;
    private int successCount;

    public FirebaseHelper(Activity activity) {
        this.activity = activity;
    }

    public void setOnPostListener(OnPostListener onPostListener) {
        this.onPostListener = onPostListener;
    }

    public void storageDelete(PostInfo postInfo) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        final String id = postInfo.getId();
        ArrayList<String> contentsList = postInfo.getContents();
        for (int i = 0; i < contentsList.size(); i++) {
            String contents = contentsList.get(i);
            if (isStorageUri(contents)) {
                successCount++;
                StorageReference desertRef = storageRef.child("posts/" + id + "/" + storageUriToName(contents));
                desertRef.delete().addOnSuccessListener(aVoid -> {
                            successCount--;
                            storeDelete(id);
                        })
                        .addOnFailureListener(exception -> showToast(activity, "ERROR"));
            }
        }
        storeDelete(id);
    }

    public void storeDelete(String id) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (successCount == 0) {
            firebaseFirestore.collection("posts").document(id)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        showToast(activity, "게시글을 삭제하였습니다");
//                        postsUpdate();
                    })
                    .addOnFailureListener(e -> showToast(activity, "게시글을 삭제하지 못하였습니다"));
        }

    }
}
