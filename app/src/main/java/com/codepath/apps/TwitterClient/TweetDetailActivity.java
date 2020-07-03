package com.codepath.apps.TwitterClient;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.TwitterClient.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import okhttp3.Headers;

public class TweetDetailActivity extends AppCompatActivity {

    ImageView ivProfileImage;
    TextView tvTimestamp;
    TextView tvContent;
    TextView tvHandle;
    ImageView ivMedia;

    ImageButton btnReply;
    ImageButton btnFavorite;
    ImageButton btnRetweet;

    public static final int REQUEST_CODE = 20;

    Tweet tweet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_detail);

        tweet = Parcels.unwrap(getIntent().getParcelableExtra("tweet"));

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvTimestamp = findViewById(R.id.tvTimestamp);
        tvContent = findViewById(R.id.tvBody);
        tvHandle = findViewById(R.id.tvScreenName);
        ivMedia = findViewById(R.id.ivMedia);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnReply = findViewById(R.id.btnReply);
        btnRetweet = findViewById(R.id.btnRetweet);

        tvContent.setText(tweet.body);
        tvHandle.setText(tweet.user.screenName);
        Glide.with(TweetDetailActivity.this).load(tweet.user.publicImageUrl).into(ivProfileImage);
        if (tweet.hasMedia) {
            Glide.with(TweetDetailActivity.this).load(tweet.mediaURL).into(ivMedia);
        }
        tvTimestamp.setText(tweet.createdAt);
        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TweetDetailActivity.this, ComposeActivity.class);
                intent.putExtra("tweet", Parcels.wrap(tweet));
                intent.putExtra("isReply", true);
                (TweetDetailActivity.this).startActivityForResult(intent, REQUEST_CODE);
            }
        });

        final TwitterClient client = new TwitterClient(TweetDetailActivity.this);

        btnRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.retweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        btnRetweet.setBackgroundColor(0x0);
                        Log.i("TweetsAdapter", "success!");
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        btnRetweet.setBackgroundColor(0x00FF0000);
                        Log.e("TweetsAdapter", response);
                    }
                });
            }
        });
        btnFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                client.favorite(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        btnFavorite.setBackgroundResource(R.drawable.ic_vector_heart);
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

                    }
                });
            }
        });

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            //get data from intent
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));

            Intent intent = new Intent(TweetDetailActivity.this, TimelineActivity.class);
            startActivity(intent);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}