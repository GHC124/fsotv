package com.fsotv;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogBase extends Dialog {

	private ImageView imgClose;
	private TextView tvHeader;
	
	public DialogBase(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void setContentView(int layoutResId) {
		super.setContentView(layoutResId);

		tvHeader = (TextView) findViewById(R.id.tvHeader);
		imgClose = (ImageView) findViewById(R.id.imgClose);

		tvHeader.setText("");
		
		imgClose.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
	}
	
	public void setHeader(String text){
		tvHeader.setText(text);
	}
}
