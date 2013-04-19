package com.fsotv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class WebHelper {
	public enum PostType {
		GET, POST;
	}
	/**
	 * Check internet connection
	 * @author CuongVM1
	 * @param context
	 * @return
	 */
	public static boolean isConnected(Context context) {
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
	/**
	 * Return inputstream base on url and web method
	 * @author ChungPV1
	 * @param url
	 * @param postType
	 * @return
	 * @throws Exception
	 */
	public static InputStream GetStream(String url, PostType postType)
			throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		InputStream is = null;
		Log.i("URL", url);

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();

		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		HttpResponse httpresponse;
		if (postType == PostType.POST) {
			HttpPost httppost = new HttpPost(url);
			httpresponse = httpclient.execute(httppost, localContext);
		} else {
			HttpGet httpget = new HttpGet(url);
			httpresponse = httpclient.execute(httpget, localContext);
		}
		is = httpresponse.getEntity().getContent();
		// StringBuilder responseString = inputStreamToString(is);
		// response = responseString.toString();

		return is;
	}
}
