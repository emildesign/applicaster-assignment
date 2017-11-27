package com.emildesign.applicaster_assignment;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> /*implements Filterable*/ {
    private ArrayList<YouTubeVideoData> mYouTubeVideoDataArrayList;
    private Context mContext;
    private RequestOptions mOptions;

    public DataAdapter(Activity aActivity, ArrayList<YouTubeVideoData> arrayList) {
        mContext = aActivity;
        mYouTubeVideoDataArrayList = arrayList;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_you_tube_video_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final DataAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mPlaceHolder.setVisibility(View.VISIBLE);
        viewHolder.mTitle.setText(mYouTubeVideoDataArrayList.get(i).getTitle());
        viewHolder.mPublishedAt.setText(String.format(mContext.getString(R.string.published_at), mYouTubeVideoDataArrayList.get(i).getPublishedDate().toString());
        BaseTarget target = new BaseTarget<BitmapDrawable>() {
            @Override
            public void onResourceReady(BitmapDrawable bitmap, Transition<? super BitmapDrawable> transition) {
                viewHolder.mVideoImage.setImageDrawable(bitmap);
                viewHolder.mPlaceHolder.setVisibility(View.GONE);
            }

            @Override
            public void getSize(SizeReadyCallback cb) {
                cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL);
            }

            @Override
            public void removeCallback(SizeReadyCallback cb) {}
        };

        Glide.with(mContext).load(mYouTubeVideoDataArrayList.get(i).getVideoImage()).into(target);

        if (mYouTubeVideoDataArrayList.get(i).getVideoDuration() != null) {
            viewHolder.mDuration.setText((int) mYouTubeVideoDataArrayList.get(i).getDurationInMiliseconds());
        }


        //TODO: Load image into view
    }

    @Override
    public int getItemCount() {
        return mYouTubeVideoDataArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView mVideoImage;
        private ImageView mPlaceHolder;
        private TextView mTitle;
        private TextView mDuration;
        private TextView mPublishedAt;

        public ViewHolder(View view) {
            super(view);
            mVideoImage = view.findViewById(R.id.ivVideoImage);
            mTitle = view.findViewById(R.id.tvTitle);
            mPublishedAt = view.findViewById(R.id.tvPublishedAt);
            mDuration = view.findViewById(R.id.tvDuration);
            mPlaceHolder = view.findViewById(R.id.ivPlaceHolder);
        }
    }
}