/**
 * 
 */
package com.fsotv;

import java.util.List;

import com.fsotv.BrowseChannelsActivity.ListChannelAdapter.ListItemHolder;
import com.fsotv.BrowseVideosActivity.ListVideoAdapter;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.YouTubeHelper;
import com.google.android.youtube.player.internal.ad;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;

/**
 * @author CuongVM1
 * 
 */
public class MainActivity10Inch extends Activity {
	TabHost tabHost;
	private ListVideoAdapter adapter;
	private List<VideoEntry> videos;
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_10inch_main);

		createTabs();
	}

	private void createTabs() {
		tabHost = (TabHost) findViewById(R.id.tabHost);
		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec("Tab 1");
		spec1.setIndicator("Tab 01");
		spec1.setContent(R.id.tab1);

		TabSpec spec2 = tabHost.newTabSpec("Tab 2");
		spec2.setIndicator("Tab 02");
		spec2.setContent(R.id.tab2);

		TabSpec spec3 = tabHost.newTabSpec("Tab 3");
		spec3.setIndicator("Tab 02");
		spec3.setContent(R.id.tab3);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);

	}

	class showVideoList extends AsyncTask<String, String, String> {

		@Override
		protected String doInBackground(String... params) {
			List<VideoEntry> items = null;
			items = YouTubeHelper.getVideosInChannel(
					YouTubeHelper.CHANNEL_MOST_SUBSCRIBED,
					YouTubeHelper.ORDERING_VIEWCOUNT, 6, 1, "Music");
			videos.addAll(items);
			return null;
		}

		@Override
		protected void onPostExecute(String args) {
			adapter.clear();
			for (VideoEntry entry : videos) {
				adapter.add(entry);
			}
			adapter.notifyDataSetChanged();

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

		public View getView(int pos, View view, ViewGroup parent) {
			View row = view;
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
			}
			VideoEntry item = data.get(pos);

			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);

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
