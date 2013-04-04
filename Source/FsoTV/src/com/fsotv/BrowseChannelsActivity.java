package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import com.fsotv.dto.Channel;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.utils.YouTubeHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class BrowseChannelsActivity extends Activity {
	
	private ProgressDialog pDialog;
	private ListView lvChannel;
	private List<ChannelEntry> channels;
	
	private String userType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_channels);
		
		lvChannel = (ListView)findViewById(R.id.lvChannel);
		
		pDialog = new ProgressDialog(BrowseChannelsActivity.this);
		pDialog.setMessage("Loading data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		
		channels = new ArrayList<ChannelEntry>();
		userType = YouTubeHelper.USER_TYPE_COMEDIANS;
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null){
			userType = extras.getString("userType");
		}
		
		setTitle("Browse Channel - " + userType);
		
		new loadChannels().execute();
	}


	/**
	 * Background Async Task to get RSS data from URL
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

		/**
		 * getting all stored website from SQLite
		 * */
		@Override
		protected String doInBackground(String... args) {
			channels = YouTubeHelper.getChannels(userType);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating list view with websites
					 * */
					ListItemAdapter adapter = new ListItemAdapter(
							BrowseChannelsActivity.this, R.layout.browse_channel_item,
							channels);
					// updating listview
					lvChannel.setAdapter(adapter);
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
			holder.title.setText(title);
			holder.description.setText(description);
			
			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
		}
	}
}
