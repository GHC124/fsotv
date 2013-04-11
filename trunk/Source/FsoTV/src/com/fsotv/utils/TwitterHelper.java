package com.fsotv.utils;

import java.net.URLDecoder;

import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
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
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fsotv.R;

public class TwitterHelper {
	private Twitter mTwitter;
	private TwitterSession mSession;
	private AccessToken mAccessToken;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	private OAuthProvider mHttpOauthprovider;
	private String mConsumerKey;
	private String mSecretKey;
	private ProgressDialog mProgressDlg;
	private TwDialogListener mListener;
	private Activity context;

	public static final String TWITTER_CONSUMER_KEY = "n1kQKdyywQf6awZqCVK6kw";
	public static final String TWITTER_CONSUMER_SECRET = "XwoUFNJhJrrRkBcHIhjhAHjfk0BfbKFrZTGzmCG8cQ";
	public static final String OAUTH_CALLBACK_SCHEME = "oauth";
	public static final String OAUTH_CALLBACK_HOST = "t4j";
	public static final String CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://"
			+ OAUTH_CALLBACK_HOST;
	private static final String TWITTER_ACCESS_TOKEN_URL = "http://api.twitter.com/oauth/access_token";
	private static final String TWITTER_AUTHORZE_URL = "https://api.twitter.com/oauth/authorize";
	private static final String TWITTER_REQUEST_URL = "https://api.twitter.com/oauth/request_token";

	public TwitterHelper(Activity context, SharedPreferences sharedPref) {
		this.context = context;

		mTwitter = new TwitterFactory().getInstance();
		mSession = new TwitterSession(context, sharedPref);
		mProgressDlg = new ProgressDialog(context);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mConsumerKey = TWITTER_CONSUMER_KEY;
		mSecretKey = TWITTER_CONSUMER_SECRET;

		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(mConsumerKey,
				mSecretKey);

		String request_url = TWITTER_REQUEST_URL;
		String access_token_url = TWITTER_ACCESS_TOKEN_URL;
		String authorize_url = TWITTER_AUTHORZE_URL;

		mHttpOauthprovider = new DefaultOAuthProvider(request_url,
				access_token_url, authorize_url);
		mAccessToken = mSession.getAccessToken();

		configureToken();
		
		mTwitter.setOAuthConsumer(mConsumerKey, mSecretKey);
	}

	public void setListener(TwDialogListener listener) {
		mListener = listener;
	}

	private void configureToken() {
		if (mAccessToken != null) {
			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();

			mAccessToken = null;
		}
	}

	public String getUsername() {
		return mSession.getUsername();
	}

	public void updateStatus(String status) throws Exception {
		try {
			mTwitter.updateStatus(status);
		} catch (TwitterException e) {
			throw e;
		}
	}

	public void authorize() {
		mProgressDlg.setMessage("Initializing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				int what = 1;

				try {
					authUrl = mHttpOauthprovider.retrieveRequestToken(
							mHttpOauthConsumer, CALLBACK_URL);
					what = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
				mHandler.sendMessage(mHandler
						.obtainMessage(what, 1, 0, authUrl));
			}
		}.start();

	}

	public void processToken(String callbackUrl) {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();

		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				int what = 1;

				try {
					mHttpOauthprovider.retrieveAccessToken(mHttpOauthConsumer,
							verifier);

					mAccessToken = new AccessToken(
							mHttpOauthConsumer.getToken(),
							mHttpOauthConsumer.getTokenSecret());

					configureToken();

					User user = mTwitter.verifyCredentials();

					mSession.storeAccessToken(mAccessToken, user.getName());

					what = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}

				mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
			}
		}.start();
	}

	private String getVerifier(String callbackUrl) {
		String verifier = "";

		try {
			callbackUrl = callbackUrl.replace("twitterapp", "http");

			Uri uri = Uri.parse(callbackUrl);
			String query = uri.getQuery();

			String array[] = query.split("&");

			for (String parameter : array) {
				String v[] = parameter.split("=");

				if (URLDecoder.decode(v[0]).equals(
						oauth.signpost.OAuth.OAUTH_VERIFIER)) {
					verifier = URLDecoder.decode(v[1]);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return verifier;
	}

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {

			public void onComplete(String value) {
				processToken(value);
			}

			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(context, url, listener).show();
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDlg.dismiss();

			if (msg.what == 1) {
				if (msg.arg1 == 1)
					mListener.onError("Error getting request token");
				else
					mListener.onError("Error getting access token");
			} else {
				if (msg.arg1 == 1)
					showLoginDialog((String) msg.obj);
				else
					mListener.onComplete("");
			}
		}
	};

	public interface TwDialogListener {
		public void onComplete(String value);

		public void onError(String value);
	}

	class TwitterSession {
		private SharedPreferences sharedPref;
		private Editor editor;

		private static final String TWEET_AUTH_KEY = "tweet_auth_key";
		private static final String TWEET_AUTH_SECRET_KEY = "tweet_auth_secret_key";
		private static final String TWEET_USER_NAME = "tweet_user_name";

		public TwitterSession(Context context, SharedPreferences sharedPref) {
			this.sharedPref = sharedPref;
			editor = sharedPref.edit();
		}

		public void storeAccessToken(AccessToken accessToken, String username) {
			editor.putString(TWEET_AUTH_KEY, accessToken.getToken());
			editor.putString(TWEET_AUTH_SECRET_KEY,
					accessToken.getTokenSecret());
			editor.putString(TWEET_USER_NAME, username);

			editor.commit();
		}

		public void resetAccessToken() {
			editor.putString(TWEET_AUTH_KEY, null);
			editor.putString(TWEET_AUTH_SECRET_KEY, null);
			editor.putString(TWEET_USER_NAME, null);

			editor.commit();
		}

		public String getUsername() {
			return sharedPref.getString(TWEET_USER_NAME, "");
		}

		public AccessToken getAccessToken() {
			String token = sharedPref.getString(TWEET_AUTH_KEY, null);
			String tokenSecret = sharedPref.getString(TWEET_AUTH_SECRET_KEY,
					null);

			if (token != null && tokenSecret != null)
				return new AccessToken(token, tokenSecret);
			else
				return null;
		}
	}

	class TwitterDialog extends Dialog {

		final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };
		final float[] DIMENSIONS_PORTRAIT = { 280, 420 };
		final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		final int MARGIN = 4;
		final int PADDING = 2;
		private String mUrl;
		private TwDialogListener mListener;
		private ProgressDialog mSpinner;
		private WebView mWebView;
		private LinearLayout mContent;
		private TextView mTitle;
		private boolean progressDialogRunning = false;

		public TwitterDialog(Context context, String url,
				TwDialogListener listener) {
			super(context);

			mUrl = url;
			mListener = listener;
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

			Display display = getWindow().getWindowManager()
					.getDefaultDisplay();
			final float scale = getContext().getResources().getDisplayMetrics().density;
			float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT
					: DIMENSIONS_LANDSCAPE;

			addContentView(mContent, new FrameLayout.LayoutParams(
					(int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
							* scale + 0.5f)));
		}

		private void setUpTitle() {
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			Drawable icon = getContext().getResources().getDrawable(
					R.drawable.ic_launcher);

			mTitle = new TextView(getContext());

			mTitle.setText("Twitter");
			mTitle.setTextColor(Color.WHITE);
			mTitle.setTypeface(Typeface.DEFAULT_BOLD);
			mTitle.setBackgroundColor(0xFFbbd7e9);
			mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
			mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
			mTitle.setCompoundDrawablesWithIntrinsicBounds(icon, null, null,
					null);

			mContent.addView(mTitle);
		}

		private void setUpWebView() {
			mWebView = new WebView(getContext());

			mWebView.setVerticalScrollBarEnabled(false);
			mWebView.setHorizontalScrollBarEnabled(false);
			mWebView.setWebViewClient(new TwitterWebViewClient());
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.loadUrl(mUrl);
			mWebView.setLayoutParams(FILL);

			mContent.addView(mWebView);
		}

		private class TwitterWebViewClient extends WebViewClient {

			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.startsWith(CALLBACK_URL)) {
					mListener.onComplete(url);

					TwitterDialog.this.dismiss();

					return true;
				} else if (url.startsWith("authorize")) {
					return false;
				}
				return true;
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				mListener.onError(description);
				TwitterDialog.this.dismiss();
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
				TwitterDialog.this.dismiss();
			}
		}
	}
}
