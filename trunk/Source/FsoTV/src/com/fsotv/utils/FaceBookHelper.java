package com.fsotv.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;

/**
 * FaceBook Helper, use facebook SDK to: + Login + Post Message
 * 
 * @author ChungPV1, NhungHTH1
 * 
 */
public class FaceBookHelper {
	private final String TAG = "FaceBook";
	/**
	 * Your Facebook APP ID
	 */
	private final String FACEBOOK_APP_ID = "527186760667593";
	/**
	 * Instance of Facebook Class
	 */
	private Facebook facebook;
	private Activity activity;
	private FbSession mSession;
	private FbListener mListener;

	/**
	 * Constructor
	 * 
	 * @param activity
	 * @param mPrefs
	 */
	public FaceBookHelper(Activity activity, SharedPreferences sharedPref) {
		this.activity = activity;
		this.mSession = new FbSession(sharedPref);
		facebook = new Facebook(FACEBOOK_APP_ID);

		// Check access token
		checkAccessToken();
	}

	public void setListener(FbListener listener) {
		mListener = listener;
	}

	private void checkAccessToken() {
		// Check access token
		String access_token = mSession.getAccessToken();
		long expires = mSession.getAccessExpire();
		if (access_token != null) {
			facebook.setAccessToken(access_token);
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}

	}

	public boolean hasAccessToken() {
		boolean valid = false;
		if (facebook.isSessionValid()) {
			valid = true;
		}
		return valid;
	}

	public void resetAccessToken() {
		mSession.resetAccessToken();
	}

	/**
	 * Function to login into facebook
	 * 
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public void authorize() {
		Log.i(TAG, "Start authorize()");

		checkAccessToken();
		if (!facebook.isSessionValid()) {
			facebook.authorize(activity, new String[] { "email",
					"publish_stream" }, new DialogListener() {
				@Override
				public void onCancel() {

				}

				@Override
				public void onComplete(Bundle values) {
					// Function to handle complete event
					mSession.storeAccessToken(facebook.getAccessToken(),
							facebook.getAccessExpires());

					mHandler.sendMessage(mHandler.obtainMessage(1, 0, 0));
				}

				@Override
				public void onError(DialogError error) {
					error.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(3, 0, 0,
							error.getMessage()));
				}

				@Override
				public void onFacebookError(FacebookError error) {
					error.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(3, 0, 0,
							error.getMessage()));
				}

			});
		}
		Log.i(TAG, "End authorize()");
	}

	/**
	 * Function to post to facebook wall
	 * 
	 * @param message
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public void postToWall(final String message) {
		Log.i(TAG, "Start postToWall()");

		new Thread() {
			@Override
			public void run() {
				try {
					Bundle parameters = new Bundle();
					parameters.putString("message", message);
					String response = facebook.request("me/feed", parameters,
							"POST");
					if (!response.equals("")) {
						if (response.contains("error")) {
							mHandler.sendMessage(mHandler.obtainMessage(3, 0,
									0, response));
						} else {
							mHandler.sendMessage(mHandler
									.obtainMessage(2, 0, 0));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendMessage(mHandler.obtainMessage(3, 0, 0,
							e.getMessage()));
				}
			}
		}.start();

		Log.i(TAG, "End postToWall()");
	}
	/**
	 * Hander 
	 * <br>
	 * What: 1-login complete; 2-post complete; 3-error
	 * <br>
	 * obj: message
	 */
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				mListener.onComplete("login");
			} else if (msg.what == 2) {
				mListener.onComplete("post");
			} else if (msg.what == 3) {
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
	public interface FbListener {
		public void onComplete(String value);

		public void onError(String value);
	}

	/**
	 * Store facebook authenticate key and expire into preference
	 * 
	 * @author ChungPV1
	 * 
	 */
	class FbSession {
		private SharedPreferences sharedPref;
		private Editor editor;
		/**
		 * Declare access token and expire: status for secure access to
		 * FacebookAPIs
		 */
		private final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
		private final String FACEBOOK_ACCESS_EXPIRE = "facebook_access_expire";

		public FbSession(SharedPreferences sharedPref) {
			this.sharedPref = sharedPref;
			editor = sharedPref.edit();
		}

		public void storeAccessToken(String accessToken, Long accessExpire) {
			editor.putString(FACEBOOK_ACCESS_TOKEN, accessToken);
			editor.putLong(FACEBOOK_ACCESS_EXPIRE, accessExpire);
			editor.commit();
		}

		public void resetAccessToken() {
			editor.putString(FACEBOOK_ACCESS_TOKEN, null);
			editor.putString(FACEBOOK_ACCESS_EXPIRE, null);

			editor.commit();
		}

		public String getAccessToken() {
			String token = sharedPref.getString(FACEBOOK_ACCESS_TOKEN, null);

			return token;
		}

		public Long getAccessExpire() {
			Long token = sharedPref.getLong(FACEBOOK_ACCESS_EXPIRE, 0);

			return token;
		}
	}
}
