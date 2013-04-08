package com.fsotv;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.fsotv.utils.DataHelper;

public class WatchVideoActivity extends ActivityBase implements
		OnCompletionListener, SeekBar.OnSeekBarChangeListener {
	private final int OPTION_VOLUME = Menu.FIRST;
	private ImageButton btnPlay;
	private ImageButton btnForward;
	private ImageButton btnBackward;
	private ImageButton btnVolume;
	private ImageButton btnFullScreen;
	private SeekBar songProgressBar;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	private Dialog volumeDialog;
	// Handler to update UI timer, progress bar etc,.
	private Handler mHandler = new Handler();
	private Handler volumeHandler = new Handler();
	private int seekForwardTime = 30000; // 30 seconds
	private int seekBackwardTime = 30000; // 30 seconds
	private int streamVolume = AudioManager.STREAM_MUSIC;
	private int seekVolume;

	private AudioManager mgr = null;

	VideoView myVideoView;
	String videoId = "";
	String videoTitle = "";
	String link = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_watch_video);

		mgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		// All player buttons
		btnPlay = (ImageButton) findViewById(R.id.btnPlay);
		btnForward = (ImageButton) findViewById(R.id.btnForward);
		btnBackward = (ImageButton) findViewById(R.id.btnBackward);
		btnVolume = (ImageButton) findViewById(R.id.btnVolume);
		btnFullScreen = (ImageButton) findViewById(R.id.btnFullScreen);
		songProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
		songCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
		songTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

		myVideoView = (VideoView) findViewById(R.id.videoView);
		
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		if (extras != null) {
			videoId = extras.getString("videoId");
			videoTitle = extras.getString("videoTitle");
			link = extras.getString("link");

			videoId = (videoId == null) ? "" : videoId;
			videoTitle = (videoTitle == null) ? "" : videoTitle;
			link = (link == null) ? "" : link;
			
			if(videoTitle.length()>50)
				videoTitle = videoTitle.substring(0, 50) + "...";
		}

		setHeader(videoTitle);
		setTitle("Watch Video");
		
		// Listeners
		songProgressBar.setOnSeekBarChangeListener(this);
		myVideoView.setOnCompletionListener(this);

		playVideo("http://media.socbay.com/public/media/Video/BT%20Video/9.4comaythoigian.3gp");

		btnPlay.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onPlayClick(v);
			}
		});

		btnForward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onForwardClick(v);
			}
		});

		/**
		 * Backward button click event Backward song to specified seconds
		 * */
		btnBackward.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackwardClick(v);
			}
		});

		btnVolume.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onVolumeClick(v);
			}
		});
		
		btnFullScreen.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onFullScreenClick(v);
			}
		});
		createDialogs(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, OPTION_VOLUME, 0, "Volume");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case OPTION_VOLUME:
			if (volumeDialog != null)
				volumeDialog.show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onPlayClick(View v) {
		if (myVideoView == null)
			return;
		// check for already playing
		if (myVideoView.isPlaying()) {
			myVideoView.pause();
			// Changing button image to play button
			btnPlay.setImageResource(R.drawable.btn_play);
		} else {
			// Resume song
			myVideoView.start();
			// Changing button image to pause button
			btnPlay.setImageResource(R.drawable.btn_pause);
		}
	}

	public void onForwardClick(View v) {
		// get current song position
		int currentPosition = myVideoView.getCurrentPosition();
		// check if seekForward time is lesser than song duration
		if (currentPosition + seekForwardTime <= myVideoView.getDuration()) {
			// forward song
			myVideoView.seekTo(currentPosition + seekForwardTime);
		} else {
			// forward to end position
			myVideoView.seekTo(myVideoView.getDuration());
		}
	}

	public void onBackwardClick(View v) {
		// get current song position
		int currentPosition = myVideoView.getCurrentPosition();
		// check if seekBackward time is greater than 0 sec
		if (currentPosition - seekBackwardTime >= 0) {
			// forward song
			myVideoView.seekTo(currentPosition - seekBackwardTime);
		} else {
			// backward to starting position
			myVideoView.seekTo(0);
		}
	}

	public void onVolumeClick(View v) {
		if(volumeDialog!=null)
			volumeDialog.show();
	}
	
	public void onFullScreenClick(View v) {
		DisplayMetrics metrics = new DisplayMetrics(); getWindowManager().getDefaultDisplay().getMetrics(metrics);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) myVideoView.getLayoutParams();
        params.width =  metrics.widthPixels;
        params.height = metrics.heightPixels;
        params.leftMargin = 0;
        myVideoView.setLayoutParams(params);
	}
	
	private void createDialogs(Context context) {
		volumeDialog = new Dialog(context);
		volumeDialog.setContentView(R.layout.change_volume);
		volumeDialog.setTitle("Volume");
		SeekBar seekBar = (SeekBar) volumeDialog.findViewById(R.id.seekBar);
		seekVolume = mgr.getStreamVolume(streamVolume);
		seekBar.setMax(mgr.getStreamMaxVolume(streamVolume));
		seekBar.setProgress(mgr.getStreamVolume(streamVolume));
		seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onProgressChanged(SeekBar bar, int progress,
					boolean fromUser) {
				seekVolume = progress;
				updateVolume();
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}
		});
	}

	private void playVideo(String link) {
		try {
			myVideoView.setVideoURI(Uri.parse(link));
			myVideoView.requestFocus();
			myVideoView.start();

			// Changing Button Image to pause image
			btnPlay.setImageResource(R.drawable.btn_pause);

			// set Progress bar values
			songProgressBar.setProgress(0);
			songProgressBar.setMax(100);

			// Updating progress bar
			updateProgressBar();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Update timer on seekbar
	 * */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	public void updateVolume() {
		volumeHandler.postDelayed(mUpdateVolumeTask, 100);
	}

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			long totalDuration = myVideoView.getDuration();
			long currentDuration = myVideoView.getCurrentPosition();

			// Displaying Total Duration time
			songTotalDurationLabel.setText(""
					+ DataHelper.milliSecondsToTimer(totalDuration));
			// Displaying time completed playing
			songCurrentDurationLabel.setText(""
					+ DataHelper.milliSecondsToTimer(currentDuration));

			// Updating progress bar
			int progress = (int) (DataHelper.getProgressPercentage(
					currentDuration, totalDuration));
			// Log.d("Progress", ""+progress);
			songProgressBar.setProgress(progress);

			// Running this thread after 100 milliseconds
			mHandler.postDelayed(this, 100);
		}
	};

	/**
	 * Background Runnable thread
	 * */
	private Runnable mUpdateVolumeTask = new Runnable() {
		public void run() {
			mgr.setStreamVolume(streamVolume, seekVolume,
					AudioManager.FLAG_PLAY_SOUND);
		}
	};

	/**
	 * 
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 * */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress hanlder
	 * */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = myVideoView.getDuration();
		int currentPosition = DataHelper.progressToTimer(seekBar.getProgress(),
				totalDuration);

		// forward or backward to certain seconds
		myVideoView.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// Changing button image to pause button
		btnPlay.setImageResource(R.drawable.btn_pause);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

	}
}
