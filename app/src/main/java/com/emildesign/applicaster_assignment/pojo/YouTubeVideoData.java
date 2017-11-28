package com.emildesign.applicaster_assignment.pojo;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.api.client.util.DateTime;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class YouTubeVideoData implements Parcelable{
    private static final String TAG = "YouTubeVideoData";
    private final String mPlayListId;
    private final String mVideoId;
    private String mVideoImage;
    private String mTitle;
    private String mVideoDuration;
    private String mPlayListTitle;
    private DateTime mPublishedDate;

    public YouTubeVideoData(String aMediumThumbnail, String aTitle, DateTime aPublishedAt, String aPlaylistId, String aVideoId) {
        mVideoImage = aMediumThumbnail;
        mTitle = aTitle;
        mPublishedDate = aPublishedAt;
        mPlayListId = aPlaylistId;
        mVideoId = aVideoId;
    }

    public String getVideoImage() {
        return mVideoImage;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getVideoDuration() {
        return mVideoDuration;
    }

    public long getDurationInMilliseconds() {
        long duration = 0;
        try {
            String time = mVideoDuration.substring(2);
            duration = 0L;
            Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
            for(int i = 0; i < indexs.length; i++) {
                int index = time.indexOf((String) indexs[i][0]);
                if(index != -1) {
                    String value = time.substring(0, index);
                    duration += Integer.parseInt(value) * (int) indexs[i][1] * 1000;
                    time = time.substring(value.length() + 1);
                }
            }
        } catch (NumberFormatException aE) {
            Log.e(TAG, "NumberFormatException: " + aE);
        }
        return duration;
    }

    public String getDurationInHumanReadableForm() {
        long durationInMilliseconds = getDurationInMilliseconds();
        String hms = String.format(Locale.US,"%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(durationInMilliseconds),
                TimeUnit.MILLISECONDS.toMinutes(durationInMilliseconds) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(durationInMilliseconds) % TimeUnit.MINUTES.toSeconds(1));

        if (hms.startsWith("00:")) {
            hms = hms.substring(3, hms.length());
        }

        if (hms.startsWith("0")) {
            hms = hms.substring(1, hms.length());
        }

        return hms;
    }

    public void setVideoDuration(String aVideoDuration) {
        mVideoDuration = aVideoDuration;
    }

    public String getPlayListTitle() {
        return mPlayListTitle;
    }

    public void setPlayListTitle(String aPlayListTitle) {
        mPlayListTitle = aPlayListTitle;
    }

    public DateTime getPublishedDate() {
        return mPublishedDate;
    }

    public String getPlayListId() {
        return mPlayListId;
    }

    public String getVideoId() {
        return mVideoId;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPlayListId);
        dest.writeString(this.mVideoId);
        dest.writeString(this.mVideoImage);
        dest.writeString(this.mTitle);
        dest.writeString(this.mVideoDuration);
        dest.writeString(this.mPlayListTitle);
        dest.writeSerializable(this.mPublishedDate);
    }

    protected YouTubeVideoData(Parcel in) {
        this.mPlayListId = in.readString();
        this.mVideoId = in.readString();
        this.mVideoImage = in.readString();
        this.mTitle = in.readString();
        this.mVideoDuration = in.readString();
        this.mPlayListTitle = in.readString();
        this.mPublishedDate = (DateTime) in.readSerializable();
    }

    public static final Creator<YouTubeVideoData> CREATOR = new Creator<YouTubeVideoData>() {
        @Override
        public YouTubeVideoData createFromParcel(Parcel source) {
            return new YouTubeVideoData(source);
        }

        @Override
        public YouTubeVideoData[] newArray(int size) {
            return new YouTubeVideoData[size];
        }
    };
}
