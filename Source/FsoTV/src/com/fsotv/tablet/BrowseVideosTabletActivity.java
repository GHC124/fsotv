package com.fsotv.tablet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.ActivityBase;
import com.fsotv.DialogBase;
import com.fsotv.R;
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dao.VideoDao;
import com.fsotv.dto.Reference;
import com.fsotv.dto.Video;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollGridViewListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.StringHelper;
import com.fsotv.utils.YouTubeHelper;

/**
 * Browse videos from youtube Extend ActivityBase, allow: +
 * Browse by category + Browse by channel id + Subscribe video + Search keyword
 * + Change category, disable when browse by channel id + Sort video + View
 * video detail when click an video + Load more items when scroll
 * 
 * @authors ChungPV1 CuongVM1 NhungHTH1 TungPT6
 */
public class BrowseVideosTabletActivity extends ActivityBase {

	// Menus
	private final int MENU_SUBSCRIBE = Menu.FIRST;
	private final int OPTION_SEARCH = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_TIME = Menu.FIRST + 2;
	private final int OPTION_CATEGORY = Menu.FIRST + 3;

	// Declare needed components for this activity
	private DialogBase typeDialog;
	private DialogBase sortDialog;
	private DialogBase timeDialog;
	private DialogBase categoryDialog;
	private GridView gvVideo;
	private TextView tvVideos;
	private TextView tvCategory;
	private TextView tvSort;
	private TextView tvTime;
	private List<VideoEntry> videos;
	private VideoEntry select;
	private List<Reference> categories;
	private ImageLoader imageLoader;

	// Subscribe Action
	private boolean isSubscribe = false;

	// Change category Action
	private boolean isCategory = false;

	private ListVideoAdapter adapter;
	private boolean isLoading = false;

	// Sort, Search
	private String channelId = "";
	private String categoryId = "";
	private String orderBy = "";
	private int maxResult = 10;
	private int maxLoad = 5;
	private int startIndex = 1;
	private String keyword = "";
	private String time = "";

	/**
	 * This function used to initialize components for this activty
	 */
	private void initComponets() {
		Log.i("Info", "Start initComponents() in BrowseVideosTabletActivity class");

		gvVideo = (GridView) findViewById(R.id.gvVideo);
		
		tvVideos = (TextView) findViewById(R.id.tvVideos);
		tvCategory = (TextView) findViewById(R.id.tvCategory);
		tvSort = (TextView) findViewById(R.id.tvSort);
		tvTime = (TextView) findViewById(R.id.tvTime);

		imageLoader = new ImageLoader(getApplicationContext());
		videos = new ArrayList<VideoEntry>();
		orderBy = YouTubeHelper.ORDERING_VIEWCOUNT;
		time = YouTubeHelper.TIME_ALL_TIME;

		// Init data
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
		// If browse videos by channelID, we'll disable function change category
		if (!channelId.isEmpty() && !channelTitle.isEmpty()) {
			header = channelTitle;
			// Disable change category
			tvCategory.setVisibility(View.GONE);

		} else if (!categoryId.isEmpty()) {
			header = categoryId;
			tvCategory.setText(categoryId);
		}

		setHeader(header);
		setTitle("Browse Video");

		Log.i("Info", "End initComponents() in BrowseVideosTabletActivity class");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_videos_tablet);

		initComponets();

		/*
		 * Add click action to control
		 */
		tvVideos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});
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
		gvVideo.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				VideoEntry item = videos.get(position);
				String videoId = item.getId();
				Intent i = new Intent(getApplicationContext(),
						VideoDetailTabletActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("videoId", videoId);
				i.putExtra("channelId", channelId);
				i.putExtra("categoryId", categoryId);
				
				startActivity(i);
			}
		});
		gvVideo.setOnScrollListener(new EndlessScrollGridViewListener(gvVideo) {
			@Override
			public void loadData() {
				if (!isLoading) {
					isLoading = true;
					startIndex = startIndex + maxResult;
					new LoadVideos().execute();
				}
			}
		});
		adapter = new ListVideoAdapter(BrowseVideosTabletActivity.this,
				R.layout.browse_videos_tablet_item, videos);
		// updating listview
		registerForContextMenu(gvVideo);
		gvVideo.setAdapter(adapter);

		new LoadVideos().execute();
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
				createCategoryDialog(BrowseVideosTabletActivity.this);
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
		menu.add(0, OPTION_TIME, 2, "Time");

		// Only show category dialog when browse by channel
		if (!categoryId.isEmpty()) {
			menu.add(0, OPTION_CATEGORY, 3, "Category");
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case OPTION_SEARCH:
			showSearch(); // Call show search method of parent
			break;
		case OPTION_SORT:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(BrowseVideosTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case OPTION_TIME:
			if (timeDialog != null)
				timeDialog.show();
			else {
				createTimeDialog(BrowseVideosTabletActivity.this);
				if (timeDialog != null)
					timeDialog.show();
			}
			break;
		case OPTION_CATEGORY:
			isCategory = true;
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(BrowseVideosTabletActivity.this);
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
		switch (v.getId()) {
		case R.id.tvVideos:
			if (typeDialog != null)
				typeDialog.show();
			else {
				createTypeDialog(BrowseVideosTabletActivity.this);
				if (typeDialog != null)
					typeDialog.show();
			}
			break;
		case R.id.tvCategory:
			isCategory = true;
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(BrowseVideosTabletActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
			}
			break;
		case R.id.tvSort:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(BrowseVideosTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case R.id.tvTime:
			if (timeDialog != null)
				timeDialog.show();
			else {
				createTimeDialog(BrowseVideosTabletActivity.this);
				if (timeDialog != null)
					timeDialog.show();
			}
			break;
		}
	}

	/**
	 * Create dialog that allow user to change to channels
	 * 
	 * @param context
	 */
	private void createTypeDialog(Context context) {
		typeDialog = new DialogBase(context);
		typeDialog.setContentView(R.layout.type);
		typeDialog.setHeader("Videos");

		final TextView txtVideos = (TextView) typeDialog
				.findViewById(R.id.tvVideos);
		final TextView txtChannels = (TextView) typeDialog
				.findViewById(R.id.tvChannels);

		txtVideos.setVisibility(View.GONE);
		txtChannels.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				typeDialog.dismiss();
				Intent i = new Intent(getApplicationContext(),
						BrowseChannelsTabletActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
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
		Log.i("BrowseVideosTabletActivity", "Start createSortDialog()");

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

		Log.i("BrowseVideosTabletActivity", "End createSortDialog()");
	}

	/**
	 * Create dialog that allow user to change time of videos
	 * 
	 * @param context
	 */

	private void createTimeDialog(Context context) {
		Log.i("BrowseVideosTabletActivity", "Start createTimeDialog()");

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

		Log.i("BrowseVideosTabletActivity", "End createTimeDialog()");
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
		Log.i("BrowseVideosTabletActivity", "Start createCategoryDialog()");

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

				if (isSubscribe) {
					isSubscribe = false;
					// Subscribe
					subscribeVideo(item.getId());

				} else if (isCategory) {
					isCategory = false;
					// Reload data
					categoryId = item.getValue();
					setHeader(categoryId);
					tvCategory.setText(item.getDisplay());

					new LoadVideos().execute();
				}
			}
		});

		new LoadCategories().execute(lvCategory);
		Log.i("BrowseVideosTabletActivity", "End createCategoryDialog()");
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
	 * Subscribe video by category id
	 * 
	 * @param idCategory
	 */
	/**
	 * 
	 * @param idCategory
	 * @author TungPT6
	 */
	private void subscribeVideo(int idCategory) {
		Log.i("BrowseVideosTabletActivity", "Start subscribeVideo()");

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

		Log.i("BrowseVideosTabletActivity", "Start subscribeVideo()");
	}

	/**
	 * Background Async Task to get Videos data from URL
	 * 
	 * @author CuongVM1
	 */
	class LoadVideos extends AsyncTask<String, String, List<VideoEntry>> {

		/**
		 * Before starting background thread Show Progress Dialog
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		/**
		 * Load data and pull it into list view
		 */
		@Override
		protected List<VideoEntry> doInBackground(String... args) {
			if (isLoading) {
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
		 */
		protected void onPostExecute(List<VideoEntry> result) {
			hideLoading();

			if (isLoading)
				isLoading = false;

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
					gvVideo.setSelection(0);
				}

			} else {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	/**
	 * Adapter to populate videos to listView
	 * 
	 */
	/**
	 * 
	 * @author CuongVM1
	 */
	class ListVideoAdapter extends ArrayAdapter<VideoEntry> {

		Context context;
		int layoutResourceId;
		List<VideoEntry> data = null;

		/**
		 * The constructor of ListVideoAdapter class
		 * 
		 * @param context
		 * @param layoutResourceId
		 * @param data
		 */
		public ListVideoAdapter(Context context, int layoutResourceId,
				List<VideoEntry> data) {
			super(context, layoutResourceId, data);

			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.i("Info", "Start getView() in ListVideoAdapter class");

			// Declare view and holder for this adapter
			View row = convertView;
			ListItemHolder holder = null;

			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);
				// New ListItemHolder
				holder = new ListItemHolder();

				// Create new components
				holder.image = (ImageView) row.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) row
						.findViewById(R.id.progressBar);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.viewCount = (TextView) row.findViewById(R.id.viewCount);
				holder.duration = (TextView) row.findViewById(R.id.duration);
				holder.published = (TextView) row.findViewById(R.id.published);

				row.setTag(holder);

			} else {
				holder = (ListItemHolder) row.getTag();
			}

			VideoEntry item = data.get(position);

			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);
			String title = item.getTitle();
			if(title.length()>30)
				title = title.substring(0, 30) + "...";
			holder.title.setText(title);
			if (item.getViewCount() == -1)
				holder.viewCount.setText("-");
			else
				holder.viewCount.setText(DataHelper.numberWithCommas(item
						.getViewCount()));
			holder.duration.setText(DataHelper.secondsToTimer(item
					.getDuration()));
			holder.published
					.setText(DataHelper.formatDate(item.getPublished()));

			Log.i("Info", "End getView() in ListVideoAdapter class");

			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView viewCount;
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
					BrowseVideosTabletActivity.this, R.layout.category_video_item,
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
}
