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
import com.fsotv.dto.Channel;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.dto.Video;
import com.fsotv.dto.Video;
import com.fsotv.dto.VideoEntry;

public class YouTubeHelper {
	// Constants
	public final static String CATEGORY_EDUCATION = "Education";
	public final static String CATEGORY_COMEDY = "Comedy";
	public final static String CATEGORY_ENTERTAIMENT = "Entertainment";
	public final static String CATEGORY_MUSIC = "Music";
	public final static String CATEGORY_SPORTS = "Sports";
	public final static String CATEGORY_FILM = "Film";
	public final static String CATEGORY_TRAVEL = "Travel";
	public final static String CATEGORY_NEWS = "News";
	public final static String USER_TYPE_COMEDIANS = "Comedians";
	public final static String USER_TYPE_DIRECTORS = "Directors";
	public final static String USER_TYPE_MUSICIANS = "Musicians";
	public final static String USER_TYPE_POLITICIANS = "Politicians";
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
	public static int MAX_RESULT = 15;
	/**
	 * Get Channels by user type
	 * @param userType
	 * @return
	 */
	public static List<ChannelEntry> getChannels(String userType) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/channelstandardfeeds/most_viewed_");
		sb.append(userType);
		sb.append("?v=2&orderby=viewCount&alt=json&max-results=");
		sb.append(MAX_RESULT);
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getChannels", ex.toString());
		}
		return getChannelsByStream(is);
	}
	
	public static List<ChannelEntry> getChannelsByStream(InputStream is){
		List<ChannelEntry> channels = new ArrayList<ChannelEntry>();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for(int i = 0; i < entries.length(); i++){
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject idObject = entryObject.getJSONObject("yt$channelId");
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject summaryObject = entryObject.getJSONObject("summary");
				JSONObject statisticsObject = entryObject.getJSONObject("yt$channelStatistics");
				JSONObject groupObject = entryObject.getJSONObject("media$group");
				JSONArray thumbnails = groupObject.getJSONArray("media$thumbnail");
				
				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = summaryObject.getString("$t");
				String link = "";
				String image = "";
				if(thumbnails.length()>0){
					image = thumbnails.getJSONObject(0).getString("url");
				}
				int commentCount = statisticsObject.getInt("commentCount");
				int videoCount = statisticsObject.getInt("videoCount");
				int viewCount = statisticsObject.getInt("viewCount");
				
				ChannelEntry channel = new ChannelEntry();
				channel.setId(id);
				channel.setIdReal(id);
				channel.setTitle(title);
				channel.setDescription(description);
				channel.setLink(link);
				channel.setImage(image);
				channel.setCommentCount(commentCount);
				channel.setVideoCount(videoCount);
				channel.setViewCount(viewCount);
				channels.add(channel);
				
				thumbnails = null;
				groupObject = null;
				statisticsObject = null;
				summaryObject = null;
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
	 * Get videos in channel
	 * 
	 */
	public static List<VideoEntry> getVideosInChannel(String channelId) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/users/");
		sb.append(channelId);
		sb.append("/uploads");
		sb.append("?v=2&orderby=viewCount&alt=json&format=6&max-results=");
		sb.append(MAX_RESULT);
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInChannel", ex.toString());
		}
		return getVideosByStream(is);
	}
	public static List<VideoEntry> getVideosInCategory(String category) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos/");
		sb.append(CategoryURL);
		sb.append(category);
		sb.append("?v=2&orderby=viewCount&alt=json&format=6&max-results=");
		sb.append(MAX_RESULT);
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInCategory", ex.toString());
		}
		return getVideosByStream(is);
	}
	public static List<VideoEntry> getVideosByStream(InputStream is){
		List<VideoEntry> videos = new ArrayList<VideoEntry>();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for(int i = 0; i < entries.length(); i++){
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject statisticsObject = entryObject.getJSONObject("yt$statistics");
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject groupObject = entryObject.getJSONObject("media$group");
				JSONObject descriptionObject = groupObject.getJSONObject("media$description");
				JSONObject idObject = groupObject.getJSONObject("yt$videoid");
				JSONArray thumbnails = groupObject.getJSONArray("media$thumbnail");
				
				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = descriptionObject.getString("$t");
				String link = "";
				String image = "";
				if(thumbnails.length()>0){
					image = thumbnails.getJSONObject(0).getString("url");
				}
				int viewCount = statisticsObject.getInt("viewCount");
				int favoriteCount = statisticsObject.getInt("favoriteCount");
				
				VideoEntry video = new VideoEntry();
				video.setId(id);
				video.setTitle(title);
				video.setDescription(description);
				video.setLink(link);
				video.setImage(image);
				video.setViewCount(viewCount);
				video.setFavoriteCount(favoriteCount);
				videos.add(video);
				
				thumbnails = null;
				statisticsObject = null;
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
	
	public static ChannelEntry getChannelDetail(String channelId) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/channels/");
		sb.append(channelId);
		sb.append("?v=2&alt=json");
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
			
		} catch (Exception ex) {
			Log.e("getChannelDetail", ex.toString());
		}
		return getChannelByStream(is);
	}
	
	public static ChannelEntry getChannelByStream(InputStream is){
		ChannelEntry channel = new ChannelEntry();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject entryObject = json.getJSONObject("entry");
			JSONObject idObject = entryObject.getJSONObject("yt$channelId");
			JSONObject titleObject = entryObject.getJSONObject("title");
			JSONObject summaryObject = entryObject.getJSONObject("summary");
			JSONObject statisticsObject = entryObject.getJSONObject("yt$channelStatistics");
			JSONArray thumbnails = entryObject.getJSONArray("media$thumbnail");
			
			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = summaryObject.getString("$t");
			String link = "";
			String image = "";
			if(thumbnails.length()>0){
				image = thumbnails.getJSONObject(0).getString("url");
			}
			int subscriberCount = statisticsObject.getInt("subscriberCount");
			int viewCount = statisticsObject.getInt("viewCount");
			
			channel.setId(id);
			channel.setIdReal(id);
			channel.setTitle(title);
			channel.setDescription(description);
			channel.setLink(link);
			channel.setImage(image);
			channel.setViewCount(viewCount);
			channel.setSubscriberCount(subscriberCount);
			
			thumbnails = null;
			statisticsObject = null;
			summaryObject = null;
			titleObject = null;
			idObject = null;
			entryObject = null;
			json = null;
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return channel;
	}
	
	/**
	 * Get video detail
	 * 
	 * @param url
	 * @return json string
	 */
	public static VideoEntry getVideoDetail(String videoId) {
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos/");
		sb.append(videoId);
		sb.append("?v=2&alt=json");
		String newUrl = sb.toString();
		try {
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
			
		} catch (Exception ex) {
			Log.e("getVideoDetail", ex.toString());
		}
		return getVideoByStream(is);
	}
		
	public static VideoEntry getVideoByStream(InputStream is){
		VideoEntry video = new VideoEntry();
		try{
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject entryObject = json.getJSONObject("entry");
			JSONObject statisticsObject = entryObject.getJSONObject("yt$statistics");
			JSONObject titleObject = entryObject.getJSONObject("title");
			JSONObject groupObject = entryObject.getJSONObject("media$group");
			JSONObject descriptionObject = groupObject.getJSONObject("media$description");
			JSONObject idObject = groupObject.getJSONObject("yt$videoid");
			JSONArray contents = groupObject.getJSONArray("media$content");
			JSONArray thumbnails = groupObject.getJSONArray("media$thumbnail");
			
			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = descriptionObject.getString("$t");
			String link = "";
			int duration = 0;
			for(int i = 0; i < contents.length(); i++){
				JSONObject contentObject = contents.getJSONObject(i);
				if(contentObject.getInt("yt$format")==6){
					link = contentObject.getString("url");
					duration = contentObject.getInt("duration");
					break;
				}
			}
			String image = "";
			if(thumbnails.length()>0){
				image = thumbnails.getJSONObject(0).getString("url");
			}
			int viewCount = statisticsObject.getInt("viewCount");
			int favoriteCount = statisticsObject.getInt("favoriteCount");
			
			video.setId(id);
			video.setTitle(title);
			video.setDescription(description);
			video.setLink(link);
			video.setImage(image);
			video.setDuration(duration);
			video.setViewCount(viewCount);
			video.setFavoriteCount(favoriteCount);
			
			contents = null;
			thumbnails = null;
			statisticsObject = null;
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
