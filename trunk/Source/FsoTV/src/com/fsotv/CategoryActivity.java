package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.utils.ImageLoader;
import com.fsotv.utils.YouTubeHelper;

/**
 * Show category that store in database
 * Extend ActivityBase
 *
 */
public class CategoryActivity extends ActivityBase {

	private ListView lvCategory;
	private List<ListItem> listItems;
	private ReferenceDao referenceDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_category);

		lvCategory = (ListView) findViewById(R.id.lvCategory);
		registerForContextMenu(lvCategory);
		
		listItems = new ArrayList<ListItem>();
		referenceDao = new ReferenceDao(getApplicationContext());
		
		setHeader("Category");
		setTitle("Category");
		
		new loadCategories().execute();
	}
	
	
	public void onSaveClick(View v) {
		for(ListItem item:listItems){
			if(!item.reference.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT)
					&& item.selected){
				// Update
				item.reference.setExtras(ReferenceDao.EXTRAS_CATEGORY_SELECT);
				referenceDao.updateReference(item.reference);
			}
			else if(item.reference.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT)
					&& !item.selected){
				// Update
				item.reference.setExtras("");
				referenceDao.updateReference(item.reference);
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
		   List<Reference> categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, null);
		   for(Reference r:categories){
			   ListItem item = new ListItem();
			   item.reference = r;
			   item.selected = (r.getExtras().equals(ReferenceDao.EXTRAS_CATEGORY_SELECT))?true:false;
			   listItems.add(item);
		   }

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			ListItemAdapter adapter = new ListItemAdapter(
					CategoryActivity.this, R.layout.category_item,
					listItems);
			// updating listview
			lvCategory.setAdapter(adapter);
			if (listItems.size() == 0) {
				Toast.makeText(getApplicationContext(), "No results",
						Toast.LENGTH_LONG).show();
			}
		}

	}
	
	class ListItem{
		public Reference reference;
		public boolean selected = false;	
	}

	class ListItemAdapter extends ArrayAdapter<ListItem> {
		Context context;
		int layoutResourceId;
		List<ListItem> data = null;

		public ListItemAdapter(Context context, int layoutResourceId,
				List<ListItem> data) {
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
				holder.title = (TextView) row.findViewById(R.id.title);
				holder.description = (TextView) row
						.findViewById(R.id.description);
				holder.cbxSelect = (CheckBox) row.findViewById(R.id.cbxSelect);
				holder.cbxSelect.setTag(position);
				holder.cbxSelect.setOnClickListener(new OnClickListener() {
					@Override
	                public void onClick(View v) {
						 CheckBox cb = (CheckBox) v ;
						 ListItem item = (ListItem)v.getTag();
						 item.selected = cb.isChecked();
	                }
				});
				row.setTag(holder);
			} else {
				holder = (ListItemHolder) row.getTag();
			}
		
			ListItem item = data.get(position);
			// format string
			String title = item.reference.getDisplay();
			String description = item.reference.getDisplay();
			if (title.length() > 50) {
				title = title.substring(0, 50) + "...";
			}
			if (description.length() > 150) {
				description = description.substring(0, 150) + "...";
			}
			holder.image.setImageResource(R.drawable.icon_cate25);
			holder.title.setText(title);
			holder.description.setText(description);
			holder.cbxSelect.setChecked(item.selected);
			holder.cbxSelect.setTag(item);
			
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
