package com.fsotv;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.json.JSONObject;

import com.fsotv.dto.Channel;
import com.fsotv.utils.JsonHelper;
import com.fsotv.utils.YouTubeHelper;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class DemoActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		
		
	}


	
	public void getChannels(View v){
		InputStream is;
		try {
			is = getAssets().open("Channels.txt");
			List<Channel> channels = YouTubeHelper.getChannels(is);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
