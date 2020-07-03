package com.codepath.apps.TwitterClient.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.*;

import java.util.ArrayList;

@Parcel
public class Tweet {
    public String body;
    public String createdAt;
    public User user;
    public String mediaURL;
    public boolean hasMedia;
    public String date;
    public String timestamp;
    public Long id;

    //empty constructor for Parceler
    public Tweet() {}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.id = jsonObject.getLong("id");

        try {
            JSONArray mediaArray = jsonObject.getJSONObject("entities").getJSONArray("media");
            tweet.hasMedia = true;
            tweet.mediaURL = mediaArray.getJSONObject(0).getString("media_url_https");
        }
        catch (JSONException e) {
            tweet.hasMedia = false;
        }
        return tweet;
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }
}
