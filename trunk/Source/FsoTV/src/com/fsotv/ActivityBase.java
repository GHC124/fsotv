package com.fsotv;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ActivityBase extends Activity {
	private final int OPTION_BACK = Menu.FIRST + 100;
	
	private ProgressBar phHeader;
	private TextView tvHeader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
		
	@Override
	public void setContentView(int layoutResId){
		super.setContentView(layoutResId);
		
		tvHeader = (TextView)findViewById(R.id.tvHeader);
		phHeader = (ProgressBar)findViewById(R.id.pbHeader);
		
		tvHeader.setText("");
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_BACK, 100, "Back");
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch(item.getItemId())
		{
		case OPTION_BACK:
			finish();
			break;
		}
		return true;
	}
	/**
	 * Call after setContentView
	 */
	protected void initBaseActivity() {
		tvHeader = (TextView)findViewById(R.id.tvHeader);
		phHeader = (ProgressBar)findViewById(R.id.pbHeader);
	}
	
	public void onBackClick(View v){
		finish();
	}
	
	protected void setHeader(String text) {
		if(tvHeader != null)
			tvHeader.setText(text);
	}
	
	protected void showLoading() {
		if(phHeader != null)
			phHeader.setVisibility(View.VISIBLE);
	}
	
	protected void hideLoading() {
		if(phHeader != null)
			phHeader.setVisibility(View.INVISIBLE);
	}

	protected ProgressBar getLoadingView() {
		return phHeader;
	}

	protected TextView getHeaderView() {
		return tvHeader;
	}
	
}