package com.emildesign.applicaster_assignment.features.search;

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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.BaseTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.transition.Transition;
import com.emildesign.applicaster_assignment.R;
import com.emildesign.applicaster_assignment.pojo.YouTubeVideoData;

import java.util.ArrayList;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class SearchResultRecyclerViewAdapter extends RecyclerView.Adapter<SearchResultRecyclerViewAdapter.ViewHolder> {

    private ArrayList<YouTubeVideoData> mYouTubeVideoDataArrayList;
    private Context mContext;
    private final PublishSubject<YouTubeVideoData> onClickSubject = PublishSubject.create();

    public SearchResultRecyclerViewAdapter(Activity aActivity, ArrayList<YouTubeVideoData> arrayList) {
        mContext = aActivity;
        mYouTubeVideoDataArrayList = arrayList;
    }

    @Override
    public SearchResultRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_you_tube_video_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SearchResultRecyclerViewAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mVideoImage.setImageDrawable(null);
        viewHolder.mPlaceHolder.setVisibility(View.VISIBLE);

        final YouTubeVideoData youTubeVideoDataItem = mYouTubeVideoDataArrayList.get(i);

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSubject.onNext(youTubeVideoDataItem);
            }
        });

        viewHolder.mTitle.setText(youTubeVideoDataItem.getTitle());
        viewHolder.mPublishedAt.setText(String.format(mContext.getString(R.string.published_at), youTubeVideoDataItem.getPublishedDate().toString()));

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

        Glide.with(mContext).load(youTubeVideoDataItem.getVideoImage())
                .transition(DrawableTransitionOptions.withCrossFade()).into(target);

        if (youTubeVideoDataItem.getVideoDuration() != null) {
            String durationInHumanReadableForm = youTubeVideoDataItem.getDurationInHumanReadableForm();
            viewHolder.mDuration.setText(durationInHumanReadableForm);
            if (viewHolder.mDuration.getVisibility() != View.VISIBLE) {
                viewHolder.mDuration.setAlpha(0.0f);
                viewHolder.mDuration.setVisibility(View.VISIBLE);
                viewHolder.mDuration.animate().alpha(1.0f).setDuration(200);
            }
        }
    }

    public Observable<YouTubeVideoData> getPositionClicks(){
        return onClickSubject.asObservable();
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