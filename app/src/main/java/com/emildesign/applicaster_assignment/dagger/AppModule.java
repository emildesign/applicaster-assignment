package com.emildesign.applicaster_assignment.dagger;

import com.emildesign.applicaster_assignment.YouTubeSearchApplication;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by EmilAdz on 11/23/17.
 * provides all the general utils for the application.
 * all objects here uses applicationContext
 */

@Singleton
@Module
public class AppModule {

    private final YouTubeSearchApplication mYouTubeSearchApplication;
    private final String mBaseUrl;

    public AppModule(YouTubeSearchApplication application, String baseUrl) {
        mYouTubeSearchApplication = application;
        mBaseUrl = baseUrl;
    }

    @Provides
    @Singleton
    YouTubeSearchApplication provideMemGameApplication() {
        return mYouTubeSearchApplication;
    }

    /*@Provides
    @Singleton
    protected NetworkChecker provideNetworkChecker(ConnectivityManager cm) {
        return new NetworkChecker(cm);
    }

    @Provides
    @Singleton
    protected FacebookHelper provideFacebookHelper() {
        return new FacebookHelper();
    }

    @Provides
    @Singleton
    ConnectivityManager provideConnectivityManager() {
        return (ConnectivityManager) mYouTubeSearchApplication.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Provides
    @Singleton
    SimpleActivityLifecycleCallbacks provideActivityLifeCycleHandler() {
        return new SimpleActivityLifecycleCallbacks();
    }

    @Provides
    @Singleton
    Observable<AppVisibilityState> provideAppVisibilityObservable() {
        return mYouTubeSearchApplication.getAppVisibilityState();
    }

    @Provides
    @Singleton
    EventBus provideEventBus() {
        return EventBus.getDefault();
    }*/
}
