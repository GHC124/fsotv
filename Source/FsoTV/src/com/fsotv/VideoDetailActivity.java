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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
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
	// Menus
	private final int OPTION_WATCH = Menu.FIRST;
	private final int OPTION_COMMENT = Menu.FIRST + 1;
	private final int OPTION_SHARE = Menu.FIRST + 2;
	// Views
	private DialogBase shareDialog;
	private TabHost tabhost;
	private LocalActivityManager mLocalActivityManager;
	// Porperties
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

		tabhost = (TabHost) findViewById(R.id.tabhost);
		mLocalActivityManager = new LocalActivityManager(this, false);
		mLocalActivityManager.dispatchCreate(savedInstanceState);
		tabhost.setup(mLocalActivityManager);

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

		shareDialog = new DialogBase(context);
		shareDialog.setContentView(R.layout.share);
		shareDialog.setHeader("Share");
		final RadioButton rdFacebook = (RadioButton) shareDialog
				.findViewById(R.id.rdFaceBook);
		final RadioButton rdTwitter = (RadioButton) shareDialog
				.findViewById(R.id.rdTwitter);
		final EditText txtMessage = (EditText) shareDialog
				.findViewById(R.id.txtMessage);
		final TextView lblPost = (TextView) shareDialog
				.findViewById(R.id.lblPost);
		txtMessage.setText(video.getLinkReal());
		Button btnShare = (Button) shareDialog.findViewById(R.id.btnShare);
		rdFacebook.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					lblPost.setVisibility(View.GONE);
					txtMessage.setVisibility(View.GONE);
				}
			}
		});
		rdTwitter.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				if (isChecked){
					lblPost.setVisibility(View.VISIBLE);
					txtMessage.setVisibility(View.VISIBLE);
					lblPost.setText("Message:");
				}
			}
		});
		
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
					onFaceBookClick(null);
				} else if (rdTwitter.isChecked()) {

					onTwitterClick(null);
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
		
	}

	public void onShareClick(View v) {
		if (shareDialog != null){
			shareDialog.show();
			RadioButton rdFacebook = (RadioButton) shareDialog
					.findViewById(R.id.rdFaceBook);
			RadioButton rdTwitter = (RadioButton) shareDialog
					.findViewById(R.id.rdTwitter);
			int id = v.getId();
			switch (id) {
			case R.id.imgFaceBook:
				rdFacebook.setChecked(true);
				break;
			case R.id.imgTwitter:
				rdTwitter.setChecked(true);
				break;
			}
		}
		else {
			createShareDialog(VideoDetailActivity.this);
			if (shareDialog != null){
				RadioButton rdFacebook = (RadioButton) shareDialog
						.findViewById(R.id.rdFaceBook);
				RadioButton rdTwitter = (RadioButton) shareDialog
						.findViewById(R.id.rdTwitter);
				int id = v.getId();
				switch (id) {
				case R.id.imgFaceBook:
					rdFacebook.setChecked(true);
					break;
				case R.id.imgTwitter:
					rdTwitter.setChecked(true);
					break;
				}
			}
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
		faceBookHelper.postToWall(video.getLinkReal());

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

			video = YouTubeHelper.getVideoDetail(videoId);

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

			title.setText(video.getTitle());
			viewCount
					.setText(DataHelper.numberWithCommas(video.getViewCount()));
			txtDuration.setText(DataHelper.secondsToTimer(video.getDuration()));
			txtPublished.setText(DataHelper.formatDate(video.getPublished()));
			txtFavorite.setText(DataHelper.numberWithCommas(video
					.getFavoriteCount()));

			imageLoader.DisplayImage(video.getImage(), img, null);
			// Description tab
			View tabDes = createTabView(getApplicationContext(), "Description", R.drawable.description16);
			TabSpec desSpec = tabhost.newTabSpec("Description");
			desSpec.setIndicator(tabDes);
			Intent desIntent = new Intent(getApplicationContext(),
					VideoDescriptionActivity.class);
			desIntent.putExtra("description", video.getDescription());
			desSpec.setContent(desIntent);
			// Comment tab
			View tabCom = createTabView(getApplicationContext(), "Comments", R.drawable.comment16);
			TabSpec comSpec = tabhost.newTabSpec("Comments");
			comSpec.setIndicator(tabCom);
			Intent comIntent = new Intent(getApplicationContext(),
					VideoCommentsActivity.class);
			comIntent.putExtra("videoId", video.getIdReal());
			comSpec.setContent(comIntent);
			// Add all tabs
			tabhost.addTab(desSpec);
			tabhost.addTab(comSpec);
			
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
