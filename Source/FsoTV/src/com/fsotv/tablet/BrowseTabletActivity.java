package com.fsotv.tablet;

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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.ActivityBase;
import com.fsotv.BrowseVideosActivity;
import com.fsotv.R;
import com.fsotv.dao.ChannelDao;
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Channel;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.dto.Reference;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollGridViewListener;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.YouTubeHelper;

public class BrowseTabletActivity extends ActivityBase {
	private final int MENU_SUBSCRIBE = Menu.FIRST;
	private final int OPTION_SORT = Menu.FIRST + 1;
	private final int OPTION_USERTYPE = Menu.FIRST + 2;
	
	private Dialog sortDialog;
	private Dialog userTypeDialog;

	private GridView gvChannel;

	private List<ChannelEntry> channels;
	private List<Reference> userTypes;
	private ImageLoader imageLoader;
	private ListChannelAdapter adapter;

	private boolean isLoading = false;
	private String orderBy = "";
	private int maxResult = 15;
	private int maxLoad = 5;
	private int startIndex = 1;
	private String time;
	private String userType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_tablet);
		

		gvChannel = (GridView) findViewById(R.id.gvChannel);

		channels = new ArrayList<ChannelEntry>();
		imageLoader = new ImageLoader(getApplicationContext());
		userType = YouTubeHelper.USER_TYPE_COMEDIANS;
		orderBy = YouTubeHelper.ORDERING_VIEWCOUNT;
		time = YouTubeHelper.TIME_ALL_TIME;
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			userType = extras.getString("userType");
			userType = (userType == null) ? "" : userType;
		}
		hideBack();
		setHeader(userType);
		setTitle("Browse Channel");

		// Launching new screen on Selecting Single ListItem
		gvChannel.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String channelId = channels.get(position).getId();
				String channelTitle = channels.get(position).getTitle();
				Intent i = new Intent(getApplicationContext(),
						VideosTabletActivity.class);
				i.putExtra("channelId", channelId);
				i.putExtra("channelTitle", channelTitle);
				startActivity(i);
			}
		});
		gvChannel.setOnScrollListener(new EndlessScrollGridViewListener(gvChannel) {
			@Override
			public void loadData() {
				if (!isLoading) {
					isLoading = true;
					startIndex = startIndex + maxResult;
					new LoadChannels().execute();
				}
			}
		});
		adapter = new ListChannelAdapter(BrowseTabletActivity.this,
				R.layout.browse_tablet_item, channels);
		// updating listview
		registerForContextMenu(gvChannel);
		gvChannel.setAdapter(adapter);

		new LoadChannels().execute();
	}

	@Override
	protected void onDestroy() {
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
		if (v.getId() == R.id.lvChannel) {
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
			ChannelEntry entry = channels.get(position);
			ChannelDao channelDao = new ChannelDao(getApplicationContext());
			Channel channel = new Channel();
			channel.setNameChannel(entry.getTitle());
			channel.setDescribes(entry.getDescription());
			channel.setThumnail(entry.getImage());
			channel.setUri(entry.getLink());
			channel.setIdRealChannel(entry.getIdReal());
			channel.setCommentCount(entry.getCommentCount());
			channel.setVideoCount(entry.getVideoCount());
			channel.setViewCount(entry.getViewCount());
			channel.setUpdated(entry.getUpdated());
			channelDao.insertChannel(channel);
			if (channel.getIdChannel() > 0) {
				Toast.makeText(this, "Subscribed", Toast.LENGTH_SHORT).show();
			}
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, OPTION_SORT, 1, "Sort");
		menu.add(0, OPTION_USERTYPE, 2, "Channel Type");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case OPTION_SORT:
			if (sortDialog != null)
				sortDialog.show();
			else {
				createSortDialog(BrowseTabletActivity.this);
				if (sortDialog != null)
					sortDialog.show();
			}
			break;
		case OPTION_USERTYPE:
			if (userTypeDialog != null)
				userTypeDialog.show();
			else {
				createUserTypeDialog(BrowseTabletActivity.this);
				if (userTypeDialog != null)
					userTypeDialog.show();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
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
				new LoadChannels().execute();
			}
		});
		
	}

	private void createUserTypeDialog(Context context) {
		userTypeDialog = new Dialog(context);
		userTypeDialog.setContentView(R.layout.usertype_channel);
		userTypeDialog.setTitle("Channel");
		ListView lvUserType = (ListView) userTypeDialog
				.findViewById(R.id.lvUserType);
		
		lvUserType.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				userType = userTypes.get(position).getValue();
				userTypeDialog.dismiss();
				// Reload data
				setHeader(userType);
				new LoadChannels().execute();
			}
		});
		new LoadUserTypes().execute(lvUserType);
	}

	/**
	 * Background Async Task to get Channels from URL
	 * */
	class LoadChannels extends AsyncTask<String, String, List<ChannelEntry>> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected List<ChannelEntry> doInBackground(String... args) {
			// Demo data
//			 try {
//				 InputStream is = getResources().getAssets().open("Channels.txt");
//				 if (isLoading) {
//				 List<ChannelEntry> items = YouTubeHelper.getChannelsByStream(is);
//				 channels.addAll(items);
//				 }else{
//				 channels = YouTubeHelper.getChannelsByStream(is);
//				 }
//				 } catch (IOException e) {
//				 // TODO Auto-generated catch block
//				 e.printStackTrace();
//			 }
			//
			if (isLoading) {
				List<ChannelEntry> items = YouTubeHelper.getChannels(userType,
						orderBy, maxLoad, startIndex, time);
				return items;
			} else {
				startIndex = 1;
				channels = YouTubeHelper.getChannels(userType, orderBy,
						maxResult, startIndex, time);
			}
			return null;
		}

		/**
		 * After completing background
		 * **/
		protected void onPostExecute(List<ChannelEntry> result) {
			hideLoading();
			if (isLoading)
				isLoading = false;
			if (result != null) {
				channels.addAll(result);
				if (result.size() == 0) {
					// decrease start index so we will load more items at previous position
					startIndex = startIndex - maxResult;
					Toast.makeText(getApplicationContext(), "No more results",
							Toast.LENGTH_LONG).show();
				}
			}
			if (channels.size() > 0) {
				adapter.clear();
				for (ChannelEntry c : channels) {
					adapter.add(c);
				}
				adapter.notifyDataSetChanged();
				if(result == null){
					// Scroll to top if refresh list from beginning
					gvChannel.setSelection(0);
				}
			} else {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class ListChannelAdapter extends ArrayAdapter<ChannelEntry> {
		Context context;
		int layoutResourceId;
		List<ChannelEntry> data = null;

		public ListChannelAdapter(Context context, int layoutResourceId,
				List<ChannelEntry> data) {
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
				holder.updated = (TextView) row
						.findViewById(R.id.updated);

				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			ChannelEntry item = data.get(position);
			// format string
			String title = item.getTitle();
			if (title.length() > 25) {
				title = title.substring(0, 25) + "...";
			}
			
			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);
			holder.title.setText(title);
			holder.updated.setText(DataHelper.formatDate(item.getUpdated()));

			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView updated;
		}
	}

	/**
	 * Background Async Task to get References from database
	 * */
	class LoadUserTypes extends AsyncTask<ListView, String, String> {

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
			final ListView lvUserType = args[0];
			ReferenceDao referenceDao = new ReferenceDao(
					getApplicationContext());
			userTypes = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_USERTYPE, null);
			runOnUiThread(new Runnable() {
				public void run() {
					ListUserTypeAdapter adapter = new ListUserTypeAdapter(
							BrowseTabletActivity.this,
							R.layout.usertype_channel_item, userTypes);
					// updating listview
					lvUserType.setAdapter(adapter);
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

	class ListUserTypeAdapter extends ArrayAdapter<Reference> {
		Context context;
		int layoutResourceId;
		List<Reference> data = null;

		public ListUserTypeAdapter(Context context, int layoutResourceId,
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
