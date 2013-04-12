package com.fsotv.tablet;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsotv.ActivityBase;
import com.fsotv.BrowseVideosActivity;
import com.fsotv.CategoryActivity;
import com.fsotv.R;
import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;

public class HomeTabletActivity  extends ActivityBase{
	private final int RESULT_CATEGORY = 1;
	private final int CATEGORY_ADD = -1;

	private HorizontalScrollView vf;
	private LinearLayout ll;
	private List<Reference> categories;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_tablet);

		// Get the Reference viewFlipper and set animation
		vf = (HorizontalScrollView) findViewById(R.id.scrollView_channel);
		// Add layout to scrollview
		ll = new LinearLayout(HomeTabletActivity.this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		ll.setLayoutParams(lp);
	
		hideBack();
		setHeader("Home");
		
		categories = new ArrayList<Reference>();
		new loadCategories().execute();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// Check which request we're responding to
		if (requestCode == RESULT_CATEGORY) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK) {
				new loadCategories().execute();
			}
		}
	}

	private void addItemToScrollView(Reference r) {
		LinearLayout ll1 = new LinearLayout(HomeTabletActivity.this);
		if (Build.VERSION.SDK_INT >= 16) {
			ll1.setBackground(getResources().getDrawable(
					R.drawable.channel_list_background));
		}else{
		}
		ll1.setOrientation(LinearLayout.VERTICAL);
		ll1.setGravity(Gravity.CENTER);
		ll1.setId(r.getId());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		ll1.setLayoutParams(lp);

		LinearLayout ll2 = new LinearLayout(HomeTabletActivity.this);
		ll2.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		ll2.setLayoutParams(lp1);

		TextView tv = new TextView(HomeTabletActivity.this);
		tv.setTextColor(Color.parseColor("#000000"));
		tv.setText(r.getDisplay());
		tv.setTextSize(50);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp2.leftMargin = 18;
		lp2.rightMargin = 10;
		tv.setLayoutParams(lp2);

		ll2.addView(tv);
		ll1.addView(ll2);
		// Add slide event
		ll1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int cateId = v.getId();
				if (cateId == CATEGORY_ADD) {
					Intent i = new Intent(getApplicationContext(),
							CategoryActivity.class);
					startActivityForResult(i, RESULT_CATEGORY);
				} else {
					Intent i = new Intent(getApplicationContext(),
							VideosTabletActivity.class);
					for (Reference r : categories) {
						if (r.getId() == cateId) {
							i.putExtra("categoryId", r.getValue());
							break;
						}
					}
					startActivity(i);
				}
			}
		});
		
		ll.addView(ll1);
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
			ReferenceDao referenceDao = new ReferenceDao(
					getApplicationContext());
			categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY,
					ReferenceDao.EXTRAS_CATEGORY_SELECT);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			hideLoading();
			ll.removeAllViews();
			vf.removeAllViews();
			for (Reference r : categories) {
				addItemToScrollView(r);
			}
			Reference add = new Reference();
			add.setId(CATEGORY_ADD);
			add.setDisplay("+");
			categories.add(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
					"add_new", "+", ""));
			
			addItemToScrollView(add);
			
			vf.addView(ll);
		}

	}
}
