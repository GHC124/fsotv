package com.fsotv;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class HomeActivity extends Activity implements OnClickListener {
	
	private ViewFlipper vf;
	private Button btn_music, btn_sport, btn_news, btn_game, btn_film;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		
		// Get the Channel viewFlipper and set animation
		vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);		
		Animation s_in  = AnimationUtils.loadAnimation(this, R.animator.slidein);
		Animation s_out = AnimationUtils.loadAnimation(this, R.animator.slideout);
        vf.setInAnimation(s_in);	//when a view is displayed    
        vf.setOutAnimation(s_out); //when a view disappears
		
        // Get all Button view
		btn_music = (Button) findViewById(R.id.btn_one);
		btn_music.setBackgroundColor(Color.GREEN);
		
		btn_sport = (Button) findViewById(R.id.btn_two);
		btn_news = (Button) findViewById(R.id.btn_three);
		btn_game = (Button) findViewById(R.id.btn_four);
		btn_film = (Button) findViewById(R.id.btn_five);
		
		
		// Set onClick listener for all button
		btn_music.setOnClickListener(this);
		btn_sport.setOnClickListener(this);
		btn_news.setOnClickListener(this);
		btn_game.setOnClickListener(this);
		btn_film.setOnClickListener(this);
	}

	
	public void onClick(View v)	{
		// If user click button music
		if (v == btn_music)
		{
			changeButtonBackground();
			btn_music.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(0);
		}
		// If user click button sport
		if (v == btn_sport)
		{
			changeButtonBackground();
			btn_sport.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(1);
		}
		// If user click button news
		if (v == btn_news)
		{
			changeButtonBackground();
			btn_news.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(2);
		}
		// If user click button game
		if (v == btn_game)
		{
			changeButtonBackground();
			btn_game.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(3);
		}
		// If user click button film
		if (v == btn_film)
		{
			changeButtonBackground();
			btn_film.setBackgroundColor(Color.GREEN);
			vf.setDisplayedChild(4);
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
