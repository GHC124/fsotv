package com.fsotv;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Base class for Dialog
 * + Add close button
 * + Add header
 * 
 * @author GHC_
 *
 */
public class DialogBase extends Dialog {

	private ImageView imgClose;
	private TextView tvHeader;

	public DialogBase(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setContentView(int layoutResId) {
		super.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.setContentView(layoutResId);

		tvHeader = (TextView) findViewById(R.id.tvHeader);
		imgClose = (ImageView) findViewById(R.id.imgClose);

		if (tvHeader != null)
			tvHeader.setText("");
		if (imgClose != null) {
			imgClose.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					dismiss();
				}
			});
		}
	}

	public void setHeader(String text) {
		if (tvHeader != null)
			tvHeader.setText(text);
	}
}
