package com.fsotv;

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
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dao.VideoDao;
import com.fsotv.dto.Reference;
import com.fsotv.dto.Video;
import com.fsotv.dto.VideoEntry;
import com.fsotv.utils.DataHelper;
import com.fsotv.utils.ImageLoader;

public class MyVideosActivity extends ActivityBase {

	private final int MENU_UNSUBSCRIBE = Menu.FIRST;

	private ExpandableListView expVideo;
	private List<ListGroup> groups;
	private ImageLoader imageLoader;
	private VideoDao videoDao;
	private ReferenceDao referenceDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_videos);

		expVideo = (ExpandableListView) findViewById(R.id.expVideo);
		registerForContextMenu(expVideo);

		groups = new ArrayList<ListGroup>();
		imageLoader = new ImageLoader(getApplicationContext());
		videoDao = new VideoDao(getApplicationContext());
		referenceDao = new ReferenceDao(getApplicationContext());
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {

		}
		setHeader("Videos");
		setTitle("My Videos");

		// Launching new screen on Selecting Single ListItem
		expVideo.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
					int groupPosition, int childPosition, long id) {
				VideoEntry item = groups.get(groupPosition).childs
						.get(childPosition);
				String videoId = item.getIdReal();
				String videoTitle = item.getTitle();
				Intent i = new Intent(getApplicationContext(),
						VideoDetailActivity.class);
				i.putExtra("videoId", videoId);
				i.putExtra("videoTitle", videoTitle);
				startActivity(i);

				return true;
			}
		});
		registerForContextMenu(expVideo);
		new loadVideos().execute();
	}

	/**
	 * Building a context menu for listview Long press on List row to see
	 * context menu
	 * */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.lvVideo) {
			menu.setHeaderTitle("Option");
			menu.add(Menu.NONE, MENU_UNSUBSCRIBE, 0, "Unsubscribe");
		}
	}

	/**
	 * Responding to context menu selected option
	 * */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int menuItemId = item.getItemId();
		// check for selected option
		if (menuItemId == MENU_UNSUBSCRIBE) {
			ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
					.getMenuInfo();
			int g = 0, c = 0;
			int type = ExpandableListView
					.getPackedPositionType(info.packedPosition);
			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				g = ExpandableListView
						.getPackedPositionGroup(info.packedPosition);
				c = ExpandableListView
						.getPackedPositionChild(info.packedPosition);
			}
			final int groupPos = g;
			final int childPos = c;
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
					this);
			alertDialogBuilder.setTitle("Unsubscribe Video");
			alertDialogBuilder
					.setMessage("Do you want to unsubscribe this video?")
					.setCancelable(false)
					.setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									VideoEntry entry = groups.get(groupPos).childs.get(childPos);
									videoDao.deleteVideo(Integer.parseInt(entry
											.getId()));
									groups.get(groupPos).childs.remove(childPos);
									expVideo.invalidateViews();
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
	 * Background Async Task to get Videos from URL
	 * */
	class loadVideos extends AsyncTask<String, String, String> {

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
			List<Reference> listChannel = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, null);
			List<Video> listVideo = videoDao.getListVideo();

			for (Reference c : listChannel) {
				ListGroup group = new ListGroup();
				group.id = c.getId();
				group.title = c.getDisplay();
				groups.add(group);
			}
			for (Video video : listVideo) {
				VideoEntry item = new VideoEntry();
				item.setId(video.getIdVideo() + "");
				item.setIdReal(video.getIdRealVideo() + "");
				item.setTitle(video.getNameVideo());
				item.setImage(video.getThumnail());
				item.setLink(video.getUri());
				item.setDescription(video.getDescribes());
				item.setDuration(video.getDuration());
				item.setViewCount(video.getViewCount());
				item.setFavoriteCount(video.getFavoriteCount());
				for (ListGroup g : groups) {
					if (g.id == video.getIdCategory()) {
						g.childs.add(item);
						break;
					}
				}
			}
			// Remove unused group
			for (int i = 0; i < groups.size(); i++) {
				if (groups.get(i).childs.size() == 0) {
					groups.remove(i);
					i--;
				}
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			ExpandListAdapter adapter = new ExpandListAdapter(
					MyVideosActivity.this, R.layout.my_video_group,
					R.layout.my_video_item, groups);
			// updating listview
			expVideo.setAdapter(adapter);
			if (groups.size() == 0) {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class ListGroup {
		public int id;
		public String title;
		public List<VideoEntry> childs;

		public ListGroup() {
			childs = new ArrayList<VideoEntry>();
		}

		public ListGroup(int id, String title, List<VideoEntry> childs) {
			super();
			this.id = id;
			this.title = title;
			this.childs = childs;
		}
	}

	class ExpandListAdapter extends BaseExpandableListAdapter {
		int layoutChildId;
		int layoutGroupId;
		private Context context;
		private List<ListGroup> groups;

		public ExpandListAdapter(Context context, int layoutGroupId,
				int layoutChildId, List<ListGroup> groups) {
			this.context = context;
			this.groups = groups;
			this.layoutChildId = layoutChildId;
			this.layoutGroupId = layoutGroupId;
		}

		public void addItem(VideoEntry item, ListGroup group) {
			if (!groups.contains(group)) {
				groups.add(group);
			}
			int index = groups.indexOf(group);
			List<VideoEntry> ch = groups.get(index).childs;
			ch.add(item);
			groups.get(index).childs = ch;
		}

		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			List<VideoEntry> chList = groups.get(groupPosition).childs;
			return chList.get(childPosition);
		}

		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return childPosition;
		}

		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View view, ViewGroup parent) {
			ListItemHolder holder = null;
			if (view == null) {
				LayoutInflater infalInflater = ((Activity) context)
						.getLayoutInflater();
				view = infalInflater.inflate(layoutChildId, null);

				holder = new ListItemHolder();
				holder.image = (ImageView) view.findViewById(R.id.image);
				holder.progressBar = (ProgressBar) view
						.findViewById(R.id.progressBar);
				holder.title = (TextView) view.findViewById(R.id.title);
				holder.description = (TextView) view
						.findViewById(R.id.description);
				holder.viewCount = (TextView) view.findViewById(R.id.viewCount);
				holder.favoriteCount = (TextView) view
						.findViewById(R.id.favoriteCount);
				holder.duration = (TextView) view.findViewById(R.id.duration);

				view.setTag(holder);
			} else {
				holder = (ListItemHolder) view.getTag();
			}

			VideoEntry item = (VideoEntry) getChild(groupPosition,
					childPosition);
			// format string
			String title = item.getTitle();
			String description = item.getDescription();
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (description.length() > 150) {
				description = description.substring(0, 150) + "...";
			}

			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);

			holder.title.setText(title);
			holder.description.setText(description);
			holder.viewCount.setText(DataHelper.numberWithCommas(item
					.getViewCount()));
			holder.favoriteCount.setText(DataHelper.numberWithCommas(item
					.getFavoriteCount()));
			holder.duration.setText(DataHelper.secondsToTimer(item
					.getDuration()));

			return view;
		}

		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			List<VideoEntry> chList = groups.get(groupPosition).childs;

			return chList.size();

		}

		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			return groups.get(groupPosition);
		}

		public int getGroupCount() {
			// TODO Auto-generated method stub
			return groups.size();
		}

		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}

		public View getGroupView(int groupPosition, boolean isLastChild,
				View view, ViewGroup parent) {
			ListGroupHolder holder = null;

			if (view == null) {
				// LayoutInflater infalInflater =
				// ((Activity)context).getLayoutInflater();
				LayoutInflater infalInflater = (LayoutInflater) context
						.getSystemService(context.LAYOUT_INFLATER_SERVICE);

				view = infalInflater.inflate(layoutGroupId, null);

				holder = new ListGroupHolder();
				holder.image = (ImageView) view.findViewById(R.id.image);
				holder.lblTitle = (TextView) view.findViewById(R.id.title);
				holder.lblCount = (TextView) view.findViewById(R.id.count);

				view.setTag(holder);
			} else {
				holder = (ListGroupHolder) view.getTag();
			}
			ListGroup group = (ListGroup) getGroup(groupPosition);
			holder.lblTitle.setText(group.title);
			holder.lblCount.setText(" - " + group.childs.size() + " video(s)");
			holder.image.setImageResource(R.drawable.icon_cate);
			return view;
		}

		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return true;
		}

		public boolean isChildSelectable(int arg0, int arg1) {
			// TODO Auto-generated method stub
			return true;
		}

		class ListItemHolder {
			ImageView image;
			ProgressBar progressBar;
			TextView title;
			TextView description;
			TextView viewCount;
			TextView favoriteCount;
			TextView duration;
		}

		class ListGroupHolder {
			ImageView image;
			TextView lblTitle;
			TextView lblCount;
		}
	}
}
