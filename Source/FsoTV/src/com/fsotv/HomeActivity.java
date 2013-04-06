package com.fsotv;

import com.fsotv.utils.YouTubeHelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class HomeActivity extends Activity implements OnClickListener {

	private ViewFlipper vf;
	private Button btn_music, btn_sport, btn_news, btn_game, btn_film;
	private LinearLayout layout_film, layout_travel, layout_music, layout_news, layout_sport;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		// Get the Channel viewFlipper and set animation
		vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);
		Animation s_in = AnimationUtils.loadAnimation(this, R.animator.slidein);
		Animation s_out = AnimationUtils.loadAnimation(this,
				R.animator.slideout);
		vf.setInAnimation(s_in); // when a view is displayed
		vf.setOutAnimation(s_out); // when a view disappears

		// Get all Button view
		btn_music = (Button) findViewById(R.id.btn_one);
		btn_music.setBackgroundColor(Color.GREEN);

		btn_sport = (Button) findViewById(R.id.btn_two);
		btn_news = (Button) findViewById(R.id.btn_three);
		btn_game = (Button) findViewById(R.id.btn_four);
		btn_film = (Button) findViewById(R.id.btn_five);
		layout_film = (LinearLayout) findViewById(R.id.layout_film);
		layout_news = (LinearLayout) findViewById(R.id.layout_news);
		layout_sport = (LinearLayout) findViewById(R.id.layout_sport);
		layout_travel = (LinearLayout) findViewById(R.id.layout_travel);
		layout_music = (LinearLayout)findViewById(R.id.layout_music);
		// Set onClick listener for all button
		btn_music.setOnClickListener(this);
		btn_sport.setOnClickListener(this);
		btn_news.setOnClickListener(this);
		btn_game.setOnClickListener(this);
		btn_film.setOnClickListener(this);
		layout_film.setOnClickListener(this);
		layout_sport.setOnClickListener(this);
		layout_news.setOnClickListener(this);
		layout_travel.setOnClickListener(this);
		layout_music.setOnClickListener(this);
	}

	public void onClick(View v) {
		// If user click button music
		if (v == btn_music) {
			changeButtonBackground();
			btn_music.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(0);
		}
		// If user click button sport
		else if (v == btn_sport) {
			changeButtonBackground();
			btn_sport.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(1);
		}
		// If user click button news
		else if (v == btn_news) {
			changeButtonBackground();
			btn_news.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(2);
		}
		// If user click button game
		else if (v == btn_game) {
			changeButtonBackground();
			btn_game.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(3);
		}
		// If user click button film
		else if (v == btn_film) {
			changeButtonBackground();
			btn_film.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(4);
		} else {
			Intent i = new Intent(getApplicationContext(),
					BrowseVideosActivity.class);

			if (v == layout_film) {
				i.putExtra("categoryId", YouTubeHelper.CATEGORY_FILM);
			} else if (v == layout_travel) {
				i.putExtra("categoryId", YouTubeHelper.CATEGORY_TRAVEL);
			} else if (v == layout_music) {
				i.putExtra("categoryId", YouTubeHelper.CATEGORY_MUSIC);
			} else if (v == layout_news) {
				i.putExtra("categoryId", YouTubeHelper.CATEGORY_NEWS);
			} else if (v == layout_sport) {
				i.putExtra("categoryId", YouTubeHelper.CATEGORY_SPORTS);
			}
			startActivity(i);
		}
	}

	public void changeButtonBackground() {
		btn_music.setBackgroundColor(Color.WHITE);
		btn_sport.setBackgroundColor(Color.WHITE);
		btn_news.setBackgroundColor(Color.WHITE);
		btn_game.setBackgroundColor(Color.WHITE);
		btn_film.setBackgroundColor(Color.WHITE);
	}

}
