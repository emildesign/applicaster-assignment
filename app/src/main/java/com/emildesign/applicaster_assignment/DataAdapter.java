package com.emildesign.applicaster_assignment;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.ViewHolder> /*implements Filterable*/ {
    private ArrayList<YouTubeVideoData> mYouTubeVideoDataArrayList;
    private ArrayList<YouTubeVideoData> mFilteredList;

    public DataAdapter(ArrayList<YouTubeVideoData> arrayList) {
        mYouTubeVideoDataArrayList = arrayList;
        mFilteredList = arrayList;
    }

    @Override
    public DataAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.layout_you_tube_video_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DataAdapter.ViewHolder viewHolder, int i) {
        viewHolder.mTitle.setText(mYouTubeVideoDataArrayList.get(i).getTitle());
        viewHolder.mDuration.setText(mYouTubeVideoDataArrayList.get(i).getVideoDuration());
        //viewHolder.mTvApiLevel.setText(mFilteredList.get(i).getApi());

        //TODO: Load image into view
    }

    @Override
    public int getItemCount() {
        return mFilteredList.size();
    }

    /*@Override
    public Filter getFilter() {

        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                String charString = charSequence.toString();

                if (charString.isEmpty()) {
                    mFilteredList = mYouTubeVideoDataArrayList;
                } else {
                    ArrayList<YouTubeVideoData> filteredList = new ArrayList<>();
                    for (YouTubeVideoData androidVersion : mYouTubeVideoDataArrayList) {
                       *//* if (androidVersion.getApi().toLowerCase().contains(charString) || androidVersion.getName().toLowerCase().contains(charString) || androidVersion.getVer().toLowerCase().contains(charString)) {
                            filteredList.add(androidVersion);
                        }*//*
                    }

                    mFilteredList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = mFilteredList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                mFilteredList = (ArrayList<YouTubeVideoData>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }*/

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView mVideoImage;
        private TextView mTitle;
        private TextView mDuration;

        public ViewHolder(View view) {
            super(view);

            mVideoImage = view.findViewById(R.id.ivVideoImage);
            mTitle = view.findViewById(R.id.tvTitle);
            mDuration = view.findViewById(R.id.tvDuration);
        }
    }
}