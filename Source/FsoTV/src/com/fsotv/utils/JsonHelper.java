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
/**
 * 
 * @author ChungPV1, CuongVM1
 */
@SuppressWarnings("unused")
public class JsonHelper {

	/**
	 * Get json by stream and return json object
	 * @param inputStream
	 * @return
	 * @throws Exception
	 */
	public static JSONObject getJSONFromStream(InputStream is) throws Exception {
		Log.i("JsonHelper", "Start getJSONFromStream()");
		
		String json = "";
		JSONObject jsonObject = null;

		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				"iso-8859-1"), 8);
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line + "\n");
		}
		
		is.close();
		json = stringBuilder.toString();

		jsonObject = new JSONObject(json);

		Log.i("JsonHelper", "End getJSONFromStream()");
		
		return jsonObject;
	}
	
}
