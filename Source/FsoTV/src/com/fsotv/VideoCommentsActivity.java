package com.fsotv;

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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dto.CommentEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.EndlessScrollListViewListener;
import com.fsotv.utils.YouTubeHelper;

/**
 * Browse comments from youtube by video
 * Extend ActivityBase, allow:
 * + Load more items when scroll
 * 
 *
 */
public class VideoCommentsActivity extends Activity {

	// Views
	private ListView lvComment;
	private ProgressBar pbLoading;
	private List<CommentEntry> comments;
	private ListCommentAdapter adapter;
	private YouTubeHelper youTubeHelper;
	
	private boolean isLoading = false;
	private String videoId = "";
	private int maxResult = 10;
	private int maxLoad = 5;
	private int startIndex = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_comments);

		lvComment = (ListView) findViewById(R.id.lvComment);
		pbLoading = (ProgressBar)findViewById(R.id.pbLoading);
		
		youTubeHelper = new YouTubeHelper();
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
		adapter = new ListCommentAdapter(VideoCommentsActivity.this,
				R.layout.comment_item, comments);
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
			pbLoading.setVisibility(View.VISIBLE);
		}

		@Override
		protected List<CommentEntry> doInBackground(String... args) {

			if (isLoading) {
				List<CommentEntry> items = youTubeHelper.getComments(videoId,
						maxLoad, startIndex);
				return items;
			} else {
				startIndex = 1;
				comments = youTubeHelper.getComments(videoId, maxResult,
						startIndex);
			}
			return null;
		}

		/**
		 * After completing background
		 * **/
		protected void onPostExecute(List<CommentEntry> result) {
			pbLoading.setVisibility(View.INVISIBLE);
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
	/**
	 * Adapter to populate Comments to ListView
	 *
	 */
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
				holder.author = (TextView) row.findViewById(R.id.author);
				
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
			String author = item.getAuthor();
			if (author.length() > 50) {
				author = author.substring(0, 50) + "...";
			}
			holder.title.setText(title);
			holder.content.setText(content);
			holder.published.setText(DataHelper.formatDate(item.getPublished()));
			holder.author.setText(author);
			
			return row;
		}

		class ListItemHolder {
			TextView title;
			TextView content;
			TextView published;
			TextView author;
		}
	}

}
