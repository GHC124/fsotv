package com.fsotv;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.YouTubeHelper;

public class VideoDetailActivity extends ActivityBase {

	private VideoEntry video;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);

		imageLoader = new ImageLoader(getApplicationContext());
		video = new VideoEntry();
		String videoId = "";

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			videoId = extras.getString("videoId");
			videoId = (videoId == null) ? "" : videoId;
		}

		setHeader("Video");
		setTitle("Video Detail");

		new loadVideo().execute(videoId);
	}

	public void onWatchClick(View c) {
		Intent i = new Intent(getApplicationContext(), WatchVideoActivity.class);
		i.putExtra("videoId", video.getId());
		i.putExtra("videoTitle", video.getTitle());
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
			showLoading();
		}

		@Override
		protected String doInBackground(String... args) {
			String videoId = args[0];
			// Demo data
//			try {
//				InputStream is = getAssets().open("VideoDetail.txt");
//				video = YouTubeHelper.getVideoByStream(is);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			video = YouTubeHelper.getVideoDetail(videoId);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					ImageView img = (ImageView) findViewById(R.id.imgThumbnail);
					TextView title = (TextView) findViewById(R.id.txtvTitle);
					TextView viewCount = (TextView) findViewById(R.id.txtvViewCount);
					TextView txtvDescriptionContent = (TextView) findViewById(R.id.txtvDescriptionContent);
					TextView txtDuration = (TextView) findViewById(R.id.txtDuration);

					title.setText(video.getTitle());
					viewCount.setText(DataHelper.numberWithCommas(video
							.getViewCount()));
					txtvDescriptionContent.setText(video.getDescription());
					txtDuration.setText(DataHelper.milliSecondsToTimer(video
							.getDuration()));

					imageLoader.DisplayImage(video.getImage(), img,
							getLoadingView());
				}
			});
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
		}

	}
}
