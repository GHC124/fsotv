package com.fsotv;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SearchingActivity extends Activity {

	EditText edVideo;
	Button btSearch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse);
		edVideo = (EditText) findViewById(R.id.etVideo);
		btSearch = (Button) findViewById(R.id.btSearch);
		btSearch.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent iSearch = new Intent(getApplicationContext(),
						SearchResultActivity.class);
				iSearch.putExtra("searchvideo", edVideo.getText().toString());
				startActivity(iSearch);
			}
		});
	}
}
