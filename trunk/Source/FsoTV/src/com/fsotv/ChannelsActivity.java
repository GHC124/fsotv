package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.fsotv.dto.Channel;
import com.fsotv.utils.YouTubeHelper;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ChannelsActivity extends Activity {

	// Progress Dialog
	private ProgressDialog pDialog;
	private ListView lvChannel;
	private List<Channel> channels;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_channels);
		
		lvChannel = (ListView)findViewById(R.id.lvChannel);
		
		pDialog = new ProgressDialog(ChannelsActivity.this);
		pDialog.setMessage("Loading data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		
		channels = new ArrayList<Channel>();
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
			InputStream is;
			try {
				is = getAssets().open("Channels.txt");
				channels = YouTubeHelper.getChannels(is);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					/**
					 * Updating list view with websites
					 * */
					ListItemAdapter adapter = new ListItemAdapter(
							ChannelsActivity.this, R.layout.channel_item,
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
	
	class ListItemAdapter extends ArrayAdapter<Channel> {
		Context context;
		int layoutResourceId;
		List<Channel> data = null;

		public ListItemAdapter(Context context, int layoutResourceId,
				List<Channel> data) {
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

			Channel item = data.get(position);
			// Default image
			holder.title.setText(item.getNameChannel());
			holder.description.setText(item.getDescribes());
			
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
