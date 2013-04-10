package com.fsotv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.TwitterHelper1;
import com.fsotv.utils.TwitterHelper1.TwDialogListener;
import com.fsotv.utils.YouTubeHelper;

public class VideoDetailActivity extends ActivityBase {

	private VideoEntry video;
	private ImageLoader imageLoader;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper1 twitterHelper = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);
		
		mPrefs = getSharedPreferences("fsotv_oauth", MODE_PRIVATE);
		
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
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		i.putExtra("link", video.getLink());
		startActivity(i);
	}
	
	public void onCommentsClick(View v){
		Intent i = new Intent(getApplicationContext(), CommentsActivity.class);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		startActivity(i);
	}

	public void onFaceBookClick(View v) {
		if (faceBookHelper == null) {
			faceBookHelper = new FaceBookHelper(this, mPrefs) {
				@Override
				public boolean onPostComplete(Bundle values) {
					Toast.makeText(getApplicationContext(), "Shared",
							Toast.LENGTH_SHORT).show();
					return true;
				}
			};
		}
		String link = video.getLinkReal();
		faceBookHelper.postToWall(link);

	}

	public void onTwitterClick(View v) {
		if (twitterHelper == null) {
			twitterHelper = new TwitterHelper1(this, mPrefs);
			twitterHelper.setListener(mTwLoginDialogListener);
		}
		String link = video.getLinkReal();
		twitterHelper.resetAccessToken();
		if (twitterHelper.hasAccessToken() == true) {
			try {
				twitterHelper.updateStatus(link);
				Log.e("TWITTER", "post success");
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					Log.e("TWITTER", "duplicate");
				}
				e.printStackTrace();
			}
			twitterHelper.resetAccessToken();
		} else {
			twitterHelper.authorize();
		}
		
	}
	
	private TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		public void onError(String value) {
			Log.e("TWITTER", value);
			twitterHelper.resetAccessToken();
		}

		public void onComplete(String value) {
			try {
				twitterHelper.updateStatus("");
				Log.e("TWITTER", "success");
			} catch (Exception e) {
				if (e.getMessage().toString().contains("duplicate")) {
					Log.e("TWITTER", "duplicate");
				}
				e.printStackTrace();
			}
			twitterHelper.resetAccessToken();
		}
	};
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		faceBookHelper.authorizeCallback(requestCode, resultCode, data);
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
			// try {
			// InputStream is = getAssets().open("VideoDetail.txt");
			// video = YouTubeHelper.getVideoByStream(is);
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
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
					txtDuration.setText(DataHelper.secondsToTimer(video
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
