package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.FacebookError;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.TwitterHelper.TwDialogListener;
import com.fsotv.utils.YouTubeHelper;

/**
 * Show video detail Extend ActivityBase, allow: + Watch video + View comments +
 * Share facebook and twitter
 * 
 */
public class VideoDetailActivity extends ActivityBase {

	private final int OPTION_WATCH = Menu.FIRST;
	private final int OPTION_COMMENTS = Menu.FIRST + 1;
	private final int OPTION_SHARE = Menu.FIRST + 2;

	private Dialog shareDialog;

	private boolean isLoading = true; // Loading data
	private VideoEntry video;
	private ImageLoader imageLoader;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	private String postMessage = "";

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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_WATCH, 0, "Watch");
		menu.add(0, OPTION_COMMENTS, 1, "Comments");
		menu.add(0, OPTION_SHARE, 2, "Share");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_WATCH:
			onWatchClick(null);
			break;
		case OPTION_COMMENTS:
			onCommentsClick(null);
			break;
		case OPTION_SHARE:
			if (shareDialog != null)
				shareDialog.show();
			else {
				createShareDialog(VideoDetailActivity.this);
				if (shareDialog != null)
					shareDialog.show();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createShareDialog(Context context) {
		if (isLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}

		shareDialog = new Dialog(context);
		shareDialog.setContentView(R.layout.share);
		shareDialog.setTitle("Share");
		final RadioButton rdFacebook = (RadioButton) shareDialog
				.findViewById(R.id.rdFaceBook);
		final RadioButton rdTwitter = (RadioButton) shareDialog
				.findViewById(R.id.rdTwitter);
		final EditText txtMessage = (EditText) shareDialog
				.findViewById(R.id.txtMessage);
		final TextView lblPost = (EditText) shareDialog
				.findViewById(R.id.lblPost);
		txtMessage.setText(video.getLinkReal());
		Button btnShare = (Button) shareDialog.findViewById(R.id.btnShare);
		Button btnCancel = (Button) shareDialog.findViewById(R.id.btnCancel);
		btnShare.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				postMessage = txtMessage.getText().toString();
				if (postMessage.isEmpty()) {
					Toast.makeText(getApplicationContext(), "Input message",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (rdFacebook.isChecked()) {
					lblPost.setText("Link:");
					// Check link
					try {
						URL url = new URL(postMessage);
						url = null;
					} catch (MalformedURLException e) {
						Toast.makeText(getApplicationContext(), "Input link",
								Toast.LENGTH_SHORT).show();
						return;
					}
					onFaceBookClick(null);
				} else if (rdTwitter.isChecked()) {
					lblPost.setText("Message:");
					onTwitterClick(null);
				}
				shareDialog.dismiss();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				shareDialog.dismiss();
			}
		});
	}

	public void onWatchClick(View c) {
		if (isLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(getApplicationContext(), WatchVideoActivity.class);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		i.putExtra("link", video.getLink());
		startActivity(i);
	}

	public void onCommentsClick(View v) {
		if (isLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(getApplicationContext(), CommentsActivity.class);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		startActivity(i);
	}

	public void onShareClick(View v) {
		if (shareDialog != null)
			shareDialog.show();
		else {
			createShareDialog(VideoDetailActivity.this);
			if (shareDialog != null)
				shareDialog.show();
		}
	}

	public void onFaceBookClick(View v) {
		if (faceBookHelper == null) {
			faceBookHelper = new FaceBookHelper(this, mPrefs) {
				@Override
				public boolean onPostComplete(Bundle values) {
					if (values.containsKey("post_id")) {
						Toast.makeText(getApplicationContext(), "Shared",
								Toast.LENGTH_SHORT).show();
					}
					return true;
				}

				@Override
				public boolean onPostFacebookError(FacebookError e) {
					Toast.makeText(getApplicationContext(),
							"Fail to post message. Please, try again!",
							Toast.LENGTH_SHORT).show();
					return false;
				}
			};
		}
		faceBookHelper.postToWall(postMessage);

	}

	public void onTwitterClick(View v) {
		if (twitterHelper == null) {
			twitterHelper = new TwitterHelper(this, mPrefs);
			twitterHelper.setListener(mTwLoginDialogListener);
		}
		twitterHelper.resetAccessToken();
		if (twitterHelper.hasAccessToken() == true) {
			try {
				twitterHelper.updateStatus(postMessage);
				Log.e("TWITTER", "post success");
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
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
			Toast.makeText(getApplicationContext(),
					"Fail to post message. Please, try again!",
					Toast.LENGTH_SHORT).show();
			twitterHelper.resetAccessToken();
		}

		public void onComplete(String value) {
			try {
				twitterHelper.updateStatus(postMessage);
				Log.e("TWITTER", "post success");
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
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
			isLoading = true;
		}

		@Override
		protected String doInBackground(String... args) {
			String videoId = args[0];
			// Demo data
			try {
				InputStream is = getAssets().open("VideoDetail.txt");
				video = YouTubeHelper.getVideoByStream(is);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
			video = YouTubeHelper.getVideoDetail(videoId);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();

			ImageView img = (ImageView) findViewById(R.id.imgThumbnail);
			TextView title = (TextView) findViewById(R.id.txtvTitle);
			TextView viewCount = (TextView) findViewById(R.id.viewCount);
			TextView txtvDescriptionContent = (TextView) findViewById(R.id.txtvDescriptionContent);
			TextView txtDuration = (TextView) findViewById(R.id.duration);
			TextView txtPublished = (TextView) findViewById(R.id.published);
			TextView txtFavorite = (TextView) findViewById(R.id.favoriteCount);

			title.setText(video.getTitle());
			viewCount
					.setText(DataHelper.numberWithCommas(video.getViewCount()));
			txtvDescriptionContent.setText(video.getDescription());
			txtDuration.setText(DataHelper.secondsToTimer(video.getDuration()));
			txtPublished.setText(DataHelper.formatDate(video.getPublished()));
			txtFavorite.setText(DataHelper.numberWithCommas(video
					.getFavoriteCount()));

			imageLoader.DisplayImage(video.getImage(), img, getLoadingView());

			isLoading = false;
		}

	}
}
