package com.fsotv.tablet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.ActivityBase;
import com.fsotv.CommentsActivity;
import com.fsotv.R;
import com.fsotv.dto.CommentEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.YouTubeHelper;

public class CommentsTabletActivity  extends ActivityBase {

	private ListView lvComment;

	private List<CommentEntry> comments;
	private ListCommentAdapter adapter;

	private boolean isLoading = false;
	private String videoId = "";
	private int maxResult = 10;
	private int maxLoad = 5;
	private int startIndex = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments_tablet);

		lvComment = (ListView) findViewById(R.id.lvComment);

		comments = new ArrayList<CommentEntry>();
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String videoTitle = "";
		if (extras != null) {
			videoId = extras.getString("videoId");
			videoTitle = extras.getString("videoTitle");
			videoId = (videoId == null) ? "" : videoId;
			videoTitle = (videoTitle == null) ? "" : videoTitle;
		}
		setHeader(videoTitle);
		setTitle("Comments");

		lvComment.setOnScrollListener(new EndlessScrollListViewListener(lvComment) {
			@Override
			public void loadData() {
				if (!isLoading) {
					isLoading = true;
					startIndex = startIndex + maxResult;
					new LoadComments().execute();
				}
			}
		});
		adapter = new ListCommentAdapter(CommentsTabletActivity.this,
				R.layout.comment_tablet_item, comments);
		// updating listview
		registerForContextMenu(lvComment);
		lvComment.setAdapter(adapter);

		new LoadComments().execute();
	}

	/**
	 * Background Async Task to get Comments from URL
	 * */
	class LoadComments extends AsyncTask<String, String, List<CommentEntry>> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoading();
		}

		@Override
		protected List<CommentEntry> doInBackground(String... args) {
			// Demo data
//			try {
//				InputStream is = getResources().getAssets()
//						.open("Comments.txt");
//				if (isLoading) {
//					List<CommentEntry> items = YouTubeHelper
//							.getCommentsByStream(is);
//					comments.addAll(items);
//				} else {
//					comments = YouTubeHelper.getCommentsByStream(is);
//				}
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}

			if (isLoading) {
				List<CommentEntry> items = YouTubeHelper.getComments(videoId,
						maxLoad, startIndex);
				return items;
			} else {
				startIndex = 1;
				comments = YouTubeHelper.getComments(videoId, maxResult,
						startIndex);
			}
			return null;
		}

		/**
		 * After completing background
		 * **/
		protected void onPostExecute(List<CommentEntry> result) {
			hideLoading();
			if (isLoading)
				isLoading = false;
			if (result != null) {
				comments.addAll(result);
				if (result.size() == 0) {
					// decrease start index so we will load more items at previous position
					startIndex = startIndex - maxResult;
					Toast.makeText(getApplicationContext(), "No more results",
							Toast.LENGTH_LONG).show();
				}
			}
			if (comments.size() > 0) {
				adapter.clear();
				for (CommentEntry c : comments) {
					adapter.add(c);
				}
				adapter.notifyDataSetChanged();
				if(result == null){
					// Scroll to top if refresh list from beginning
					lvComment.setSelectionAfterHeaderView();
				}
			} else {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class ListCommentAdapter extends ArrayAdapter<CommentEntry> {
		Context context;
		int layoutResourceId;
		List<CommentEntry> data = null;

		public ListCommentAdapter(Context context, int layoutResourceId,
				List<CommentEntry> data) {
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
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.content = (TextView) row.findViewById(R.id.content);
				holder.published = (TextView) row.findViewById(R.id.published);

				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			CommentEntry item = data.get(position);
			// format string
			String title = item.getTitle();
			String content = item.getContent();
			if (title.length() > 100) {
				title = title.substring(0, 100) + "...";
			}
			if (content.length() > 350) {
				content = content.substring(0, 350) + "...";
			}

			holder.title.setText(title);
			holder.content.setText(content);
			holder.published
					.setText(DataHelper.formatDate(item.getPublished()));

			return row;
		}

		class ListItemHolder {
			TextView title;
			TextView content;
			TextView published;
		}
	}

}
