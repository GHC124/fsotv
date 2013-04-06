package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import com.fsotv.utils.YoutubeFeedReader;



import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SearchResultActivity extends Activity {

	ListView lSearchedVideos;

	//private SearchResultActivity local;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search_result);
		List<String> items;
		String searchVideo = getIntent().getExtras().getString("searchvideo");
		
		try {
			String url = "https://gdata.youtube.com/feeds/api/videos?q=" 
			          + URLEncoder.encode(searchVideo,"UTF-8");
			URL uLink;
			uLink = new URL(url);
			HttpURLConnection urlRequest;
			urlRequest = (HttpURLConnection) uLink.openConnection();
			urlRequest.setDoInput(true);
			MyLoadVideoTask load = new MyLoadVideoTask();
			load.execute(urlRequest);
			items = load.get();
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(
					getApplicationContext(),
					android.R.layout.simple_list_item_1, items);
			lSearchedVideos = (ListView) findViewById(R.id.lvSearchedVideos);
			lSearchedVideos.setAdapter(adapter);
			lSearchedVideos.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					Intent youtubeplayerIntent = new Intent(
							getApplicationContext(),
							YoutubePlayerActivity.class);
					youtubeplayerIntent.putExtra("Id",
							(String) arg0.getItemAtPosition(position));
					startActivity(youtubeplayerIntent);

				}

			});

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("Error ",e.toString());
		}

	}

	public class MyLoadVideoTask extends
			AsyncTask<HttpURLConnection, Void, List<String>> {

		@Override
		protected List<String> doInBackground(HttpURLConnection... params) {
			InputStream is = null;
			List<String> items = null;
			try {
				is = params[0].getInputStream();
				items = YoutubeFeedReader.parse(is);

			} catch (IOException e) {
				Log.e("Error AsyncTask ", e.toString());
			}
			return items;
		}

	}

}
