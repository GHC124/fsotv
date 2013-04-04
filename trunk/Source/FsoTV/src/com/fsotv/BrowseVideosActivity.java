package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import com.fsotv.BrowseVideosActivity.ListItemAdapter;
import com.fsotv.BrowseVideosActivity.ListItemAdapter.ListItemHolder;
import com.fsotv.BrowseVideosActivity.loadVideos;
import com.fsotv.dto.VideoEntry;
import com.fsotv.dto.VideoEntry;
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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseVideosActivity extends Activity {
	
	private ProgressDialog pDialog;
	private ListView lvVideo;
	private List<VideoEntry> videos;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_browse_videos);
		
		lvVideo = (ListView)findViewById(R.id.lvVideo);
		
		pDialog = new ProgressDialog(BrowseVideosActivity.this);
		pDialog.setMessage("Loading data ...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		
		videos = new ArrayList<VideoEntry>();
		String channelId = "";
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if(extras!=null){
			channelId = extras.getString("channelId");
		}
		
		setTitle("Browse Video");
				
		new loadVideos().execute(channelId);
	}


	/**
	 * Background Async Task to get Videos data from URL
	 * */
	class loadVideos extends AsyncTask<String, String, String> {

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
			String channelId = args[0];
			videos = YouTubeHelper.getVideosInChannel(channelId);
			// updating UI from Background Thread
			runOnUiThread(new Runnable() {
				public void run() {
					ListItemAdapter adapter = new ListItemAdapter(
							BrowseVideosActivity.this, R.layout.browse_video_item,
							videos);
					// updating listview
					lvVideo.setAdapter(adapter);
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
	
	class ListItemAdapter extends ArrayAdapter<VideoEntry> {
		Context context;
		int layoutResourceId;
		List<VideoEntry> data = null;

		public ListItemAdapter(Context context, int layoutResourceId,
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
				holder.progressBar = (ProgressBar) row.findViewById(R.id.progressBar);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row.findViewById(R.id.description);
				holder.viewCount = (TextView) row.findViewById(R.id.viewCount);
				holder.favoriteCount = (TextView) row.findViewById(R.id.favoriteCount);
				
				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			VideoEntry item = data.get(position);
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
			holder.viewCount.setText(item.getViewCount() + "");
			holder.favoriteCount.setText(item.getFavoriteCount() + "");
			
			return row;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView viewCount;
			TextView favoriteCount;
		}
	}
}
