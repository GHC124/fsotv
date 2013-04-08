package com.fsotv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;

public class DemoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.demo, menu);
		return true;
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
