package com.example.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sns_project.FirebaseHelper;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.example.sns_project.activity.PostActivity;
import com.example.sns_project.activity.WritePostActivity;
import com.example.sns_project.listener.OnPostListener;
import com.example.sns_project.view.ReadContentsView;
import com.google.android.exoplayer2.ExoPlayer;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {

    private ArrayList<PostInfo> localDataSet;
    private Activity activity;
    private FirebaseHelper firebaseHelper;
    private ArrayList<ArrayList<ExoPlayer>> ArrayListOfPlayerArrayList = new ArrayList<>();
    private final int MORE_INDEX = 2;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        public ViewHolder(CardView view) {
            super(view);
            cardView = view;
        }

    }

    public HomeAdapter(Activity activity, ArrayList<PostInfo> dataSet) {
        this.localDataSet = dataSet;
        this.activity = activity;
        firebaseHelper = new FirebaseHelper(activity);
    }

    public void setOnPostListener(OnPostListener onPostListener) {
        firebaseHelper.setOnPostListener(onPostListener);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_post, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(cardView);

        cardView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, PostActivity.class);
            intent.putExtra("postInfo", localDataSet.get(viewHolder.getAdapterPosition()));
            activity.startActivity(intent);
        });

        cardView.findViewById(R.id.postMenu).setOnClickListener(view -> showPopup(view, viewHolder.getAdapterPosition()));

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        CardView cardView = viewHolder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);

        PostInfo postInfo = localDataSet.get(position);
        titleTextView.setText(postInfo.getTitle());

        ReadContentsView readContentsView = cardView.findViewById(R.id.readContentsView);
        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);

        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {
            contentsLayout.setTag(postInfo);
            contentsLayout.removeAllViews();

            readContentsView.setMoreIndex(MORE_INDEX);
            readContentsView.setPostInfo(postInfo);

            ArrayList<ExoPlayer> playerArrayList = readContentsView.getPlayerArrayList();
            if (playerArrayList != null) {
                ArrayListOfPlayerArrayList.add(playerArrayList);
            }
        }
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void showPopup(View v, int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.modify:
                    startMyActivity(WritePostActivity.class, localDataSet.get(position));
                    return true;
                case R.id.delete:
                    firebaseHelper.storageDelete(localDataSet.get(position));
                    return true;
                default:
                    return false;
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.show();
    }

    public void playerStop() {
        for (int i = 0; i < ArrayListOfPlayerArrayList.size(); i++) {
            ArrayList<ExoPlayer> playerArrayList = ArrayListOfPlayerArrayList.get(i);
            for (int ii = 0; ii < playerArrayList.size(); ii++) {
                ExoPlayer player = playerArrayList.get(ii);
                if(player.getPlayWhenReady()){
                    player.setPlayWhenReady(false);
                }
            }
        }
    }

    private void startMyActivity(Class C, PostInfo postInfo) {
        Intent intent = new Intent(activity, C);
        intent.putExtra("postInfo", postInfo);
        activity.startActivity(intent);
    }
}
