package com.fsotv.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.fsotv.dto.ChannelEntry;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

class ChannelAsync {
	public int subscriberCount;
	public int viewCount;
	public Bitmap image;
}

public class DownloadChannel extends AsyncTask<String, Void, ChannelAsync> {
	TextView viewCount;
	TextView subscriberCount;
	ImageView bmImage;
	ProgressBar progressBar;

	public DownloadChannel(TextView viewCount, 
			TextView subscriberCount, ImageView bmImage, ProgressBar progressBar) {
		this.viewCount = viewCount;
		this.subscriberCount = subscriberCount;
		this.bmImage = bmImage;
		this.progressBar = progressBar;
		// starting task, show progress bar
		if(progressBar!=null)
			progressBar.setVisibility(View.VISIBLE);
	}

	protected ChannelAsync doInBackground(String... args) {
		String channelId = args[0];
		ChannelAsync channel = new ChannelAsync();
		try {
			// Get channel
			ChannelEntry entry = YouTubeHelper.getChannelDetail(channelId);
			channel.viewCount = entry.getViewCount();
			channel.subscriberCount = entry.getSubscriberCount();
			// Get image
			URL url = new URL(entry.getImage());
			URLConnection conn = url.openConnection();
			InputStream is = conn.getInputStream();
			// b = FileHelper.decodeImage(is, 50);
			channel.image = BitmapFactory.decodeStream(is);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return channel;
	}

	protected void onPostExecute(ChannelAsync result) {
		// task done, hide it again
		if(progressBar!=null)
			progressBar.setVisibility(View.GONE);
		viewCount.setText(result.viewCount + "");
		subscriberCount.setText(result.subscriberCount + "");
		if(result.image!=null)
			bmImage.setImageBitmap(result.image);
	}
}