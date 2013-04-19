package com.fsotv.tablet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.ActivityBase;
import com.fsotv.DialogBase;
import com.fsotv.MainActivity;
import com.fsotv.R;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.FaceBookHelper.FbListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.TwitterHelper.TwListener;
import com.fsotv.utils.YouTubeHelper;

public class VideoDetailTabletActivity extends ActivityBase {
	private final int MENU_SUBSCRIBE = Menu.FIRST;
	private final int OPTION_SEARCH = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_CATEGORY = Menu.FIRST + 2;

	private final int OPTION_WATCH = Menu.FIRST + 3;
	private final int OPTION_COMMENT = Menu.FIRST + 4;
	private final int OPTION_SHARE = Menu.FIRST + 5;

	private DialogBase shareDialog;

	private VideoEntry video;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	private String postMessage = "";

	private ListView lvVideo;
	private List<VideoEntry> videos;
	private ImageLoader imageLoader;
	private ListVideoAdapter adapter;
	private boolean isVideosLoading = false;
	private boolean isVideoLoading = false;

	private ImageView imgThumbnail;
	private TextView lblTitle;
	private TextView lblDuration;
	private TextView lblPublished;
	private TextView lblViewCount;
	private TextView lblFavoriteCount;
	private TabHost tabHost;
	private LocalActivityManager mLocalActivityManager;

	private String videoId = "";
	private String channelId = "";
	private String categoryId = "";
	private String orderBy = "";
	private int maxResult = 10;
	private int maxLoad = 5;
	private int startIndex = 1;
	private String keyword = "";
	private String time = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_detail_tablet);
		
		tabHost = (TabHost) findViewById(R.id.tabhost);
		mLocalActivityManager = new LocalActivityManager(this, false);
		mLocalActivityManager.dispatchCreate(savedInstanceState);
		tabHost.setup(mLocalActivityManager);
		
		mPrefs = getSharedPreferences(MainActivity.SHARED_PREFERENCE, MODE_PRIVATE);

		lvVideo = (ListView) findViewById(R.id.lvVideo);
		imgThumbnail = (ImageView) findViewById(R.id.imgThumbnail);
		lblTitle = (TextView) findViewById(R.id.txtvTitle);
		lblDuration = (TextView) findViewById(R.id.duration);
		lblPublished = (TextView) findViewById(R.id.published);
		lblViewCount = (TextView) findViewById(R.id.viewCount);
		lblFavoriteCount = (TextView) findViewById(R.id.favoriteCount);

		video = new VideoEntry();
		imageLoader = new ImageLoader(getApplicationContext());
		videos = new ArrayList<VideoEntry>();

		String channelTitle = "";
		String header = "";
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			videoId = extras.getString("videoId");
			channelId = extras.getString("channelId");
			categoryId = extras.getString("categoryId");
			channelTitle = extras.getString("channelTitle");

			videoId = (videoId == null) ? "" : videoId;
			channelId = (channelId == null) ? "" : channelId;
			categoryId = (categoryId == null) ? "" : categoryId;
			channelTitle = (channelTitle == null) ? "" : channelTitle;
		}
		if (!channelTitle.isEmpty()) {
			header = channelTitle;
			if (header.length() > 250)
				header = header.substring(0, 250) + "...";
		} else if (!categoryId.isEmpty()) {
			header = categoryId;
		}

		setHeader(header);
		setTitle("Browse Video");

		// Launching new screen on Selecting Single ListItem
		lvVideo.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				//view.getFocusables(position);
				//view.setSelected(true);
				
				VideoEntry item = videos.get(position);
				videoId = item.getId();
				new loadVideo().execute(videoId);
			}
		});
		lvVideo.setOnScrollListener(new EndlessScrollListViewListener(lvVideo) {
			@Override
			public void loadData() {
				if (!isVideosLoading) {
					isVideosLoading = true;
					startIndex = startIndex + maxResult;
					new LoadVideos().execute();
				}
			}
		});
		adapter = new ListVideoAdapter(VideoDetailTabletActivity.this,
				R.layout.video_tablet_item, videos);
		// updating listview
		registerForContextMenu(lvVideo);
		lvVideo.setAdapter(adapter);
		// Load videos
		new LoadVideos().execute();
		new loadVideo().execute(videoId);
	}

	@Override
	public void onDestroy() {
		imageLoader.cancel();

		super.onDestroy();
	}

	/**
	 * Building a context menu for listview Long press on List row to see
	 * context menu
	 * */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lvVideo) {
			menu.setHeaderTitle("Option");
			menu.add(Menu.NONE, MENU_SUBSCRIBE, 0, "Subscribe");
		}
	}

	/**
	 * Responding to context menu selected option
	 * */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_SEARCH, 0, "Search");
		menu.add(0, OPTION_SORT, 1, "Sort");
		// Only show category dialog when browse by channel
		if (!categoryId.isEmpty()) {
			menu.add(0, OPTION_CATEGORY, 2, "Category");
		}

		menu.add(0, OPTION_WATCH, 0, "Watch");
		menu.add(0, OPTION_COMMENT, 1, "Comments");
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
				createShareDialog(VideoDetailTabletActivity.this);
				if (shareDialog != null)
					shareDialog.show();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void createShareDialog(Context context) {
		if (isVideoLoading) {
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
					onFaceBookClick(null);
				} else if (rdTwitter.isChecked()) {

					onTwitterClick(null);
				}
				shareDialog.dismiss();
			}
		});
	}

	public void onWatchClick(View c) {
		if (isVideoLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(getApplicationContext(), WatchVideoTabletActivity.class);
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
			if (id == R.id.imgFaceBook) {
				rdFacebook.setChecked(true);
			} else if (id == R.id.imgTwitter) {
				rdTwitter.setChecked(true);
			}
		}
		else {
			createShareDialog(VideoDetailTabletActivity.this);
			if (shareDialog != null){
				shareDialog.show();
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
	}

	public void onFaceBookClick(View v) {
		if (faceBookHelper == null) {
			faceBookHelper = new FaceBookHelper(this, mPrefs);
			faceBookHelper.setListener(mFbListener);
		}
		if (faceBookHelper.hasAccessToken()) {
			faceBookHelper.postToWall(postMessage);

		} else {
			faceBookHelper.authorize();
		}

	}

	public void onTwitterClick(View v) {
		if (twitterHelper == null) {
			twitterHelper = new TwitterHelper(this, mPrefs);
			twitterHelper.setListener(mTwListener);
		}
		if (twitterHelper.hasAccessToken() == true) {
			try {
				twitterHelper.updateStatus(postMessage);
				Log.e("TWITTER", "post success");
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			twitterHelper.authorize();
		}

	}
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
			if (value.equals("login")) {
				faceBookHelper.postToWall(postMessage);
			} else if (value.equals("post")) {
				Log.i("FaceBook", "post success");
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
			if (value.equals("login")) {
				twitterHelper.updateStatus(postMessage);
			} else if (value.equals("post")) {
				Log.i("Twitter", "post success");
				Toast.makeText(getApplicationContext(), "Shared",
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	/**
	 * Background Async Task to get Videos data from URL
	 * */
	class LoadVideos extends AsyncTask<String, String, List<VideoEntry>> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected List<VideoEntry> doInBackground(String... args) {
			if (isVideosLoading) {
				List<VideoEntry> items = null;
				if (!channelId.isEmpty()) {
					items = YouTubeHelper.getVideosInChannel(channelId,
							orderBy, maxLoad, startIndex, keyword, time);
				} else if (!categoryId.isEmpty()) {
					items = YouTubeHelper.getVideosInCategory(categoryId,
							orderBy, maxLoad, startIndex, keyword, time);
				}
				return items;
			} else {
				startIndex = 1;
				if (!channelId.isEmpty()) {
					videos = YouTubeHelper.getVideosInChannel(channelId,
							orderBy, maxResult, startIndex, keyword, time);
				} else if (!categoryId.isEmpty()) {
					videos = YouTubeHelper.getVideosInCategory(categoryId,
							orderBy, maxResult, startIndex, keyword, time);
				}
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(List<VideoEntry> result) {
			hideLoading();
			if (isVideosLoading)
				isVideosLoading = false;
			if (result != null) {
				videos.addAll(result);
				if (result.size() == 0) {
					// decrease start index so we will load more items at
					// previous position
					startIndex = startIndex - maxResult;
					Toast.makeText(getApplicationContext(), "No more results",
							Toast.LENGTH_LONG).show();
				}
			}
			if (videos.size() > 0) {
				adapter.clear();
				for (VideoEntry c : videos) {
					adapter.add(c);
				}
				adapter.notifyDataSetChanged();
				if (result == null) {
					// Scroll to top if refresh list from beginning
					lvVideo.setSelectionAfterHeaderView();
				}
			} else {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class ListVideoAdapter extends ArrayAdapter<VideoEntry> {
		Context context;
		int layoutResourceId;
		List<VideoEntry> data = null;

		public ListVideoAdapter(Context context, int layoutResourceId,
				List<VideoEntry> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ListItemHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new ListItemHolder();
				holder.image = (ImageView) row.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) row
						.findViewById(R.id.progressBar);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row
						.findViewById(R.id.description);
				holder.duration = (TextView) row.findViewById(R.id.duration);
				holder.published = (TextView) row.findViewById(R.id.published);

				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			VideoEntry item = data.get(position);
			// format string
			String title = item.getTitle();
			String description = item.getDescription();
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (description.length() > 100) {
				description = description.substring(0, 100) + "...";
			}

			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);

			holder.title.setText(title);
			holder.description.setText(description);
			holder.duration.setText(DataHelper.secondsToTimer(item
					.getDuration()));
			holder.published
					.setText(DataHelper.formatDate(item.getPublished()));

			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView duration;
			TextView published;
		}
	}

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
			isVideoLoading = true;
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

			lblTitle.setText(video.getTitle());
			lblDuration.setText(DataHelper.secondsToTimer(video.getDuration()));
			lblPublished.setText(DataHelper.formatDate(video.getPublished()));
			lblViewCount.setText(DataHelper.numberWithCommas(video
					.getViewCount()));
			lblFavoriteCount.setText(DataHelper.numberWithCommas(video
					.getFavoriteCount()));

			imageLoader.DisplayImage(video.getImage(), imgThumbnail, null);
			
			// Clear all tabs
			tabHost.setCurrentTab(0); 
			tabHost.clearAllTabs();
			// Description tab
			View tabDes = createTabView(getApplicationContext(), "Description",
					R.drawable.description16);
			TabSpec desSpec = tabHost.newTabSpec("Description");
			desSpec.setIndicator(tabDes);
			Intent desIntent = new Intent(getApplicationContext(),
					VideoDescriptionTabletActivity.class);
			desIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			desIntent.putExtra("description", video.getDescription());
			desSpec.setContent(desIntent);
			// Comment tab
			View tabCom = createTabView(getApplicationContext(), "Comments",
					R.drawable.comment16);
			TabSpec comSpec = tabHost.newTabSpec("Comments");
			comSpec.setIndicator(tabCom);
			Intent comIntent = new Intent(getApplicationContext(),
					VideoCommentsTabletActivity.class);
			comIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			comIntent.putExtra("videoId", video.getIdReal());
			comSpec.setContent(comIntent);
			// Add all tabs
			tabHost.addTab(desSpec);
			tabHost.addTab(comSpec);

			postMessage = video.getLinkReal();

			isVideoLoading = false;
		}

	}

	private View createTabView(Context context, String text, int img) {
	    View view = LayoutInflater.from(context).inflate(R.layout.tab_bg, null);
	    ImageView im = (ImageView) view.findViewById(R.id.tabImg);
	    im.setBackgroundResource(img);
	    TextView tv = (TextView) view.findViewById(R.id.tabText);
	    tv.setTextSize(18);
	    tv.setText(text);
	    return view;
	}
	
}
