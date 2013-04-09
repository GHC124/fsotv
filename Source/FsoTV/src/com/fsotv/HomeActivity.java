package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.utils.OnSwipeTouchListener;

public class HomeActivity extends Activity implements OnClickListener {
	private final int RESULT_CATEGORY = 1;
	private final int CATEGORY_ADD = -1;

	private TextView textView1;
	private ViewFlipper vf;
	private ImageView imgLeft, imgRight;
	private List<Reference> categories;
	private int currentLayout = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Get the Reference viewFlipper and set animation
		vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);
		Animation s_in = AnimationUtils.loadAnimation(this, R.animator.slidein);
		Animation s_out = AnimationUtils.loadAnimation(this,
				R.animator.slideout);
		vf.setInAnimation(s_in); // when a view is displayed
		vf.setOutAnimation(s_out); // when a view disappears
		
		textView1 = (TextView)findViewById(R.id.textView1);
		textView1.setOnTouchListener(new OnSwipeTouchListener() {
			@Override
	        public boolean onSwipeTop() {
	            Toast.makeText(HomeActivity.this, "top", Toast.LENGTH_SHORT).show();
	            return true;
	        }
			@Override
	        public boolean onSwipeRight() {
	            Toast.makeText(HomeActivity.this, "right", Toast.LENGTH_SHORT).show();
	            return true;
	        }
			@Override
	        public boolean onSwipeLeft() {
	            Toast.makeText(HomeActivity.this, "left", Toast.LENGTH_SHORT).show();
	            return true;
	        }
			@Override
	        public boolean onSwipeBottom() {
	            Toast.makeText(HomeActivity.this, "bottom", Toast.LENGTH_SHORT).show();
	            return true;
	        }
	    });
		
		imgLeft = (ImageView) findViewById(R.id.imgLeft);
		imgRight = (ImageView) findViewById(R.id.imgRight);
		// Set onClick listener for all button
		imgLeft.setOnClickListener(this);
		imgRight.setOnClickListener(this);

		categories = new ArrayList<Reference>();
		new loadCategories().execute();
	}

	public void onClick(View v) {
		if (v == imgLeft) {
			currentLayout--;
			if (currentLayout < 0){
				currentLayout = 0;
			}
			else{ 
				vf.setDisplayedChild(currentLayout);
				if(currentLayout == 0){
					imgLeft.setVisibility(View.INVISIBLE);
				}
				if(imgRight.getVisibility() == View.INVISIBLE){
					imgRight.setVisibility(View.VISIBLE);
				}
			}
		} else if (v == imgRight) {
			currentLayout++;
			if (currentLayout > categories.size() - 1){
				currentLayout = categories.size() - 1;
			}
			else{
				vf.setDisplayedChild(currentLayout);
				if(currentLayout == categories.size() - 1){
					imgRight.setVisibility(View.INVISIBLE);
				}
				if(imgLeft.getVisibility() == View.INVISIBLE){
					imgLeft.setVisibility(View.VISIBLE);
				}
			}
		} else if (v instanceof LinearLayout) {
			int cateId = v.getId();
			if (cateId == CATEGORY_ADD) {
				Intent i = new Intent(getApplicationContext(),
						CategoryActivity.class);
				startActivityForResult(i, RESULT_CATEGORY);
			} else {
				Intent i = new Intent(getApplicationContext(),
						BrowseVideosActivity.class);
				for (Reference r : categories) {
					if (r.getId() == cateId) {
						i.putExtra("categoryId", r.getValue());
						break;
					}
				}
				startActivity(i);
			}
		}
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
			// showLoading();
		}

		@Override
		protected String doInBackground(String... args) {
			ReferenceDao referenceDao = new ReferenceDao(
					getApplicationContext());
			categories = referenceDao.getListReference(
					ReferenceDao.KEY_YOUTUBE_CATEGORY, ReferenceDao.EXTRAS_CATEGORY_SELECT);
			runOnUiThread(new Runnable() {
				public void run() {
					vf.removeAllViews();
					currentLayout = 0;
					imgLeft.setVisibility(View.INVISIBLE);
					for (Reference r : categories) {
						LinearLayout ll = new LinearLayout(HomeActivity.this);
						ll.setBackground(getResources().getDrawable(
								R.drawable.channel_list_background));
						ll.setOrientation(LinearLayout.VERTICAL);
						ll.setOnClickListener(HomeActivity.this);
						ll.setGravity(Gravity.CENTER);
						ll.setId(r.getId());
						LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.MATCH_PARENT,
								LinearLayout.LayoutParams.MATCH_PARENT);
						ll.setLayoutParams(lp);
						
						LinearLayout ll1 = new LinearLayout(HomeActivity.this);
						ll1.setOrientation(LinearLayout.VERTICAL);
						LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						ll1.setLayoutParams(lp1);
						
						TextView tv = new TextView(HomeActivity.this);
						tv.setTextColor(Color.parseColor("#000000"));
						tv.setText(r.getDisplay());
						tv.setTextSize(50);
						LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
								LinearLayout.LayoutParams.WRAP_CONTENT,
								LinearLayout.LayoutParams.WRAP_CONTENT);
						lp2.leftMargin = 18;
						lp2.rightMargin = 10;
						tv.setLayoutParams(lp2);
						
						ll1.addView(tv);
						ll.addView(ll1);
						vf.addView(ll);
					}
					if(categories.size()>0){
						imgRight.setVisibility(View.VISIBLE);
					}
					LinearLayout ll = new LinearLayout(HomeActivity.this);
					ll.setBackground(getResources().getDrawable(
							R.drawable.channel_list_background));
					ll.setOrientation(LinearLayout.VERTICAL);
					ll.setOnClickListener(HomeActivity.this);
					ll.setGravity(Gravity.CENTER);
					ll.setId(CATEGORY_ADD);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.MATCH_PARENT,
							LinearLayout.LayoutParams.MATCH_PARENT);
					ll.setLayoutParams(lp);
					
					LinearLayout ll1 = new LinearLayout(HomeActivity.this);
					ll1.setOrientation(LinearLayout.VERTICAL);
					LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					ll1.setLayoutParams(lp1);
					
					TextView tv = new TextView(HomeActivity.this);
					tv.setTextColor(Color.parseColor("#000000"));
					tv.setText("+");
					tv.setTextSize(50);
					LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
							LinearLayout.LayoutParams.WRAP_CONTENT,
							LinearLayout.LayoutParams.WRAP_CONTENT);
					tv.setLayoutParams(lp2);
					
					ll1.addView(tv);
					ll.addView(ll1);
					vf.addView(ll);
					vf.setDisplayedChild(0);
					
					categories.add(new Reference(-1,
							ReferenceDao.KEY_YOUTUBE_CATEGORY, "add_new", "+",
							""));
				}
			});
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			// hideLoading();
		}

	}
}
