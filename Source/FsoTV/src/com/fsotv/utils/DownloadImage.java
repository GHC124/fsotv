package com.fsotv.utils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class DownloadImage extends AsyncTask<String, Void, Bitmap> {
	ImageView bmImage;
    ProgressBar progressBar;
    
    public DownloadImage(ImageView bmImage, ProgressBar progressBar) {
        this.bmImage = bmImage;
        this.progressBar = progressBar;
        //starting task, show progress bar
        if(progressBar!=null)
        	progressBar.setVisibility(View.VISIBLE);
    }

    protected Bitmap doInBackground(String... urls) {
        String strUrl = urls[0];
        Bitmap b = null;
        try {
        	URL url = new URL(strUrl);
        	URLConnection conn = url.openConnection();
        	InputStream is = conn.getInputStream();
            //b = FileHelper.decodeImage(is, 50);
        	b = BitmapFactory.decodeStream(is);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return b;
    }

    protected void onPostExecute(Bitmap result) {
    	//task done, hide it again
    	if(progressBar!=null)
    		progressBar.setVisibility(View.GONE);
    	if(result!=null)
    		bmImage.setImageBitmap(result);
    }
}