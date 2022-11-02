package com.example.sns_project.adapter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.sns_project.R;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private ArrayList<String> localDataSet;
    private Activity activity;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;

        public ViewHolder(CardView view) {
            super(view);
            // Define click listener for the ViewHolder's View
            cardView = view;

//            cardView = (TextView) view.findViewById(R.id.textView);

        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     *                by RecyclerView.
     */
    public GalleryAdapter(Activity activity, ArrayList<String> dataSet) {
        localDataSet = dataSet;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override @NonNull
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        CardView cardView = (CardView) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_gallery, viewGroup, false);

        final ViewHolder viewHolder = new ViewHolder(cardView);

        cardView.setOnClickListener(view -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("profilePath", localDataSet.get(viewHolder.getAdapterPosition()));
            activity.setResult(Activity.RESULT_OK, resultIntent);
            activity.finish();
        });

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        CardView cardView = viewHolder.cardView;

        ImageView imageView = cardView.findViewById(R.id.imageView);
        Glide.with(activity).load(localDataSet.get(position)).centerCrop().override(500).into(imageView);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
