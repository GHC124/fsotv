package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DownloadImage;
import com.fsotv.utils.YouTubeHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;

public class VideoDetailActivity extends Activity {

	private ProgressDialog pDialog;
	private VideoEntry video;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);
		
		pDialog = new ProgressDialog(VideoDetailActivity.this);
		pDialog.setMessage("Loading data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		
		video = new VideoEntry();
		String videoId = "";
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null){
			videoId = extras.getString("videoId");
			
			videoId = (videoId==null)?"":videoId;
		}
		
		setTitle("Video Detail");
		
		new loadVideo().execute(videoId);
	}

	public void onWatchClick(View c){
		Intent i = new Intent(getApplicationContext(), WatchVideoActivity.class);
		i.putExtra("videoId", video.getId());
		i.putExtra("link", video.getLink());
		startActivity(i);
	}

	/**
	 * Background Async Task to get Videos data from URL
	 * */
	class loadVideo extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog.show();
		}

		@Override
		protected String doInBackground(String... args) {
			String videoId = args[0];
			video = YouTubeHelper.getVideoDetail(videoId);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					ImageView img = (ImageView)findViewById(R.id.imgThumbnail);
					TextView title = (TextView)findViewById(R.id.txtvTitle);
					TextView viewCount = (TextView)findViewById(R.id.txtvViewCount);
					TextView txtvDescriptionContent = (TextView)findViewById(R.id.txtvDescriptionContent);
					TextView txtDuration = (TextView)findViewById(R.id.txtDuration);
					
					title.setText(video.getTitle());
					viewCount.setText(video.getViewCount() + "");
					txtvDescriptionContent.setText(video.getDescription());
					txtDuration.setText(video.getDuration() + "");
					new DownloadImage(img, null).execute(video.getImage());
				}
			});
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
		}

	}
}
