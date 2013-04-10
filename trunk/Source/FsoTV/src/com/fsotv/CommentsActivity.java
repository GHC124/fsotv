package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dto.CommentEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListener;
import com.fsotv.utils.YouTubeHelper;

public class CommentsActivity extends ActivityBase {

	private ListView lvComment;

	private List<CommentEntry> comments;
	private ListCommentAdapter adapter;

	private boolean isLoading = false;
	private String videoId = "";
	private int maxResult = 5;
	private int startIndex = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);

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

		lvComment.setOnScrollListener(new EndlessScrollListener(lvComment) {
			@Override
			public void loadData() {
				if (!isLoading) {
					isLoading = true;
					startIndex = startIndex + maxResult;
					new LoadComments().execute();
				}
			}
		});
		adapter = new ListCommentAdapter(CommentsActivity.this,
				R.layout.comment_item, comments);
		// updating listview
		registerForContextMenu(lvComment);
		lvComment.setAdapter(adapter);

		new LoadComments().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_comments, menu);
		return true;
	}

	/**
	 * Background Async Task to get Comments from URL
	 * */
	class LoadComments extends AsyncTask<String, String, String> {

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
						maxResult, startIndex);
				comments.addAll(items);
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
		protected void onPostExecute(String args) {
			hideLoading();
			if (isLoading)
				isLoading = false;
			adapter.clear();
			for (CommentEntry c : comments) {
				adapter.add(c);
			}
			adapter.notifyDataSetChanged();
			if (comments.size() == 0) {
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
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (content.length() > 150) {
				content = content.substring(0, 150) + "...";
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
