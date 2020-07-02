package com.codepath.apps.restclienttemplate;

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
import android.widget.Toast;
import org.parceler.Parcels;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;

import okhttp3.Headers;
import java.util.*;

public class TimelineActivity extends AppCompatActivity {

    ProgressBar pb;

    TwitterClient client;
    public static final String TAG = "TimelineActivity";
    public static final int REQUEST_CODE = 20;
    public static final int REPLY_REQUEST_CODE = 25;

    private SwipeRefreshLayout swipeContainer;

    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        client = TwitterApp.getRestClient(this);

        //find recycler view
        rvTweets = findViewById(R.id.rvTweets);

        pb = (ProgressBar) findViewById(R.id.pbLoading);

        //initialize list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this, tweets);

        rvTweets.setLayoutManager(new LinearLayoutManager(this));

        rvTweets.setAdapter(adapter);

        // Lookup the swipe container view
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                populateHomeTimeline();
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        //configure recycler view: layout manager and adapter
        populateHomeTimeline();
    }

    private void populateHomeTimeline() {
        pb.setVisibility(ProgressBar.VISIBLE);
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "success! " + json.toString());
                try {
                    adapter.clear();
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));

      /*              for (Tweet tweet : tweets) {
                        client.getID(tweet.id, new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode, Headers headers, JSON json) {

                            }

                            @Override
                            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                            }
                        });
                    } */
                    //adapter.notifyDataSetChanged();
                    swipeContainer.setRefreshing(false);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "json exception");
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG, "failure! " + response, throwable);
            }
        });
        pb.setVisibility(ProgressBar.INVISIBLE);
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