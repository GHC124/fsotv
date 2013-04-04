package com.fsotv.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.util.Log;

import com.fsotv.dto.Channel;
import com.fsotv.dto.Video;

public class YouTubeHelper {
	// Constants
	public final static String CATEGORY_EDUCATION = "Education";
	public final static String CATEGORY_COMEDY = "Comedy";
	public final static String CATEGORY_ENTERTAIMENT = "Entertainment";
	public final static String CATEGORY_MUSIC = "Music";
	public final static String CATEGORY_SPORTS = "Sports";
	public final static int CHANNEL_MOST_VIEWED = 1;
	public final static int CHANNEL_MOST_SUBSCRIBED = 2;
	public final static int ORDERING_RELEVANCE = 1;
	public final static int ORDERING_PUBLISHED = 2;
	public final static int ORDERING_VIEWCOUNT = 3;

	private final static String KEY_CHANNELS = "channels";
	private final static String KEY_CHANNEL_STANDARD = "channelstandardfeeds";
	private final static String KEY_CHANNEL_MOST_VIEWED = "most_viewed";
	private final static String KEY_CHANNEL_MOST_SUBSCRIBED = "most_subscribed";
	private final static String KEY_VIDEOS = "videos";
	private final static String KEY_USERS = "users";
	private final static String KEY_OUTPUT_FORMAT = "alt";
	private final static String KEY_MAX_RESULT = "max-results";
	private final static String KEY_ORDERBY = "orderby";
	private final static String KEY_VIDEO_FORMAT = "format";
	private final static String KEY_SEARCH_QUERY = "q";
	
	private final static String VALUE_OUTPUT_FORMAT_JSON = "json";
	private final static String VALUE_OUTPUT_FORMAT_RSS = "rss";
	private final static String VALUE_ORDERBY_PUBLISHED = "published";
	private final static String VALUE_ORDERBY_VIEWCOUNT = "viewCount";
	private final static int VALUE_VIDEO_FORMAT_H263 = 1;
	private final static int VALUE_VIDEO_FORMAT_EMBEDDABLE = 5;
	private final static int VALUE_VIDEO_FORMAT_MPEG4SP = 6;
	
	private final static String GdataURL = "http://gdata.youtube.com/feeds/api";
	private final static String CategoryURL = "-/%7Bhttp%3A%2F%2Fgdata.youtube.com%2Fschemas%2F2007%2Fcategories.cat%7D";
	
	/**
	 * Get channels
	 * 
	 * @param url
	 * @return json string
	 */
	public static List<Channel> getChannels(String category) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/channelstandardfeeds/most_viewed");
		//sb.append(CategoryURL);
		//sb.append(category);
		sb.append("?v=2&max-results=15&orderby=viewCount&alt=json");
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getChannels", ex.toString());
		}
		return getChannels(is);
	}
	
	public static List<Channel> getChannels(InputStream is){
		List<Channel> channels = new ArrayList<Channel>();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for(int i = 0; i < entries.length(); i++){
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject idObject = entryObject.getJSONObject("yt$channelId");
				JSONObject titleObject = entryObject.getJSONObject("title");
				
				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String uri = GdataURL + "/users/" + id + "/uploads?v=2&alt=json&max-results=5";
				
				Channel channel = new Channel();
				channel.setNameChannel(title);
				channel.setUri(uri);
				channels.add(channel);
				
				titleObject = null;
				idObject = null;
				entryObject = null;
			}
			entries = null;
			feed = null;
			json = null;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return channels;
	}
	/**
	 * Get video detail
	 * 
	 * @param url
	 * @return json string
	 */
	public static List<Video> getVideosInChannel(String channelId) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/users/");
		sb.append(channelId);
		sb.append("/uploads");
		sb.append("?v=2&max-results=15&orderby=viewCount");
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInChannel", ex.toString());
		}
		return getVideos(is);
	}
	
	public static List<Video> getVideos(InputStream is){
		List<Video> videos = new ArrayList<Video>();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for(int i = 0; i < entries.length(); i++){
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject groupObject = entryObject.getJSONObject("media$group");
				JSONObject descriptionObject = groupObject.getJSONObject("media$description");
				JSONObject titleObject = groupObject.getJSONObject("media$title");
				JSONObject idObject = groupObject.getJSONObject("yt$videoid");
								
				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = descriptionObject.getString("$t");
				String uri = GdataURL + "/videos/" + id + "?v=2&alt=json";
				
				Video video = new Video();
				video.setNameVideo(title);
				video.setDescribes(description);
				video.setUri(uri);
				videos.add(video);
				
				descriptionObject = null;
				titleObject = null;
				idObject = null;
				groupObject = null;
				entryObject = null;
			}
			entries = null;
			feed = null;
			json = null;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return videos;
	}
	/**
	 * Get video detail
	 * 
	 * @param url
	 * @return json string
	 */
	public static Video getVideoDetail(String videoId) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos/");
		sb.append(videoId);
		sb.append("?v=2");
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
			
		} catch (Exception ex) {
			Log.e("getVideoDetail", ex.toString());
		}
		return getVideoDetail(is);
	}
	
	public static Video getVideoDetail(InputStream is){
		Video video = new Video();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
				JSONObject entryObject = json.getJSONObject("entry");
				JSONObject groupObject = entryObject.getJSONObject("media$group");
				JSONObject descriptionObject = groupObject.getJSONObject("media$description");
				JSONObject titleObject = groupObject.getJSONObject("media$title");
				JSONObject idObject = groupObject.getJSONObject("yt$videoid");
								
				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = descriptionObject.getString("$t");
				String uri = GdataURL + "/videos/" + id + "?v=2&alt=json";
								
				video.setNameVideo(title);
				video.setDescribes(description);
				video.setUri(uri);
				
				
				descriptionObject = null;
				titleObject = null;
				idObject = null;
				groupObject = null;
				entryObject = null;
			
			json = null;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return video;
	}

}
