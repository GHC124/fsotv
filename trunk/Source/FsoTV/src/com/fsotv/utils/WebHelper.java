package com.fsotv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.entity.StringEntity;
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
	 * Execute HTTP Post/Get request
	 * @param url
	 * @param postType
	 * @param headers
	 * @param params
	 * @return HttpStatus
	 * @throws Exception
	 */
	public static int execute(String url, PostType postType,  List<NameValuePair> headers, StringEntity se)
			throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		Log.i("URL", url);

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();

		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
		
		HttpResponse response = null;
		
		if (postType == PostType.POST) {
			HttpPost httppost = new HttpPost(url);
			if(headers != null){
				for(NameValuePair header:headers){
					httppost.setHeader(header.getName(), header.getValue());
				}
			}
			if(se != null){
				httppost.setEntity(se);
			}
			
			response = httpclient.execute(httppost, localContext);
		} else {
			HttpGet httpget = new HttpGet(url);
			response = httpclient.execute(httpget, localContext);
		}
		int status = response.getStatusLine().getStatusCode();
		Log.i("HttpStatus", status + "");
		
		logResponse(response);
		
		return status;
	}
	/**
	 * Execute HTTP Post/Get request and return input stream
	 * @param url
	 * @param postType
	 * @param headers
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public static InputStream getStream(String url, PostType postType,  List<NameValuePair> headers, List<NameValuePair> params)
			throws Exception {
		HttpClient httpclient = new DefaultHttpClient();
		CookieStore cookieStore = new BasicCookieStore();
		InputStream is = null;
		Log.i("URL", url);

		// Create local HTTP context
		HttpContext localContext = new BasicHttpContext();

		// Bind custom cookie store to the local context
		localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

		HttpResponse response;
		if (postType == PostType.POST) {
			HttpPost httppost = new HttpPost(url);
			if(headers != null){
				for(NameValuePair header:headers){
					httppost.setHeader(header.getName(), header.getValue());
				}
			}
			if(params != null){
				httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
			}
			response = httpclient.execute(httppost, localContext);
		} else {
			HttpGet httpget = new HttpGet(url);
			response = httpclient.execute(httpget, localContext);
		}
		int status = response.getStatusLine().getStatusCode();
		Log.i("HttpStatus", status + "");
		
		is = response.getEntity().getContent();
		
		return is;
	}
	
	public static void logResponse(HttpResponse response) throws Exception{
		BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), 8192);
        StringBuffer sb = new StringBuffer();
        String line = "";
        String newLine = System.getProperty("line.separator");
        while((line = in.readLine()) != null) {
            sb.append(line + newLine);
        }
        Log.i("HttpResponse", sb.toString());
	}
}
