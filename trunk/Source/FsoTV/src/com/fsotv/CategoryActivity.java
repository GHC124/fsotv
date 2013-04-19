package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;

/**
 * Show category that store in database, allow:
 * + Add new category
 * + Remove current category
 * 
 */
public class CategoryActivity extends ActivityBase {

	private ListView lvCategory;
	private List<ListItem> listItems;
	private ReferenceDao referenceDao;
	
	private void init(){
		lvCategory = (ListView) findViewById(R.id.lvCategory);
		registerForContextMenu(lvCategory);
		
		listItems = new ArrayList<ListItem>();
		referenceDao = new ReferenceDao(getApplicationContext());
		
		setHeader("Category");
		setTitle("Category");
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_category);
		
		init();
				
		new loadCategories().execute();
	}
	
	/**
	 * Save choose category to database
	 * @param v
	 */
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

		/**
		 * 
		 */
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
	/**
	 * List category item, store state of item(selected or not)
	 */
	class ListItem{
		public Reference reference;
		public boolean selected = false;	
	}
	/**
	 * Adapter that populate categories to listView
	 *
	 */
	class ListItemAdapter extends ArrayAdapter<ListItem> {
		Context context;
		int layoutResourceId;
		List<ListItem> data = null;

		/**
		 * The constuctor
		 * 
		 * @param context
		 * @param layoutResourceId
		 * @param data
		 */
		public ListItemAdapter(Context context, int layoutResourceId,
				List<ListItem> data) {
			super(context, layoutResourceId, data);
			this.layoutResourceId = layoutResourceId;
			this.context = context;
			this.data = data;
		}

		/**
		 * 
		 */
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
			holder.title.setText(title);
			holder.description.setText(description);
			holder.cbxSelect.setChecked(item.selected);
			holder.cbxSelect.setTag(item);
			// Image
			String value = item.reference.getValue();
			if (value != null) {
				if (value.equals("Comedy")) {
					holder.image.setBackgroundResource(R.drawable.comedy32);
				}else if (value.equals("Music")) {
					holder.image.setBackgroundResource(R.drawable.music32);
				}else if (value.equals("News")) {
					holder.image.setBackgroundResource(R.drawable.news32);
				}else if (value.equals("Autos")) {
					holder.image.setBackgroundResource(R.drawable.auto32);
				}else if (value.equals("Education")) {
					holder.image.setBackgroundResource(R.drawable.edu32);
				}else if (value.equals("Entertainment")) {
					holder.image.setBackgroundResource(R.drawable.enter32);
				}else if (value.equals("Film")) {
					holder.image.setBackgroundResource(R.drawable.film32);
				}else if (value.equals("Howto")) {
					holder.image.setBackgroundResource(R.drawable.howto32);
				}else if (value.equals("People")) {
					holder.image.setBackgroundResource(R.drawable.people32);
				}else if (value.equals("Animals")) {
					holder.image.setBackgroundResource(R.drawable.animal32);
				}else if (value.equals("Tech")) {
					holder.image.setBackgroundResource(R.drawable.tech32);
				}else if (value.equals("Sports")) {
					holder.image.setBackgroundResource(R.drawable.sport32);
				}else if (value.equals("Travel")) {
					holder.image.setBackgroundResource(R.drawable.travel32);
				}
				
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
