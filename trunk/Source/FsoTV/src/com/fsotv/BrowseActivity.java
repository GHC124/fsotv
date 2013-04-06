package com.fsotv;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

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
	
	public void onMyChannels(View v){
		Intent i = new Intent(getApplicationContext(), MyChannelsActivity.class);
		startActivity(i);
	}
}
