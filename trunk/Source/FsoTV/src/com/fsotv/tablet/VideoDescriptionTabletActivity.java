package com.fsotv.tablet;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.fsotv.R;
/**
 * Show video description
 * 
 */
public class VideoDescriptionTabletActivity extends Activity {
	TextView tvDescription;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_description_tablet);

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
