package com.fsotv;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;
/**
 * Show video description
 * 
 */
public class VideoDescriptionActivity extends Activity {
	TextView tvDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_description);

		tvDescription = (TextView) findViewById(R.id.tvDescription);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String description = "";
		if (extras != null) {
			description = extras.getString("description");
			description = description == null ? "" : description;
		}
		tvDescription.setText(description);
	}
}
