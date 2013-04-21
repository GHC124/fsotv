package com.fsotv;

import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.FaceBookHelper.FbListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.TwitterHelper.TwListener;
import com.fsotv.utils.YouTubeHelper;
import com.fsotv.utils.YouTubeHelper.YtListener;

/**
 * Show video detail, allow: + Watch video + View comments + Share facebook and
 * twitter
 * 
 */
public class VideoDetailActivity extends ActivityBase {
	private final String TAG = "VideoDetail";
	// Menus
	private final int OPTION_WATCH = Menu.FIRST;
	private final int OPTION_COMMENT = Menu.FIRST + 1;
	private final int OPTION_SHARE = Menu.FIRST + 2;
	// Views
	private DialogBase shareDialog;
	private DialogBase commentDialog;
	private TabHost tabHost;
	private LocalActivityManager mLocalActivityManager;
	// Porperties
	private YouTubeHelper youTubeHelper;
	private boolean isLoading = true; // Loading data
	private VideoEntry video;
	private ImageLoader imageLoader;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	private String postMessage = ""; // FaceBook and Twitter post message
	private String commentMessage = ""; // YouTube comment

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail);

		// Setup tabhost
		tabHost = (TabHost) findViewById(R.id.tabhost);
		mLocalActivityManager = new LocalActivityManager(this, false);
		mLocalActivityManager.dispatchCreate(savedInstanceState);
		tabHost.setup(mLocalActivityManager);

		mPrefs = getSharedPreferences(MainActivity.SHARED_PREFERENCE,
				MODE_PRIVATE);

		youTubeHelper = new YouTubeHelper(this, mPrefs);
		youTubeHelper.setListener(mYtListener);
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
		menu.add(0, OPTION_COMMENT, 1, "Comment");
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
		case OPTION_COMMENT:
			onCommentClick(null);
			break;
		case OPTION_SHARE:
			onShareClick(null);
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Create dialog that allow user to comment videos
	 * 
	 * @param context
	 */
	private void createCommentDialog(Context context) {
		if (isLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Log.i(TAG, "Start createCommentDialog()");

		commentDialog = new DialogBase(context);
		commentDialog.setContentView(R.layout.comment);
		commentDialog.setHeader("Comment");

		final EditText txtComment = (EditText) commentDialog
				.findViewById(R.id.txtComment);

		Button btnComment = (Button) commentDialog
				.findViewById(R.id.btnComment);
		btnComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				commentMessage = txtComment.getText().toString();
				if (commentMessage.isEmpty()) {
					Toast.makeText(getApplicationContext(), "Input comment!",
							Toast.LENGTH_SHORT).show();
					return;
				}
				commentDialog.dismiss();
				// Add comment
				if (youTubeHelper.hasAccessToken()) {
					youTubeHelper.validateAccessToken();
				} else {
					youTubeHelper.authorize();
				}
			}
		});

		Log.i(TAG, "End createCommentDialog()");
	}

	private void createShareDialog(Context context) {
		if (isLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}

		shareDialog = new DialogBase(context);
		shareDialog.setContentView(R.layout.share);
		shareDialog.setHeader("Share");
		final RadioButton rdFacebook = (RadioButton) shareDialog
				.findViewById(R.id.rdFaceBook);
		final RadioButton rdTwitter = (RadioButton) shareDialog
				.findViewById(R.id.rdTwitter);
		final EditText txtMessage = (EditText) shareDialog
				.findViewById(R.id.txtMessage);
		txtMessage.setText(video.getLinkReal());

		Button btnShare = (Button) shareDialog.findViewById(R.id.btnShare);

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
					if (faceBookHelper == null) {
						faceBookHelper = new FaceBookHelper(
								VideoDetailActivity.this, mPrefs);
						faceBookHelper.setListener(mFbListener);
					}
					if (faceBookHelper.hasAccessToken()) {
						faceBookHelper.postToWall(postMessage);

					} else {
						faceBookHelper.authorize();
					}
				} else if (rdTwitter.isChecked()) {
					if (twitterHelper == null) {
						twitterHelper = new TwitterHelper(
								VideoDetailActivity.this, mPrefs);
						twitterHelper.setListener(mTwListener);
					}
					if (twitterHelper.hasAccessToken() == true) {
						twitterHelper.updateStatus(postMessage);

					} else {
						twitterHelper.authorize();
					}
				}
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
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		i.putExtra("link", video.getLink());
		startActivity(i);
	}

	public void onCommentClick(View v) {
		if (commentDialog != null)
			commentDialog.show();
		else {
			createCommentDialog(VideoDetailActivity.this);
			if (commentDialog != null)
				commentDialog.show();
		}
	}

	public void onShareClick(View v) {
		if (shareDialog != null) {
			shareDialog.show();
		} else {
			createShareDialog(VideoDetailActivity.this);
			if (shareDialog != null) {
				shareDialog.show();
			}
		}
		if (shareDialog != null && v != null) {
			RadioButton rdFacebook = (RadioButton) shareDialog
					.findViewById(R.id.rdFaceBook);
			RadioButton rdTwitter = (RadioButton) shareDialog
					.findViewById(R.id.rdTwitter);
			int id = v.getId();
			if (id == R.id.imgFaceBook) {
				rdFacebook.setChecked(true);
			} else if (id == R.id.imgTwitter) {
				rdTwitter.setChecked(true);
			}
		}
	}

	/**
	 * YouTube listener
	 */
	private YtListener mYtListener = new YtListener() {

		public void onError(String value) {
			Log.e("YouTube", value);
			// Check status response from youtube
			if (value.startsWith("status:")) {
				int status = Integer.parseInt(value.split(":")[1]);
				if (status == 403) {
					Toast.makeText(getApplicationContext(),
							"You are outside the comment quota limits.",
							Toast.LENGTH_LONG).show();
				}
			} else {
				Toast.makeText(getApplicationContext(),
						"Fail to add comment. Please, try again!",
						Toast.LENGTH_SHORT).show();
			}
		}

		public void onComplete(String value) {
			Log.i("YouTube", value);
			if (value.equals("login")) {
				youTubeHelper.addComment(video.getIdReal(), commentMessage);
			} else if (value.equals("comment")) {
				Toast.makeText(getApplicationContext(), "Commented",
						Toast.LENGTH_SHORT).show();
			} else if (value.equals("validate")) {
				youTubeHelper.addComment(video.getIdReal(), commentMessage);
			}
		}
	};
	/**
	 * FaceBook listener
	 */
	private FbListener mFbListener = new FbListener() {

		public void onError(String value) {
			Log.e("FaceBook", value);
			Toast.makeText(getApplicationContext(),
					"Fail to post message. Please, try again!",
					Toast.LENGTH_SHORT).show();
		}

		public void onComplete(String value) {
			Log.i("FaceBook", value);
			if (value.equals("login")) {
				faceBookHelper.postToWall(postMessage);
			} else if (value.equals("post")) {
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			}
		}
	};
	/**
	 * Twitter listener
	 */
	private TwListener mTwListener = new TwListener() {

		public void onError(String value) {
			Log.e("Twitter", value);
			Toast.makeText(getApplicationContext(),
					"Fail to post message. Please, try again!",
					Toast.LENGTH_SHORT).show();
		}

		public void onComplete(String value) {
			Log.i("Twitter", value);
			if (value.equals("login")) {
				twitterHelper.updateStatus(postMessage);
			} else if (value.equals("post")) {
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Background Async Task to get Video data from URL
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

			video = youTubeHelper.getVideoDetail(videoId);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();

			final ImageView img = (ImageView) findViewById(R.id.imgThumbnail);
			TextView title = (TextView) findViewById(R.id.txtvTitle);
			TextView viewCount = (TextView) findViewById(R.id.viewCount);
			TextView txtDuration = (TextView) findViewById(R.id.duration);
			TextView txtPublished = (TextView) findViewById(R.id.published);
			TextView txtFavorite = (TextView) findViewById(R.id.favoriteCount);
			TextView txtAuthor = (TextView) findViewById(R.id.author);

			title.setText(video.getTitle());
			viewCount
					.setText(DataHelper.numberWithCommas(video.getViewCount()));
			txtDuration.setText(DataHelper.secondsToTimer(video.getDuration()));
			txtPublished.setText(DataHelper.formatDate(video.getPublished()));
			txtFavorite.setText(DataHelper.numberWithCommas(video
					.getFavoriteCount()));
			txtAuthor.setText(video.getAuthor());
			imageLoader.DisplayImage(video.getImage(), img, null);
			// Description tab
			View tabDes = createTabView(getApplicationContext(), "Description",
					R.drawable.description16);
			TabSpec desSpec = tabHost.newTabSpec("Description");
			desSpec.setIndicator(tabDes);
			Intent desIntent = new Intent(getApplicationContext(),
					VideoDescriptionActivity.class);
			desIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			desIntent.putExtra("description", video.getDescription());
			desSpec.setContent(desIntent);
			// Comment tab
			View tabCom = createTabView(getApplicationContext(), "Comments",
					R.drawable.comment16);
			TabSpec comSpec = tabHost.newTabSpec("Comments");
			comSpec.setIndicator(tabCom);
			Intent comIntent = new Intent(getApplicationContext(),
					VideoCommentsActivity.class);
			comIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			comIntent.putExtra("videoId", video.getIdReal());
			comSpec.setContent(comIntent);
			// Add all tabs
			tabHost.addTab(desSpec);
			tabHost.addTab(comSpec);

			postMessage = video.getLinkReal();

			isLoading = false;
		}

	}

	private View createTabView(Context context, String text, int img) {
		View view = LayoutInflater.from(context).inflate(R.layout.tab_bg, null);
		ImageView im = (ImageView) view.findViewById(R.id.tabImg);
		im.setBackgroundResource(img);
		TextView tv = (TextView) view.findViewById(R.id.tabText);
		tv.setText(text);
		return view;
	}
}
