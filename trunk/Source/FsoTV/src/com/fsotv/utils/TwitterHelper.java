package com.fsotv.utils;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fsotv.MainActivity;

public class TwitterHelper {
	// Constants
	/**
	 * Register your here app https://dev.twitter.com/apps/new and get your
	 * consumer key and secret
	 * */
	String TWITTER_CONSUMER_KEY = "n1kQKdyywQf6awZqCVK6kw";
	String TWITTER_CONSUMER_SECRET = "XwoUFNJhJrrRkBcHIhjhAHjfk0BfbKFrZTGzmCG8cQ";

	// Preference Constants
	final String PREF_KEY_OAUTH_TOKEN = "twitter_oauth_token";
	final String PREF_KEY_OAUTH_SECRET = "twitter_oauth_token_secret";
	final String PREF_KEY_TWITTER_LOGIN = "twitter_is_loged_in";

	final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	// Twitter oauth urls
	final String URL_TWITTER_AUTH = "auth_url";
	final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";

	// Twitter
	private Twitter twitter;
	private RequestToken requestToken;

	// Shared Preferences
	private SharedPreferences mPrefs;

	private Activity activity;
	// Progress dialog
	private ProgressDialog pDialog;

	public TwitterHelper(Activity activity, SharedPreferences mPrefs) {
		this.activity = activity;
		// Check if twitter keys are set
		if (TWITTER_CONSUMER_KEY.trim().length() == 0
				|| TWITTER_CONSUMER_SECRET.trim().length() == 0) {
			Log.e("Twitter", "Please set your twitter oauth tokens first!");
			return;
		}
		// Shared Preferences
		this.mPrefs = mPrefs;

	}
	/**
	 * This conditions is tested once is redirected from twitter page.
	 * Parse the uri to get oAuth Verifier
	 * */
	public void twitterCallBack() {
		if (!isTwitterLoggedInAlready()) {
			Uri uri = activity.getIntent().getData();
			if (uri != null && uri.toString().startsWith(TWITTER_CALLBACK_URL)) {
				// oAuth verifier
				String verifier = uri
						.getQueryParameter(URL_TWITTER_OAUTH_VERIFIER);

				try {
					// Get the access token
					AccessToken accessToken = twitter.getOAuthAccessToken(
							requestToken, verifier);

					// Shared Preferences
					Editor e = mPrefs.edit();

					// After getting access token, access token secret
					// store them in application preferences
					e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
					e.putString(PREF_KEY_OAUTH_SECRET,
							accessToken.getTokenSecret());
					// Store login status - true
					e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
					e.commit(); // save changes

					Log.e("Twitter OAuth Token", "> " + accessToken.getToken());
					onCallBackCompleted();
					
				} catch (Exception e) {
					// Check log for login errors
					Log.e("Twitter Login Error", "> " + e.getMessage());
				}
			}
		}
	}
	
	public void onCallBackCompleted(){
		
	}

	/**
	 * Function to login twitter
	 * */
	public void loginToTwitter() {
		// Check if already logged in
		if (!isTwitterLoggedInAlready()) {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
			builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);
			Configuration configuration = builder.build();

			TwitterFactory factory = new TwitterFactory(configuration);
			twitter = factory.getInstance();

			try {
				requestToken = twitter
						.getOAuthRequestToken(TWITTER_CALLBACK_URL);
				activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri
						.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		} else {
			// user already logged into twitter
			Toast.makeText(activity.getApplicationContext(),
					"Already Logged into twitter", Toast.LENGTH_LONG).show();
		}
	}

	/**
	 * Check user already logged in your application using twitter Login flag is
	 * fetched from Shared Preferences
	 * */
	public boolean isTwitterLoggedInAlready() {
		// return twitter login status from Shared Preferences
		return mPrefs.getBoolean(PREF_KEY_TWITTER_LOGIN, false);
	}

	/**
	 * Function to logout from twitter It will just clear the application shared
	 * preferences
	 * */
	public void logoutFromTwitter() {
		// Clear the shared preferences
		Editor e = mPrefs.edit();
		e.remove(PREF_KEY_OAUTH_TOKEN);
		e.remove(PREF_KEY_OAUTH_SECRET);
		e.remove(PREF_KEY_TWITTER_LOGIN);
		e.commit();
	}

	public void postTwitter(String link) {
		new updateTwitterStatus().execute(link);
	}

	/**
	 * Function to update status
	 * */
	class updateTwitterStatus extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(activity);
			pDialog.setMessage("Updating to twitter...");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		/**
		 * getting Places JSON
		 * */
		protected String doInBackground(String... args) {
			Log.d("Tweet Text", "> " + args[0]);
			String status = args[0];
			try {
				ConfigurationBuilder builder = new ConfigurationBuilder();
				builder.setOAuthConsumerKey(TWITTER_CONSUMER_KEY);
				builder.setOAuthConsumerSecret(TWITTER_CONSUMER_SECRET);

				// Access Token
				String access_token = mPrefs.getString(
						PREF_KEY_OAUTH_TOKEN, "");
				// Access Token Secret
				String access_token_secret = mPrefs.getString(
						PREF_KEY_OAUTH_SECRET, "");

				AccessToken accessToken = new AccessToken(access_token,
						access_token_secret);
				Twitter twitter = new TwitterFactory(builder.build())
						.getInstance(accessToken);

				// Update status
				twitter4j.Status response = twitter.updateStatus(status);

				Log.d("Status", "> " + response.getText());
			} catch (TwitterException e) {
				// Error in updating status
				Log.d("Twitter Update Error", e.getMessage());
			}
			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog and show
		 * the data in UI Always use runOnUiThread(new Runnable()) to update UI
		 * from background thread, otherwise you will get error
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog after getting all products
			pDialog.dismiss();
			// updating UI from Background Thread
			activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					Toast.makeText(activity.getApplicationContext(),
							"Status tweeted successfully", Toast.LENGTH_SHORT)
							.show();
				}
			});
		}

	}
}
