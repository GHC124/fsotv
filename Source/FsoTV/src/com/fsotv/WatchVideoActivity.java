package com.fsotv;

import java.io.File;

import com.fsotv.utils.YouTubeFailureRecoveryActivity;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayer.Provider;
import com.google.android.youtube.player.YouTubePlayerFragment;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

public class WatchVideoActivity extends YouTubeFailureRecoveryActivity {

	VideoView videoView;
	String videoId = "";
	String link = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watch_video);

		YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager()
				.findFragmentById(R.id.youtube_fragment);
		youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
	
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			videoId = extras.getString("videoId");
			link = extras.getString("link");

			videoId = (videoId == null) ? "" : videoId;
			link = (link == null) ? "" : link;
		}
		
	}

	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer player,
			boolean arg2) {
		if (!arg2) {
			player.cueVideo(videoId);
		}

	}

	@Override
	protected Provider getYouTubePlayerProvider() {
		return (YouTubePlayerFragment) getFragmentManager().findFragmentById(
				R.id.youtube_fragment);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_watch_video, menu);
		return true;
	}
	
}
