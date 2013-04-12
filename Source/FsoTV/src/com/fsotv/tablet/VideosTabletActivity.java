package com.fsotv.tablet;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
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
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.FacebookError;
import com.fsotv.ActivityBase;
import com.fsotv.CommentsActivity;
import com.fsotv.R;
import com.fsotv.VideoDetailActivity;
import com.fsotv.WatchVideoActivity;
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dao.VideoDao;
import com.fsotv.dto.Reference;
import com.fsotv.dto.Video;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.FaceBookHelper;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.TwitterHelper;
import com.fsotv.utils.YouTubeHelper;
import com.fsotv.utils.TwitterHelper.TwDialogListener;

public class VideosTabletActivity extends ActivityBase {
	private final int MENU_SUBSCRIBE = Menu.FIRST;
	private final int OPTION_SEARCH = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_CATEGORY = Menu.FIRST + 2;
		
	private final int OPTION_WATCH = Menu.FIRST + 3;
	private final int OPTION_COMMENTS = Menu.FIRST + 4;
	private final int OPTION_SHARE = Menu.FIRST + 5;

	private Dialog shareDialog;

	private VideoEntry video;
	private SharedPreferences mPrefs;
	private FaceBookHelper faceBookHelper = null;
	private TwitterHelper twitterHelper = null;
	private String postMessage = "";
	
	private Dialog sortDialog;
	private Dialog searchDialog;
	private Dialog categoryDialog;
	private ListView lvVideo;
	private List<VideoEntry> videos;
	private VideoEntry select;
	private List<Reference> categories;
	private ImageLoader imageLoader;
	private boolean isSubscribe = false; // Subscribe Action
	private boolean isCategory = false; // Change category Action
	private ListVideoAdapter adapter;
	private boolean isLoading = false;

	private ScrollView svDetail;
	private ImageView imgThumbnail;
	private TextView lblTitle;
	private TextView lblDescription;
	private TextView lblDuration;
	private TextView lblPublished;
	private TextView lblViewCount;
	private TextView lblFavoriteCount;
	
	private String channelId = "";
	private String categoryId = "";
	private String orderBy = "";
	private int maxResult = 5;
	private int maxLoad = 5;
	private int startIndex = 1;
	private String keyword = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_videos_tablet);
		
		mPrefs = getSharedPreferences("fsotv_oauth", MODE_PRIVATE);

		lvVideo = (ListView) findViewById(R.id.lvVideo);
		svDetail = (ScrollView)findViewById(R.id.svDetail);
		imgThumbnail = (ImageView) findViewById(R.id.imgThumbnail);
		lblTitle = (TextView) findViewById(R.id.txtvTitle);
		lblDescription = (TextView) findViewById(R.id.txtvDescriptionContent);
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
			channelId = extras.getString("channelId");
			categoryId = extras.getString("categoryId");
			channelTitle = extras.getString("channelTitle");

			channelId = (channelId == null) ? "" : channelId;
			categoryId = (categoryId == null) ? "" : categoryId;
			channelTitle = (channelTitle == null) ? "" : channelTitle;
		}
		if (!channelTitle.isEmpty()) {
			header = channelTitle;
			if (header.length() > 50)
				header = header.substring(0, 50);
		} else if (!categoryId.isEmpty()) {
			header = categoryId;
		}

		setHeader(header);
		setTitle("Browse Video");

		// Launching new screen on Selecting Single ListItem
		lvVideo.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				VideoEntry item = videos.get(position);
				String videoId = item.getId();
				new loadVideo().execute(videoId);
			}
		});
		lvVideo.setOnScrollListener(new EndlessScrollListViewListener(lvVideo){
			@Override 
			public void loadData(){
				if(!isLoading){
					isLoading = true;
					startIndex = startIndex + maxResult;
					new LoadVideos().execute();
				}
			}
		});
		adapter = new ListVideoAdapter(
				VideosTabletActivity.this, R.layout.video_tablet_item,
				videos);
		// updating listview
		registerForContextMenu(lvVideo);
		lvVideo.setAdapter(adapter);
		// Invisible detail
		svDetail.setVisibility(View.INVISIBLE);
		// Load videos
		new LoadVideos().execute();
	}

	@Override
	public void onDestroy(){
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
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item
				.getMenuInfo();
		int menuItemId = item.getItemId();
		// check for selected option
		if (menuItemId == MENU_SUBSCRIBE) {
			final int position = info.position;
			select = videos.get(position);
			isSubscribe = true;
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(VideosTabletActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
			}
		}

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
		menu.add(0, OPTION_COMMENTS, 1, "Comments");
		menu.add(0, OPTION_SHARE, 2, "Share");
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_SEARCH:
			if (searchDialog != null)
				searchDialog.show();
			else {
				createSearchDialog(VideosTabletActivity.this);
				if (searchDialog != null)
					searchDialog.show();
			}

			break;
		case OPTION_SORT:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(VideosTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case OPTION_CATEGORY:
			isCategory = true;
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(VideosTabletActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
			}

			break;
			
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
				createShareDialog(VideosTabletActivity.this);
				if (shareDialog != null)
					shareDialog.show();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void createSearchDialog(Context context) {
		searchDialog = new Dialog(context);
		searchDialog.setContentView(R.layout.search);
		searchDialog.setTitle("Search Video");
		final TextView txtSearch = (TextView) searchDialog
				.findViewById(R.id.txtSearch);
		Button btnSearch = (Button) searchDialog.findViewById(R.id.btnSearch);
		Button btnCancel = (Button) searchDialog.findViewById(R.id.btnCancel);
		btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				keyword = txtSearch.getText().toString();
				searchDialog.dismiss();
				// Get data again
				new LoadVideos().execute();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchDialog.dismiss();
			}
		});
	}
	
	private void createSortDialog(Context context) {
		sortDialog = new Dialog(context);
		sortDialog.setContentView(R.layout.sort);
		sortDialog.setTitle("Sort");
		final RadioButton rdViewed = (RadioButton) sortDialog
				.findViewById(R.id.rdViewed);
		final RadioButton rdPublished = (RadioButton) sortDialog
				.findViewById(R.id.rdPublished);
		Button btnSort = (Button) sortDialog.findViewById(R.id.btnSort);
		Button btnCancel = (Button) sortDialog.findViewById(R.id.btnCancel);
		btnSort.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String sort = YouTubeHelper.ORDERING_VIEWCOUNT;
				if (rdViewed.isChecked())
					sort = YouTubeHelper.ORDERING_VIEWCOUNT;
				else if (rdPublished.isChecked())
					sort = YouTubeHelper.ORDERING_PUBLISHED;
				orderBy = sort;
				sortDialog.dismiss();
				// Get data again
				new LoadVideos().execute();
			}
		});
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				sortDialog.dismiss();
			}
		});
	}
	
	private void createCategoryDialog(Context context) {
		categoryDialog = new Dialog(context);
		categoryDialog.setContentView(R.layout.category_video);
		categoryDialog.setTitle("Category");
		ListView lvCategory = (ListView) categoryDialog
				.findViewById(R.id.lvCategory);
		Button btnCancel = (Button) categoryDialog.findViewById(R.id.btnCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				categoryDialog.dismiss();
			}
		});
		lvCategory.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Reference item = categories.get(position);
				categoryDialog.dismiss();
				if (isSubscribe) {
					isSubscribe = false;
					// Subscribe
					subscribeVideo(item.getId());
				} else if (isCategory) {
					isCategory = false;
					// Reload data
					categoryId = item.getValue();
					setHeader(categoryId);
					new LoadVideos().execute();
				}
			}
		});
		new LoadCategories().execute(lvCategory);
	}

	private void subscribeVideo(int idCategory) {
		VideoDao videoDao = new VideoDao(getApplicationContext());
		Video video = new Video();
		video.setIdCategory(idCategory);
		video.setNameVideo(select.getTitle());
		video.setDescribes(select.getDescription());
		video.setThumnail(select.getImage());
		video.setUri(select.getLink());
		video.setAccount("");
		video.setTypeVideo(1);
		video.setIdRealVideo(select.getIdReal());
		video.setDuration(select.getDuration());
		video.setViewCount(select.getViewCount());
		video.setFavoriteCount(select.getFavoriteCount());
		video.setPublished(select.getPublished());
		video.setUpdated(select.getUpdated());
		videoDao.insertVideo(video);
		if (video.getIdVideo() > 0) {
			Toast.makeText(this, "Subscribed", Toast.LENGTH_SHORT).show();
		}
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
		final TextView lblPost = (TextView) shareDialog
				.findViewById(R.id.lblPost);
		txtMessage.setText(video.getLinkReal());
		Button btnShare = (Button) shareDialog.findViewById(R.id.btnShare);
		Button btnCancel = (Button) shareDialog.findViewById(R.id.btnCancel);
		rdFacebook.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					lblPost.setText("Link:");
			}
		});
		rdTwitter.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked)
					lblPost.setText("Message:");
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
		Intent i = new Intent(getApplicationContext(), CommentsTabletActivity.class);
		i.putExtra("videoId", video.getIdReal());
		i.putExtra("videoTitle", video.getTitle());
		startActivity(i);
	}

	public void onShareClick(View v) {
		if (shareDialog != null)
			shareDialog.show();
		else {
			createShareDialog(VideosTabletActivity.this);
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
			// Demo data
//			try {
//				InputStream is = getResources().getAssets().open(
//						"VideosInChannel.txt");
//				if(isLoading){
//					List<VideoEntry> items = null;
//					items = YouTubeHelper.getVideosByStream(is);
//					videos.addAll(items);
//				}else{
//					videos = YouTubeHelper.getVideosByStream(is);
//				}
//
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//
//			 
			if (isLoading) {
				List<VideoEntry> items = null;
				if (!channelId.isEmpty()) {
					items = YouTubeHelper.getVideosInChannel(channelId,
							orderBy, maxLoad, startIndex, keyword);
				} else if (!categoryId.isEmpty()) {
					items = YouTubeHelper.getVideosInCategory(categoryId,
							orderBy, maxLoad, startIndex, keyword);
				}
				return items;
			} else {
				startIndex = 1;
				if (!channelId.isEmpty()) {
					videos = YouTubeHelper.getVideosInChannel(channelId,
							orderBy, maxResult, startIndex, keyword);
				} else if (!categoryId.isEmpty()) {
					videos = YouTubeHelper.getVideosInCategory(categoryId,
							orderBy, maxResult, startIndex, keyword);
				}
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(List<VideoEntry> result) {
			hideLoading();
			if (isLoading)
				isLoading = false;
			if (result != null) {
				videos.addAll(result);
				if (result.size() == 0) {
					// decrease start index so we will load more items at previous position
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
				if(result == null){
					// Scroll to top if refresh list from beginning
					lvVideo.setSelectionAfterHeaderView();
				}
				// Load first video
				if(result == null){
					if(svDetail.getVisibility() == View.INVISIBLE){
						svDetail.setVisibility(View.VISIBLE);
					}
					String videoId = videos.get(0).getId();
					new loadVideo().execute(videoId);
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
				holder.duration = (TextView) row
						.findViewById(R.id.duration);
				holder.published = (TextView) row
						.findViewById(R.id.published);
				
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
			holder.duration.setText(DataHelper.secondsToTimer(item.getDuration()));
			holder.published.setText(DataHelper.formatDate(item.getPublished()));

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
			// try {
			// InputStream is = getAssets().open("VideoDetail.txt");
			// video = YouTubeHelper.getVideoByStream(is);
			// } catch (IOException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
			//
			video = YouTubeHelper.getVideoDetail(videoId);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();

			lblTitle.setText(video.getTitle());
			lblDescription.setText(video.getDescription());
			lblDuration.setText(DataHelper.secondsToTimer(video.getDuration()));
			lblPublished.setText(DataHelper.formatDate(video.getPublished()));
			lblViewCount.setText(DataHelper.numberWithCommas(video.getViewCount()));
			lblFavoriteCount.setText(DataHelper.numberWithCommas(video.getFavoriteCount()));
			
			imageLoader.DisplayImage(video.getImage(), imgThumbnail, null);

			isLoading = false;
		}

	}
	
	/**
	 * Background Async Task to get References from database
	 * */
	class LoadCategories extends AsyncTask<ListView, String, String> {

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
			final ListView lvCategory = args[0];
			ReferenceDao referenceDao = new ReferenceDao(
					getApplicationContext());
			categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, null);
			runOnUiThread(new Runnable() {
				public void run() {
					ListCategoryAdapter adapter = new ListCategoryAdapter(
							VideosTabletActivity.this,
							R.layout.category_video_item, categories);
					// updating listview
					lvCategory.setAdapter(adapter);
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

			// format string
			String title = item.getDisplay();
			String description = item.getDisplay();
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (description.length() > 150) {
				description = description.substring(0, 150) + "...";
			}
			holder.image.setImageResource(R.drawable.icon_cate25);
			holder.title.setText(title);
			holder.description.setText(description);

			return row;
		}

		class ListItemHolder {
			ImageView image;
			TextView title;
			TextView description;
		}
	}
}
