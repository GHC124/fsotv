package com.fsotv.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

import com.fsotv.dto.ChannelEntry;
import com.fsotv.dto.CommentEntry;
import com.fsotv.dto.VideoEntry;
/**
 * YouTube Helper class. Allow:
 * + Get channels by type
 * + Get video by category, channelId
 * + Get channel detail
 * + Get video detail
 * + Get comments by videoId
 * 
 */
public class YouTubeHelper {
 
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
	public final static String TIME_TODAY = "today";
	public final static String TIME_THIS_WEEK = "this_week";
	public final static String TIME_THIS_MONTH = "this_month";
	public final static String TIME_ALL_TIME = "all_time";

	private final static String InforURL = "http://www.youtube.com/get_video_info?&video_id=";
	private final static String GdataURL = "http://gdata.youtube.com/feeds/api";
	private final static String CategoryURL = "-/%7Bhttp%3A%2F%2Fgdata.youtube.com%2Fschemas%2F2007%2Fcategories.cat%7D";

	/**
	 * Get Channels by user type
	 * 
	 * @param userType
	 * @return
	 */
	public static List<ChannelEntry> getChannels(String userType,
			String orderBy, int maxResult, int startIndex, String time) {
		
		Log.i("YouTubeHelper","Start getChannels()");
		
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
		if (time != null && !time.isEmpty()) {
			sb.append("&time=");
			sb.append(time);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);
		} catch (Exception ex) {
			Log.e("getChannels", ex.toString());
		}
		channels = getChannelsByStream(is);
		
		Log.i("YouTubeHelper","Start getChannels()");
		
		return channels;
	}
	
	/**
	 * 
	 * @param inputStream
	 * @return
	 */
	public static List<ChannelEntry> getChannelsByStream(InputStream inputStream) {
		List<ChannelEntry> channels = new ArrayList<ChannelEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(inputStream);
			JSONObject feed = json.getJSONObject("feed");
			if(feed.isNull("entry")){
				return channels;
			}
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject idObject = entryObject.getJSONObject("yt$channelId");
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject summaryObject = entryObject.getJSONObject("summary");
				JSONObject updatedObject = entryObject.getJSONObject("updated");
				JSONObject statisticsObject = entryObject
						.getJSONObject("yt$channelStatistics");
				JSONObject groupObject = entryObject
						.getJSONObject("media$group");
				JSONArray thumbnails = groupObject
						.getJSONArray("media$thumbnail");

				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = summaryObject.getString("$t");
				String updated = updatedObject.getString("$t");
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
				channel.setUpdated(updated);
				channel.setImage(image);
				channel.setCommentCount(commentCount);
				channel.setVideoCount(videoCount);
				channel.setViewCount(viewCount);
				channels.add(channel);

				thumbnails = null;
				groupObject = null;
				statisticsObject = null;
				updatedObject = null;
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
	 * 
	 * @param channelId
	 * @param orderBy
	 * @param maxResult
	 * @param startIndex
	 * @param keyword
	 * @param time
	 * @return
	 */
	public static List<VideoEntry> getVideosInChannel(String channelId,
			String orderBy, int maxResult, int startIndex, String keyword, String time) {
		List<VideoEntry> videos = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/users/");
		sb.append(channelId);
		sb.append("/uploads");
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
		if (time != null && !time.isEmpty()) {
			sb.append("&time=");
			sb.append(time);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInChannel", ex.toString());
		}
		videos = getVideosByStream(is);
		return videos;
	}

	/**
	 * Get videos in specific category
	 * 
	 * @param category
	 * @param orderBy
	 * @param maxResult
	 * @param startIndex
	 * @param keyword
	 * @return
	 */
	public static List<VideoEntry> getVideosInCategory(String category,
			String orderBy, int maxResult, int startIndex, String keyword, String time) {
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
		if (time != null && !time.isEmpty()) {
			sb.append("&time=");
			sb.append(time);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);
		} catch (Exception ex) {
			Log.e("getVideosInCategory", ex.toString());
		}
		videos = getVideosByStream(is);
		return videos;
	}
	/**
	 * Get videos by stream
	 * @param is
	 * @return
	 */
	public static List<VideoEntry> getVideosByStream(InputStream is) {
		List<VideoEntry> videos = new ArrayList<VideoEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			if(feed.isNull("entry")){
				return videos;
			}
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject statisticsObject = null;
				// if orderBy=published->statistic is null
				if (!entryObject.isNull("yt$statistics")) {
					statisticsObject = entryObject
							.getJSONObject("yt$statistics");
				}
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject publishedObject = entryObject
						.getJSONObject("published");
				JSONObject updatedObject = entryObject.getJSONObject("updated");
				JSONObject groupObject = entryObject
						.getJSONObject("media$group");
				JSONObject descriptionObject = groupObject
						.getJSONObject("media$description");
				JSONObject idObject = groupObject.getJSONObject("yt$videoid");
				JSONArray thumbnails = groupObject
						.getJSONArray("media$thumbnail");
				JSONArray contents = groupObject.getJSONArray("media$content");

				String id = idObject.getString("$t");
				String title = titleObject.getString("$t");
				String description = descriptionObject.getString("$t");
				String published = publishedObject.getString("$t");
				String updated = updatedObject.getString("$t");
				String link = "";
				String image = "";
				if (thumbnails.length() > 0) {
					image = thumbnails.getJSONObject(0).getString("url");
				}
				int viewCount = -1;
				int favoriteCount = -1;
				if (statisticsObject != null) {
					viewCount = statisticsObject.getInt("viewCount");
					favoriteCount = statisticsObject.getInt("favoriteCount");
				}

				long duration = 0;
				for (int j = 0; j < contents.length(); j++) {
					JSONObject contentObject = contents.getJSONObject(j);
					if (contentObject.getInt("yt$format") == 6) {
						link = contentObject.getString("url");
						duration = contentObject.getLong("duration");
						contentObject = null;
						break;
					}
					contentObject = null;
				}

				VideoEntry video = new VideoEntry();
				video.setId(id);
				video.setIdReal(id);
				video.setTitle(title);
				video.setDescription(description);
				video.setLink(link);
				video.setImage(image);
				video.setViewCount(viewCount);
				video.setFavoriteCount(favoriteCount);
				video.setDuration(duration);
				video.setPublished(published);
				video.setUpdated(updated);
				videos.add(video);

				thumbnails = null;
				statisticsObject = null;
				descriptionObject = null;
				titleObject = null;
				publishedObject = null;
				updatedObject = null;
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
	/**
	 * Get comments by videoId
	 * @param videoId
	 * @param maxResult
	 * @param startIndex
	 * @return
	 */
	public static List<CommentEntry> getComments(String videoId,
			int maxResult, int startIndex) {
		List<CommentEntry> comments = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos/");
		sb.append(videoId);
		sb.append("/comments");
		sb.append("?v=2&alt=json");
		if (maxResult > 0) {
			sb.append("&max-results=");
			sb.append(maxResult);
		}
		if (startIndex > 0) {
			sb.append("&start-index=");
			sb.append(startIndex);
		}
		String newUrl = sb.toString();
		try {
			newUrl = DataHelper.parseUrl(newUrl);
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);
		} catch (Exception ex) {
			Log.e("getComments", ex.toString());
		}
		comments = getCommentsByStream(is);
		return comments;
	}
	
	public static List<CommentEntry> getCommentsByStream(InputStream is) {
		List<CommentEntry> comments = new ArrayList<CommentEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			if(feed.isNull("entry")){
				return comments;
			}
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject publishedObject = entryObject
						.getJSONObject("published");
				JSONObject contentObject = entryObject.getJSONObject("content");

				String title = titleObject.getString("$t");
				String published = publishedObject.getString("$t");
				String content = contentObject.getString("$t");
				
				CommentEntry comment = new CommentEntry();
				comment.setTitle(title);
				comment.setContent(content);
				comment.setPublished(published);
				
				comments.add(comment);

				titleObject = null;
				contentObject  = null;
				publishedObject = null;
				entryObject = null;
			}
			entries = null;
			feed = null;
			json = null;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return comments;
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
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);
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
			JSONObject updatedObject = entryObject.getJSONObject("updated");
			JSONObject statisticsObject = entryObject
					.getJSONObject("yt$channelStatistics");
			JSONArray thumbnails = entryObject.getJSONArray("media$thumbnail");

			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = summaryObject.getString("$t");
			String updated = updatedObject.getString("$t");
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
			channel.setUpdated(updated);

			thumbnails = null;
			statisticsObject = null;
			updatedObject = null;
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
			is = WebHelper.GetStream(newUrl, WebHelper.PostType.GET);

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
			JSONArray links = entryObject.getJSONArray("link");
			JSONObject groupObject = entryObject.getJSONObject("media$group");
			JSONObject descriptionObject = groupObject
					.getJSONObject("media$description");
			JSONObject publishedObject = entryObject.getJSONObject("published");
			JSONObject updatedObject = entryObject.getJSONObject("updated");
			JSONObject idObject = groupObject.getJSONObject("yt$videoid");
			JSONArray contents = groupObject.getJSONArray("media$content");
			JSONArray thumbnails = groupObject.getJSONArray("media$thumbnail");

			String id = idObject.getString("$t");
			String title = titleObject.getString("$t");
			String description = descriptionObject.getString("$t");
			String published = publishedObject.getString("$t");
			String updated = updatedObject.getString("$t");
			String linkReal = "";
			for (int i = 0; i < links.length(); i++) {
				JSONObject linkObject = links.getJSONObject(i);
				if (linkObject.getString("rel").equals("alternate")
						&& linkObject.getString("type").equals("text/html")) {
					linkReal = linkObject.getString("href");
					linkObject = null;
					break;
				}
				linkObject = null;
			}
			String link = "";
			int duration = 0;
			for (int i = 0; i < contents.length(); i++) {
				JSONObject contentObject = contents.getJSONObject(i);
				if (contentObject.getInt("yt$format") == 6) {
					link = contentObject.getString("url");
					duration = contentObject.getInt("duration");
					contentObject = null;
					break;
				}
				contentObject = null;
			}
			String image = "";
			if (thumbnails.length() > 0) {
				image = thumbnails.getJSONObject(0).getString("url");
			}
			int viewCount = statisticsObject.getInt("viewCount");
			int favoriteCount = statisticsObject.getInt("favoriteCount");

			video.setId(id);
			video.setIdReal(id);
			video.setTitle(title);
			video.setDescription(description);
			video.setLink(link);
			video.setLinkReal(linkReal);
			video.setImage(image);
			video.setDuration(duration);
			video.setViewCount(viewCount);
			video.setFavoriteCount(favoriteCount);
			video.setPublished(published);
			video.setUpdated(updated);

			links = null;
			contents = null;
			thumbnails = null;
			statisticsObject = null;
			publishedObject = null;
			updatedObject = null;
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

	/**
	 * Calculate the YouTube URL to load the video. Includes retrieving a token
	 * that YouTube requires to play the video.
	 * 
	 * @param pYouTubeFmtQuality
	 *            quality of the video. 17=low, 18=high
	 * @param bFallback
	 *            whether to fallback to lower quality in case the supplied
	 *            quality is not available
	 * @param pYouTubeVideoId
	 *            the id of the video
	 * @return the url string that will retrieve the video
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws UnsupportedEncodingException
	 */
	public static String calculateYouTubeUrl(String pYouTubeFmtQuality,
			boolean pFallback, String pYouTubeVideoId) throws IOException,
			ClientProtocolException, UnsupportedEncodingException {

		String lUriStr = null;
		HttpClient lClient = new DefaultHttpClient();
		HttpGet lGetMethod = new HttpGet(InforURL + pYouTubeVideoId);
		HttpResponse lResp = null;
		lResp = lClient.execute(lGetMethod);
		ByteArrayOutputStream lBOS = new ByteArrayOutputStream();
		String lInfoStr = null;
		lResp.getEntity().writeTo(lBOS);
		lInfoStr = new String(lBOS.toString("UTF-8"));
		String[] lArgs = lInfoStr.split("&");
		Map<String, String> lArgMap = new HashMap<String, String>();
		for (int i = 0; i < lArgs.length; i++) {
			String[] lArgValStrArr = lArgs[i].split("=");
			if (lArgValStrArr != null) {
				if (lArgValStrArr.length >= 2) {
					lArgMap.put(lArgValStrArr[0],
							URLDecoder.decode(lArgValStrArr[1]));
				}
			}
		}

		// Find out the URI string from the parameters

		// Populate the list of formats for the video
		String lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
		ArrayList<Format> lFormats = new ArrayList<Format>();
		if (null != lFmtList) {
			String lFormatStrs[] = lFmtList.split(",");

			for (String lFormatStr : lFormatStrs) {
				Format lFormat = new Format(lFormatStr);
				lFormats.add(lFormat);
			}
		}

		// Populate the list of streams for the video
		String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
		if (null != lStreamList) {
			String lStreamStrs[] = lStreamList.split(",");
			ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
			for (String lStreamStr : lStreamStrs) {
				VideoStream lStream = new VideoStream(lStreamStr);
				lStreams.add(lStream);
			}

			// Search for the given format in the list of video formats
			// if it is there, select the corresponding stream
			// otherwise if fallback is requested, check for next lower format
			int lFormatId = Integer.parseInt(pYouTubeFmtQuality);

			Format lSearchFormat = new Format(lFormatId);
			while (!lFormats.contains(lSearchFormat) && pFallback) {
				int lOldId = lSearchFormat.getId();
				int lNewId = getSupportedFallbackId(lOldId);

				if (lOldId == lNewId) {
					break;
				}
				lSearchFormat = new Format(lNewId);
			}

			int lIndex = lFormats.indexOf(lSearchFormat);
			if (lIndex >= 0) {
				VideoStream lSearchStream = lStreams.get(lIndex);
				lUriStr = lSearchStream.getUrl();
			}

		}
		// Return the URI string. It may be null if the format (or a fallback
		// format if enabled)
		// is not found in the list of formats for the video
		return lUriStr;
	}

	public static int getSupportedFallbackId(int pOldId) {
		final int lSupportedFormatIds[] = { 13, // 3GPP (MPEG-4 encoded) Low
												// quality
				17, // 3GPP (MPEG-4 encoded) Medium quality
				18, // MP4 (H.264 encoded) Normal quality
				22, // MP4 (H.264 encoded) High quality
				37 // MP4 (H.264 encoded) High quality
		};
		int lFallbackId = pOldId;
		for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
			if (pOldId == lSupportedFormatIds[i] && i > 0) {
				lFallbackId = lSupportedFormatIds[i - 1];
			}
		}
		return lFallbackId;
	}

	static class VideoStream {

		protected String mUrl;

		/**
		 * Construct a video stream from one of the strings obtained from the
		 * "url_encoded_fmt_stream_map" parameter if the video_info
		 * 
		 * @param pStreamStr
		 *            - one of the strings from "url_encoded_fmt_stream_map"
		 */
		public VideoStream(String pStreamStr) {
			String[] lArgs = pStreamStr.split("&");
			Map<String, String> lArgMap = new HashMap<String, String>();
			for (int i = 0; i < lArgs.length; i++) {
				String[] lArgValStrArr = lArgs[i].split("=");
				if (lArgValStrArr != null) {
					if (lArgValStrArr.length >= 2) {
						lArgMap.put(lArgValStrArr[0], lArgValStrArr[1]);
					}
				}
			}
			mUrl = lArgMap.get("url");
		}

		public String getUrl() {
			return mUrl;
		}
	}

	static class Format {
		protected int mId;

		/**
		 * Construct this object from one of the strings in the "fmt_list"
		 * parameter
		 * 
		 * @param pFormatString
		 *            one of the comma separated strings in the "fmt_list"
		 *            parameter
		 */
		public Format(String pFormatString) {
			String lFormatVars[] = pFormatString.split("/");
			mId = Integer.parseInt(lFormatVars[0]);
		}

		/**
		 * Construct this object using a format id
		 * 
		 * @param pId
		 *            id of this format
		 */
		public Format(int pId) {
			this.mId = pId;
		}

		/**
		 * Retrieve the id of this format
		 * 
		 * @return the id
		 */
		public int getId() {
			return mId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object pObject) {
			if (!(pObject instanceof Format)) {
				return false;
			}
			return ((Format) pObject).mId == mId;
		}
	}
}
