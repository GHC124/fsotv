package com.fsotv;

import com.fsotv.R;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;
import com.google.android.youtube.player.YouTubePlayer.Provider;

import com.fsotv.utils.DeveloperKey;
import com.fsotv.utils.YouTubeFailureRecoveryActivity;
import android.os.Bundle;


public class YoutubePlayerActivity extends YouTubeFailureRecoveryActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_youtube_player);
		YouTubePlayerFragment youTubePlayerFragment = (YouTubePlayerFragment) getFragmentManager()
				.findFragmentById(R.id.youtube_fragment);
		youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
	}

	@Override
	public void onInitializationSuccess(Provider arg0, YouTubePlayer player,
			boolean arg2) {
		if (!arg2) {
			player.cueVideo(getIntent().getExtras().getString("Id"));
		}

	}

	@Override
	protected Provider getYouTubePlayerProvider() {
		return (YouTubePlayerFragment) getFragmentManager().findFragmentById(
				R.id.youtube_fragment);
	}

}
