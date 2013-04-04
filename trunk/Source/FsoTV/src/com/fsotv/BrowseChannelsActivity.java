package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import com.fsotv.dto.ChannelEntry;
import com.fsotv.utils.DownloadImage;
import com.fsotv.utils.YouTubeHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

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
		
		// Launching new screen on Selecting Single ListItem
		lvChannel.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String channelId = channels.get(position).getId();
				Intent i = new Intent(getApplicationContext(), BrowseVideosActivity.class);
				i.putExtra("channelId", channelId);
				startActivity(i);
			}
		});
		
		new loadChannels().execute();
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
			channels = YouTubeHelper.getChannels(userType);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					
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
			if(item.getImage() == null || item.getImage().isEmpty()){
				Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.question50);
				holder.image.setImageBitmap(b);
			}else{
				new DownloadImage(holder.image, holder.progressBar).execute(item.getImage());
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
