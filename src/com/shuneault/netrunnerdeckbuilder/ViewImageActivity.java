package com.shuneault.netrunnerdeckbuilder;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import com.shuneault.netrunnerdeckbuilder.game.Card;
import com.shuneault.netrunnerdeckbuilder.helper.AppManager;

public class ViewImageActivity extends Activity {
	
	// EXTRA
	public static final String EXTRA_CARD_CODE = "com.shuneault.netrunnerdeckbuilder.EXTRA_CARD_CODE"; 
	
	// GUI
	ImageView imgImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_view_image);
		
		
		// GUI
		imgImage = (ImageView) findViewById(R.id.imgImage);
		
		// Set the image
		Card card = AppManager.getInstance().getCard(getIntent().getExtras().getString(EXTRA_CARD_CODE));
		imgImage.setImageBitmap(card.getImage(this));
	}
	
}
