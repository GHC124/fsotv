package com.fsotv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.fsotv.tablet.BrowseVideosTabletActivity;
import com.fsotv.tablet.MyVideosTabletActivity;
import com.fsotv.utils.YouTubeHelper;

/**
 * Base class for all activities that have same header + Add loading progress
 * bar + Add header title + Add Back button + Add search function + Add option
 * button.
 * @author ChungPV1
 */
public class ActivityBase extends Activity {
	// Back option
	private final int OPTION_BACK = Menu.FIRST + 100;
	// Views
	private DialogBase optionDialog;
	private LinearLayout llHeader;
	private ImageView imgBack;
	private ProgressBar phHeader;
	private TextView tvHeader;
	private ImageView imgSearch;
	private RelativeLayout rlSearch;
	private EditText txtSearch;

	private ImageView imgCancelSearch;
	private ImageView imgGoSearch;

	private ImageView imgOption;
	private boolean canSearch = true;
	private boolean isSearch = false;
	private boolean isOpenSearch = false; // Open textbox to search
	private boolean isCloseSearch = false; // Close textbox for search

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}



	@Override
	public void setContentView(int layoutResId) {
		// Check device, if it is tablet, use only landscape ...
		if (MainActivity.IsTablet) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			// ... otherwise, use portrait
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

		super.setContentView(layoutResId);

		llHeader = (LinearLayout) findViewById(R.id.llHeader);
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		phHeader = (ProgressBar) findViewById(R.id.pbHeader);
		rlSearch = (RelativeLayout) findViewById(R.id.rlSearch);
		imgBack = (ImageView) findViewById(R.id.imgBack);
		imgSearch = (ImageView) findViewById(R.id.imgSearch);
		imgCancelSearch = (ImageView) findViewById(R.id.imgCancelSearch);
		imgGoSearch = (ImageView) findViewById(R.id.imgGoSearch);
		imgOption = (ImageView) findViewById(R.id.imgOption);
		txtSearch = (EditText) findViewById(R.id.txtSearch);

		tvHeader.setText("");
		// Open/close search control
		if (imgSearch != null) {
			imgSearch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (canSearch && !isSearch && !isOpenSearch) { // Show
																	// search
						new Thread(new Runnable() {
							@Override
							public void run() {
								Message msg = new Message();
								msg.what = 1;
								hander.sendMessage(msg);
							}
						}).start();

					} else if (isSearch && !isCloseSearch) { // Hide search
						new Thread(new Runnable() {
							@Override
							public void run() {
								Message msg = new Message();
								msg.what = 2;
								hander.sendMessage(msg);
							}
						}).start();
					}
				}
			});
			// Close search
			imgCancelSearch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if (isSearch && !isCloseSearch) { // Hide search
						new Thread(new Runnable() {
							@Override
							public void run() {
								Message msg = new Message();
								msg.what = 2;
								hander.sendMessage(msg);
							}
						}).start();
					}
				}
			});
			// Start search
			imgGoSearch.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					String keyword = txtSearch.getText().toString();
					if (keyword.length() > 0) {
						// Call function
						Search(keyword);
					} else {
						Toast.makeText(getApplicationContext(),
								"Input text to search", Toast.LENGTH_SHORT)
								.show();
					}
				}
			});
		}
		// Show option
		imgOption.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (optionDialog != null)
					optionDialog.show();
				else {
					createOptionDialog(ActivityBase.this);
					if (optionDialog != null)
						optionDialog.show();
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_BACK, 100, "Back");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTION_BACK:
			finish();
			break;
		}
		return true;
	}

	/**
	 * Create dialog that allow user to navigate to main functions: home,
	 * browse, favorite
	 * 
	 * @param context
	 */
	private void createOptionDialog(Context context) {
		optionDialog = new DialogBase(context);
		optionDialog.setContentView(R.layout.option);
		optionDialog.setHeader("Options");
		final TextView txtHome = (TextView) optionDialog
				.findViewById(R.id.tvHome);
		final TextView txtBrowse = (TextView) optionDialog
				.findViewById(R.id.tvBrowse);
		final TextView txtFavorite = (TextView) optionDialog
				.findViewById(R.id.tvFavorite);

		txtHome.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				optionDialog.dismiss();
				Intent i = new Intent(getApplicationContext(),
						MainActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
			}
		});
		txtBrowse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				optionDialog.dismiss();
				if (MainActivity.IsTablet) {
					Intent i = new Intent(getApplicationContext(),
							BrowseVideosTabletActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					Intent i = new Intent(getApplicationContext(),
							BrowseVideosActivity.class);
					i.putExtra("categoryId", YouTubeHelper.CATEGORY_FILM);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
			}
		});
		txtFavorite.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				optionDialog.dismiss();
				if (MainActivity.IsTablet) {
					Intent i = new Intent(getApplicationContext(),
							MyVideosTabletActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				} else {
					Intent i = new Intent(getApplicationContext(),
							MyVideosActivity.class);
					i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}

			}
		});
	}

	/**
	 * Hide header control
	 */
	protected void hideHeader() {
		if (llHeader != null)
			llHeader.setVisibility(View.GONE);
	}

	/**
	 * Show header control
	 */
	protected void showHeader() {
		if (llHeader != null)
			llHeader.setVisibility(View.VISIBLE);
	}

	/**
	 * Back to previous screen
	 * 
	 * @param v
	 */
	public void onBackClick(View v) {
		finish();
	}

	/**
	 * Show search control, can be called by subclass
	 */
	protected void showSearch() {
		if (canSearch && !isSearch && !isOpenSearch) { // Show search
			new Thread(new Runnable() {
				@Override
				public void run() {
					Message msg = new Message();
					msg.what = 1;
					hander.sendMessage(msg);
				}
			}).start();

		}
	}

	/**
	 * Enable search function
	 */
	protected void enableSearch() {
		canSearch = true;
	}

	/**
	 * Disable search function
	 */
	protected void disableSearch() {
		canSearch = false;
		if (imgSearch != null)
			imgSearch.setVisibility(View.GONE);
	}

	// Update search textbox
	private Handler hander = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) { // Show search
				isOpenSearch = true;
				if (rlSearch.getVisibility() == View.GONE) {
					rlSearch.setVisibility(View.VISIBLE);
					isOpenSearch = false;
					imgSearch.setVisibility(View.GONE);
				}
			} else if (msg.what == 2) { // Hide search
				isCloseSearch = true;
				if (rlSearch.getVisibility() == View.VISIBLE) {
					rlSearch.setVisibility(View.GONE);
					isCloseSearch = false;
					imgSearch.setVisibility(View.VISIBLE);
					// Call close search action
					if (txtSearch.getText().toString().length() > 0) {
						CloseSearch();
						txtSearch.setText("");
					}
				}
			}
			isSearch = !isSearch;
		}

	};

	/**
	 * This method will be override by subclass to hander search action
	 * 
	 * @param keyword
	 */
	protected void Search(String keyword) {

	}

	/**
	 * This method will be override by subclass to hander close search action
	 * 
	 * @param keyword
	 */
	protected void CloseSearch() {

	}

	protected void showBack() {
		if (imgBack != null)
			imgBack.setVisibility(View.VISIBLE);
	}

	protected void hideBack() {
		if (imgBack != null)
			imgBack.setVisibility(View.INVISIBLE);
	}
	
	protected void setKeyword(String text) {
		if (txtSearch != null) {
			txtSearch.setText(text);
		}
	}

	protected void setHeader(String text) {
		if (tvHeader != null) {
			tvHeader.setText(text);
		}
	}

	protected void showLoading() {
		if (phHeader != null)
			phHeader.setVisibility(View.VISIBLE);
	}

	protected void hideLoading() {
		if (phHeader != null)
			phHeader.setVisibility(View.INVISIBLE);
	}

	protected ProgressBar getLoadingView() {
		return phHeader;
	}

	protected TextView getHeaderView() {
		return tvHeader;
	}

}
