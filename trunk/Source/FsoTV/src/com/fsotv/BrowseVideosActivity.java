package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.BrowseChannelsActivity.LoadChannels;
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dao.VideoDao;
import com.fsotv.dto.Reference;
import com.fsotv.dto.Video;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.YouTubeHelper;
/**
 * Browse videos from youtube
 * Extend ActivityBase, allow:
 * + Subscribe video
 * + Search keyword
 * + Change category
 * + Sort video
 * + Load more items when scroll
 * 
 *
 */
public class BrowseVideosActivity extends ActivityBase {
	private final int MENU_SUBSCRIBE = Menu.FIRST;
	private final int OPTION_SEARCH = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_CATEGORY = Menu.FIRST + 2;
		
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

	String channelId = "";
	String categoryId = "";
	private String orderBy = "";
	private int maxResult = 5;
	private int maxLoad = 5;
	private int startIndex = 1;
	String keyword = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_videos);

		lvVideo = (ListView) findViewById(R.id.lvVideo);

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
				Intent i = new Intent(getApplicationContext(),
						VideoDetailActivity.class);
				i.putExtra("videoId", videoId);
				startActivity(i);
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
				BrowseVideosActivity.this, R.layout.browse_video_item,
				videos);
		// updating listview
		registerForContextMenu(lvVideo);
		lvVideo.setAdapter(adapter);
		
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
				createCategoryDialog(BrowseVideosActivity.this);
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
				createSearchDialog(BrowseVideosActivity.this);
				if (searchDialog != null)
					searchDialog.show();
			}

			break;
		case OPTION_SORT:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(BrowseVideosActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case OPTION_CATEGORY:
			isCategory = true;
			if (categoryDialog != null)
				categoryDialog.show();
			else {
				createCategoryDialog(BrowseVideosActivity.this);
				if (categoryDialog != null)
					categoryDialog.show();
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

	/**
	 * Background Async Task to get Videos data from URL
	 * */
	class LoadVideos extends AsyncTask<String, String, String> {

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
			// Demo data
			try {
				InputStream is = getResources().getAssets().open(
						"VideosInChannel.txt");
				if(isLoading){
					List<VideoEntry> items = null;
					items = YouTubeHelper.getVideosByStream(is);
					videos.addAll(items);
				}else{
					videos = YouTubeHelper.getVideosByStream(is);
				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//
//			 
//			if (isLoading) {
//				List<VideoEntry> items = null;
//				if (!channelId.isEmpty()) {
//					items = YouTubeHelper.getVideosInChannel(channelId,
//							orderBy, maxLoad, startIndex, keyword);
//				} else if (!categoryId.isEmpty()) {
//					items = YouTubeHelper.getVideosInCategory(categoryId,
//							orderBy, maxLoad, startIndex, keyword);
//				}
//				videos.addAll(items);
//			} else {
//				startIndex = 1;
//				if (!channelId.isEmpty()) {
//					videos = YouTubeHelper.getVideosInChannel(channelId,
//							orderBy, maxResult, startIndex, keyword);
//				} else if (!categoryId.isEmpty()) {
//					videos = YouTubeHelper.getVideosInCategory(categoryId,
//							orderBy, maxResult, startIndex, keyword);
//				}
//			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			if(isLoading)
				isLoading = false;
			adapter.clear();
			for (VideoEntry c : videos){
	            adapter.add(c);
	        }
			adapter.notifyDataSetChanged();
			if(videos.size()==0){
				Toast.makeText(getApplicationContext(), "No results", Toast.LENGTH_LONG).show();
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
				holder.viewCount = (TextView) row.findViewById(R.id.viewCount);
				holder.favoriteCount = (TextView) row
						.findViewById(R.id.favoriteCount);
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
			if (description.length() > 150) {
				description = description.substring(0, 150) + "...";
			}

			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);

			holder.title.setText(title);
			holder.description.setText(description);
			if(item.getViewCount()==-1)
				holder.viewCount.setText("-");
			else holder.viewCount.setText(DataHelper.numberWithCommas(item
					.getViewCount()));
			if(item.getFavoriteCount()==-1)
				holder.favoriteCount.setText("-");
			else holder.favoriteCount.setText(DataHelper.numberWithCommas(item
					.getFavoriteCount()));
			holder.duration.setText(DataHelper.secondsToTimer(item.getDuration()));
			holder.published.setText(DataHelper.formatDate(item.getPublished()));

			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView viewCount;
			TextView favoriteCount;
			TextView duration;
			TextView published;
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
							BrowseVideosActivity.this,
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
