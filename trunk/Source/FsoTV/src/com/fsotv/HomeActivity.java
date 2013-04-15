package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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

/**
 * Allow: + See video category + Add, remove category + Slide left, right to see
 * category
 * 
 * 
 */
@SuppressLint("NewApi")
public class HomeActivity extends Activity implements OnClickListener {
	private final int RESULT_CATEGORY = 1;
	private final int CATEGORY_ADD = -1;

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
			swipeLeft();
		} else if (v == imgRight) {
			swipeRight();
		} else if (v instanceof LinearLayout) {

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

	private void addItemToViewFilpper(Reference r) {
		final LinearLayout ll = new LinearLayout(HomeActivity.this);
		if (Build.VERSION.SDK_INT >= 16) {
			if (r.getValue() != null) {
				switch (r.getId()) {
				case 1: {
					ll.setBackground(getResources().getDrawable(
							R.drawable.popular));
					break;
				}
				case 2: {
					ll.setBackground(getResources().getDrawable(
							R.drawable.music));
					break;
				}
				case 3: {
					ll.setBackground(getResources().getDrawable(
							R.drawable.newyoutube));
					break;
				}
				default:
					ll.setBackground(getResources().getDrawable(
							R.drawable.newyoutube));
					break;
				}
			}
		} else {
		}
		ll.setOrientation(LinearLayout.VERTICAL);
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

		ImageView imgView = new ImageView(HomeActivity.this);
		if (r.getValue() != null) {
			switch (r.getId()) {
			case 1: {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.popular1));
				break;
			}
			case 2: {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.music1));
				break;
			}
			case 3: {
				imgView.setImageDrawable(getResources().getDrawable(
						R.drawable.newyoutube1));
				break;
			}
			default:
				break;
			}
		}
		// if (r.getValue() != null && r.getValue().equals("Comedy")) {
		//
		// } else if (r.getValue() != null) {
		// imgView.setImageDrawable(getResources().getDrawable(
		// R.drawable.youtube));
		// }

		TextView tv = new TextView(HomeActivity.this);
		tv.setTextColor(Color.parseColor("#FFFFFF"));
		tv.setText(r.getDisplay());
		tv.setTextSize(50);
		LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		lp2.leftMargin = 18;
		lp2.rightMargin = 10;
		tv.setLayoutParams(lp2);

		ll1.addView(imgView);
		ll1.addView(tv);
		ll.addView(ll1);
		// Add slide event
		ll.setOnTouchListener(new OnSwipeTouchListener() {
			@Override
			public boolean onSwipeLeft() {
				swipeLeft();
				return true;
			}

			@Override
			public boolean onSwipeRight() {
				swipeRight();
				return true;
			}

			@Override
			public void onSwipeDown() {
				int cateId = ll.getId();
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

			@Override
			public void onSwipePress() {

			}
		});

		vf.addView(ll);
	}

	private void swipeLeft() {
		currentLayout--;
		if (currentLayout < 0) {
			currentLayout = 0;
		} else {
			vf.setInAnimation(this, R.animator.slide_in_from_left);
			vf.setOutAnimation(this, R.animator.slide_out_to_right);
			vf.setDisplayedChild(currentLayout);
			if (currentLayout == 0) {
				imgLeft.setVisibility(View.INVISIBLE);
			}
			if (imgRight.getVisibility() == View.INVISIBLE) {
				imgRight.setVisibility(View.VISIBLE);
			}
		}
	}

	private void swipeRight() {
		currentLayout++;
		if (currentLayout > categories.size() - 1) {
			currentLayout = categories.size() - 1;
		} else {
			vf.setInAnimation(this, R.animator.slide_in_from_right);
			vf.setOutAnimation(this, R.animator.slide_out_to_left);
			vf.setDisplayedChild(currentLayout);
			if (currentLayout == categories.size() - 1) {
				imgRight.setVisibility(View.INVISIBLE);
			}
			if (imgLeft.getVisibility() == View.INVISIBLE) {
				imgLeft.setVisibility(View.VISIBLE);
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
					ReferenceDao.KEY_YOUTUBE_CATEGORY,
					ReferenceDao.EXTRAS_CATEGORY_SELECT);

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String args) {
			vf.removeAllViews();
			currentLayout = 0;
			imgLeft.setVisibility(View.INVISIBLE);
			for (Reference r : categories) {
				addItemToViewFilpper(r);
			}
			if (categories.size() > 0) {
				imgRight.setVisibility(View.VISIBLE);
			}
			Reference add = new Reference();
			add.setId(CATEGORY_ADD);
			add.setDisplay("+");
			addItemToViewFilpper(add);

			vf.setDisplayedChild(0);

			categories.add(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
					"add_new", "+", ""));
		}

	}
}
