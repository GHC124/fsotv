package com.fsotv.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class JsonHelper {

	public static JSONObject getJSONFromStream(InputStream is)
			throws IOException, JSONException {
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
}
