package com.fsotv.tablet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.ActivityBase;
import com.fsotv.BrowseVideosActivity;
import com.fsotv.DialogBase;
import com.fsotv.MyVideosActivity;
import com.fsotv.R;
import com.fsotv.dao.ChannelDao;
import com.fsotv.dto.Channel;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.ImageLoader;
/**
 * Show channels that are subscribe and store in database
 * Extend ActivityBase, allow:
 * + Unsubscribe
 * + View videos when click an channel
 *
 */
public class MyChannelsTabletActivity extends ActivityBase {
	// Menus
	private final int MENU_UNSUBSCRIBE = Menu.FIRST;
	// Views
	private DialogBase typeDialog;
	private TextView tvChannels;
	private GridView gvChannel;
	// Properties
	private List<ChannelEntry> channels;
	private ImageLoader imageLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_channels);
		
		gvChannel = (GridView)findViewById(R.id.gvChannel);
		tvChannels = (TextView)findViewById(R.id.tvChannels);
		registerForContextMenu(gvChannel);
		
		channels = new ArrayList<ChannelEntry>();
		imageLoader = new ImageLoader(getApplicationContext());
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null){
			
		}
		setHeader("Channels");
		setTitle("My Channels");
		/*
		 * Add click action to control
		 */
		tvChannels.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});
		// Launching new screen on Selecting Single ListItem
		gvChannel.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String channelId = channels.get(position).getIdReal();
				String channelTitle = channels.get(position).getTitle();
				Intent i = new Intent(getApplicationContext(), BrowseVideosActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("channelId", channelId);
				i.putExtra("channelTitle", channelTitle);
				startActivity(i);
			}
		});
		
		new loadChannels().execute();
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
			menu.add(Menu.NONE, MENU_UNSUBSCRIBE, 0, "Unsubscribe");
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
		if (menuItemId == MENU_UNSUBSCRIBE) {
			final int position = info.position;
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
			alertDialogBuilder.setTitle("Unsubscribe Channel");
			alertDialogBuilder
					.setMessage("Do you want to unsubscribe this channel?")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id){ 									
									ChannelEntry entry = channels.get(position);
									ChannelDao channelDao = new ChannelDao(getApplicationContext());
									channelDao.deleteChannel(Integer.parseInt(entry.getId()));
									channels.remove(position);
									gvChannel.invalidateViews();
									
									dialog.dismiss();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		return true;
	}
	/**
	 * Hander click event when user select search, sort, category, time
	 * @param v
	 */
	public void onOptionClick(View v){
		switch(v.getId())
		{
		case R.id.tvChannels:
			if (typeDialog != null)
				typeDialog.show();
			else {
				createTypeDialog(MyChannelsTabletActivity.this);
				if (typeDialog != null)
					typeDialog.show();
			}
			break;
		}
	}
	/**
	 * Create dialog that allow user to change to channels
	 * @param context
	 */
	private void createTypeDialog(Context context) {
		typeDialog = new DialogBase(context);
		typeDialog.setContentView(R.layout.type);
		typeDialog.setHeader("Channels");
		final TextView txtVideos = (TextView) typeDialog
				.findViewById(R.id.tvVideos);
		final TextView txtChannels = (TextView) typeDialog
				.findViewById(R.id.tvChannels);
		txtChannels.setVisibility(View.GONE);
		txtVideos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				typeDialog.dismiss();
				Intent i = new Intent(getApplicationContext(), MyVideosActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
	}
	/**
	 * Background Async Task to get Channels from URL
	 * */
	class loadChannels extends AsyncTask<String, String, String> {

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
			ChannelDao channelDao = new ChannelDao(getApplicationContext());
			List<Channel> list = channelDao.getListChannel();
			for (Channel channel:list) {
				ChannelEntry item = new ChannelEntry();
				item.setId(channel.getIdChannel()+"");
				item.setIdReal(channel.getIdRealChannel()+"");
				item.setTitle(channel.getNameChannel());
				item.setImage(channel.getThumnail());
				item.setLink(channel.getUri());
				item.setDescription(channel.getDescribes());
				item.setCommentCount(channel.getCommentCount());
				item.setVideoCount(channel.getVideoCount());
				item.setViewCount(channel.getViewCount());
				item.setUpdated(channel.getUpdated());
				channels.add(item);
			}
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			ListItemAdapter adapter = new ListItemAdapter(
					MyChannelsTabletActivity.this, R.layout.my_channels_item_tablet,
					channels);
			// updating listview
			gvChannel.setAdapter(adapter);
			if(channels.size()==0){
				Toast.makeText(getApplicationContext(), "No results", Toast.LENGTH_LONG).show();
			}
		}

	}
	/**
	 * Adapter to populate Channels to listView
	 *
	 */
	class ListItemAdapter extends ArrayAdapter<ChannelEntry> {
		Context context;
		int layoutResourceId;
		List<ChannelEntry> data = null;

		public ListItemAdapter(Context context, int layoutResourceId,
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
				holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row.findViewById(R.id.description);
				holder.videoCount = (TextView) row
						.findViewById(R.id.videoCount);
				
				
				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			ChannelEntry item = data.get(position);
			// format string
			String title = item.getTitle();
			String description = item.getDescription();
			if(title.length()>50){
				title = title.substring(0, 50) + "...";
			}
			if(description.length()>150){
				description = description.substring(0, 150) + "...";
			}

			imageLoader.DisplayImage(item.getImage(), holder.image, holder.progressBar);
		
			holder.title.setText(title);
			holder.description.setText(description);
			holder.videoCount.setText(DataHelper.numberWithCommas(item
					.getVideoCount()));
			
			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView videoCount;
		}
	}
}
