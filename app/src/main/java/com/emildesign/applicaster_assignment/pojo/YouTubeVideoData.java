package com.emildesign.applicaster_assignment.pojo;

import com.google.api.client.util.DateTime;

/**
 * Created by EmilAdz on 11/23/17.
 */
public class YouTubeVideoData {
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

    public long getDurationInMiliseconds() {
        String time = mVideoDuration.substring(2);
        long duration = 0L;
        Object[][] indexs = new Object[][]{{"H", 3600}, {"M", 60}, {"S", 1}};
        for(int i = 0; i < indexs.length; i++) {
            int index = time.indexOf((String) indexs[i][0]);
            if(index != -1) {
                String value = time.substring(0, index);
                duration += Integer.parseInt(value) * (int) indexs[i][1] * 1000;
                time = time.substring(value.length() + 1);
            }
        }
        return duration;
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
}
