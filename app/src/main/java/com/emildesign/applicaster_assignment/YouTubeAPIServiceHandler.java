package com.emildesign.applicaster_assignment;

import android.app.Activity;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.util.HashMap;
import java.util.List;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by EmilAdz on 11/23/17.
 */

public class YouTubeAPIServiceHandler implements GooglePlayServicesAuthenticationHandler.GooglePlayServicesHandlerCallback {

    //KEYS
    public static final String PART = "part";
    public static final String MAX_RESULTS = "maxResults";
    public static final String QUERY = "q";
    public static final String TYPE = "type";
    public static final String ID = "id";

    //VALUES
    public static final String SNIPPET = "snippet";
    public static final String VIDEO = "video";
    public static final String MAX_RETURN_VALUES = "10";
    public static final String CONTENT_DETAILS = "contentDetails";
    public static final String CHANNEL_ID = "channelId";


    GoogleAccountCredential mCredential;
    public static final int REQUEST_AUTHORIZATION = 1001;

    public YouTubeAPIServiceHandler(GoogleAccountCredential aCredential) {
        this.mCredential = aCredential;
    }

    private YouTube getYouTubeDataApiService(GoogleAccountCredential credential) {
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        YouTube youTube = new YouTube.Builder(transport, jsonFactory, credential)
                .setApplicationName("applicaster-assignment")
                .build();

        return youTube;
    }

    public Observable<List<SearchResult>> generateSearchObservableWithSearchText(final String searchText) {
        Observable<List<SearchResult>> fetchSearchResultsFromYouTube = Observable.create(new Observable.OnSubscribe<List<SearchResult>>() {
            @Override
            public void call(Subscriber<? super List<SearchResult>> subscriber) {
                try {
                    YouTube youTubeDataApiService = getYouTubeDataApiService(mCredential);
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(PART, SNIPPET);
                    parameters.put(MAX_RESULTS, MAX_RETURN_VALUES);
                    parameters.put(QUERY, searchText);
                    parameters.put(TYPE, VIDEO);

                    YouTube.Search.List searchListByKeywordRequest = youTubeDataApiService.search().list(parameters.get(PART).toString());
                    if (parameters.containsKey(MAX_RESULTS)) {
                        searchListByKeywordRequest.setMaxResults(Long.parseLong(parameters.get(MAX_RESULTS).toString()));
                    }

                    if (parameters.containsKey(QUERY) && parameters.get(QUERY) != "") {
                        searchListByKeywordRequest.setQ(parameters.get(QUERY).toString());
                    }

                    if (parameters.containsKey(TYPE) && parameters.get(TYPE) != "") {
                        searchListByKeywordRequest.setType(parameters.get(TYPE).toString());
                    }

                    SearchListResponse response = searchListByKeywordRequest.execute();
                    List<SearchResult> items = response.getItems();
                    subscriber.onNext(items);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return fetchSearchResultsFromYouTube;
    }

    public Observable<List<Video>> generateVideoSearchObservableWithIds(final String ids) {
        Observable<List<Video>> fetchVideoResultsFromYouTube = Observable.create(new Observable.OnSubscribe<List<Video>>() {
            @Override
            public void call(Subscriber<? super List<Video>> subscriber) {
                try {
                    YouTube youTubeDataApiService = getYouTubeDataApiService(mCredential);
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(PART, CONTENT_DETAILS);
                    parameters.put(ID, ids);

                    YouTube.Videos.List videosListMultipleIdsRequest = youTubeDataApiService.videos().list(parameters.get(PART).toString());
                    if (parameters.containsKey(ID) && parameters.get(ID) != "") {
                        videosListMultipleIdsRequest.setId(parameters.get(ID).toString());
                    }

                    VideoListResponse response = videosListMultipleIdsRequest.execute();
                    List<Video> items = response.getItems();
                    subscriber.onNext(items);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return fetchVideoResultsFromYouTube;
    }

    public Observable<List<Playlist>> generatePlaylistSearchObservableWithIds(final String ids) {
        Observable<List<Playlist>> fetchPlaylistResultsFromYouTube = Observable.create(new Observable.OnSubscribe<List<Playlist>>() {
            @Override
            public void call(Subscriber<? super List<Playlist>> subscriber) {
                try {
                    YouTube youTubeDataApiService = getYouTubeDataApiService(mCredential);
                    HashMap<String, String> parameters = new HashMap<>();
                    parameters.put(PART, SNIPPET);
                    parameters.put(CHANNEL_ID, ids);
                    parameters.put(MAX_RESULTS, "10");

                    YouTube.Playlists.List playlistsListByChannelIdRequest = youTubeDataApiService.playlists().list(parameters.get(PART).toString());
                    if (parameters.containsKey(CHANNEL_ID) && parameters.get(CHANNEL_ID) != "") {
                        playlistsListByChannelIdRequest.setChannelId(parameters.get(CHANNEL_ID).toString());
                    }

                    if (parameters.containsKey(MAX_RESULTS)) {
                        playlistsListByChannelIdRequest.setMaxResults(Long.parseLong(parameters.get(MAX_RESULTS).toString()));
                    }

                    PlaylistListResponse response = playlistsListByChannelIdRequest.execute();
                    List<Playlist> items = response.getItems();
                    subscriber.onNext(items);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return fetchPlaylistResultsFromYouTube;
    }

    @Override
    public void onAccountSelected() {

    }
}
