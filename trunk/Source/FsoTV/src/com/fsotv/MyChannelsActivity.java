package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import com.fsotv.dao.ChannelDao;
import com.fsotv.dto.Channel;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.utils.DownloadChannel;
import com.fsotv.utils.DownloadImage;
import com.fsotv.utils.YouTubeHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

public class MyChannelsActivity extends Activity {
	
	private final int MENU_UNSUBSCRIBE = Menu.FIRST;	
	
	private ProgressDialog pDialog;
	private ListView lvChannel;
	private List<ChannelEntry> channels;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_channels);
		
		lvChannel = (ListView)findViewById(R.id.lvChannel);
		
		pDialog = new ProgressDialog(MyChannelsActivity.this);
		pDialog.setMessage("Loading data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		
		channels = new ArrayList<ChannelEntry>();
				
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null){
			
		}
		
		setTitle("My Channels");
		
		// Launching new screen on Selecting Single ListItem
		lvChannel.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String channelId = channels.get(position).getIdReal();
				Intent i = new Intent(getApplicationContext(), BrowseVideosActivity.class);
				i.putExtra("channelId", channelId);
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
									lvChannel.invalidateViews();
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}

		return true;
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
			pDialog.show();
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
				channels.add(item);
			}
			runOnUiThread(new Runnable() {
				public void run() {
					ListItemAdapter adapter = new ListItemAdapter(
							MyChannelsActivity.this, R.layout.my_channel_item,
							channels);
					// updating listview
					registerForContextMenu(lvChannel);
					lvChannel.setAdapter(adapter);
					if(channels.size()==0){
						Toast.makeText(getApplicationContext(), "No results", Toast.LENGTH_LONG).show();
					}
				}
			});
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
		}

	}
	
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
				holder.viewCount = (TextView) row.findViewById(R.id.viewCount);
				holder.subscriberCount = (TextView) row.findViewById(R.id.subscriberCount);

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
			new DownloadChannel(holder.viewCount, holder.subscriberCount, 
					holder.image, holder.progressBar).execute(item.getIdReal());

			holder.title.setText(title);
			holder.description.setText(description);
						
			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView viewCount;
			TextView subscriberCount;
		}
	}
}
