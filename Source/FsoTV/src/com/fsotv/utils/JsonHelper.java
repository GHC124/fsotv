package com.fsotv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonHelper {

	public static JSONObject getJSONFromStream(InputStream is) throws Exception {
		String json = "";
		JSONObject jObj = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"iso-8859-1"), 8);
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line + "\n");
		}
		is.close();
		json = sb.toString();

		// try parse the string to a JSON object

		jObj = new JSONObject(json);

		// return JSON String
		return jObj;

	}

	/**
	 * 
	 * @return
	 * @throws IOException
	 */
	public static String getJSONC() throws IOException {
		URL url = new URL(
				"http://gdata.youtube.com/feeds/api/videos/9bZkp7q19f0?v=2&alt=jsonc");
		StringBuilder result = new StringBuilder();

		URLConnection connection = url.openConnection();
		connection.setConnectTimeout(30000);
		connection.setReadTimeout(30000);

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(connection.getInputStream()));

		String inputLine;
		while ((inputLine = bufferedReader.readLine()) != null) {
			result.append(inputLine);

		}

		bufferedReader.close();

		return result.toString();
	}
}
