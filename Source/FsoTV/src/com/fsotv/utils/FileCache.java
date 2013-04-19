package com.fsotv.utils;

import java.io.File;
import java.net.URLEncoder;

import android.content.Context;

/**
 * 
 * @author ChungPV1
 * 
 */
public class FileCache {

	/**
	 * 
	 */
	private File cacheDir;

	/**
	 * 
	 * @param context
	 */
	public FileCache(Context context) {

		// Find the directory to save cached images
		if (android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED))
			cacheDir = new File(
					android.os.Environment.getExternalStorageDirectory(),
					"fsotv");

		else
			cacheDir = context.getCacheDir();

		if (!cacheDir.exists())
			cacheDir.mkdirs();
	}

	/**
	 * 
	 * @param url
	 * @return
	 */
	public File getFile(String url) {
		
		String filename = URLEncoder.encode(url);
		
		File f = new File(cacheDir, filename);
		
		return f;

	}

	/**
     * 
     */
	public void clear() {
		File[] files = cacheDir.listFiles();
		
		if (files == null)
			return;
		
		for (File f : files)
			f.delete();
	}

}