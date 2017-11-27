package com.emildesign.applicaster_assignment.features.search;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.emildesign.applicaster_assignment.R;
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

import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func3;
import rx.schedulers.Schedulers;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class SearchActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, GooglePlayServicesAuthenticationHandler.GooglePlayServicesHandlerCallback {

    private RecyclerView mRecyclerView;
    private ArrayList<YouTubeVideoData> mYouTubeVideoDataArrayList;
    private SearchResultRecyclerViewAdapter mAdapter;
    private YouTubeAPIServiceHandler mYouTubeAPIServiceHandler;
    private GooglePlayServicesAuthenticationHandler mGooglePlayServicesAuthenticationHandler;
    private String mLastSearchRequestText;
    private Subscription mSearchSubscribe;
    private ArrayList<String> mVideoIds;
    private ArrayList<String> mPlaylistIds;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGooglePlayServicesAuthenticationHandler = new GooglePlayServicesAuthenticationHandler(this, this);
        initViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!AndroidUtils.isDeviceOnline(this)) {
            //TODO: Show error.
            //mOutputText.setText("No network connection available.");
        } else {
            if (!mGooglePlayServicesAuthenticationHandler.isAuthenticated()) {
                mGooglePlayServicesAuthenticationHandler.runAuthenticationSequence();
            }
        }
    }

    private void initViews() {
        initActionBar();
        initRecyclerView();
    }

    private void initActionBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.card_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new SimpleRecyclerViewDividerItemDecoration(this));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mSearchView != null) {
            String lastSearchText = mSearchView.getQuery().toString();
            outState.putString("last_search", lastSearchText);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem search = menu.findItem(R.id.search);

        mSearchView = (SearchView) search.getActionView();
        setOnQueryTextListener(mSearchView);

        if (mYouTubeVideoDataArrayList == null || mYouTubeVideoDataArrayList.isEmpty()) {
            mSearchView.setIconified(false);
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void removeOnQueryTextListener(SearchView aSearchView) {
        aSearchView.setOnQueryTextListener(null);
    }

    private void setOnQueryTextListener(SearchView aSearchView) {
        aSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO: filtering is committed here
                //if (mAdapter != null) mAdapter.getFilter().filter(newText);

                //Instead making a request to youtube api:
                if (newText.length() > 1) {
                    mLastSearchRequestText = newText;
                    runSearchYouTubeApi();
                }
                return true;
            }
        });
    }

    private void runSearchYouTubeApi() {
        Observable<List<SearchResult>> fetchSearchResultsFromYouTubeObservable = getYouTubeAPIServiceHandler().generateSearchObservableWithSearchText(mLastSearchRequestText);
        mSearchSubscribe = fetchSearchResultsFromYouTubeObservable
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
            //TODO: Show dialog with the error.
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
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case GooglePlayServicesAuthenticationHandler.REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode != RESULT_OK) {
                    /*mOutputText.setText(
                            "This app requires Google Play Services. Please install " +
                                    "Google Play Services on your device and relaunch this app.");*/
                    //TODO: Show dialog with this error
                } else {
                    runSearchYouTubeApi();
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
     * @param requestCode The request code associated with the requested
     *         permission
     * @param list The requested permission list. Never null.
     */
    @Override
    public void onPermissionsGranted(int requestCode, List<String> list) {
        // Do nothing.
    }

    /**
     * Callback for when a permission is denied using the EasyPermissions
     * library.
     * @param requestCode The request code associated with the requested
     *         permission
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

        String playlistIdString = TextUtils.join(",", mPlaylistIds);
        Observable<List<Playlist>> playlistSearchObservableWithIds = getYouTubeAPIServiceHandler().generatePlaylistSearchObservableWithIds(playlistIdString);

        Observable<ArrayList<YouTubeVideoData>> currentList = Observable.just(mYouTubeVideoDataArrayList);

        Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>> func3 = new Func3<List<Video>, List<Playlist>, List<YouTubeVideoData>, List<YouTubeVideoData>>() {
            @Override
            public List<YouTubeVideoData> call(List<Video> aVideos, List<Playlist> aPlaylists, List<YouTubeVideoData> aYouTubeVideoData) {
                for (YouTubeVideoData youTubeVideoDataItem : aYouTubeVideoData) {
                    for (Video video : aVideos) {
                        if (youTubeVideoDataItem.getVideoId().equals(video.getId())) {
                            youTubeVideoDataItem.setVideoDuration(video.getContentDetails().getDuration());
                            break;
                        }
                    }

                    for (Playlist playlist : aPlaylists) {
                        if (youTubeVideoDataItem.getPlayListId().equals(playlist.getId())) {
                            youTubeVideoDataItem.setPlayListTitle(playlist.getSnippet().getTitle());
                            break;
                        }
                    }
                }

                return aYouTubeVideoData;
            }
        };

        Observable<List<YouTubeVideoData>> zip = Observable.zip(videoSearchObservableWithIds, playlistSearchObservableWithIds, currentList, func3);
        mSearchSubscribe = zip
        .subscribeOn(Schedulers.newThread())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<List<YouTubeVideoData>>() {
            @Override
            public void onCompleted() {
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(SearchActivity.this, "there was and error: " + e, Toast.LENGTH_SHORT).show();
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

    private void displaySearchResult() {
        mAdapter = new SearchResultRecyclerViewAdapter(this, mYouTubeVideoDataArrayList);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void generateYouTubeVideoDataArray(List<SearchResult> aSearchResults, ArrayList<YouTubeVideoData> aYouTubeVideoDataArrayList) {
        YouTubeVideoData item;

        mVideoIds = new ArrayList<>();
        mPlaylistIds = new ArrayList<>();

        for (SearchResult searchResult : aSearchResults) {
            String mediumThumbnailUrl = searchResult.getSnippet().getThumbnails().getMedium().getUrl();
            String title = searchResult.getSnippet().getTitle();
            DateTime publishedAt = searchResult.getSnippet().getPublishedAt();

            String videoId = searchResult.getId().getVideoId();
            mVideoIds.add(videoId);

            String playlistId = searchResult.getId().getPlaylistId();
            mPlaylistIds.add(playlistId);

            item = new YouTubeVideoData(mediumThumbnailUrl, title, publishedAt, playlistId, videoId);
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
}
