package com.fsotv;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

@SuppressWarnings("deprecation")
public class MainActivity extends TabActivity {
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		TabHost tabHost = getTabHost();
		 
        // Tab Home, setting Title and Icon for the Tab
        TabSpec homeSpec = tabHost.newTabSpec("Home");
        homeSpec.setIndicator("Home", getResources().getDrawable(R.drawable.icon_home));
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeSpec.setContent(homeIntent);
        
        // Tab Browse, setting Title and Icon for the Tab
        TabSpec browseSpec = tabHost.newTabSpec("Browse");
        browseSpec.setIndicator("Browse", getResources().getDrawable(R.drawable.icon_browse));
        Intent browseIntent = new Intent(this, BrowseActivity.class);
        browseSpec.setContent(browseIntent);
        
        // Tab Record, setting Title and Icon for the Tab
        TabSpec recordSpec = tabHost.newTabSpec("Record");
        recordSpec.setIndicator("Record", getResources().getDrawable(R.drawable.icon_record));
        Intent recordIntent = new Intent(this, RecordActivity.class);
        recordSpec.setContent(recordIntent);
        
        // Tab Social, setting Title and Icon for the Tab
        TabSpec socialSpec = tabHost.newTabSpec("Social");
        socialSpec.setIndicator("Social", getResources().getDrawable(R.drawable.icon_social));
        Intent socialIntent = new Intent(this, SocialActivity.class);
        socialSpec.setContent(socialIntent);
        
        // Adding all TabSpec to TabHost
        tabHost.addTab(homeSpec);
        tabHost.addTab(browseSpec);
        tabHost.addTab(recordSpec);
        tabHost.addTab(socialSpec);
	}

	
}
