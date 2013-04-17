package com.fsotv.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;

/**
 * FaceBook Helper, use facebook SDK to: + Login + Post Message
 * 
 * @author ChungPV1, NhungHTH1
 * 
 */
@SuppressWarnings({ "deprecation", "unused" })
public class FaceBookHelper {
	//
	private Activity activity;
	// Store authenticate
	private SharedPreferences mPrefs;
	/**
	 * Your Facebook APP ID
	 */
	private final String FACEBOOK_APP_ID = "527186760667593";

	/**
	 * Declare access token and expire: status for secure access to FacebookAPIs
	 */
	private final String FACEBOOK_ACCESS_TOKEN = "facebook_access_token";
	private final String FACEBOOK_ACCESS_EXPIRE = "facebook_access_expire";

	/**
	 * Instance of Facebook Class
	 */
	private Facebook facebook;
	private AsyncFacebookRunner mAsyncRunner;

	private boolean isLogin = false;
	private boolean isPost = false;
	private boolean requestPost = false;
	private String postMessage = "";

	/**
	 * Constructor
	 * 
	 * @param activity
	 * @param mPrefs
	 */
	@SuppressWarnings({ "deprecation" })
	public FaceBookHelper(Activity activity, SharedPreferences mPrefs) {
		Log.i("FaceBookHelper", "Start constructor FaceBookHelper");
		this.activity = activity;
		this.mPrefs = mPrefs;
		facebook = new Facebook(FACEBOOK_APP_ID);
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		Log.i("FaceBookHelper", "End constructor FaceBookHelper");
	}

	/**
	 * isLogin
	 * 
	 * @return
	 */
	public boolean isLogin() {
		return isLogin;
	}

	/**
	 * isPost
	 * 
	 * @return
	 */
	public boolean isPost() {
		return isPost;
	}

	/**
	 * authorizeCallback
	 * 
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@SuppressWarnings("deprecation")
	public void authorizeCallback(int requestCode, int resultCode, Intent data) {
		Log.i("FaceBookHelper", "Start authorizeCallback()");
		facebook.authorizeCallback(requestCode, resultCode, data);
		Log.i("FaceBookHelper", "End authorizeCallback()");
	}

	/**
	 * Function to login into facebook
	 * 
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public boolean loginToFacebook() {
		Log.i("FaceBookHelper", "Start loginToFacebook()");
		String access_token = mPrefs.getString(FACEBOOK_ACCESS_TOKEN, null);
		long expires = mPrefs.getLong(FACEBOOK_ACCESS_EXPIRE, 0);
		if (access_token != null) {
			facebook.setAccessToken(access_token);
			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}
		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}
		if (!facebook.isSessionValid()) {
			facebook.authorize(activity, new String[] { "email",
					"publish_stream" }, new DialogListener() {
				@Override
				public void onCancel() {
					// Function to handle cancel event
					isLogin = onLoginCancel();
				}

				@Override
				public void onComplete(Bundle values) {
					// Function to handle complete event
					// Edit Preferences and update facebook acess_token
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString(FACEBOOK_ACCESS_TOKEN,
							facebook.getAccessToken());
					editor.putLong(FACEBOOK_ACCESS_EXPIRE,
							facebook.getAccessExpires());
					editor.commit();

					isLogin = onLoginComplete(values);

					if (requestPost) {
						postToWall(postMessage);
					}
				}

				@Override
				public void onError(DialogError error) {
					// Function to handle error
					isLogin = onLoginError(error);
				}

				@Override
				public void onFacebookError(FacebookError fberror) {
					// Function to handle Facebook errors
					isLogin = onLoginFacebookError(fberror);
				}

			});
		} else {
			isLogin = true;
		}
		Log.i("FaceBookHelper", "End loginToFacebook()");
		return isLogin;
	}

	/**
	 * Function to post to facebook wall
	 * 
	 * @param message
	 * @return
	 */
	@SuppressWarnings({ "deprecation" })
	public boolean postToWall(String message) {
		Log.i("FaceBookHelper", "Start postToWall()");
		postMessage = message;
		requestPost = true;

		Bundle data = new Bundle();
		// put a link into data
		data.putString("link", postMessage);
		// post on user's wall.
		facebook.dialog(activity, "feed", data, new DialogListener() {
			@Override
			public void onFacebookError(FacebookError e) {
				isPost = onPostFacebookError(e);
			}

			@Override
			public void onError(DialogError e) {
				isPost = onPostError(e);
			}

			@Override
			public void onComplete(Bundle values) {
				requestPost = false;
				isPost = onPostComplete(values);
			}

			@Override
			public void onCancel() {
				isPost = onPostCancel();
			}
		});
		Log.i("FaceBookHelper", "End postToWall()");
		return isPost;
	}

	public boolean onLoginFacebookError(FacebookError e) {
		e.printStackTrace();
		return false;
	}

	public boolean onLoginError(DialogError e) {
		e.printStackTrace();
		return false;
	}

	public boolean onLoginComplete(Bundle values) {
		Log.e("LOGIN FACE", "Completed");
		return true;
	}

	public boolean onLoginCancel() {
		Log.e("LOGIN FACE", "Canceled");
		return false;
	}

	public boolean onPostFacebookError(FacebookError e) {
		e.printStackTrace();
		return false;
	}

	public boolean onPostError(DialogError e) {
		e.printStackTrace();
		return false;
	}

	public boolean onPostComplete(Bundle values) {
		Log.e("POST FACE", "Completed");
		return true;
	}

	public boolean onPostCancel() {
		Log.e("POST FACE", "CANCEL");
		return false;
	}
}