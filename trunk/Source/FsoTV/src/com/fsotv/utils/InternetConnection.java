/**
 * 
 */
package com.fsotv.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * @author CuongVM1
 * 
 */
public class InternetConnection {
	private Context context;

	public InternetConnection(Context context) {
		this.context = context;
	}

	public boolean isConnected() {
		boolean result = false;
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		if (connectivityManager != null) {
			NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						result = true;
					}
				}
			}

		}

		return result;
	}
}
