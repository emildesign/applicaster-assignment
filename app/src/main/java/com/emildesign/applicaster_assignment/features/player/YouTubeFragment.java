package com.emildesign.applicaster_assignment.features.player;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emildesign.applicaster_assignment.R;
import com.emildesign.applicaster_assignment.dialog.DialogUtils;
import com.emildesign.applicaster_assignment.dialog.GeneralDialogFragment;
import com.emildesign.applicaster_assignment.features.search.SearchActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.OnInitializedListener;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.Locale;

/**
 * Created by EmilAdz on 11/28/17.
 */

public class YouTubeFragment extends Fragment {

    public static final String VIDEO_ID = "videoId";
    private static final String YOUTUBE_DEVELOPER_KEY = "AIzaSyA7yygxFvL0sIT-45KePWtSP7tfcQkztuA";

    private AppCompatActivity myContext;
    private YouTubePlayer mYouTubePlayer;

    // Create an instance of the Dialog with the input
    public static YouTubeFragment newInstance(String videoId) {
        YouTubeFragment frag = new YouTubeFragment();
        Bundle args = new Bundle();
        args.putString(VIDEO_ID, videoId);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {

        if (activity instanceof AppCompatActivity) {
            myContext = (AppCompatActivity) activity;
        }

        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_you_tube_fragment, container, false);
        YouTubePlayerSupportFragment youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
        youTubePlayerFragment.initialize(YOUTUBE_DEVELOPER_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player, boolean wasRestored) {

                if (!wasRestored) {
                    mYouTubePlayer = player;
                    //mYouTubePlayer.setFullscreen(true);
                    mYouTubePlayer.loadVideo(getArguments().getString(VIDEO_ID));
                    mYouTubePlayer.play();
                }
            }

            @Override
            public void onInitializationFailure(YouTubePlayer.Provider arg0, YouTubeInitializationResult arg1) {
                DialogUtils.showGeneralErrorDialog(myContext, String.format(Locale.US,getString(R.string.video_init_error), arg1.name()));
            }
        });

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_fragment, youTubePlayerFragment).commit();

        return rootView;
    }
}
