package com.fsotv.tablet;

import com.fsotv.R;
import com.fsotv.R.layout;
import com.fsotv.R.menu;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class FavoriteTabletActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_favorite_tablet);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_favorite_tablet, menu);
		return true;
	}

}
