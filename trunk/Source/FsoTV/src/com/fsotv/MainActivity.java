package com.fsotv;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.utils.DialogHelper;
import com.fsotv.utils.InternetConnection;
import com.fsotv.utils.OnSwipeTouchListener;
import com.fsotv.utils.YouTubeHelper;

/**
 * Main activity, allow + Change icon of image when selected + Show categories
 * on home tab + Show list of category + Start activity BrowseVideosActivity
 * when click Browse Icon + Start activity MyVideosActivity when click Favorite
 * Icon
 */
public class MainActivity extends Activity {
	// This device is tablet
	public static boolean IsTablet = false;
	// Result code for activity category
	private final int RESULT_CATEGORY = 1;
	private final int CATEGORY_ADD = -1;
	// Views
	private ImageView imgHome;
	private ImageView imgBrowse;
	private ImageView imgFavorite;
	private TextView tvHome;
	private TextView tvBrowse;
	private TextView tvFavorite;
	private TextView tvHomeTop;
	private TextView tvBrowseTop;
	private TextView tvFavoriteTop;
	private ViewFlipper vf;
	private ImageView imgLeft, imgRight;
	// List of category
	private List<Reference> categories;
	private int currentLayout = 0; // Current category that visible to user

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Check internet connection
		InternetConnection internetConnection = new InternetConnection(this);
		boolean isConnected = internetConnection.isConnected();
		if (!isConnected) {
			DialogHelper.showAlertDialog(this, "Internet",
					"No internet connection!", false);
		}
		// Check device
		IsTablet = isTabletDevice(this);
		Log.e("IsTablet", IsTablet ? "True" : "False");

		// Get views
		imgHome = (ImageView) findViewById(R.id.imgHome);
		imgBrowse = (ImageView) findViewById(R.id.imgBrowse);
		imgFavorite = (ImageView) findViewById(R.id.imgFavorite);
		tvHome = (TextView) findViewById(R.id.tvHome);
		tvBrowse = (TextView) findViewById(R.id.tvBrowse);
		tvFavorite = (TextView) findViewById(R.id.tvFavorite);
		tvHomeTop = (TextView) findViewById(R.id.tvHomeTop);
		tvBrowseTop = (TextView) findViewById(R.id.tvBrowseTop);
		tvFavoriteTop = (TextView) findViewById(R.id.tvFavoriteTop);
		vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);
		imgLeft = (ImageView) findViewById(R.id.imgLeft);
		imgRight = (ImageView) findViewById(R.id.imgRight);

		categories = new ArrayList<Reference>();
		new loadCategories().execute();
	}

	@Override
	protected void onResume() {
		super.onResume();

		changeSelected(R.id.llHome);
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
	 * Swipe viewFilpper when user click on left or right image
	 * 
	 * @param v
	 */
	public void onSwipeClick(View v) {
		if (v == imgLeft) {
			swipeLeft();
		} else if (v == imgRight) {
			swipeRight();
		} else if (v instanceof LinearLayout) {

		}
	}

	/**
	 * Add an category layout to viewFilpper
	 * 
	 * @param r
	 */
	private void addItemToViewFilpper(Reference r) {
		final LinearLayout ll = new LinearLayout(MainActivity.this);
		if (Build.VERSION.SDK_INT >= 16) {
			ll.setBackgroundColor(Color.parseColor("#ffffff"));
		} 
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.setGravity(Gravity.CENTER);
		ll.setId(r.getId());
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		ll.setLayoutParams(lp);

		LinearLayout ll1 = new LinearLayout(MainActivity.this);
		ll1.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		ll1.setLayoutParams(lp1);
		ImageView imgView = new ImageView(MainActivity.this);
		String value = r.getValue();
		if (value != null) {
			if (value.equals("Comedy")) {
				imgView.setBackgroundResource(R.drawable.comedy120);
			}else if (value.equals("Music")) {
				imgView.setBackgroundResource(R.drawable.music120);
			}else if (value.equals("News")) {
				imgView.setBackgroundResource(R.drawable.news120);
			}else if (value.equals("Autos")) {
				imgView.setBackgroundResource(R.drawable.auto120);
			}else if (value.equals("Education")) {
				imgView.setBackgroundResource(R.drawable.edu120);
			}else if (value.equals("Entertainment")) {
				imgView.setBackgroundResource(R.drawable.enter120);
			}else if (value.equals("Film")) {
				imgView.setBackgroundResource(R.drawable.film120);
			}else if (value.equals("Howto")) {
				imgView.setBackgroundResource(R.drawable.howto120);
			}else if (value.equals("People")) {
				imgView.setBackgroundResource(R.drawable.people120);
			}else if (value.equals("Animals")) {
				imgView.setBackgroundResource(R.drawable.animal120);
			}else if (value.equals("Tech")) {
				imgView.setBackgroundResource(R.drawable.tech120);
			}else if (value.equals("Sports")) {
				imgView.setBackgroundResource(R.drawable.sport120);
			}else if (value.equals("Travel")) {
				imgView.setBackgroundResource(R.drawable.travel120);
			}
			
		}

		TextView tv = new TextView(MainActivity.this);
		tv.setTextColor(Color.parseColor("#000000"));
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

	/**
	 * Swipe viewFlipper to left
	 */
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

	/**
	 * Swipe viewFilpper to right
	 */
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
	 * Event when user click on Home, Browse or Favorite layout
	 * 
	 * @param v
	 */
	public void onTabClick(View v) {
		switch (v.getId()) {
		case R.id.llHome:

			changeSelected(v.getId());
			break;
		case R.id.llBrowse:
			changeSelected(v.getId());
			Intent i1 = new Intent(getApplicationContext(),
					BrowseVideosActivity.class);
			i1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			i1.putExtra("categoryId", YouTubeHelper.CATEGORY_FILM);
			startActivity(i1);
			break;
		case R.id.llFavorite:
			changeSelected(v.getId());
			Intent i2 = new Intent(getApplicationContext(),
					MyVideosActivity.class);
			i2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i2);
			break;
		}
	}

	/**
	 * Change ui base on user's selected
	 * 
	 * @param v
	 */
	private void changeSelected(int id) {
		switch (id) {
		case R.id.llHome: // Home
			imgHome.setImageResource(R.drawable.icon_home_press);
			imgBrowse.setImageResource(R.drawable.icon_browse);
			imgFavorite.setImageResource(R.drawable.icon_favorite);
			tvHome.setTextColor(Color.parseColor("#ffffff"));
			tvBrowse.setTextColor(Color.parseColor("#000000"));
			tvFavorite.setTextColor(Color.parseColor("#000000"));
			tvHomeTop.setVisibility(View.VISIBLE);
			tvBrowseTop.setVisibility(View.INVISIBLE);
			tvFavoriteTop.setVisibility(View.INVISIBLE);
			break;
		case R.id.llBrowse: // Browse
			imgHome.setImageResource(R.drawable.icon_home);
			imgBrowse.setImageResource(R.drawable.icon_browse_press);
			imgFavorite.setImageResource(R.drawable.icon_favorite);
			tvHome.setTextColor(Color.parseColor("#000000"));
			tvBrowse.setTextColor(Color.parseColor("#ffffff"));
			tvFavorite.setTextColor(Color.parseColor("#000000"));
			tvHomeTop.setVisibility(View.INVISIBLE);
			tvBrowseTop.setVisibility(View.VISIBLE);
			tvFavoriteTop.setVisibility(View.INVISIBLE);
			break;
		case R.id.llFavorite: // Favorite
			imgHome.setImageResource(R.drawable.icon_home);
			imgBrowse.setImageResource(R.drawable.icon_browse);
			imgFavorite.setImageResource(R.drawable.icon_favorite_press);
			tvHome.setTextColor(Color.parseColor("#000000"));
			tvBrowse.setTextColor(Color.parseColor("#000000"));
			tvFavorite.setTextColor(Color.parseColor("#ffffff"));
			tvHomeTop.setVisibility(View.INVISIBLE);
			tvBrowseTop.setVisibility(View.INVISIBLE);
			tvFavoriteTop.setVisibility(View.VISIBLE);
			break;
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

	/**
	 * Check current device base on screen size
	 * 
	 * @param activityContext
	 * @return true if is tablet
	 */
	private boolean isTabletDevice(Context activityContext) {
		// Verifies if the Generalized Size of the device is XLARGE to be
		// considered a Tablet
		boolean xlarge = ((activityContext.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);

		// If XLarge, checks if the Generalized Density is at least MDPI
		// (160dpi)
		if (xlarge) {
			DisplayMetrics metrics = new DisplayMetrics();
			Activity activity = (Activity) activityContext;
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

			// MDPI=160, DEFAULT=160, DENSITY_HIGH=240, DENSITY_MEDIUM=160,
			// DENSITY_TV=213, DENSITY_XHIGH=320
			if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT
					|| metrics.densityDpi == DisplayMetrics.DENSITY_HIGH
					|| metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM
					|| metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {

				// Yes, this is a tablet!
				return true;
			}
		}

		// No, this is not a tablet!
		return false;
	}
}
