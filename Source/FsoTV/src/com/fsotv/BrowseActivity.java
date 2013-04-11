package com.fsotv;

import com.fsotv.utils.YouTubeHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
/**
 * Browse Activity class
 * + Browse channels from youtube
 * + Browse videos form youtube
 * 
 *
 */
public class BrowseActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
	}
	
	public void onBrowseChannels(View v){
		Intent i = new Intent(getApplicationContext(), BrowseChannelsActivity.class);
		startActivity(i);
	}
	
	public void onBrowseVideos(View v){
		Intent i = new Intent(getApplicationContext(), BrowseVideosActivity.class);
		i.putExtra("categoryId", YouTubeHelper.CATEGORY_FILM);
		startActivity(i);
	}
	
}
