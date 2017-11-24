package com.emildesign.applicaster_assignment.dagger;

import javax.inject.Singleton;
import dagger.Component;


/**
 * Created by EmilAdz on 11/23/17.
 */
@Singleton
@Component(modules = {AppModule.class, ServerModule.class})
public interface AppComponent {
/*
    String appInstallationId();

    Pref prefs();

    NetworkChecker networkChecker();

    EventBus getEventBus();

    Observable<AppVisibilityState> appVisibilityObservable();

    SimpleActivityLifecycleCallbacks activityLifeCycleCallbacks();

    AnalyticsHelper analyticsHelper();

    FacebookHelper facebookHelper();*/
}

