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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
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

/**
 * Show videos that are subscribe and store in database Extend ActivityBase,
 * allow: Unsubscribe, View video detail when click on video
 * 
 * @author ChungPV1
 */
public class MyVideosActivity extends ActivityBase {
	
	// Views
	private DialogBase typeDialog;
	private ExpandableListView expVideo;
	private TextView tvVideos;

	// Properties
	private List<ListGroup> groups;
	private ImageLoader imageLoader;
	private VideoDao videoDao;
	private ReferenceDao referenceDao;

	private void initComponents() {
		expVideo = (ExpandableListView) findViewById(R.id.expVideo);
		tvVideos = (TextView) findViewById(R.id.tvVideos);

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
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_videos);

		initComponents();

		/*
		 * Add click action to control
		 */
		tvVideos.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onOptionClick(v);
			}
		});

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
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra("videoId", videoId);
				i.putExtra("videoTitle", videoTitle);
				startActivity(i);

				return true;
			}
		});
		expVideo.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					final int groupPos = ExpandableListView
							.getPackedPositionGroup(id);
					final int childPos = ExpandableListView
							.getPackedPositionChild(id);
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							MyVideosActivity.this);
					alertDialogBuilder.setTitle("Unsubscribe Video");
					alertDialogBuilder
							.setMessage(
									"Do you want to unsubscribe this video?")
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											VideoEntry entry = groups
													.get(groupPos).childs
													.get(childPos);
											videoDao.deleteVideo(Integer
													.parseInt(entry.getId()));
											dialog.dismiss();
											new LoadVideos().execute();
										}
									})
							.setNegativeButton("Cancel",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.dismiss();
										}
									});
					AlertDialog alertDialog = alertDialogBuilder.create();
					alertDialog.show();
					return true;
				}

				return false;
			}
		});
		
		// registerForContextMenu(expVideo);
		new LoadVideos().execute();
	}

	/**
	 * Hander click event when user select search, sort, category, time
	 * 
	 * @param v
	 */
	public void onOptionClick(View v) {
		if (v.getId() == R.id.tvVideos) {
			if (typeDialog != null)
				typeDialog.show();
			else {
				createTypeDialog(MyVideosActivity.this);
				if (typeDialog != null)
					typeDialog.show();
			}
		}
	}

	/**
	 * Create dialog that allow user to change to channels
	 * 
	 * @param context
	 */
	private void createTypeDialog(Context context) {
		typeDialog = new DialogBase(context);
		typeDialog.setContentView(R.layout.type);
		typeDialog.setHeader("Videos");
		final TextView txtVideos = (TextView) typeDialog
				.findViewById(R.id.tvVideos);
		final TextView txtChannels = (TextView) typeDialog
				.findViewById(R.id.tvChannels);
		txtVideos.setVisibility(View.GONE);
		txtChannels.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				typeDialog.dismiss();
				Intent i = new Intent(getApplicationContext(),
						MyChannelsActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
	}

	/**
	 * Background Async Task to get Videos from URL
	 * */
	class LoadVideos extends AsyncTask<String, String, String> {
		
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
			groups.clear();
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
				item.setPublished(video.getPublished());
				item.setUpdated(video.getUpdated());
				item.setAuthor(video.getAuthor());
				
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
					MyVideosActivity.this, R.layout.my_videos_group,
					R.layout.my_videos_item, groups);
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

				holder.duration = (TextView) view.findViewById(R.id.duration);
				holder.published = (TextView) view.findViewById(R.id.published);
				holder.author = (TextView) view.findViewById(R.id.author);
				
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
			String author = item.getAuthor();
			if (author.length() > 50) {
				author = author.substring(0, 50) + "...";
			}
			imageLoader.DisplayImage(item.getImage(), holder.image,
					holder.progressBar);

			holder.title.setText(title);
			holder.description.setText(description);
			holder.viewCount.setText(DataHelper.numberWithCommas(item.getViewCount()));
			holder.duration.setText(DataHelper.secondsToTimer(item.getDuration()));
			holder.published.setText(DataHelper.formatDate(item.getPublished()));
			holder.author.setText(author);
			
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

		@SuppressWarnings("static-access")
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
			TextView duration;
			TextView published;
			TextView author;
		}

		class ListGroupHolder {
			ImageView image;
			TextView lblTitle;
			TextView lblCount;
		}
	}
}
