package com.fsotv.utils;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsotv.R;
import com.fsotv.dto.ChannelEntry;
import com.fsotv.dto.CommentEntry;
import com.fsotv.dto.VideoEntry;

/**
 * YouTube Helper class. Allow: + Get channels by type + Get video by category,
 * channelId + Get channel detail + Get video detail + Get comments by videoId +
 * Login + Add comment
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

	private final String TAG = "YouTube";

	// YouTube Key
	private final String API_KEY = "AIzaSyCNvmKz77Wtg7QEgS8Bad3br2pN4k_tmFs";
	private final String CLIENT_ID = "113980155278.apps.googleusercontent.com";
	private final String REDIRECT_URI = "http://localhost/oauth2callback";// "urn:ietf:wg:oauth:2.0:oob";
	private final String RESPONSE_TYPE = "code";
	private final String SCOPE = "https://gdata.youtube.com";

	private final String GdataURL = "http://gdata.youtube.com/feeds/api";
	private final String GdataURLs = "https://gdata.youtube.com/feeds/api";
	private final String CategoryURL = "-/%7Bhttp%3A%2F%2Fgdata.youtube.com%2Fschemas%2F2007%2Fcategories.cat%7D";

	private Activity activity;
	private ProgressDialog mProgressDlg;
	private YtListener mListener;
	private YtSession mSession;
	private String accessToken;
	private String refreshToken;
	private Long accessExpires;

	public YouTubeHelper() {

	}

	public YouTubeHelper(Activity context, SharedPreferences sharedPref) {
		this.activity = context;
		this.mSession = new YtSession(sharedPref);
		mProgressDlg = new ProgressDialog(context);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		// Check access token
		checkAccessToken();
	}

	public void setListener(YtListener listener) {
		mListener = listener;
	}

	public boolean hasAccessToken() {
		boolean valid = false;
		if (accessToken != null && refreshToken != null) {
			valid = true;
		}

		return valid;
	}

	public void resetAccessToken() {
		accessToken = null;
		accessExpires = 0L;
		
		mSession.resetAccessToken();
	}

	/**
	 * Login to twitter
	 */
	public void authorize() {
		Log.i(TAG, "Start authorize...");

		StringBuilder sb = new StringBuilder(); // Build authenticate URL
		sb.append("https://accounts.google.com/o/oauth2/auth?client_id=");
		sb.append(CLIENT_ID);
		sb.append("&redirect_uri=");
		sb.append(REDIRECT_URI);
		sb.append("&scope=");
		sb.append(SCOPE);
		sb.append("&response_type=");
		sb.append(RESPONSE_TYPE);

		showLoginDialog(sb.toString());
	
		Log.i(TAG, "End authorize...");
	}

	/**
	 * Add comment after logged
	 * 
	 * @param comment
	 */
	public void addComment(final String videoId, final String comment) {
		Log.i(TAG, "Start add comment...");
		new Thread() {
			@Override
			public void run() {
				try {
					// Build comment
					StringBuilder sbComment = new StringBuilder();
					sbComment
							.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					sbComment
							.append("<entry xmlns=\"http://www.w3.org/2005/Atom\"");
					sbComment
							.append(" xmlns:yt=\"http://gdata.youtube.com/schemas/2007\"><content>");
					sbComment.append(comment);
					sbComment.append("</content>");
					sbComment.append("</entry>");
					String mComment = sbComment.toString();
					
					StringBuilder sbUrl = new StringBuilder(); // Build URL
					sbUrl.append(GdataURL);
					sbUrl.append("/videos/");
					sbUrl.append(videoId);
					sbUrl.append("/comments");
					String mUrl = sbUrl.toString();

					List<NameValuePair> headers = new ArrayList<NameValuePair>();
					headers.add(new BasicNameValuePair("Content-Type",
							"application/atom+xml"));
					headers.add(new BasicNameValuePair("Authorization",
							"Bearer " + accessToken));
					headers.add(new BasicNameValuePair("GData-Version", 2 + ""));
					headers.add(new BasicNameValuePair("X-GData-Key", "key="
							+ API_KEY));

					StringEntity se = new StringEntity(mComment);
					int status = WebHelper.execute(mUrl, WebHelper.PostType.POST, headers, se);
					
					if(status == 200 || status == 201) // OK
						mHandler.sendMessage(mHandler.obtainMessage(2, 0, 0));
					else{ 
						mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0, "status:" + status));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}
			}
		}.start();
		Log.i(TAG, "End add comment");
	}

	/**
	 * Check access token was stored
	 */
	private void checkAccessToken() {
		// Check access token
		accessToken = mSession.getAccessToken();
		refreshToken = mSession.getRefreshToken();
		accessExpires = mSession.getAccessExpire();
	}

	private void refreshAccessToken(){
		Log.i(TAG, "Start refresh access token...");

		mProgressDlg.setMessage("Refresh login infor ...");
		mProgressDlg.show();
		
		resetAccessToken();
		
		new Thread() {
			@Override
			public void run() {
				try {
					List<NameValuePair> headers = new ArrayList<NameValuePair>();
					headers.add(new BasicNameValuePair("Content-Type",
							"application/x-www-form-urlencoded"));

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("client_id", CLIENT_ID));
					params.add(new BasicNameValuePair("refresh_token",
							refreshToken));
					params.add(new BasicNameValuePair("grant_type",
							"refresh_token"));

					InputStream is = WebHelper.getStream(
							"https://accounts.google.com/o/oauth2/token",
							WebHelper.PostType.POST, headers, params);
					JSONObject json = JsonHelper.getJSONFromStream(is);
					String token = null;
					Long expires = 0L;
					if (!json.isNull("access_token")) {
						token = json.getString("access_token");
					}
					if (!json.isNull("expires_in")) {
						expires = json.getLong("expires_in");
					}
					if (token != null && expires != 0) {
						// Set access token
						accessToken = token;
						accessExpires = expires;

						// Store access token
						mSession.storeAccessToken(accessToken, refreshToken,
								accessExpires);

						mHandler.sendMessage(mHandler.obtainMessage(1, 0, 0));
					}else{
						mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
								"Can't get access token"));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}

			}
		}.start();
		Log.i(TAG, "End refresh access token");
	}
	
	/**
	 * Validate access token is valid
	 */
	public void validateAccessToken() {
		Log.i(TAG, "Start validate access token...");
		new Thread() {
			@Override
			public void run() {
				StringBuilder sb = new StringBuilder();
				sb.append("https://www.googleapis.com/oauth2/v1/tokeninfo?");
				sb.append(accessToken);
				String url = sb.toString();
				try {
					InputStream is = WebHelper.getStream(url,
							WebHelper.PostType.GET, null, null);
					JSONObject json = JsonHelper.getJSONFromStream(is);
					Long expires = 0L;
					if (!json.isNull("expires_in")) {
						expires = json.getLong("expires_in");
					}
					if (expires > 0) {
						mHandler.sendMessage(mHandler.obtainMessage(5, 0, 0,
								sb.toString()));
					} else {
						mHandler.sendMessage(mHandler.obtainMessage(6, 0, 0,
								sb.toString()));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}
			}
		}.start();
		Log.i(TAG, "End validate access token");
	}

	/**
	 * Get access token
	 * 
	 * @param callbackUrl
	 */
	private void processToken(String callbackUrl) {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();

		Log.i(TAG, "Start process token...");

		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				try {
					List<NameValuePair> headers = new ArrayList<NameValuePair>();
					headers.add(new BasicNameValuePair("Content-Type",
							"application/x-www-form-urlencoded"));

					List<NameValuePair> params = new ArrayList<NameValuePair>();
					params.add(new BasicNameValuePair("code", verifier));
					params.add(new BasicNameValuePair("client_id", CLIENT_ID));
					params.add(new BasicNameValuePair("redirect_uri",
							REDIRECT_URI));
					params.add(new BasicNameValuePair("grant_type",
							"authorization_code"));

					InputStream is = WebHelper.getStream(
							"https://accounts.google.com/o/oauth2/token",
							WebHelper.PostType.POST, headers, params);
					JSONObject json = JsonHelper.getJSONFromStream(is);
					String token = null;
					String refresh = null;
					Long expires = 0L;
					if (!json.isNull("access_token")) {
						token = json.getString("access_token");
					}
					if (!json.isNull("refresh_token")) {
						refresh = json.getString("refresh_token");
					}
					if (!json.isNull("expires_in")) {
						expires = json.getLong("expires_in");
					}
					if (token != null && expires != 0) {
						// Set access token
						accessToken = token;
						refreshToken = refresh;
						accessExpires = expires;

						// Store access token
						mSession.storeAccessToken(accessToken, refreshToken,
								accessExpires);

						mHandler.sendMessage(mHandler.obtainMessage(1, 0, 0));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}

			}
		}.start();
		Log.i(TAG, "End process token");
	}

	/**
	 * Get oauth_verifier that stored in callback url
	 * 
	 * @param callbackUrl
	 * @return
	 */
	private String getVerifier(String callbackUrl) {
		String verifier = "";

		try {
			Uri uri = Uri.parse(callbackUrl);
			verifier = uri.getQueryParameter("code");
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0, e.getMessage()));
		}

		return verifier;
	}

	/**
	 * Show login dialog
	 * 
	 * @param url
	 */
	private void showLoginDialog(String url) {
		final YtListener listener = new YtListener() {
			public void onComplete(String value) {
				processToken(value);
			}

			public void onError(String value) {
				mListener.onError(value);
			}
		};

		new YouTubeDialog(activity, url, listener).show();
	}

	/**
	 * Hander <br>
	 * What: 1-login complete; 2-comment complete; 3-show login; 4-error;
	 * 5-access token valid; 6-access token invalid <br>
	 * obj: message, can be an normal message or an status code response(ex: 'status:403')
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDlg.dismiss();
			if (msg.what == 1) {
				mListener.onComplete("login");
			} else if (msg.what == 2) {
				mListener.onComplete("comment");
			} else if (msg.what == 3) {
				showLoginDialog((String) msg.obj);
			} else if (msg.what == 4) {
				String error = (String) msg.obj;
				mListener.onError(error == null ? "" : error);
			} else if (msg.what == 5) {
				mListener.onComplete("validate");
			} else if (msg.what == 6) {
				refreshAccessToken();
			}
		}
	};

	/**
	 * Interface to handle complete and error event
	 * 
	 * @author ChungPV1
	 * 
	 */
	public interface YtListener {
		public void onComplete(String value);

		public void onError(String value);
	}

	/**
	 * Store twitter authenticate key and serect key into preference
	 * 
	 * @author ChungPV1
	 * 
	 */
	class YtSession {
		private SharedPreferences sharedPref;
		private Editor editor;
		/**
		 * Declare access token and expire: status for secure access to YouTube
		 * API
		 */
		private final String YOUTUBE_ACCESS_TOKEN = "youtube_access_token";
		private final String YOUTUBE_REFRESH_TOKEN = "youtube_refresh_token";
		private final String YOUTUBE_ACCESS_EXPIRE = "youtube_access_expire";

		public YtSession(SharedPreferences sharedPref) {
			this.sharedPref = sharedPref;
			editor = sharedPref.edit();
		}

		public void storeAccessToken(String accessToken, String refreshToken,
				Long accessExpire) {
			editor.putString(YOUTUBE_ACCESS_TOKEN, accessToken);
			editor.putString(YOUTUBE_REFRESH_TOKEN, refreshToken);
			editor.putLong(YOUTUBE_ACCESS_EXPIRE, accessExpire);
			editor.commit();
		}

		public void resetAccessToken() {
			editor.putString(YOUTUBE_ACCESS_TOKEN, null);
			editor.putString(YOUTUBE_REFRESH_TOKEN, null);
			editor.putString(YOUTUBE_ACCESS_EXPIRE, null);

			editor.commit();
		}

		public String getAccessToken() {
			String token = sharedPref.getString(YOUTUBE_ACCESS_TOKEN, null);

			return token;
		}

		public String getRefreshToken() {
			String token = sharedPref.getString(YOUTUBE_REFRESH_TOKEN, null);

			return token;
		}

		public Long getAccessExpire() {
			Long token = sharedPref.getLong(YOUTUBE_ACCESS_EXPIRE, 0);

			return token;
		}
	}

	/**
	 * Create dialog that contain webview to load twitter
	 * 
	 * @author ChungPV1
	 * 
	 */
	class YouTubeDialog extends Dialog {
		private String mUrl;
		private YtListener mDialogListener;
		private ProgressDialog mSpinner;
		private LinearLayout mContent;
		private WebView mWebView;
		private TextView mTitle;
		private boolean progressDialogRunning = false;

		public YouTubeDialog(Context context, String url, YtListener listener) {
			super(context);

			mUrl = url;
			mDialogListener = listener;

			Log.i(TAG, url);
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			mSpinner = new ProgressDialog(getContext());

			mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
			mSpinner.setMessage("Loading...");

			mContent = new LinearLayout(getContext());
			mContent.setOrientation(LinearLayout.VERTICAL);

			setUpTitle();
			setUpWebView();

			Display display = getWindow().getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			int height = display.getHeight();
			
			addContentView(mContent, new FrameLayout.LayoutParams(
					width, height));
		}

		private void setUpTitle() {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			Drawable icon = getContext().getResources().getDrawable(
					R.drawable.youtube32);
			mTitle = new TextView(getContext());
			mTitle.setText("YouTube");
			mTitle.setTextColor(Color.WHITE);
			mTitle.setTypeface(Typeface.DEFAULT_BOLD);
			mTitle.setBackgroundColor(0xFFbbd7e9);
			mTitle.setPadding(4 + 2, 4, 4, 4);
			mTitle.setCompoundDrawablePadding(4 + 2);
			mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null,
					null);
			mContent.addView(mTitle);
		}

		private void setUpWebView() {
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			
			mWebView = new WebView(getContext());
			mWebView.setWebViewClient(new YouTubeDialog.YouTubeWebViewClient());
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.setLayoutParams(lp);
			mWebView.loadUrl(mUrl);

			mContent.addView(mWebView);
		}

		private class YouTubeWebViewClient extends WebViewClient {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				Log.i(TAG, url);
				if (url.startsWith(REDIRECT_URI)) {
					mDialogListener.onComplete(url);
					YouTubeDialog.this.dismiss();
				} else {
					view.loadUrl(url);
				}
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				YouTubeDialog.this.dismiss();

				mDialogListener.onError(description);
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
				mSpinner.show();
				progressDialogRunning = true;
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				String title = mWebView.getTitle();
				if (title != null && title.length() > 0) {
					mTitle.setText(title);
				}
				progressDialogRunning = false;
				mSpinner.dismiss();
			}

		}

		@Override
		protected void onStop() {
			progressDialogRunning = false;
			super.onStop();
		}

		public void onBackPressed() {
			if (!progressDialogRunning) {
				YouTubeDialog.this.dismiss();
			}
		}
	}

	/**
	 * Get Channels by user type
	 * 
	 * @param userType
	 * @return
	 */
	public List<ChannelEntry> getChannels(String userType, String orderBy,
			int maxResult, int startIndex, String time) {

		Log.i("YouTubeHelper", "Start getChannels()");

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
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);
		} catch (Exception ex) {
			Log.e("getChannels", ex.toString());
		}
		channels = getChannelsByStream(is);

		Log.i("YouTubeHelper", "Start getChannels()");

		return channels;
	}

	/**
	 * Get channels by stream
	 * 
	 * @param inputStream
	 * @return
	 */
	public List<ChannelEntry> getChannelsByStream(InputStream is) {
		List<ChannelEntry> channels = new ArrayList<ChannelEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			if (feed.isNull("entry")) {
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
	 * Get videos in channel
	 * 
	 * @param channelId
	 * @param orderBy
	 * @param maxResult
	 * @param startIndex
	 * @param keyword
	 * @param time
	 * @return
	 */
	public List<VideoEntry> getVideosInChannel(String channelId,
			String orderBy, int maxResult, int startIndex, String keyword,
			String time) {
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
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);
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
	public List<VideoEntry> getVideosInCategory(String category,
			String orderBy, int maxResult, int startIndex, String keyword,
			String time) {
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
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);
		} catch (Exception ex) {
			Log.e("getVideosInCategory", ex.toString());
		}
		videos = getVideosByStream(is);
		return videos;
	}

	/**
	 * Get videos by stream
	 * 
	 * @param is
	 * @return
	 */
	public List<VideoEntry> getVideosByStream(InputStream is) {
		List<VideoEntry> videos = new ArrayList<VideoEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			if (feed.isNull("entry")) {
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
				JSONObject ratingObject = null;
				// if orderBy=published->rating is null
				if (!entryObject.isNull("yt$rating")) {
					ratingObject = entryObject
							.getJSONObject("yt$rating");
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
				JSONArray authors = entryObject.getJSONArray("author");
				
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
				if (statisticsObject != null) {
					viewCount = statisticsObject.getInt("viewCount");
				}
				int favoriteCount = -1;
				if (ratingObject != null) {
					favoriteCount = ratingObject.getInt("numLikes");
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
				String author = "";
				if (authors.length() > 0) {
					author = authors.getJSONObject(0).getJSONObject("name").getString("$t");
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
				video.setAuthor(author);
				
				videos.add(video);

				authors = null;
				thumbnails = null;
				ratingObject = null;
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
	 * 
	 * @param videoId
	 * @param maxResult
	 * @param startIndex
	 * @return
	 */
	public List<CommentEntry> getComments(String videoId, int maxResult,
			int startIndex) {
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
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);
		} catch (Exception ex) {
			Log.e("getComments", ex.toString());
		}
		comments = getCommentsByStream(is);
		return comments;
	}

	/**
	 * Get comments by stream
	 * 
	 * @param is
	 * @return
	 */
	public List<CommentEntry> getCommentsByStream(InputStream is) {
		List<CommentEntry> comments = new ArrayList<CommentEntry>();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject feed = json.getJSONObject("feed");
			if (feed.isNull("entry")) {
				return comments;
			}
			JSONArray entries = feed.getJSONArray("entry");
			for (int i = 0; i < entries.length(); i++) {
				JSONObject entryObject = entries.getJSONObject(i);
				JSONObject titleObject = entryObject.getJSONObject("title");
				JSONObject publishedObject = entryObject
						.getJSONObject("published");
				JSONObject contentObject = entryObject.getJSONObject("content");
				JSONArray authors = entryObject.getJSONArray("author");
				
				String title = titleObject.getString("$t");
				String published = publishedObject.getString("$t");
				String content = contentObject.getString("$t");
				String author = "";
				if (authors.length() > 0) {
					author = authors.getJSONObject(0).getJSONObject("name").getString("$t");
				}
				
				CommentEntry comment = new CommentEntry();
				comment.setTitle(title);
				comment.setContent(content);
				comment.setPublished(published);
				comment.setAuthor(author);
				
				comments.add(comment);

				authors = null;
				titleObject = null;
				contentObject = null;
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

	/**
	 * Get channel detail by channel id
	 * 
	 * @param channelId
	 * @return
	 */
	public ChannelEntry getChannelDetail(String channelId) {
		ChannelEntry channel = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/channels/");
		sb.append(channelId);
		sb.append("?v=2&alt=json");
		String newUrl = sb.toString();
		try {
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);
		} catch (Exception ex) {
			Log.e("getChannelDetail", ex.toString());
		}
		channel = getChannelByStream(is);
		return channel;
	}

	/**
	 * Get channels by stream
	 * 
	 * @param is
	 * @return
	 */
	public ChannelEntry getChannelByStream(InputStream is) {
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
	public VideoEntry getVideoDetail(String videoId) {
		VideoEntry video = null;
		InputStream is = null;
		StringBuilder sb = new StringBuilder();
		sb.append(GdataURL);
		sb.append("/videos/");
		sb.append(videoId);
		sb.append("?v=2&alt=json");
		String newUrl = sb.toString();
		try {
			is = WebHelper
					.getStream(newUrl, WebHelper.PostType.GET, null, null);

		} catch (Exception ex) {
			Log.e("getVideoDetail", ex.toString());
		}
		video = getVideoByStream(is);
		return video;
	}

	/**
	 * Get video detail by stream
	 * 
	 * @param is
	 * @return
	 */
	public VideoEntry getVideoByStream(InputStream is) {
		VideoEntry video = new VideoEntry();
		try {
			JSONObject json = JsonHelper.getJSONFromStream(is);
			JSONObject entryObject = json.getJSONObject("entry");
			JSONObject statisticsObject = entryObject
					.getJSONObject("yt$statistics");
			JSONObject ratingObject = entryObject.getJSONObject("yt$rating");
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
			JSONArray authors = entryObject.getJSONArray("author");
			
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
			int favoriteCount = ratingObject.getInt("numLikes");
			String author = "";
			if (authors.length() > 0) {
				author = authors.getJSONObject(0).getJSONObject("name").getString("$t");
			}
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
			video.setAuthor(author);
			
			authors = null;
			links = null;
			contents = null;
			thumbnails = null;
			ratingObject = null;
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

}
