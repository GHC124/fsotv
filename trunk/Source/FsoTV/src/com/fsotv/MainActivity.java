package com.fsotv;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

import com.fsotv.tablet.BrowseTabletActivity;
import com.fsotv.tablet.FavoriteTabletActivity;
import com.fsotv.tablet.HomeTabletActivity;
import com.fsotv.utils.DialogHelper;
import com.fsotv.utils.InternetConnection;

/**
 * Main activity
 * 
 */
@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	// This device is tablet
	public static boolean IsTablet = false;

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

		TabHost tabHost = getTabHost();

		// Tab Home, setting Title and Icon for the Tab
		TabSpec homeSpec = tabHost.newTabSpec("Home");
		homeSpec.setIndicator("Home",
				getResources().getDrawable(R.drawable.icon_home));
		Intent homeIntent = new Intent();
		if (IsTablet) {
			homeIntent.setClass(this, HomeTabletActivity.class);
		} else {
			homeIntent.setClass(this, HomeActivity.class);
		}
		homeSpec.setContent(homeIntent);

		// Tab Browse, setting Title and Icon for the Tab
		TabSpec browseSpec = tabHost.newTabSpec("Browse");
		browseSpec.setIndicator("Browse",
				getResources().getDrawable(R.drawable.icon_browse));
		Intent browseIntent = new Intent();
		if (IsTablet) {
			browseIntent.setClass(this, BrowseTabletActivity.class);
		} else {
			browseIntent.setClass(this, BrowseActivity.class);
		}
		browseSpec.setContent(browseIntent);

		// Tab Favorite, setting Title and Icon for the Tab
		TabSpec favoriteSpec = tabHost.newTabSpec("Favorite");
		favoriteSpec.setIndicator("Favorite",
				getResources().getDrawable(R.drawable.icon_record));
		Intent favoriteIntent = new Intent();
		if (IsTablet) {
			favoriteIntent.setClass(this, FavoriteTabletActivity.class);
		} else {
			favoriteIntent.setClass(this, FavoriteActivity.class);
		}
		favoriteSpec.setContent(favoriteIntent);

		// Adding all TabSpec to TabHost
		tabHost.addTab(homeSpec);
		tabHost.addTab(browseSpec);
		tabHost.addTab(favoriteSpec);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				Log.e("TAB", tabId + "");
			}
		});
	}

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
