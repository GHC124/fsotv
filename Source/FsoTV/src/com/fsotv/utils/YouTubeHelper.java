package com.fsotv.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.fsotv.dto.ChannelEntry;
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
	public final static String CHANNEL_MOST_VIEWED = "most_viewed";
	public final static String CHANNEL_MOST_SUBSCRIBED = "most_subscribed";
	public final static String ORDERING_PUBLISHED = "published";
	public final static String ORDERING_VIEWCOUNT = "viewCount";

	private final static String GdataURL = "http://gdata.youtube.com/feeds/api";
	private final static String CategoryURL = "-/%7Bhttp%3A%2F%2Fgdata.youtube.com%2Fschemas%2F2007%2Fcategories.cat%7D";

	/**
	 * Get Channels by user type
	 * 
	 * @param userType
	 * @return
	 */
	public static List<ChannelEntry> getChannels(String userType,
			String orderBy, int maxResult, int startIndex, String keyword) {
		List<ChannelEntry> channels = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/channelstandardfeeds/most_viewed");
		if (userType != null && !userType.isEmpty()) {
			sb.append("_");
			sb.append(userType);
		}
		sb.append("?v=2&alt=json");
		if (orderBy != null && !orderBy.isEmpty()) {
			sb.append("&orderby=");
			sb.append(orderBy);
		}
		if (maxResult > 0) {
			sb.append("&max-results=");
			sb.append(maxResult);
		}
		if (startIndex > 0) {
			sb.append("&start-index=");
			sb.append(startIndex);
		}
		if (keyword != null && !keyword.isEmpty()) {
			sb.append("&q=");
			try {
				keyword = URLEncoder.encode(keyword, "UTF-8").replaceAll("\\+",
						"%20");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(keyword);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getChannels", ex.toString());
		}
		channels = getChannelsByStream(is);
		return channels;
	}

	public static List<ChannelEntry> getChannelsByStream(InputStream is) {
		List<ChannelEntry> channels = new ArrayList<ChannelEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject idObject = entryObject.getJSONObject("yt$channelId");
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject summaryObject = entryObject.getJSONObject("summary");
				JSONObject statisticsObject = entryObject
						.getJSONObject("yt$channelStatistics");
				JSONObject groupObject = entryObject
						.getJSONObject("media$group");
				JSONArray thumbnails = groupObject
						.getJSONArray("media$thumbnail");

				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = summaryObject.getString("$t");
				String link = "";
				String image = "";
				if (thumbnails.length() > 0) {
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return channels;
	}

	/**
	 * Get videos in channel, order by published
	 * 
	 */
	public static List<VideoEntry> getVideosInChannel(String channelId,
			String orderBy, int maxResult, int startIndex, String keyword) {
		List<VideoEntry> videos = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/users/");
		sb.append(channelId);
		sb.append("/uploads");
		sb.append("?v=2&alt=json&format=6");
		if (orderBy != null && !orderBy.isEmpty()) {
			sb.append("&orderby=");
			sb.append(orderBy);
		}
		if (maxResult > 0) {
			sb.append("&max-results=");
			sb.append(maxResult);
		}
		if (startIndex > 0) {
			sb.append("&start-index=");
			sb.append(startIndex);
		}
		if (keyword != null && !keyword.isEmpty()) {
			sb.append("&q=");
			try {
				keyword = URLEncoder.encode(keyword, "UTF-8").replaceAll("\\+",
						"%20");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(keyword);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInChannel", ex.toString());
		}
		videos = getVideosByStream(is);
		return videos;
	}

	public static List<VideoEntry> getVideosInCategory(String category,
			String orderBy, int maxResult, int startIndex, String keyword) {
		List<VideoEntry> videos = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos");
		if (category != null && !category.isEmpty()) {
			sb.append("/");
			sb.append(CategoryURL);
			sb.append(category);
		}
		sb.append("?v=2&alt=json&format=6");
		if (orderBy != null && !orderBy.isEmpty()) {
			sb.append("&orderby=");
			sb.append(orderBy);
		}
		if (maxResult > 0) {
			sb.append("&max-results=");
			sb.append(maxResult);
		}
		if (startIndex > 0) {
			sb.append("&start-index=");
			sb.append(startIndex);
		}
		if (keyword != null && !keyword.isEmpty()) {
			sb.append("&q=");
			try {
				keyword = URLEncoder.encode(keyword, "UTF-8").replaceAll("\\+",
						"%20");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sb.append(keyword);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebRequest.GetStream(newUrl, WebRequest.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInCategory", ex.toString());
		}
		videos = getVideosByStream(is);
		return videos;
	}

	public static List<VideoEntry> getVideosByStream(InputStream is) {
		List<VideoEntry> videos = new ArrayList<VideoEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject statisticsObject = entryObject
						.getJSONObject("yt$statistics");
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject groupObject = entryObject
						.getJSONObject("media$group");
				JSONObject descriptionObject = groupObject
						.getJSONObject("media$description");
				JSONObject idObject = groupObject.getJSONObject("yt$videoid");
				JSONArray thumbnails = groupObject
						.getJSONArray("media$thumbnail");

				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = descriptionObject.getString("$t");
				String link = "";
				String image = "";
				if (thumbnails.length() > 0) {
					image = thumbnails.getJSONObject(0).getString("url");
				}
				int viewCount = statisticsObject.getInt("viewCount");
				int favoriteCount = statisticsObject.getInt("favoriteCount");

				VideoEntry video = new VideoEntry();
				video.setId(id);
				video.setIdReal(id);
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return videos;
	}

	public static ChannelEntry getChannelDetail(String channelId) {
		ChannelEntry channel = null;
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
		channel = getChannelByStream(is);
		return channel;
	}

	public static ChannelEntry getChannelByStream(InputStream is) {
		ChannelEntry channel = new ChannelEntry();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject entryObject = json.getJSONObject("entry");
			JSONObject idObject = entryObject.getJSONObject("yt$channelId");
			JSONObject titleObject = entryObject.getJSONObject("title");
			JSONObject summaryObject = entryObject.getJSONObject("summary");
			JSONObject statisticsObject = entryObject
					.getJSONObject("yt$channelStatistics");
			JSONArray thumbnails = entryObject.getJSONArray("media$thumbnail");

			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = summaryObject.getString("$t");
			String link = "";
			String image = "";
			if (thumbnails.length() > 0) {
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

		} catch (Exception e) {
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
		VideoEntry video = null;
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
		video = getVideoByStream(is);
		return video;
	}

	public static VideoEntry getVideoByStream(InputStream is) {
		VideoEntry video = new VideoEntry();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject entryObject = json.getJSONObject("entry");
			JSONObject statisticsObject = entryObject
					.getJSONObject("yt$statistics");
			JSONObject titleObject = entryObject.getJSONObject("title");
			JSONObject groupObject = entryObject.getJSONObject("media$group");
			JSONObject descriptionObject = groupObject
					.getJSONObject("media$description");
			JSONObject idObject = groupObject.getJSONObject("yt$videoid");
			JSONArray contents = groupObject.getJSONArray("media$content");
			JSONArray thumbnails = groupObject.getJSONArray("media$thumbnail");

			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = descriptionObject.getString("$t");
			String link = "";
			int duration = 0;
			for (int i = 0; i < contents.length(); i++) {
				JSONObject contentObject = contents.getJSONObject(i);
				if (contentObject.getInt("yt$format") == 6) {
					link = contentObject.getString("url");
					duration = contentObject.getInt("duration");
					break;
				}
			}
			String image = "";
			if (thumbnails.length() > 0) {
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

		} catch (Exception e) {
			e.printStackTrace();
		}

		return video;
	}

}
