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
import android.text.method.HideReturnsTransformationMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.fsotv.dao.ReferenceDao;
import com.fsotv.dto.Reference;
import com.fsotv.tablet.BrowseVideosTabletActivity;
import com.fsotv.tablet.MyVideosTabletActivity;
import com.fsotv.utils.DialogHelper;
import com.fsotv.utils.OnSwipeTouchListener;
import com.fsotv.utils.WebHelper;
import com.fsotv.utils.YouTubeHelper;

/**
 * Main activity, allow + Change icon of image when selected + Show categories
 * on home tab + Show list of category + Start activity BrowseVideosActivity
 * when click Browse Icon + Start activity MyVideosActivity when click Favorite
 * Icon
 */
public class MainActivity extends Activity {
	/**
	 * SharedPreference location
	 */
	public static final String SHARED_PREFERENCE = "fsotv_pre";
	/**
	 * This device is tablet
	 */
	public static boolean IsTablet = false;
	private final String TAG = "MainActivity";
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
	private HorizontalScrollView hsv;
	private LinearLayout ll;
	// List of category
	private List<Reference> categories;
	private int currentLayout = 0; // Current category that visible to user

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Check device
		IsTablet = isTabletDevice(this);
		if (!IsTablet) {
			setContentView(R.layout.activity_main);

			vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);
			imgLeft = (ImageView) findViewById(R.id.imgLeft);
			imgRight = (ImageView) findViewById(R.id.imgRight);

			vf.setInAnimation(this, R.animator.slide_in_from_left);
			vf.setOutAnimation(this, R.animator.slide_out_to_right);
		} else {
			setContentView(R.layout.activity_main_tablet);

			hsv = (HorizontalScrollView) findViewById(R.id.scrollView_channel);
			// Add layout to HorizontalScrollView
			ll = new LinearLayout(MainActivity.this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			ll.setGravity(Gravity.CENTER);
			ll.setLayoutParams(lp);
		}

		Log.i(TAG, IsTablet ? "True" : "False");

		// Check Internet connection
		boolean isConnected = WebHelper.isConnected(this);
		if (!isConnected) {
			DialogHelper.showAlertDialog(this, "Internet",
					"No internet connection!", false);
		}

		imgHome = (ImageView) findViewById(R.id.imgHome);
		imgBrowse = (ImageView) findViewById(R.id.imgBrowse);
		imgFavorite = (ImageView) findViewById(R.id.imgFavorite);
		tvHome = (TextView) findViewById(R.id.tvHome);
		tvBrowse = (TextView) findViewById(R.id.tvBrowse);
		tvFavorite = (TextView) findViewById(R.id.tvFavorite);
		tvHomeTop = (TextView) findViewById(R.id.tvHomeTop);
		tvBrowseTop = (TextView) findViewById(R.id.tvBrowseTop);
		tvFavoriteTop = (TextView) findViewById(R.id.tvFavoriteTop);

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
		}
	}

	/**
	 * Add an category layout to view base on screen size
	 * 
	 * @param r
	 */
	private void addItemToView(Reference r) {
		final LinearLayout ll1 = new LinearLayout(MainActivity.this);
		ll1.setOrientation(LinearLayout.VERTICAL);
		ll1.setGravity(Gravity.CENTER);
		ll1.setId(r.getId());
		if (!IsTablet) {
			if (Build.VERSION.SDK_INT >= 16) {
				ll1.setBackgroundColor(Color.parseColor("#ffffff"));
			}
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.MATCH_PARENT);
			ll1.setLayoutParams(lp);
		} else {
			if (Build.VERSION.SDK_INT >= 16) {
				ll1.setBackgroundResource(R.drawable.corner_white);
			}
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(280,
					300);
			lp.setMargins(0, 0, 5, 0);
			ll1.setLayoutParams(lp);
		}

		LinearLayout ll2 = new LinearLayout(MainActivity.this);
		ll2.setOrientation(LinearLayout.VERTICAL);
		LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		ll2.setLayoutParams(lp1);
		ImageView imgView = new ImageView(MainActivity.this);
		String value = r.getValue();
		if (value != null) {
			if (value.equals("Comedy")) {
				imgView.setBackgroundResource(R.drawable.comedy120);
			} else if (value.equals("Music")) {
				imgView.setBackgroundResource(R.drawable.music120);
			} else if (value.equals("News")) {
				imgView.setBackgroundResource(R.drawable.news120);
			} else if (value.equals("Autos")) {
				imgView.setBackgroundResource(R.drawable.auto120);
			} else if (value.equals("Education")) {
				imgView.setBackgroundResource(R.drawable.edu120);
			} else if (value.equals("Entertainment")) {
				imgView.setBackgroundResource(R.drawable.enter120);
			} else if (value.equals("Film")) {
				imgView.setBackgroundResource(R.drawable.film120);
			} else if (value.equals("Howto")) {
				imgView.setBackgroundResource(R.drawable.howto120);
			} else if (value.equals("People")) {
				imgView.setBackgroundResource(R.drawable.people120);
			} else if (value.equals("Animals")) {
				imgView.setBackgroundResource(R.drawable.animal120);
			} else if (value.equals("Tech")) {
				imgView.setBackgroundResource(R.drawable.tech120);
			} else if (value.equals("Sports")) {
				imgView.setBackgroundResource(R.drawable.sport120);
			} else if (value.equals("Travel")) {
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

		ll2.addView(imgView);
		ll2.addView(tv);
		ll1.addView(ll2);
		if (!IsTablet) {
			// Add slide event
			ll1.setOnTouchListener(new OnSwipeTouchListener() {
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
					int cateId = ll1.getId();
					Intent i = new Intent();
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if (cateId == CATEGORY_ADD) {
						i.setClass(getApplicationContext(),
								CategoryActivity.class);
						startActivityForResult(i, RESULT_CATEGORY);
					} else {
						i.setClass(getApplicationContext(),
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

			});
			vf.addView(ll1);
		} else {
			// Add click event
			ll1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					int cateId = v.getId();
					Intent i = new Intent();
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					if (cateId == CATEGORY_ADD) {
						i.setClass(getApplicationContext(),
								CategoryActivity.class);
						startActivityForResult(i, RESULT_CATEGORY);
					} else {
						i.setClass(getApplicationContext(),
								BrowseVideosTabletActivity.class);
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
	}

	/**
	 * Swipe viewFlipper to left
	 */
	private void swipeLeft() {
		currentLayout--;
		if (currentLayout < 0) {
			currentLayout = 0;
		} else {
			vf.setInAnimation(this, R.animator.slide_in_from_right);
			vf.setOutAnimation(this, R.animator.slide_out_to_left);
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
			vf.setInAnimation(this, R.animator.slide_in_from_left);
			vf.setOutAnimation(this, R.animator.slide_out_to_right);
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
		changeSelected(v.getId());
		if (v.getId() == R.id.llHome) {
			return;
		}
		Intent i = new Intent();
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		if (v.getId() == R.id.llBrowse) {
			i.putExtra("categoryId", YouTubeHelper.CATEGORY_FILM);
			if (!IsTablet) {
				i.setClass(getApplicationContext(), BrowseVideosActivity.class);
			} else {
				i.setClass(getApplicationContext(),
						BrowseVideosTabletActivity.class);
			}
		} else if (v.getId() == R.id.llFavorite) {
			if (!IsTablet) {
				i.setClass(getApplicationContext(), MyVideosActivity.class);
			} else {
				i.setClass(getApplicationContext(),
						MyVideosTabletActivity.class);
			}
		}
		startActivity(i);
	}

	/**
	 * Change ui base on user's selected
	 * 
	 * @param v
	 */
	private void changeSelected(int id) {
		if (id == R.id.llHome) {
			imgHome.setImageResource(R.drawable.icon_home_press);
			imgBrowse.setImageResource(R.drawable.icon_browse);
			imgFavorite.setImageResource(R.drawable.icon_favorite);
			tvHome.setTextColor(Color.parseColor("#ffffff"));
			tvBrowse.setTextColor(Color.parseColor("#000000"));
			tvFavorite.setTextColor(Color.parseColor("#000000"));
			tvHomeTop.setVisibility(View.VISIBLE);
			tvBrowseTop.setVisibility(View.INVISIBLE);
			tvFavoriteTop.setVisibility(View.INVISIBLE);
		} else if (id == R.id.llBrowse) {
			imgHome.setImageResource(R.drawable.icon_home);
			imgBrowse.setImageResource(R.drawable.icon_browse_press);
			imgFavorite.setImageResource(R.drawable.icon_favorite);
			tvHome.setTextColor(Color.parseColor("#000000"));
			tvBrowse.setTextColor(Color.parseColor("#ffffff"));
			tvFavorite.setTextColor(Color.parseColor("#000000"));
			tvHomeTop.setVisibility(View.INVISIBLE);
			tvBrowseTop.setVisibility(View.VISIBLE);
			tvFavoriteTop.setVisibility(View.INVISIBLE);
		} else if (id == R.id.llFavorite) {
			imgHome.setImageResource(R.drawable.icon_home);
			imgBrowse.setImageResource(R.drawable.icon_browse);
			imgFavorite.setImageResource(R.drawable.icon_favorite_press);
			tvHome.setTextColor(Color.parseColor("#000000"));
			tvBrowse.setTextColor(Color.parseColor("#000000"));
			tvFavorite.setTextColor(Color.parseColor("#ffffff"));
			tvHomeTop.setVisibility(View.INVISIBLE);
			tvBrowseTop.setVisibility(View.INVISIBLE);
			tvFavoriteTop.setVisibility(View.VISIBLE);
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
			Reference add = new Reference();
			add.setId(CATEGORY_ADD);
			add.setDisplay("+");
			categories.add(new Reference(-1, ReferenceDao.KEY_YOUTUBE_CATEGORY,
					"add_new", "+", ""));
			addItemToView(add);

			if (!IsTablet) {
				vf.removeAllViews();
				currentLayout = 0;
				imgLeft.setVisibility(View.INVISIBLE);
				for (Reference r : categories) {
					addItemToView(r);
				}
				if (categories.size() > 1) {
					imgRight.setVisibility(View.VISIBLE);
				}
				vf.setDisplayedChild(0);
			} else {
				ll.removeAllViews();
				hsv.removeAllViews();
				for (Reference r : categories) {
					addItemToView(r);
				}
				hsv.addView(ll);
			}
		}
	}

	/**
	 * Check current device base on screen size
	 * 
	 * @param activityContext
	 * @return true if is tablet
	 */
	private boolean isTabletDevice(Context activityContext) {
		DisplayMetrics metrics = new DisplayMetrics();
		Activity activity = (Activity) activityContext;
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		Log.i(TAG, "Width:" + width);
		Log.i(TAG, "Height:" + height);
		// 7 inch and 10 inch
		if (width >= 1024) {
			if (metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM) {
				return true;
			}
		}
		// No, this is not a tablet!
		return false;
	}
}
