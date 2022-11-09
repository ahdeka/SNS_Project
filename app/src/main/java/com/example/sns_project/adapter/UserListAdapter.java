package com.example.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.FirebaseHelper;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.UserInfo;
import com.example.sns_project.activity.PostActivity;
import com.example.sns_project.activity.WritePostActivity;
import com.example.sns_project.listener.OnPostListener;
import com.example.sns_project.view.ReadContentsView;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.ArrayList;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {
    private ArrayList<UserInfo> localDataSet;
    private Activity activity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
        }

    }

    public UserListAdapter(Activity activity, ArrayList<UserInfo> dataSet) {
        this.localDataSet = dataSet;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_user_list, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(cardView);

        cardView.setOnClickListener(view -> {
//            Intent intent = new Intent(activity, PostActivity.class);
//            intent.putExtra("postInfo", localDataSet.get(viewHolder.getAdapterPosition()));
//            activity.startActivity(intent);
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        CardView cardView = viewHolder.cardView;
        ImageView photoImageView = cardView.findViewById(R.id.photoImageView);
        TextView nameTextView = cardView.findViewById(R.id.nameTextView);
        TextView addressTextView = cardView.findViewById(R.id.addressTextView);

        UserInfo userInfo = localDataSet.get(position);
        if (localDataSet.get(position).getPhotoUri() != null) {
            Glide.with(activity).load(localDataSet.get(position).getPhotoUri()).centerCrop().override(500).into(photoImageView);
        }
        nameTextView.setText(userInfo.getName());
        addressTextView.setText(userInfo.getAddress());
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

}
