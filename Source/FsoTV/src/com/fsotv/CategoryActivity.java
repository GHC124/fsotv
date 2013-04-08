package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.utils.ImageLoader;

public class CategoryActivity extends ActivityBase {

	private ListView lvCategory;

	private List<Reference> categories;
	private List<Reference> select;
	private ReferenceDao referenceDao;
	
	private ImageLoader imageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);

		lvCategory = (ListView) findViewById(R.id.lvCategory);

		categories = new ArrayList<Reference>();
		select = new ArrayList<Reference>();
		imageLoader = new ImageLoader(getApplicationContext());
		referenceDao = new ReferenceDao(getApplicationContext());
		
		setHeader("Category");
		setTitle("Category");

		new loadCategories().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.category, menu);
		return true;
	}

	public void onSaveClick(View v) {
		for(Reference r:categories){
			if(!r.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT)
					&& select.contains(r)){
				// Update
				r.setExtras(ReferenceDao.EXTRAS_CATEGORY_SELECT);
				referenceDao.updateReference(r);
			}
			else if(r.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT)
					&& !select.contains(r)){
				// Update
				r.setExtras("");
				referenceDao.updateReference(r);
			}
		}
		setResult(RESULT_OK);
		finish();
	}

	/**
	 * Background Async Task to get References from database
	 * */
	class loadCategories extends AsyncTask<String, String, String> {

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
			categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, null);
			
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			ListItemAdapter adapter = new ListItemAdapter(
					CategoryActivity.this, R.layout.category_item,
					categories);
			// updating listview
			registerForContextMenu(lvCategory);
			lvCategory.setAdapter(adapter);
			if (categories.size() == 0) {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}

	class ListItemAdapter extends ArrayAdapter<Reference> {
		Context context;
		int layoutResourceId;
		List<Reference> data = null;

		public ListItemAdapter(Context context, int layoutResourceId,
				List<Reference> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;
			ListItemHolder holder = null;
			final Reference item = data.get(position);
			
			if (row == null) {
				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(layoutResourceId, parent, false);

				holder = new ListItemHolder();
				holder.image = (ImageView) row.findViewById(R.id.image);
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row
						.findViewById(R.id.description);
				holder.cbxSelect = (CheckBox) row.findViewById(R.id.cbxSelect);
				holder.cbxSelect.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						CheckBox c = (CheckBox) v;
						if (c.isChecked()) {
							if(!select.contains(item))
								select.add(item);
						} else {
							select.remove(item);
						}
					}
				});
				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}

			// format string
			String title = item.getDisplay();
			String description = item.getDisplay();
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (description.length() > 150) {
				description = description.substring(0, 150) + "...";
			}
			holder.image.setImageResource(R.drawable.icon_cate25);
			holder.title.setText(title);
			holder.description.setText(description);
			if (item.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT)) {
				holder.cbxSelect.setChecked(true);
				// Add item to select list
				if(!select.contains(item))
					select.add(item);
			} else {
				holder.cbxSelect.setChecked(false);
				// Remove item from select list
				if(select.contains(item))
					select.remove(item);
			}

			return row;
		}

		class ListItemHolder {
			ImageView image;
			TextView title;
			TextView description;
			CheckBox cbxSelect;
		}
	}
}
