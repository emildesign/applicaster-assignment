package com.emildesign.applicaster_assignment.dagger;

import dagger.Module;


/**
 * Created by EmilAdz on 11/23/17.
 */
@Module
public class ServerModule {

    /*@Provides
    @Singleton
    public ServerAPI provideAServerApi(Cache cache, OkHttpClient okHttpClient, RetrofitRequestInterceptor interceptor, String mBaseUrl) {
//        return new MockServer();
        OkHttpClient.Builder okHttpBuilder = okHttpClient.newBuilder()
                .cache(cache)
                .addInterceptor(interceptor)
                .connectTimeout(30000, TimeUnit.MILLISECONDS).readTimeout(30000, TimeUnit.MILLISECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            okHttpBuilder.addInterceptor(logging);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(mBaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(okHttpBuilder.build())
                .build();
        return retrofit.create(ServerAPI.class);
    }*/
}
