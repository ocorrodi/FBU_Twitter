package com.codepath.apps.TwitterClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.parceler.Parcels;

import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;

import okhttp3.Headers;
import java.util.*;

public class TimelineActivity extends AppCompatActivity {

    ProgressBar pb;

    private EndlessRecyclerViewScrollListener scrollListener;

    TwitterClient client;
    public static final String TAG = "TimelineActivity";
    public static final int REQUEST_CODE = 20;
    public static final int REPLY_REQUEST_CODE = 25;

    public static int numItems;

    private SwipeRefreshLayout swipeContainer;

    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        numItems = 0;

        rvTweets = findViewById(R.id.rvTweets);

        //initialize list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        rvTweets.setLayoutManager(linearLayoutManager);

        rvTweets.setAdapter(adapter);

        // Retain an instance so that you can call `resetState()` for fresh searches
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.i(TAG, "loading more items");
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        client = TwitterApp.getRestClient(this);

        pb = (ProgressBar) findViewById(R.id.pbLoading);



        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateHomeTimeline(1);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //configure recycler view: layout manager and adapter
        populateHomeTimeline(1);
    }

    private void loadNextDataFromApi(int page) {
        long lastID = 1;
        if (!tweets.isEmpty()) {
            Tweet lastTweet = tweets.get(tweets.size() - 1);
            lastID = lastTweet.id;
        }
        populateHomeTimeline(lastID);
        scrollListener.resetState();
    }

    private void populateHomeTimeline(long startID) {
        pb.setVisibility(ProgressBar.VISIBLE);
        client.getHomeTimeline(startID, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "success! " + json.toString());
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray), numItems);
                    numItems += 10;
                    swipeContainer.setRefreshing(false);
                    Log.i(TAG, "num tweets: " + tweets.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "json exception");
                }
                pb.setVisibility(ProgressBar.INVISIBLE);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "failure! " + response, throwable);
            }
        });
        //pb.setVisibility(ProgressBar.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.compose) {
            //Toast.makeText(this, "compose", Toast.LENGTH_SHORT).show();
        }
        Intent intent = new Intent(this, ComposeActivity.class);
        intent.putExtra("isReply", Parcels.wrap(false));
        startActivityForResult(intent, REQUEST_CODE);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == REQUEST_CODE || requestCode == REPLY_REQUEST_CODE) && resultCode == RESULT_OK) {
            //get data from intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            //update recycler view with tweet
            tweets.add(0, tweet);
            adapter.notifyItemInserted(0);

            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}