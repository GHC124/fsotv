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
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.FaceBookHelper.FbListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.StringHelper;
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.TwitterHelper.TwListener;
import com.fsotv.utils.YouTubeHelper;
import com.fsotv.utils.YouTubeHelper.YtListener;

public class VideoDetailTabletActivity extends ActivityBase {
	private final String TAG = "VideoDetail";

	private final int OPTION_SEARCH = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_TIME = Menu.FIRST + 2;
	private final int OPTION_CATEGORY = Menu.FIRST + 3;
	private final int OPTION_WATCH = Menu.FIRST + 4;
	private final int OPTION_COMMENT = Menu.FIRST + 5;
	private final int OPTION_SHARE = Menu.FIRST + 6;
	
	private DialogBase sortDialog;
	private DialogBase timeDialog;
	private DialogBase categoryDialog;
	private DialogBase shareDialog;
	private DialogBase commentDialog;
	private VideoEntry video;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	private String postMessage = ""; // FaceBook and Twitter post message
	private String commentMessage = ""; // YouTube comment

	private ListView lvVideo;
	private List<VideoEntry> videos;
	private List<Reference> categories;
	private ImageLoader imageLoader;
	private ListVideoAdapter adapter;
	private boolean isVideosLoading = false;
	private boolean isVideoLoading = false;

	private TextView tvCategory;
	private TextView tvSort;
	private TextView tvTime;

	private ImageView imgThumbnail;
	private TextView lblTitle;
	private TextView lblDuration;
	private TextView lblPublished;
	private TextView lblViewCount;
	private TextView lblFavoriteCount;
	private TextView lblAuthor;
	private TabHost tabHost;
	private LocalActivityManager mLocalActivityManager;
	private YouTubeHelper youTubeHelper;

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

		mPrefs = getSharedPreferences(MainActivity.SHARED_PREFERENCE,
				MODE_PRIVATE);

		tvCategory = (TextView) findViewById(R.id.tvCategory);
		tvSort = (TextView) findViewById(R.id.tvSort);
		tvTime = (TextView) findViewById(R.id.tvTime);
		lvVideo = (ListView) findViewById(R.id.lvVideo);
		imgThumbnail = (ImageView) findViewById(R.id.imgThumbnail);
		lblTitle = (TextView) findViewById(R.id.txtvTitle);
		lblDuration = (TextView) findViewById(R.id.duration);
		lblPublished = (TextView) findViewById(R.id.published);
		lblViewCount = (TextView) findViewById(R.id.viewCount);
		lblFavoriteCount = (TextView) findViewById(R.id.favoriteCount);
		lblAuthor = (TextView) findViewById(R.id.author);

		youTubeHelper = new YouTubeHelper(this, mPrefs);
		youTubeHelper.setListener(mYtListener);

		video = new VideoEntry();
		imageLoader = new ImageLoader(getApplicationContext());
		videos = new ArrayList<VideoEntry>();

		orderBy = YouTubeHelper.ORDERING_PUBLISHED;
		time = YouTubeHelper.TIME_ALL_TIME;
		maxResult = 10;
		maxLoad = 5;
		startIndex = 1;
		keyword = "";

		String channelTitle = "";
		String orderByTitle = "";
		String timeTitle = "";
		String header = "";
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			videoId = extras.getString("videoId");
			channelId = extras.getString("channelId");
			categoryId = extras.getString("categoryId");
			orderBy = extras.getString("orderBy");
			keyword = extras.getString("keyword");
			time = extras.getString("time");
			channelTitle = extras.getString("channelTitle");
			orderByTitle = extras.getString("orderByTitle");
			timeTitle = extras.getString("timeTitle");
			
			videoId = (videoId == null) ? "" : videoId;
			channelId = (channelId == null) ? "" : channelId;
			categoryId = (categoryId == null) ? "" : categoryId;
			orderBy = (orderBy == null) ? YouTubeHelper.ORDERING_PUBLISHED : orderBy;
			keyword = (keyword == null) ? "" : keyword;
			time = (time == null) ? YouTubeHelper.TIME_ALL_TIME : time;
			channelTitle = (channelTitle == null) ? "" : channelTitle;
			orderByTitle = (orderByTitle == null) ? "" : orderByTitle;
			timeTitle = (timeTitle == null) ? "" : timeTitle;
		}
		// If browse videos by channelID, we'll disable function change category
		if (!channelId.isEmpty() && !channelTitle.isEmpty()) {
			header = channelTitle;
			// Disable change category
			tvCategory.setVisibility(View.GONE);

		} else if (!categoryId.isEmpty()) {
			header = categoryId;
			tvCategory.setText(categoryId);
		}
		tvSort.setText(orderByTitle);
		tvTime.setText(timeTitle);
		// Show search control if keyword is set
		if(!keyword.isEmpty()){
			setKeyword(keyword);
			showSearch();
		}
		setHeader(header);
		setTitle("Browse Video");

		/*
		 * Add click action to control
		 */
		tvCategory.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});
		tvSort.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});
		tvTime.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});
		// Launching new screen on Selecting Single ListItem
		lvVideo.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// view.getFocusables(position);
				// view.setSelected(true);

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
		menu.add(0, OPTION_WATCH, 0, "Watch");
		menu.add(0, OPTION_COMMENT, 1, "Comments");
		menu.add(0, OPTION_SHARE, 2, "Share");
		menu.add(0, OPTION_SEARCH, 3, "Search");
		menu.add(0, OPTION_SORT, 4, "Sort");
		menu.add(0, OPTION_TIME, 5, "Time");
		// Only show category dialog when browse by category
		if (!categoryId.isEmpty()) {
			menu.add(0, OPTION_CATEGORY, 6, "Category");
		}	
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
			break;
		case OPTION_SEARCH:
			showSearch(); // Call show search method of parent
			break;
		case OPTION_SORT:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(VideoDetailTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case OPTION_TIME:
			if (timeDialog != null)
				timeDialog.show();
			else {
				createTimeDialog(VideoDetailTabletActivity.this);
				if (timeDialog != null)
					timeDialog.show();
			}
			break;
		case OPTION_CATEGORY:
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(VideoDetailTabletActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
			}

			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Hander click event when user select search, sort, category, time
	 * 
	 * @param v
	 */
	public void onOptionClick(View v) {
		if (v.getId() == R.id.tvCategory) {
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(VideoDetailTabletActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
			}
		} else if (v.getId() == R.id.tvSort) {
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(VideoDetailTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
		} else if (v.getId() == R.id.tvTime) {
			if (timeDialog != null)
				timeDialog.show();
			else {
				createTimeDialog(VideoDetailTabletActivity.this);
				if (timeDialog != null)
					timeDialog.show();
			}
		}
	}
	/**
	 * 
	 * @param context
	 */

	/**
	 * Create dialog that allow user to sort videos
	 * 
	 * @param context
	 */

	private void createSortDialog(Context context) {
		Log.i("VideoDetailTabletActivity", "Start createSortDialog()");

		sortDialog = new DialogBase(context);
		sortDialog.setContentView(R.layout.sort);
		sortDialog.setHeader("Sort");

		final RadioButton rdViewed = (RadioButton) sortDialog
				.findViewById(R.id.rdViewed);

		final RadioButton rdPublished = (RadioButton) sortDialog
				.findViewById(R.id.rdPublished);

		Button btnSort = (Button) sortDialog.findViewById(R.id.btnSort);
		btnSort.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String display = "";

				if (rdViewed.isChecked()) {
					orderBy = YouTubeHelper.ORDERING_VIEWCOUNT;
					display = rdViewed.getText().toString();

				} else if (rdPublished.isChecked()) {
					orderBy = YouTubeHelper.ORDERING_PUBLISHED;
					display = rdPublished.getText().toString();
				}

				sortDialog.dismiss();

				// Get data again
				tvSort.setText(display);

				new LoadVideos().execute();
			}
		});

		Log.i("VideoDetailTabletActivity", "End createSortDialog()");
	}

	/**
	 * Create dialog that allow user to change time of videos
	 * 
	 * @param context
	 */

	private void createTimeDialog(Context context) {
		Log.i("VideoDetailTabletActivity", "Start createTimeDialog()");

		timeDialog = new DialogBase(context);
		timeDialog.setContentView(R.layout.time);
		timeDialog.setHeader("Time");

		final RadioButton rdToday = (RadioButton) timeDialog
				.findViewById(R.id.rdToday);

		final RadioButton rdThisWeek = (RadioButton) timeDialog
				.findViewById(R.id.rdThisWeek);

		final RadioButton rdThisMonth = (RadioButton) timeDialog
				.findViewById(R.id.rdThisMonth);

		final RadioButton rdAllTime = (RadioButton) timeDialog
				.findViewById(R.id.rdAllTime);

		Button btnOk = (Button) timeDialog.findViewById(R.id.btnOk);
		btnOk.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String display = "";
				if (rdToday.isChecked()) {
					time = YouTubeHelper.TIME_TODAY;
					display = rdToday.getText().toString();

				} else if (rdThisWeek.isChecked()) {
					time = YouTubeHelper.TIME_THIS_WEEK;
					display = rdThisWeek.getText().toString();

				} else if (rdThisMonth.isChecked()) {
					time = YouTubeHelper.TIME_THIS_MONTH;
					display = rdThisMonth.getText().toString();

				} else if (rdAllTime.isChecked()) {
					time = YouTubeHelper.TIME_ALL_TIME;
					display = rdAllTime.getText().toString();
				}

				timeDialog.dismiss();

				// Get data again
				tvTime.setText(display);

				new LoadVideos().execute();
			}
		});

		Log.i("VideoDetailTabletActivity", "End createTimeDialog()");
	}

	/**
	 * 
	 * @param context
	 */

	/**
	 * Create dialog that allow user to choose category of videos
	 * 
	 * @param context
	 */

	private void createCategoryDialog(Context context) {
		Log.i("VideoDetailTabletActivity", "Start createCategoryDialog()");

		categoryDialog = new DialogBase(context);
		categoryDialog.setContentView(R.layout.category_video);
		categoryDialog.setHeader("Category");

		ListView lvCategory = (ListView) categoryDialog
				.findViewById(R.id.lvCategory);

		lvCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Reference item = categories.get(position);
				categoryDialog.dismiss();
				// Reload data
				categoryId = item.getValue();
				setHeader(categoryId);
				tvCategory.setText(item.getDisplay());

				new LoadVideos().execute();
			}
		});

		new LoadCategories().execute(lvCategory);
		Log.i("VideoDetailTabletActivity", "End createCategoryDialog()");
	}

	/**
	 * Override Search function to search
	 */
	@Override
	protected void Search(String key) {
		keyword = key;
		new LoadVideos().execute();
	}

	/**
	 * Override Close Search function when close search control
	 */
	@Override
	protected void CloseSearch() {
		keyword = "";
		new LoadVideos().execute();
	}

	/**
	 * Create share dialog, allow user type message
	 * 
	 * @param context
	 */
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
					if (faceBookHelper == null) {
						faceBookHelper = new FaceBookHelper(
								VideoDetailTabletActivity.this, mPrefs);
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
								VideoDetailTabletActivity.this, mPrefs);
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

	/**
	 * Create dialog that allow user to comment videos
	 * 
	 * @param context
	 */
	private void createCommentDialog(Context context) {
		if (isVideoLoading) {
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

	public void onWatchClick(View c) {
		if (isVideoLoading) {
			Toast.makeText(getApplicationContext(), "Loading data",
					Toast.LENGTH_SHORT).show();
			return;
		}
		Intent i = new Intent(getApplicationContext(),
				WatchVideoTabletActivity.class);
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
			createCommentDialog(VideoDetailTabletActivity.this);
			if (commentDialog != null)
				commentDialog.show();
		}
	}

	public void onShareClick(View v) {
		if (shareDialog != null) {
			shareDialog.show();

		} else {
			createShareDialog(VideoDetailTabletActivity.this);
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
					items = youTubeHelper.getVideosInChannel(channelId,
							orderBy, maxLoad, startIndex, keyword, time);
				} else if (!categoryId.isEmpty()) {
					items = youTubeHelper.getVideosInCategory(categoryId,
							orderBy, maxLoad, startIndex, keyword, time);
				}
				return items;
			} else {
				startIndex = 1;
				if (!channelId.isEmpty()) {
					videos = youTubeHelper.getVideosInChannel(channelId,
							orderBy, maxResult, startIndex, keyword, time);
				} else if (!categoryId.isEmpty()) {
					videos = youTubeHelper.getVideosInCategory(categoryId,
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
	/**
	 * Adapter to populate videos to listview 
	 *
	 */
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
	 * Background Async Task to get References from database
	 * */
	class LoadCategories extends AsyncTask<ListView, String, String> {
		ListView lvCategory = null;

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected String doInBackground(ListView... args) {
			lvCategory = args[0];

			ReferenceDao referenceDao = new ReferenceDao(
					getApplicationContext());

			categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, null);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {

			ListCategoryAdapter adapter = new ListCategoryAdapter(
					VideoDetailTabletActivity.this, R.layout.category_video_item,
					categories);

			// updating listview
			lvCategory.setAdapter(adapter);

			hideLoading();
		}

	}

	/**
	 * Adapter to populate Category to category listView
	 * 
	 * 
	 */
	class ListCategoryAdapter extends ArrayAdapter<Reference> {
		Context context;
		int layoutResourceId;
		List<Reference> data = null;

		public ListCategoryAdapter(Context context, int layoutResourceId,
				List<Reference> data) {
			super(context, layoutResourceId, data);

			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			View row = convertView;
			ListItemHolder holder = null;
			final Reference item = data.get(position);

			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new ListItemHolder();
				holder.image = (ImageView) row.findViewById(R.id.image);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row
						.findViewById(R.id.description);

				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			holder.title.setText(StringHelper.fomatCategoryTitle(item
					.getDisplay()));
			holder.description.setText(StringHelper.formatDescription(item
					.getDisplay()));
			// Image
			String value = item.getValue();
			if (value != null) {
				if (value.equals("Comedy")) {
					holder.image.setBackgroundResource(R.drawable.comedy32);
				}else if (value.equals("Music")) {
					holder.image.setBackgroundResource(R.drawable.music32);
				}else if (value.equals("News")) {
					holder.image.setBackgroundResource(R.drawable.news32);
				}else if (value.equals("Autos")) {
					holder.image.setBackgroundResource(R.drawable.auto32);
				}else if (value.equals("Education")) {
					holder.image.setBackgroundResource(R.drawable.edu32);
				}else if (value.equals("Entertainment")) {
					holder.image.setBackgroundResource(R.drawable.enter32);
				}else if (value.equals("Film")) {
					holder.image.setBackgroundResource(R.drawable.film32);
				}else if (value.equals("Howto")) {
					holder.image.setBackgroundResource(R.drawable.howto32);
				}else if (value.equals("People")) {
					holder.image.setBackgroundResource(R.drawable.people32);
				}else if (value.equals("Animals")) {
					holder.image.setBackgroundResource(R.drawable.animal32);
				}else if (value.equals("Tech")) {
					holder.image.setBackgroundResource(R.drawable.tech32);
				}else if (value.equals("Sports")) {
					holder.image.setBackgroundResource(R.drawable.sport32);
				}else if (value.equals("Travel")) {
					holder.image.setBackgroundResource(R.drawable.travel32);
				}
				
			}

			return row;
		}

		class ListItemHolder {
			ImageView image;
			TextView title;
			TextView description;
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

			video = youTubeHelper.getVideoDetail(videoId);

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
			lblAuthor.setText(video.getAuthor());

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
