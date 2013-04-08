package com.fsotv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FavoriteActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite);
	}

	public void onMyChannels(View v){
		Intent i = new Intent(getApplicationContext(), MyChannelsActivity.class);
		startActivity(i);
	}
	
	public void onMyVideos(View v){
		Intent i = new Intent(getApplicationContext(), MyVideosActivity.class);
		startActivity(i);
	}
}
