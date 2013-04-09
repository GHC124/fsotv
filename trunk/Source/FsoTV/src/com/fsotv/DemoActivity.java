package com.fsotv;

import com.fsotv.utils.OnSwipeTouchListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class DemoActivity extends Activity {
	private String TAG = "TOUCH"; 
	private TextView textView1;
	private ViewFlipper vf;
	GestureDetector mGestureDetector;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);

		// Get the Reference viewFlipper and set animation
		vf = (ViewFlipper) findViewById(R.id.viewFlip_channel);
		Animation s_in = AnimationUtils.loadAnimation(this, R.animator.slidein);
		Animation s_out = AnimationUtils.loadAnimation(this,
				R.animator.slideout);
		vf.setInAnimation(s_in); // when a view is displayed
		vf.setOutAnimation(s_out); // when a view disappears

		
		textView1 = (TextView)findViewById(R.id.textView1);
		textView1.setOnTouchListener(new OnSwipeTouchListener() {
			@Override
			public boolean onSwipeTop() {
				Toast.makeText(DemoActivity.this, "top", Toast.LENGTH_SHORT)
						.show();
				return true;
			}

			@Override
			public boolean onSwipeRight() {
				Toast.makeText(DemoActivity.this, "right", Toast.LENGTH_SHORT)
						.show();
				return true;
			}

			@Override
			public boolean onSwipeLeft() {
				Toast.makeText(DemoActivity.this, "left", Toast.LENGTH_SHORT)
						.show();
				return true;
			}

			@Override
			public boolean onSwipeBottom() {
				Toast.makeText(DemoActivity.this, "bottom", Toast.LENGTH_SHORT)
						.show();
				return true;
			}
		});
	}
}
