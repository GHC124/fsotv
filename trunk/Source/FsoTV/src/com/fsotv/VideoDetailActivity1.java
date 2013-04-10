package com.fsotv;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.YouTubeHelper;

public class VideoDetailActivity1 extends ActivityBase {

	private final String PREF_SHARING_FACEBOOK = "pref_sharing_facebook";	
	private final String PREF_SHARING_TWITTER = "pref_sharing_twitter";
	private final String PREF_SHARING_TWITTER_LINK = "pref_sharing_twitter_link";
	
	private VideoEntry video;
	private ImageLoader imageLoader;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	
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

		// Check current action when redirected from twitter login page
		boolean sharingTwitter = mPrefs.getBoolean(PREF_SHARING_TWITTER, false);
		if(sharingTwitter){
			twitterHelper = new TwitterHelper(this, mPrefs){
				@Override
				public void onCallBackCompleted(){
					// Post twitter
					String link = mPrefs.getString(PREF_SHARING_TWITTER_LINK, "");
					if(link.isEmpty()){
						Log.e("TWITTER", "Empty link");
					}
					else {
						twitterHelper.postTwitter(link);
					}
				}
			};
			twitterHelper.twitterCallBack();
		}
		
		new loadVideo().execute(videoId);
	}

	public void onWatchClick(View c) {
		Intent i = new Intent(getApplicationContext(), WatchVideoActivity.class);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		i.putExtra("link", video.getLink());
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
			twitterHelper = new TwitterHelper(this, mPrefs);
		}
		String link = video.getLinkReal();
		if(twitterHelper.isTwitterLoggedInAlready()){
			twitterHelper.postTwitter(link);
		}else{
			Editor e = mPrefs.edit();
			e.putBoolean(PREF_SHARING_TWITTER, true);
			e.putString(PREF_SHARING_TWITTER_LINK, link);
			e.commit();
			twitterHelper.loginToTwitter();
		}
	}

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
					TextView txtDuration = (TextView) findViewById(R.id.duration);

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
