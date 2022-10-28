package com.example.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.util.Patterns;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.PostInfo;
import com.example.sns_project.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    private ArrayList<PostInfo> localDataSet;
    private Activity activity;
    private FirebaseFirestore firebaseFirestore;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        public ViewHolder(Activity activity, CardView view, PostInfo postInfo) {
            super(view);
            cardView = view;

            LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);
            ViewGroup.LayoutParams layoutParams = new ViewGroup
                    .LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            ArrayList<String> contentsList = postInfo.getContents();

            if (contentsLayout.getChildCount() == 0) { // 이 조건식은, 스크롤할 때 뷰가 더 추가되지 않게하기 위함
                for (int i = 0; i < contentsList.size(); i++) {
                    String contents = contentsList.get(i);
                    if (Patterns.WEB_URL.matcher(contents).matches()) {
                        ImageView imageView = new ImageView(activity);
                        imageView.setLayoutParams(layoutParams);
                        imageView.setAdjustViewBounds(true);
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        contentsLayout.addView(imageView);
                    } else {
                        TextView textView = new TextView(activity);
                        textView.setLayoutParams(layoutParams);
                        contentsLayout.addView(textView);
                    }
                }
            }
        }

        public CardView getCardView() {
            return cardView;
        }
    }

    public PostAdapter(Activity activity, ArrayList<PostInfo> dataSet) {
        localDataSet = dataSet;
        this.activity = activity;
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    // Create new views (invoked by the layout manager)
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        CardView cardView = (CardView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_post, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(activity, cardView, localDataSet.get(viewType));

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        cardView.findViewById(R.id.postMenu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopup(view, viewHolder.getAdapterPosition());
            }
        });

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        CardView cardView = viewHolder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);
        titleTextView.setText(localDataSet.get(position).getTitle());

        TextView createAtTextView = cardView.findViewById(R.id.createAtTextView);
        createAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(localDataSet.get(position).getCreatedAt()));

        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);
        ArrayList<String> contentsList = localDataSet.get(position).getContents();

        for (int i = 0; i < contentsList.size(); i++) {
            String contents = contentsList.get(i);
            if (Patterns.WEB_URL.matcher(contents).matches()) {
                Glide.with(activity).load(contents).override(1000).thumbnail(0.1f).into((ImageView)contentsLayout.getChildAt(i));
            } else {
                ((TextView)contentsLayout.getChildAt(i)).setText(contents);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void showPopup(View v, int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.modify:

                        return true;
                    case R.id.delete:
                        firebaseFirestore.collection("posts").document(localDataSet.get(position).getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startToast("게시글을 삭제하였습니다.");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        startToast("게시글을 삭제하는데 실패했습니다.");
                                    }
                                });
                        return true;
                    default:
                        return false;
                }
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.show();
    }

    private void startToast(String msg) {
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
    }

}
