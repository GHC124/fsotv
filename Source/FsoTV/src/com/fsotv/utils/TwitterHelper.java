package com.fsotv.utils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.annotation.SuppressLint;
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

/**
 * Twitter Helper, use twitter4j library to: + Login twitter + Post twitter
 * message
 * 
 * @author ChungPV1
 * 
 */
public class TwitterHelper {
	private final String TAG = "Twitter";

	// Twitter Key
	public static final String TWITTER_CONSUMER_KEY = "n1kQKdyywQf6awZqCVK6kw";
	public static final String TWITTER_CONSUMER_SECRET = "XwoUFNJhJrrRkBcHIhjhAHjfk0BfbKFrZTGzmCG8cQ";
	public static final String CALLBACK_URL = "oauth://t4j";

	private Twitter mTwitter;
	private RequestToken mReqToken;
	private TwSession mSession;
	private AccessToken mAccessToken;
	private ProgressDialog mProgressDlg;
	private TwListener mListener;
	private Activity activity;

	public TwitterHelper(Activity context, SharedPreferences sharedPref) {
		this.activity = context;

		mTwitter = new TwitterFactory().getInstance();
		mSession = new TwSession(sharedPref);
		mProgressDlg = new ProgressDialog(context);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mTwitter.setOAuthConsumer(TWITTER_CONSUMER_KEY, TWITTER_CONSUMER_SECRET);

		// Check access token
		mAccessToken = mSession.getAccessToken();

		// Set access token
		if (mAccessToken != null) {
			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public void setListener(TwListener listener) {
		mListener = listener;
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

	/**
	 * Login to twitter
	 */
	public void authorize() {
		Log.i(TAG, "Start authorize...");
		mProgressDlg.setMessage("Initializing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {
				String authUrl = "";
				try {
					mReqToken = mTwitter.getOAuthRequestToken(CALLBACK_URL);
					authUrl = mReqToken.getAuthorizationURL();
					mHandler.sendMessage(mHandler.obtainMessage(3, 0, 0,
							authUrl));
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}
			}
		}.start();

	}
	/**
	 * Add post
	 * @param status
	 */
	public void updateStatus(final String status) {
		new Thread() {
			@Override
			public void run() {
				try {
					mTwitter.updateStatus(status);
					mHandler.sendMessage(mHandler.obtainMessage(2, 0, 0));
				} catch (TwitterException e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}
			}
		}.start();
	}

	/**
	 * Get access token
	 * 
	 * @param callbackUrl
	 */
	public void processToken(String callbackUrl) {
		mProgressDlg.setMessage("Finalizing ...");
		mProgressDlg.show();

		final String verifier = getVerifier(callbackUrl);

		new Thread() {
			@Override
			public void run() {
				try {
					mAccessToken = mTwitter.getOAuthAccessToken(mReqToken,
							verifier);
					if (mAccessToken != null) {
						// Set access token
						mTwitter.setOAuthAccessToken(mAccessToken);
						// Store access token
						mSession.storeAccessToken(mAccessToken);
						
						mHandler.sendMessage(mHandler.obtainMessage(1, 0, 0));
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
							e.getMessage()));
				}

			}
		}.start();
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
			verifier = uri.getQueryParameter("oauth_verifier");
		} catch (Exception e) {
			e.printStackTrace();
			mHandler.sendMessage(mHandler.obtainMessage(4, 0, 0,
					e.getMessage()));
		}

		return verifier;
	}

	/**
	 * Show login dialog
	 * 
	 * @param url
	 */
	private void showLoginDialog(String url) {
		final TwListener listener = new TwListener() {
			public void onComplete(String value) {
				processToken(value);
			}
			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(activity, url, listener).show();
	}

	/**
	 * Hander <br>
	 * What: 1-login complete; 2-post complete; 3-show login; 4-error <br>
	 * obj: message
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			mProgressDlg.dismiss();
			if (msg.what == 1) {
				mListener.onComplete("login");
			} else if (msg.what == 2) {
				mListener.onComplete("post");
			} else if (msg.what == 3) {
				showLoginDialog((String) msg.obj);
			} else if (msg.what == 4) {
				mListener.onError((String) msg.obj);
			}
		}
	};

	/**
	 * Interface to handle complete and error event
	 * 
	 * @author ChungPV1
	 * 
	 */
	public interface TwListener {
		public void onComplete(String value);

		public void onError(String value);
	}

	/**
	 * Store twitter authenticate key and serect key into preference
	 * 
	 * @author ChungPV1
	 * 
	 */
	class TwSession {
		private SharedPreferences sharedPref;
		private Editor editor;

		private static final String TWEET_AUTH_KEY = "tweet_auth_key";
		private static final String TWEET_AUTH_SECRET_KEY = "tweet_auth_secret_key";

		public TwSession(SharedPreferences sharedPref) {
			this.sharedPref = sharedPref;
			editor = sharedPref.edit();
		}

		public void storeAccessToken(AccessToken accessToken) {
			editor.putString(TWEET_AUTH_KEY, accessToken.getToken());
			editor.putString(TWEET_AUTH_SECRET_KEY,
					accessToken.getTokenSecret());

			editor.commit();
		}

		public void resetAccessToken() {
			editor.putString(TWEET_AUTH_KEY, null);
			editor.putString(TWEET_AUTH_SECRET_KEY, null);

			editor.commit();
		}

		public AccessToken getAccessToken() {
			String token = sharedPref.getString(TWEET_AUTH_KEY, null);
			String tokenSecret = sharedPref.getString(TWEET_AUTH_SECRET_KEY,
					null);
			if (token != null && tokenSecret != null) {

				Log.i(TAG, token);
				Log.i(TAG, tokenSecret);

				return new AccessToken(token, tokenSecret);
			} else
				return null;
		}
	}

	/**
	 * Create dialog that contain webview to load twitter
	 * 
	 * @author ChungPV1
	 * 
	 */
	class TwitterDialog extends Dialog {

		final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };
		final float[] DIMENSIONS_PORTRAIT = { 280, 420 };
		final FrameLayout.LayoutParams FILL = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		final int MARGIN = 4;
		final int PADDING = 2;
		private String mUrl;
		private TwListener mDialogListener;
		private ProgressDialog mSpinner;
		private WebView mWebView;
		private LinearLayout mContent;
		private TextView mTitle;
		private boolean progressDialogRunning = false;

		public TwitterDialog(Context context, String url, TwListener listener) {
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

			Display display = getWindow().getWindowManager()
					.getDefaultDisplay();
			final float scale = getContext().getResources().getDisplayMetrics().density;
			float[] dimensions = (display.getWidth() < display.getHeight()) ? DIMENSIONS_PORTRAIT
					: DIMENSIONS_LANDSCAPE;

			addContentView(mContent, new FrameLayout.LayoutParams(
					(int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1]
							* scale + 0.5f)));

			// addContentView(mContent, FILL);
		}

		private void setUpTitle() {
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			Drawable icon = getContext().getResources().getDrawable(
					R.drawable.twitter32);

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

		@SuppressLint("SetJavaScriptEnabled")
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
					mDialogListener.onComplete(url);

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
				TwitterDialog.this.dismiss();
				
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
				TwitterDialog.this.dismiss();
			}
		}
	}
}
