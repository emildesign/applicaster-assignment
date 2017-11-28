package com.emildesign.applicaster_assignment.features.search;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.emildesign.applicaster_assignment.R;
import com.emildesign.applicaster_assignment.dialog.DialogUtils;
import com.emildesign.applicaster_assignment.dialog.GeneralDialogFragment;
import com.emildesign.applicaster_assignment.features.player.YouTubeFragment;
import com.emildesign.applicaster_assignment.pojo.YouTubeVideoData;
import com.emildesign.applicaster_assignment.utils.AndroidUtils;
import com.emildesign.applicaster_assignment.utils.GooglePlayServicesAuthenticationHandler;
import com.emildesign.applicaster_assignment.utils.YouTubeAPIServiceHandler;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class SearchActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GooglePlayServicesAuthenticationHandler.GooglePlayServicesHandlerCallback, GeneralDialogFragment.OnDialogFragmentClickListener {

    private static final String TAG = "SearchActivity";
    private RecyclerView mRecyclerView;
    private ArrayList<YouTubeVideoData> mYouTubeVideoDataArrayList;
    private SearchResultRecyclerViewAdapter mAdapter;
    private YouTubeAPIServiceHandler mYouTubeAPIServiceHandler;
    private GooglePlayServicesAuthenticationHandler mGooglePlayServicesAuthenticationHandler;
    private String mLastSearchRequestText;
    private ArrayList<String> mVideoIds;
    private ArrayList<String> mPlaylistIds;
    private SearchView mSearchView;
    private RelativeLayout mFragmentContainer;
    private Observable<YouTubeVideoData> mPositionClicks;
    private YouTubeFragment mYouTubePlayerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        restoreDataIfThereIsAny(savedInstanceState);
        mGooglePlayServicesAuthenticationHandler = new GooglePlayServicesAuthenticationHandler(this, this);
        initViews();
    }

    private void restoreDataIfThereIsAny(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mYouTubeVideoDataArrayList = savedInstanceState.getParcelableArrayList("last_search_result");
            mLastSearchRequestText = savedInstanceState.getString("last_search");
        }
    }

    private void initViews() {
        initActionBar();
        initRecyclerView();
        mFragmentContainer = findViewById(R.id.fragmentContainer);
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.card_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleRecyclerViewDividerItemDecoration(this));

        if (mYouTubeVideoDataArrayList != null) {
            displaySearchResult();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSearchView != null) {
            String lastSearchText = mSearchView.getQuery().toString();
            outState.putString("last_search", lastSearchText);
            outState.putParcelableArrayList("last_search_result", mYouTubeVideoDataArrayList);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem search = menu.findItem(R.id.search);

        mSearchView = (SearchView) search.getActionView();
        if (mLastSearchRequestText != null) {
            mSearchView.setQuery(mLastSearchRequestText, false);
            mLastSearchRequestText = null;
        }

        setOnQueryTextListener(mSearchView);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && mYouTubeVideoDataArrayList == null || mYouTubeVideoDataArrayList.isEmpty()) {
            mSearchView.setIconified(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setOnQueryTextListener(SearchView aSearchView) {
        aSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1) {
                    mLastSearchRequestText = newText;
                    boolean isPassedPreconditions = runPreconditionVerifications();
                    if (isPassedPreconditions) {
                        runSearchYouTubeApi();
                    }
                }
                return true;
            }
        });
    }

    private boolean runPreconditionVerifications() {
        boolean isPassedPreconditions = false;
        if (!AndroidUtils.isDeviceOnline(SearchActivity.this)) {
            DialogUtils.showNetworkErrorDialog(SearchActivity.this);
        } else if (!mGooglePlayServicesAuthenticationHandler.isAuthenticated()) {
            mGooglePlayServicesAuthenticationHandler.runAuthenticationSequence();
        } else {
            isPassedPreconditions = true;
        }

        return isPassedPreconditions;
    }

    private void runSearchYouTubeApi() {
        Observable<List<SearchResult>> fetchSearchResultsFromYouTubeObservable = getYouTubeAPIServiceHandler().generateSearchObservableWithSearchText(mLastSearchRequestText);
        fetchSearchResultsFromYouTubeObservable
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<SearchResult>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(SearchActivity.this, "there was and error: " + e, Toast.LENGTH_SHORT).show();
                handleErrorIfPossible(e);
            }

            @Override
            public void onNext(List<SearchResult> aSearchResults) {
                handleSearchResults(aSearchResults);
            }
        });
    }

    private void handleErrorIfPossible(Throwable e) {
        if (e instanceof GooglePlayServicesAvailabilityIOException) {
            mGooglePlayServicesAuthenticationHandler.showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) e).getConnectionStatusCode());
        } else if (e instanceof UserRecoverableAuthIOException) {
            startActivityForResult(((UserRecoverableAuthIOException) e).getIntent(), mYouTubeAPIServiceHandler.REQUEST_AUTHORIZATION);
        } else {
            DialogUtils.showGeneralErrorDialog(this, e.getMessage());
        }
    }

    /**
     * Called when an activity launched here (specifically, AccountPicker
     * and authorization) exits, giving you the requestCode you started it with,
     * the resultCode it returned, and any additional data from it.
     * @param requestCode code indicating which activity result is incoming.
     * @param resultCode code indicating the result of the incoming
     *     activity result.
     * @param data Intent (containing result data) returned by incoming
     *     activity result.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case GooglePlayServicesAuthenticationHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    DialogUtils.showGooglePlayServicesErrorDialog(this);
                }
                break;
            case GooglePlayServicesAuthenticationHandler.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        handleSelectedAccount(accountName);
                    }
                }
                break;
            case YouTubeAPIServiceHandler.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    runSearchYouTubeApi();
                }
                break;
        }
    }

    private void handleSelectedAccount(String aAccountName) {
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(GooglePlayServicesAuthenticationHandler.PREF_ACCOUNT_NAME, aAccountName);
        editor.apply();
        mGooglePlayServicesAuthenticationHandler.setSelectedAccountName(aAccountName);
    }

    /**
     * Respond to requests for permissions at runtime for API 23 and above.
     * @param requestCode The request code passed in
     *     requestPermissions(android.app.Activity, String, int, String[])
     * @param permissions The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *     which is either PERMISSION_GRANTED or PERMISSION_DENIED. Never null.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Callback for when a permission is granted using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requestedpermission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        if (requestCode == mGooglePlayServicesAuthenticationHandler.REQUEST_PERMISSION_GET_ACCOUNTS) {
            boolean isPassedPreconditions = runPreconditionVerifications();
            if (isPassedPreconditions) {
                runSearchYouTubeApi();
            }
        }
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requestedpermission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsDenied(int requestCode, List<String> list) {
        // Do nothing.
    }

    private void handleSearchResults(List<SearchResult> aSearchResults) {
        mLastSearchRequestText = null;
        mYouTubeVideoDataArrayList = new ArrayList<>();
        generateYouTubeVideoDataArray(aSearchResults, mYouTubeVideoDataArrayList);
        displaySearchResult();
        fetchTheRestOfTheData();
    }

    private void fetchTheRestOfTheData() {
        String videoIdsString = TextUtils.join(",", mVideoIds);
        Observable<List<Video>> videoSearchObservableWithIds = getYouTubeAPIServiceHandler().generateVideoSearchObservableWithIds(videoIdsString);
        Observable<ArrayList<YouTubeVideoData>> currentList = Observable.just(mYouTubeVideoDataArrayList);
        getDurationsAndCombineWithResults(videoSearchObservableWithIds, currentList);

//        boolean isAllPlaylistIdsAreNull = true;
//        for (String playlistId : mPlaylistIds) {
//            if (playlistId != null && !playlistId.equals("null")) {
//                isAllPlaylistIdsAreNull = false;
//            }
//        }
//
//        String playlistIdString;
//        if (!isAllPlaylistIdsAreNull) {
//            playlistIdString = TextUtils.join(",", mPlaylistIds);
//        } else {
//            playlistIdString = null;
//        }
//
//        Observable<List<Playlist>> playlistSearchObservableWithIds = getYouTubeAPIServiceHandler().generatePlaylistSearchObservableWithIds(playlistIdString);
//        Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>> func3 = getFunc3();
//        handleAllThreeResponses(videoSearchObservableWithIds, playlistSearchObservableWithIds, currentList, func3);
    }

    private void getDurationsAndCombineWithResults(Observable<List<Video>> aVideoSearchObservableWithIds, Observable<ArrayList<YouTubeVideoData>> aCurrentList) {
        Func2<List<Video>, List<YouTubeVideoData>, List<YouTubeVideoData>> func2 = getFunc2();
        Observable<List<YouTubeVideoData>> zip = Observable.zip(aVideoSearchObservableWithIds, aCurrentList, func2);
        zip.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<YouTubeVideoData>>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                handleErrorIfPossible(e);
            }

            @Override
            public void onNext(List<YouTubeVideoData> aUpdatedVideoList) {
                mYouTubeVideoDataArrayList = (ArrayList<YouTubeVideoData>) aUpdatedVideoList;
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private void handleAllThreeResponses(Observable<List<Video>> aVideoSearchObservableWithIds, Observable<List<Playlist>> aPlaylistSearchObservableWithIds, Observable<ArrayList<YouTubeVideoData>> aCurrentList, Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>> aFunc3) {
        Observable<List<YouTubeVideoData>> zip = Observable.zip(aVideoSearchObservableWithIds, aPlaylistSearchObservableWithIds, aCurrentList, aFunc3);
        zip.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<YouTubeVideoData>>() {
            @Override
            public void onCompleted() {}

            @Override
            public void onError(Throwable e) {
                handleErrorIfPossible(e);
            }

            @Override
            public void onNext(List<YouTubeVideoData> aUpdatedVideoList) {
                mYouTubeVideoDataArrayList.clear();
                mYouTubeVideoDataArrayList.addAll(aUpdatedVideoList);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @NonNull
    private Func2<List<Video>, List<YouTubeVideoData>, List<YouTubeVideoData>> getFunc2() {
        return new Func2<List<Video>, List<YouTubeVideoData>, List<YouTubeVideoData>>() {
            @Override
            public List<YouTubeVideoData> call(List<Video> aVideos, List<YouTubeVideoData> aYouTubeVideoData) {
                for (YouTubeVideoData youTubeVideoDataItem : aYouTubeVideoData) {
                    for (Video video : aVideos) {
                        if (youTubeVideoDataItem.getVideoId().equals(video.getId())) {
                            youTubeVideoDataItem.setVideoDuration(video.getContentDetails().getDuration());
                        }
                    }
                }

                return aYouTubeVideoData;
            }
        };
    }

    @NonNull
    private Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>> getFunc3() {
        return new Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>>() {
                @Override
                public List<YouTubeVideoData> call(List<Video> aVideos, List<Playlist> aPlaylists, List<YouTubeVideoData> aYouTubeVideoData) {
            for (YouTubeVideoData youTubeVideoDataItem : aYouTubeVideoData) {
                for (Video video : aVideos) {
                    if (youTubeVideoDataItem.getVideoId().equals(video.getId())) {
                        youTubeVideoDataItem.setVideoDuration(video.getContentDetails().getDuration());
                    }
                }

                for (Playlist playlist : aPlaylists) {
                    if (youTubeVideoDataItem.getPlayListId().equals(playlist.getId())) {
                        youTubeVideoDataItem.setPlayListTitle(playlist.getSnippet().getTitle());
                    }
                }
            }

            return aYouTubeVideoData;
            }
        };
    }

    private void displaySearchResult() {
        mAdapter = new SearchResultRecyclerViewAdapter(this, mYouTubeVideoDataArrayList);
        mRecyclerView.setAdapter(mAdapter);

        mPositionClicks = mAdapter.getPositionClicks();
        mPositionClicks.subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Action1<YouTubeVideoData>() {
            @Override
            public void call(YouTubeVideoData aYouTubeVideoData) {
                showVideo(aYouTubeVideoData);
            }
        });
    }

    private void showVideo(final YouTubeVideoData aYouTubeVideoData) {
        mYouTubePlayerFragment = YouTubeFragment.newInstance(aYouTubeVideoData.getVideoId());
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.addToBackStack("video");
        transaction.add(R.id.fragmentContainer, mYouTubePlayerFragment).commit();
    }

    private void generateYouTubeVideoDataArray(List<SearchResult> aSearchResults, ArrayList<YouTubeVideoData> aYouTubeVideoDataArrayList) {
        YouTubeVideoData item;

        mVideoIds = new ArrayList<>();
        mPlaylistIds = new ArrayList<>();

        for (SearchResult searchResult : aSearchResults) {
            String mediumThumbnailUrl = searchResult.getSnippet().getThumbnails().getMedium().getUrl();
            String title = searchResult.getSnippet().getTitle();
            DateTime publishedAt = searchResult.getSnippet().getPublishedAt();

            SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

            String formattedDate = null;
            Date date;
            try {
                date = form.parse(publishedAt.toString());
                SimpleDateFormat postFormatter = new SimpleDateFormat("HH:mm dd/MM/yyyy");
                formattedDate = postFormatter.format(date);
            } catch (Exception e) {
                Log.e(TAG, String.format(Locale.US, getString(R.string.date_formating_error), e));
            }

            String videoId = searchResult.getId().getVideoId();
            mVideoIds.add(videoId);

            String playlistId = searchResult.getId().getPlaylistId();
            mPlaylistIds.add(playlistId);

            item = new YouTubeVideoData(mediumThumbnailUrl, title, publishedAt, playlistId, videoId, formattedDate);
            aYouTubeVideoDataArrayList.add(item);
        }
    }

    @Override
    public void onAccountSelected() {
        if (mLastSearchRequestText != null) {
            runSearchYouTubeApi();
        }
    }

    public YouTubeAPIServiceHandler getYouTubeAPIServiceHandler() {
        if (mYouTubeAPIServiceHandler == null) {
            mYouTubeAPIServiceHandler = new YouTubeAPIServiceHandler(mGooglePlayServicesAuthenticationHandler.getCredential());
        }

        return mYouTubeAPIServiceHandler;
    }

    @Override
    public void onOkClicked(GeneralDialogFragment dialog) {
        //String tag = dialog.getTag();
    }

    @Override
    public void onCancelClicked(GeneralDialogFragment dialog) {

    }
}
